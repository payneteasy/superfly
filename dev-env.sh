#!/bin/bash
#
# Local environment for checking the admin UI by hand.
#
#   ./dev-env.sh up      start MySQL 5.7 and install schema + stored procedures
#   ./dev-env.sh seed    add sample actions/groups for the actions screens
#   ./dev-env.sh app     run the web app on http://localhost:8085/superfly/
#   ./dev-env.sh sql     open a mysql shell on the dev database
#   ./dev-env.sh down    remove the container and the network
#
# Login: admin / 123admin123
#
# MySQL 5.7 is not a free choice: on 8.0 the schema does not install at all,
# because `groups` became a reserved word.

set -e

CONTAINER=superfly-mysql-dev
NETWORK=superfly-dev-net
IMAGE=mysql:5.7
ROOT_PASSWORD=charpa

ROOT_DIR=$(cd "$(dirname "$0")" && pwd)
SHIM_DIR=$ROOT_DIR/target/dev-env

export SSO_DB_DATABASE=${SSO_DB_DATABASE:-sso}
export SSO_DB_USERNAME=sso
export SSO_DB_PASSWORD=123sso123
export SSO_DB_ROOT=root
export SSO_DB_ROOT_PASSWORD=$ROOT_PASSWORD

# The MI and all-proc scripts shell out to `mysql`, and mysql 9.x dropped
# support for the `\.` (source) directive that all-proc.sql is built on. When
# the client on PATH is too new, put a shim in front of it that forwards every
# invocation to the client inside the MySQL 5.7 image.
setup_client() {
    local major
    major=$(mysql --version 2>/dev/null | sed -n 's/.*Ver \([0-9]*\).*/\1/p')

    if [ -n "$major" ] && [ "$major" -lt 9 ]; then
        export SSO_DB_HOST=127.0.0.1
        echo "Using local mysql client (major version $major)"
        return
    fi

    mkdir -p "$SHIM_DIR"
    cat > "$SHIM_DIR/mysql" <<EOF
#!/bin/sh
exec docker run --rm -i --platform linux/amd64 --network $NETWORK \\
  -v "$ROOT_DIR:$ROOT_DIR" -w "\$(pwd)" $IMAGE mysql "\$@"
EOF
    chmod +x "$SHIM_DIR/mysql"
    export PATH="$SHIM_DIR:$PATH"
    # inside the shim container "localhost" is the container itself, so the
    # scripts have to address the database by its container name
    export SSO_DB_HOST=$CONTAINER
    echo "Local mysql client is ${major:-missing} — routing through $IMAGE instead"
}

wait_for_mysql() {
    local i
    for i in $(seq 1 60); do
        if mysql --protocol=TCP -h "$SSO_DB_HOST" -u root -p"$ROOT_PASSWORD" \
                -e "select 1" > /dev/null 2>&1; then
            return 0
        fi
        sleep 3
    done
    echo "MySQL did not become ready in time" >&2
    exit 1
}

# Strips the per-invocation noise the MI scripts produce (the password warning
# and their own progress lines) so that anything left on screen is a real
# problem. sed rather than grep -v: grep exits 1 when it prints nothing, which
# under set -e would kill the function before it can report the real exit code.
quietly() {
    "$@" 2>&1 \
        | sed -e '/Using a password on the command line interface/d' \
              -e '/^Installing to /d'
    return "${PIPESTATUS[0]}"
}

run_root_sql() {
    quietly mysql --protocol=TCP -h "$SSO_DB_HOST" \
        -u root -p"$ROOT_PASSWORD" "$@"
}

run_sso_sql() {
    quietly mysql --protocol=TCP -h "$SSO_DB_HOST" \
        -u "$SSO_DB_USERNAME" -p"$SSO_DB_PASSWORD" "$SSO_DB_DATABASE" "$@"
}

cmd_up() {
    docker network create "$NETWORK" > /dev/null 2>&1 || true
    docker rm -f "$CONTAINER" > /dev/null 2>&1 || true
    docker run -d --name "$CONTAINER" --platform linux/amd64 \
        --network "$NETWORK" -e MYSQL_ROOT_PASSWORD="$ROOT_PASSWORD" \
        -p 3306:3306 "$IMAGE" --log-bin-trust-function-creators=1 > /dev/null

    setup_client
    echo "Waiting for MySQL ..."
    wait_for_mysql

    run_root_sql -e "grant all privileges on *.* to '$SSO_DB_USERNAME'@'%' \
        identified by '$SSO_DB_PASSWORD' with grant option;
        flush privileges;
        drop database if exists $SSO_DB_DATABASE;
        create database $SSO_DB_DATABASE default character set utf8 \
        collate utf8_general_ci;"

    echo "Installing migrations ..."
    for dir in $(ls "$ROOT_DIR/superfly-sql/mi" | grep '^R' | sort); do
        if [ -d "$ROOT_DIR/superfly-sql/mi/$dir" ]; then
            echo "  $dir"
            quietly bash -c \
                "cd '$ROOT_DIR/superfly-sql/mi/$dir' && bash ./*.sh"
        fi
    done

    echo "Installing stored procedures ..."
    quietly bash -c "cd '$ROOT_DIR/superfly-sql/src' && ./all-proc.sh"

    echo
    echo "Database $SSO_DB_DATABASE is ready. Next: ./dev-env.sh seed && ./dev-env.sh app"
}

cmd_seed() {
    setup_client
    run_sso_sql <<'SQL'
set @ssys = (select ssys_id from subsystems where subsystem_name = 'superfly');

insert into actions (action_name, action_description, ssys_ssys_id, log_action)
values ('brand-new-action',   'not in any group, not in any role', @ssys, 'N'),
       ('multi-group-action', 'belongs to two groups',             @ssys, 'N'),
       ('zeta-one',           'filler for paging checks',          @ssys, 'N'),
       ('zeta-two',           'filler for paging checks',          @ssys, 'N');

insert into groups (group_name, ssys_ssys_id) values ('group-a', @ssys), ('group-b', @ssys);

insert into group_actions (grop_grop_id, actn_actn_id)
select g.grop_id, a.actn_id
  from groups g, actions a
 where g.group_name in ('group-a', 'group-b')
   and a.action_name = 'multi-group-action';

select a.action_name, g.group_name
  from actions a
       left join group_actions ga on ga.actn_actn_id = a.actn_id
       left join groups g on g.grop_id = ga.grop_grop_id
 order by a.action_name;
SQL
}

cmd_app() {
    echo "Starting on http://localhost:8085/superfly/ — login admin / 123admin123"
    cd "$ROOT_DIR/superfly-web"
    ../mvnw test-compile org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
        -Dexec.mainClass=com.payneteasy.superfly.Start \
        -Dexec.classpathScope=test
}

cmd_sql() {
    setup_client
    # not run through the warning filter: piping would break the interactive shell
    mysql --protocol=TCP -h "$SSO_DB_HOST" -u "$SSO_DB_USERNAME" \
        -p"$SSO_DB_PASSWORD" "$SSO_DB_DATABASE"
}

cmd_down() {
    docker rm -f "$CONTAINER" > /dev/null 2>&1 || true
    docker network rm "$NETWORK" > /dev/null 2>&1 || true
    rm -rf "$SHIM_DIR"
    echo "Removed $CONTAINER and $NETWORK"
}

case "$1" in
    up)   cmd_up   ;;
    seed) cmd_seed ;;
    app)  cmd_app  ;;
    sql)  cmd_sql  ;;
    down) cmd_down ;;
    *)
        sed -n '2,15p' "$0" | sed 's/^# \{0,1\}//'
        exit 1
        ;;
esac
