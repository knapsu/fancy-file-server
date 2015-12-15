#!/bin/sh
BASEDIR=$(dirname "$0")
exec java -d64 -jar "$BASEDIR/fancy-file-server.jar"
