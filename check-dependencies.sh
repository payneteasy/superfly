#!/bin/bash

FORMAT=""
VERSION=""

if [ "$1" ]; then
    FORMAT="-Dformat=$1"
fi

if [ "$2" ]; then
    VERSION="-Dowasp.dependencyCheck.version=$2"
fi

mvn verify -Powasp-dependency-check -DskipTests $FORMAT $VERSION
