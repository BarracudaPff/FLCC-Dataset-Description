#!/usr/bin/env bash

jupyter labextension install jupyterlab-code-completion
pip install jupyterlab-code-completion
jupyter serverextension enable --py jupyterlab-code-completion
