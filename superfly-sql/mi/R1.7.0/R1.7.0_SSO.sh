#!/bin/bash

. ../../functions.sh

runScript quartz-to-2.0.sql

runScript R1.7.0_SSO.sql

runScript R1.7.0_SSO_DML.sql

