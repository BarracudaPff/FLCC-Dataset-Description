#!/bin/bash

folder=$1
file=$2
mkdir ${folder}
pv ${file} | tar xzf - -C ${folder}
