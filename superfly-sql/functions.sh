#!/bin/bash

export SSO_DB_USERNAME=${SSO_DB_USERNAME:-sso}
export SSO_DB_PASSWORD=${SSO_DB_PASSWORD:-123sso123}
export SSO_DB_DATABASE=${SSO_DB_DATABASE:-sso}
export SSO_DB_HOST=${SSO_DB_HOST:-localhost}
export SSO_DB_PORT=${SSO_DB_PORT:-3306}

die() {
    errorCode=$?
    errorMessage=$1
    
    if [ $errorCode != 0 ] 
    then
        echo ".ERROR.: $errorCode - $errorMessage"
        exit 1
    fi    
}


runScript() {

    aScript=$1

    mkdir -p target
    echo "Installing to $SSO_DB_USERNAME@$SSO_DB_HOST:$SSO_DB_PORT/$SSO_DB_DATABASE $aScript ..."
    mysql --default-character-set=utf8 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_USERNAME -p$SSO_DB_PASSWORD $SSO_DB_DATABASE --show-warnings < $aScript  > target/$aScript.log 

    die "can not process $aScript"

    #echo "Current DDL/DML warnings:"
    #grep "Warning " target/$aScript.log 
    #grep "Warnings:" target/$aScript.log 
    #grep "Note" target/$aScript.log
}
