#!/bin/sh


java ${JAVA_OPTS:''} \
-Djava.security.egd=file:/dev/./urandom \
-cp /app/resources/:/app/classes/:/app/libs/* "com.anjia.unidbgserver.UnidbgServerApplication"
