#!/bin/sh

dir=demoCA

mkdir -p $dir/private
mkdir -p $dir/newcerts
touch $dir/index.txt
touch $dir/index.txt.attr
echo 01 > $dir/serial

openssl genrsa -des3 -out $dir/private/cakey.pem 2048
openssl req -new -x509 -days 3650 -key $dir/private/cakey.pem -out $dir/cacert.pem
keytool -importcert -alias ssoCA -keystore cacert.store -file $dir/cacert.pem -storepass changeit -noprompt
openssl x509 -in $dir/cacert.pem -out $dir/cacert.der -outform der

echo
echo Java keystore containing CA certificate stored to the following file: cacert.store
