#!/bin/sh

for start in `seq 0 20 $2`;
do
    echo java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main $1 $start `expr $start + 20` 
    java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main $1 $start `expr $start + 20`
done
