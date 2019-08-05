#!/bin/bash

# Pass 1 parameters. First one for the number of loops

# Next parameters may be as input and customizable ($2,$3,...)
# Script to run
file=../src/get_dataset.py

# Folder to save dataset (Auto mkdir)
target=../dataset/v2/source-py3

# Folder, where borges saves siva. (Default is /tmp/root-repositories/)
sivas=/tmp/root-repositories/

# The preferred slice for memory and speed is 500
slice=500

# Email to notify, type none to disable
mail=mrm.kikll@gmail.com

# Use this one for downloading from Borges
# Looping script cause more effective progress and way faster
for (( i=1; i < ${1}; ++i ))
do
    python3 ${file} \
    --target_directory ${target} \
    --sivas_folder ${sivas} \
    --sivas_folder none \
    --slice ${slice}
done

if ((${1} > 0))
then python3 ${file} \
  --target_directory ${target} \
  --sivas_folder ${sivas} \
  --email_notify ${mail} \
  --slice ${slice}
fi


# Use this one for PGA downloading

# for (( i=1; i < ${1}; ++i ))
# do
#     python3 ${file} \
#     --target_directory ${target} \
#     --use_pga true \
#     --email_notify none \
#     --slice ${slice}
# done
#
# if ((${1} > 0))
# then python3 ${file} \
#   --target_directory ${target} \
#   --use_pga true \
#   --email_notify ${mail} \
#   --slice ${slice}
# fi
