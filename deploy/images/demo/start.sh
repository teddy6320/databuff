#!/bin/sh
cd "$(dirname "$0")"
exec java ${JAVA_TOOL_OPTIONS} -jar demo-seeder.jar
