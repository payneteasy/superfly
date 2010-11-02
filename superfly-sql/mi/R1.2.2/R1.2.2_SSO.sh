export MYSQL_USERNAME=${MYSQL_USERNAME:-sso}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-123sso123}
export MYSQL_DATABASE=${MYSQL_DATABASE:-sso}

( cd ../../src && sh ./all-proc.sh )

mkdir -p target
echo "Current DDL errors:"
mysql -b -vv -u $MYSQL_USERNAME -p$MYSQL_PASSWORD $MYSQL_DATABASE --show-warnings < R1.2.2_SSO.sql  > target/R1.2.2_SSO.log 
echo "Current DDL warnings:"
grep " warning" target/R1.2.2_SSO.log 
grep "Note" target/R1.2.2_SSO.log

echo "Current DML errors:"
mysql -b -vv -u $MYSQL_USERNAME -p$MYSQL_PASSWORD $MYSQL_DATABASE --show-warnings < R1.2.2_SSO_DML.sql  > target/R1.2.2_SSO_DML.log 
echo "Current DML warnings:"
grep " warning" target/R1.2.2_SSO_DML.log 
grep "Note" target/R1.2.2_SSO_DML.log
