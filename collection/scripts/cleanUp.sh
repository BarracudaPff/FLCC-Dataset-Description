#!/bin/bash

# Customizable variables
workers=16
timeout=10m
root=/tmp/root-repositories

# Dump errored repos
source main.sh dump_errors

# Prepare list of repositories
source main.sh prepare_list

# Restart databases (cause of errors)
source scripts/restart.sh

sleep 3

# Clean space
rm -r /tmp/borges/

# Init database for borges
../../../../borges/borges init
sleep 5

# Init repositories to download and compress
../../../../borges/borges producer file data/repo-list.txt

# Start jobs.
../../../../borges/borges consumer --workers=${workers} --timeout=${timeout} --root-repositories-dir=${root}

echo "Finished"
