#!/bin/sh
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
MYDIR=$(dirname $MYSELF)

if test "x$1" = "x-java-term" -o "x$1" = "x-JT"; then
    shift
    export TERM="$1"
    shift
fi

if test "x$1" = "x-java-home" -o "x$1" = "x-JH"; then
    shift
    export JAVA_HOME="$1"
    shift
fi

if test "x$1" = "x-java-opts" -o "x$1" = "x-JO"; then
    shift
    export _JAVA_ARGS="$1"
    shift
fi

if test ! -n "$JAVA_HOME"; then
    if test -d "$MYDIR/java"; then
        export JAVA_HOME="$MYDIR/java"
    elif test -d "$MYDIR/jre"; then
        export JAVA_HOME="$MYDIR/jre"
    elif test -d "$MYDIR/../java"; then
        export JAVA_HOME="$MYDIR/../java"
    elif test -d "$MYDIR/../jre"; then
        export JAVA_HOME="$MYDIR/../jre"
    fi
fi

java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi

exec "$java" $_JAVA_ARGS -jar $MYSELF "$@"
exit 1
