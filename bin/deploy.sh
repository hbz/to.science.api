#!/bin/bash

if (( $EUID == 0 )); then
    echo "Don't run as root!"
    exit
fi

export TERM=xterm-color
deployingApp="regal-api"
branch=$(git status | grep branch | cut -d ' ' -f3)
echo "git currently on branch: "$branch
if [ ! -z "$1" ]; then
    branch="$1"
fi

cd /opt/regal/$deployingApp
git pull origin $branch
/opt/regal/activator/bin/activator -java-home /opt/jdk clean
/opt/regal/activator/bin/activator -java-home /opt/jdk clean-files
/opt/regal/activator/bin/activator -java-home /opt/jdk dist
