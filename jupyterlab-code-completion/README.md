# Code Completion

A JupyterLab extension for code completion.

## Prerequisites

* JupyterLab 

*(stable at 0.35.4)*

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

Also for server extension, do the following in the repository directory:

```
cd jupyterlab-code-completion
pip install .
jupyter serverextension enable --py jupyterlab-code-completion --sys-prefix
```

To use extension:
```
Select "Select statement" or "Show completions" in command palette, while editing notebook's cell
```
