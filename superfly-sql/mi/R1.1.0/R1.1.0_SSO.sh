#!/bin/bash

. ../../functions.sh

runScript ../../src/run_install_command.sql

runScript R1.1.0_SSO.sql

runScript R1.1.0_SSO_DML.sql

runScript quartz-tables.sql
