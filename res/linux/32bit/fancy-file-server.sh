#!/bin/sh
BASEDIR=$(dirname "$0")
exec java -d32 -jar "$BASEDIR/fancy-file-server.jar"
