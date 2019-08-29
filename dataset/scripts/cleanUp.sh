#!/bin/bash

# Customizable variables
workers=16
timeout=10m
root=/tmp/root-repositories

# Dump errored repos
PYTHONPATH=. python3 -m cProfile -o perf.prof src/dump_error_reps.py

# Prepare list of repositories
PYTHONPATH=. python3 -m cProfile -o perf.prof src/prepare_repo_list.py

# Restart databases (cause of errors)
source scripts/restart.sh

sleep 3

# Clean space
rm -r /tmp/borges/

# Init database for borges
../../../../borges/borges init
sleep 5

# Init repositories to download and compress
../../../../borges/borges producer file data/repo_list.txt

# Start jobs.
../../../../borges/borges consumer --workers=${workers} --timeout=${timeout} --root-repositories-dir=${root}

echo "Finished"
