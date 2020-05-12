#!/usr/bin/env bash

cd jupyterlab-code-completion
npm install
npm run build
jupyter labextension link .

cd ju
pip3 install .
cd ..
jupyter serverextension enable --py jupyterlab-code-completion
