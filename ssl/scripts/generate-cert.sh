#!/bin/sh

name=$1
if [ -z "$name" ]; then
	echo "Usage: generate-cert.sh <cert-name> [<openssl config file>]"
	exit 1
fi
config=$2
config_str=
if [ -n "$config" ]; then
	config_str=-config $config
fi

dir=demoCA

# generating secret key + sign request
openssl req -newkey rsa:1024 -keyout "$name.key" -out "$name.req" -passout pass:changeit && \
# signing request with CA -> certificate is produced
openssl ca $config_str -days 3650 -out "$name.crt" -infiles "$name.req" && \
# converting to PKCS#12 format to be able to import both secret key and cert
# to java key store
openssl pkcs12 -export -in "$name.crt" -inkey "$name.key" \
               -out "$name.p12" -name "$name" \
               -CAfile $dir/cacert.pem -caname root \
               -password pass:changeit -passin pass:changeit && \
# converting PKCS#12 store to java keystore
keytool -importkeystore \
        -deststorepass changeit -destkeypass changeit -destkeystore "$name.store" \
        -srckeystore "$name.p12" -srcstoretype PKCS12 -srcstorepass changeit \
        -alias "$name" && \
# importing CA cert
keytool -importcert -file $dir/cacert.der -keystore "$name.store" -alias ssoca -storepass changeit

echo
echo "Java key store saved in the following file: $name.store"
echo "Also, intermediate files are left in the current directory (they all have $name prefix). Please note that keys and stores all have default password (changeit)."
