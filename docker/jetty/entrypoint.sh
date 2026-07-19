#!/bin/sh
set -eu

# Pass DB connection params as Java system properties
# so ROOT.xml <Property> tags pick them up
exec "$@" \
  -Ddb.host="${DB_HOST:-mysql}" \
  -Ddb.port="${DB_PORT:-3306}" \
  -Ddb.name="${DB_NAME:-sso}" \
  -Ddb.user="${DB_USER:-sso}" \
  -Ddb.password="${DB_PASSWORD:-}" \
  -Ddb.timezone="${DB_TIMEZONE:-UTC}" \
  -Djetty.http.port="${JETTY_PORT:-8080}"
