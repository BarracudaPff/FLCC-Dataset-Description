#!/bin/bash

folder=$1
outfile=$2
tar cf - ${folder} -P | pv -s $(du -sb ${folder} | awk '{print $1}') | gzip > ${outfile}
