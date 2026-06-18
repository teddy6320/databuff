#!/bin/sh
cd "$(dirname "$0")"
exec java ${JAVA_TOOL_OPTIONS} -jar ./*.jar --spring.config.additional-location=file:./application.yml
