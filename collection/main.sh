#!/bin/bash

PYTHONPATH=. python3 -m cProfile -o perf.prof src/main.py "$@"
