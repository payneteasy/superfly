#!/bin/bash

export SSO_DB_USERNAME=${SSO_DB_USERNAME:-sso}
export SSO_DB_PASSWORD=${SSO_DB_PASSWORD:-123sso123}
export SSO_DB_DATABASE=${SSO_DB_DATABASE:-sso}
export SSO_DB_HOST=${SSO_DB_HOST:-localhost}
export SSO_DB_PORT=${SSO_DB_PORT:-3344}
export SSO_DB_ROOT=${SSO_DB_ROOT:-root}
export SSO_DB_ROOT_PASSWORD=${SSO_DB_ROOT_PASSWORD:-1234}

logInfo() {
    # or tts -s
    test -t 0 && tput setaf 2 # green
    echo $1
    test -t 0 && tput sgr0
    logger "$USER INFO: $1"
}

logError() {
    test -t 0 && tput setaf 1 # red
    echo $1
    test -t 0 && tput sgr0
    logger "$USER ERROR: $1"
}

logWarn() {
    test -t 0 && tput setaf 3 # yellow
    echo $1
    test -t 0 && tput sgr0
    logger "$USER WARN: $1"
}

# Trap errors globally to handle them
trap 'logError "Error occurred in ${BASH_SOURCE[0]} at line $LINENO"' ERR

# Custom error handler without exiting the script
die() {
    errorCode=$?
    errorMessage=$1

    if [ $errorCode != 0 ]
    then
        logError ".ERROR.: $errorCode - $errorMessage"
        return $errorCode
    fi
}

# Function to expand .\ commands in SQL files
expandSourceCommands() {
    local inputFile=$1
    local outputFile=$2
    local baseDir=$(dirname "$inputFile")
    local firstLine=true

    # Remove BOM if present and read file
    sed '1s/^\xEF\xBB\xBF//' "$inputFile" | while IFS= read -r line || [ -n "$line" ]; do
        # Check for .\ command (backslash followed by dot, then whitespace and filename)
        if [[ "$line" =~ ^\\\.\ +(.+)$ ]]; then
            # Found a .\ command - include the file
            local includeFile="${BASH_REMATCH[1]}"
            # Trim whitespace
            includeFile=$(echo "$includeFile" | xargs)
            # Resolve relative path
            if [[ "$includeFile" != /* ]]; then
                includeFile="$baseDir/$includeFile"
            fi
            if [ -f "$includeFile" ]; then
                # Recursively expand the included file
                expandSourceCommands "$includeFile" "$outputFile"
            else
                logWarn "Warning: Include file not found: $includeFile"
            fi
        else
            # Regular line - output as is
            echo "$line" >> "$outputFile"
        fi
    done
}

runScript() {

    aScript=$1
    aScriptLog="target/$(basename $aScript).log"

    mkdir -p target
    echo "Installing to $SSO_DB_USERNAME@$SSO_DB_HOST:$SSO_DB_PORT/$SSO_DB_DATABASE $aScript ..."

    # Expand source commands if file contains .\ commands
    if grep -q "^\\\." "$aScript" 2>/dev/null; then
        expandedScript=$(mktemp)
        expandSourceCommands "$aScript" "$expandedScript"
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_USERNAME -p$SSO_DB_PASSWORD $SSO_DB_DATABASE --show-warnings < "$expandedScript" > "$aScriptLog"
        rm -f "$expandedScript"
    else
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_USERNAME -p$SSO_DB_PASSWORD $SSO_DB_DATABASE --show-warnings < $aScript > "$aScriptLog"
    fi

    die "can not process $aScript"

    #echo "Current DDL/DML warnings:"
    #grep "Warning " target/$aScript.log
    #grep "Warnings:" target/$aScript.log
    #grep "Note" target/$aScript.log
}

runScriptNoDb() {

    aScript=$1
    aScriptLog="target/$(basename $aScript).log"

    mkdir -p target
    echo "Installing to $SSO_DB_USERNAME@$SSO_DB_HOST:$SSO_DB_PORT $aScript ..."

    # Expand source commands if file contains .\ commands
    if grep -q "^\\\." "$aScript" 2>/dev/null; then
        expandedScript=$(mktemp)
        expandSourceCommands "$aScript" "$expandedScript"
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_USERNAME -p$SSO_DB_PASSWORD --show-warnings < "$expandedScript" > "$aScriptLog"
        rm -f "$expandedScript"
    else
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_USERNAME -p$SSO_DB_PASSWORD --show-warnings < $aScript > "$aScriptLog"
    fi

    die "can not process $aScript"

    #echo "Current DDL/DML warnings:"
    #grep "Warning " target/$aScript.log
    #grep "Warnings:" target/$aScript.log
    #grep "Note" target/$aScript.log
}

runRoot() {

    aScript=$1
    aScriptLog="target/$(basename $aScript).log"

    mkdir -p target
    echo "Installing to $SSO_DB_ROOT@$SSO_DB_HOST:$SSO_DB_PORT $aScript ..."

    # Expand source commands if file contains .\ commands
    if grep -q "^\\\." "$aScript" 2>/dev/null; then
        expandedScript=$(mktemp)
        expandSourceCommands "$aScript" "$expandedScript"
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_ROOT -p$SSO_DB_ROOT_PASSWORD --show-warnings < "$expandedScript" > "$aScriptLog"
        rm -f "$expandedScript"
    else
        mysql --default-character-set=utf8mb4 --protocol=TCP --port $SSO_DB_PORT -h $SSO_DB_HOST -b -vv -u $SSO_DB_ROOT -p$SSO_DB_ROOT_PASSWORD --show-warnings < $aScript > "$aScriptLog"
    fi

    die "can not process $aScript"

    #echo "Current DDL/DML warnings:"
    #grep "Warning " target/$aScript.log
    #grep "Warnings:" target/$aScript.log
    #grep "Note" target/$aScript.log
}
