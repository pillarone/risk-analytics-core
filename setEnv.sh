#!/bin/sh
export GRAILS_HOME=$PWD/../grails
export ANT_OPTS="-Xmx512m -XX:MaxPermSize=512m -Duser.language=en"
export JAVA_OPTS="-Xmx512m -XX:MaxPermSize=512m -Duser.language=en"
export PATH=$GRAILS_HOME/bin:$PATH
