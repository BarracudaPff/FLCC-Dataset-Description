#!/bin/bash

echo "/tmp/root-repositories/"
ls /tmp/root-repositories/ | wc -l
echo "/tmp/skipped-repos/"
ls /tmp/skipped-repos/ | wc -l
echo "/tmp/timeout-repositories/"
ls /tmp/timeout-repositories/ | wc -l
