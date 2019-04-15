# Code Completion

A JupyterLab extension for code completion.

## Prerequisites

* JupyterLab 

*(stable at 0.35.4)*

## Installation
Perform step by step
```bash
jupyter labextension install jupyterlab-code-completion
pip install jupyterlab-code-completion
jupyter serverextension enable --py jupyterlab-code-completion
```

## Usage
Select `Select statement` or `Show completions` in command palette, while editing notebook's cell


## Development
Requires npm version 4 or later

```bash
cd jupyterlab-code-completion
npm install
npm run build
jupyter labextension link .
```
Also for server extension
```bash
pip install .
jupyter serverextension enable --py jupyterlab-code-completion
```

To rebuild the package and the JupyterLab app:

```bash
npm run build
jupyter lab build
```
