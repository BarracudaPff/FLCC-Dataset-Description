#!/bin/bash

# Customizable variables
workers=16
timeout=10m
root=/tmp/root-repositories

# Dump errored repos
python3 ../src/prepare_repo_list.py

# Prepare list of repositories
python3 ../src/prepare_repo_list.py

# Restart databases (cause of errors)
source ./restart.sh

sleep 3

# Clean space
rm -r /tmp/borges/

# Init database for borges
../../../borges/borges init
sleep 5

# Init repositories to download and compress
../../../borges/borges producer file ../data/repo_list.txt

# Start jobs.
../../../borges/borges consumer --workers=${workers} --timeout=${timeout} --timeout-repositories-dir=${root}

# Extract py code. Better to use in another terminal, parallel
# python3 ../src/get_dataset.py

echo "Finished"
