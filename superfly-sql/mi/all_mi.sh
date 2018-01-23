#!/bin/bash

version_from=${version_from:-"R1.0.0"}
version_to=${version_to:-"R2.0.0"}
run_script="N"

for i in `ls | grep ^R | sort` ; do
    if [ -d "$i" ]; then
        if [ "$i" = "$version_from" ]; then
            run_script="Y"
        elif [ "$i" = "$version_to" ]; then
            run_script="N"
        fi

        cd "$i"

        if [ "$run_script" = "Y" ]; then
            echo "-  $i"
            for j in *.sh ; do
                if [ -f "$j" ]; then
                    bash "./$j" || exit $?
                fi
            done
#        else
#            echo "skip" ./$i
        fi

        cd ..
    fi
done

exit 0
