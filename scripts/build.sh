#!/usr/bin/env bash

cd jupyterlab-code-completion
npm run build
jupyter labextension link .
