#!/bin/bash

. ../../functions.sh

runRoot R1.0.0_SSO_ROOT.sql

runScript R1.0.0_SSO.sql

runScript R1.0.0_SSO_DML.sql
