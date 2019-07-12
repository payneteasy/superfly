#!/bin/bash

mvn verify -Powasp-dependency-check -DskipTests "$@"