#!/bin/sh
set -eu

# DB_* variables are read by ROOT.xml straight from the environment; only the
# port goes through as a Jetty property (name=value, not -D, so no warning)
exec "$@" "jetty.http.port=${JETTY_PORT:-8080}"
