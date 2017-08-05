#!/bin/sh

for start in `seq $3 20 $4`;
do
    echo java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main $1 $2 $start `expr $start + 20` 
    java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main $1 $2 $start `expr $start + 20`
done
