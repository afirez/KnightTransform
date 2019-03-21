#!/usr/bin/env bash

echo "begin init project~~"

list="knight-transform knight-tinyImage-plugin knight-byteK knight-doubleCheck-plugin knight-shrinkR-plugin  knight-doubleCheck-library library"

for state in ${list}
do
    echo "this word is $state"
    sed -i "" "s/\/\/k//g" ${state}/build.gradle
    ./gradlew clean :${state}:uploadArchives --stacktrace 2>&1| tee log.txt
    buildResult=`grep "BUILD FAILED" log.txt`
    if [[ ${buildResult} =~ "BUILD FAILED" ]]
    then
        echo "please check your project"
        rm -f log.txt
        exit 1
    fi
    rm -f log.txt
done

#project build.gradle
sed -i "" "s/\/\/k//g" build.gradle

#app build.gradle
sed -i "" "s/\/\/k//g" app/build.gradle
