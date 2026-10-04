#!/bin/sh
set -eu

# DB_* variables are read by ROOT.xml straight from the environment; only the
# port goes through as a Jetty property (name=value, not -D, so no warning).
# X-Forwarded-* / Forwarded headers are trusted only on request: Jetty cannot restrict
# them to proxy addresses, so enable this only when the port is reachable by the proxy alone.
if [ "${JETTY_TRUST_FORWARDED:-false}" = "true" ]; then
    exec "$@" --module=forwarded "jetty.http.port=${JETTY_PORT:-8080}"
fi
exec "$@" "jetty.http.port=${JETTY_PORT:-8080}"
