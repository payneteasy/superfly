#!/bin/sh

export SSO_DB_DATABASE=ssotest

. ../superfly-sql/functions.sh

(
cd src/test/sql && runScriptNoDb create_test_database.sql
)

(
cd ../superfly-sql/mi/
for i in `ls | grep '^R' | sort` ; do
   if [ -d "$i" ]; then
       logInfo "-  $i"
       ( cd "$i" && ls ./*.sh > /dev/null 2>&1 && bash ./*.sh )
return_code=$?
if [ "$return_code" != '0' ]; then
    logError "Error $return_code"
    exit $return_code
fi
   fi
done
)

( cd ../superfly-sql/src && ./all-proc.sh )
return_code=$?
if [ "$return_code" != '0' ]; then
    logError "Error $return_code"
    exit $return_code
fi

exit 0