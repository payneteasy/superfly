#!/bin/bash
#
# Creates the test database $SSO_DB_DATABASE (default ssotest) and installs the schema and the stored procedures.
# The connection comes from SSO_DB_HOST/SSO_DB_PORT (default localhost:3344), the root account from
# SSO_DB_ROOT/SSO_DB_ROOT_PASSWORD (see superfly-sql/functions.sh). Only the test database is touched.

export SSO_DB_DATABASE=${SSO_DB_DATABASE:-ssotest}

if [ "$SSO_DB_DATABASE" = "sso" ]; then
    echo "Refusing to recreate the database 'sso'; set SSO_DB_DATABASE to a test database" >&2
    exit 1
fi

. ../superfly-sql/functions.sh

# R1.0.0 starts with a root step that recreates the database 'sso', so it is run here without it:
# the database is created and granted below, the rest of R1.0.0 is installed the way R1.0.0_SSO.sh does it
mkdir -p target
echo "drop database if exists $SSO_DB_DATABASE;
create database $SSO_DB_DATABASE default character set utf8 collate utf8_general_ci;
grant all privileges on $SSO_DB_DATABASE.* to '$SSO_DB_USERNAME'@'%' identified by '$SSO_DB_PASSWORD';
flush privileges;" > target/create_test_database.sql
runRoot target/create_test_database.sql

(
cd ../superfly-sql/mi/
for i in `ls | grep '^R' | sort` ; do
    if [ -d "$i" ]; then
        logInfo "-  $i"
        if [ "$i" = "R1.0.0" ]; then
            ( cd "$i" && . ../../functions.sh && runScript R1.0.0_SSO.sql && runScript R1.0.0_SSO_DML.sql )
        else
            ( cd "$i" && ls ./*.sh > /dev/null 2>&1 && bash ./*.sh )
        fi
        return_code=$?
        if [ "$return_code" != '0' ]; then
            logError "Error $return_code"
            exit $return_code
        fi
    fi
done
)
return_code=$?
if [ "$return_code" != '0' ]; then
    exit $return_code
fi

( cd ../superfly-sql/src && ./all-proc.sh )
return_code=$?
if [ "$return_code" != '0' ]; then
    logError "Error $return_code"
    exit $return_code
fi

exit 0
