#!/bin/bash

## Pass 1 parameters. First one for the number of loops

# Next parameters may be as input and customizable ($2,$3,...)
# Script to run
file=src/get_dataset.py

# Folder to save dataset (Auto mkdir)
target=dataset/v2

# Folder, where borges saves siva. (Default is /tmp/root-repositories/)
sivas=/tmp/root-repositories/

# The preferred slice for memory and speed is 500
slice=1000

# Email to notify, type "none" to disable
mail=none

# Use this one for downloading from Borges
# Looping script cause more effective progress and way faster
for (( i=1; i < ${1}; ++i ))
do
    PYTHONPATH=. python3 -m cProfile -o perf.prof ${file} \
    --target_directory ${target} \
    --sivas_folder ${sivas} \
    --email_notify none \
    --slice ${slice}
done

if ((${1} > 0))
then PYTHONPATH=. python3 -m cProfile -o perf.prof ${file} \
  --target_directory ${target} \
  --use_pga false \
  --email_notify ${mail} \
  --slice ${slice}
fi
