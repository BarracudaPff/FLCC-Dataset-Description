#!/bin/bash

## Pass 1 parameters. First one for the number of loops
# Next parameters may be as input and customizable ($2,$3,...)

# Folder to save dataset (Auto mkdir)
target=dataset/v3

# Folder, where borges saves siva. (Default is /tmp/root-repositories/)
sivas=/tmp/root-repositories/

# The preferred slice for memory and speed is 500
slice=1000

# Email to notify, type "none" to disable
mail=none

# File with list of languages.
# !!! Must be in data folder !!!
languages=languages.txt

# Choose how you downloading sivas. With PGA or Borges
mode=borges

# Use this one for downloading from Borges
# Looping script cause more effective progress and way faster
for (( i=1; i < ${1}; ++i ))
do
    source main.sh dataset \
        --target_directory ${target} \
        --sivas_folder ${sivas} \
        --email_notify none \
        --languages_file ${languages} \
        --slice ${slice} \
        -mode ${mode}
done

if ((${1} > 0))
then source main.sh dataset \
    --target_directory ${target} \
    --sivas_folder ${sivas} \
    --email_notify ${mail} \
    --languages_file ${languages} \
    --slice ${slice} \
    -mode ${mode}
fi
