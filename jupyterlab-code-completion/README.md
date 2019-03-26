# Code Completion

A JupyterLab extension for code completion.

## Prerequisites

* JupyterLab

## Installation
```
jupyter labextension install jupyterlab-code-completion
```

## Development
For a development install (requires npm version 4 or later), do the following in the repository directory:

```bash
npm install
npm run build
jupyter labextension link .
```

Also for server extension (to be sure)

```
cd jupyterlab-code-completion
pip install .
jupyter serverextension enable --py jupyterlab-code-completion --sys-prefix
```

To use extension:
```
run "Hello World" or "Get Code" in command palette
```
