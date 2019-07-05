#!/usr/bin/env bash

jupyter labextension install jupyterlab-code-completion
cd jupyterlab-code-completion
pip3 install .
cd ..
jupyter serverextension enable --py jupyterlab-code-completion
