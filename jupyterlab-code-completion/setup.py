from distutils.core import setup
from setuptools import find_packages

setup(
    name                = 'jupyterlab-code-completion',
    version             = '0.1.0',
    description         = 'A Jupyter server extension for code completion',
    packages            = find_packages(),
    author              = '*author here*',
    author_email        = '*author email here*',
    url                 = 'http://github.com/vidartf/jupyterlab_discovery',
    platforms           = "Linux, Mac OS X, Windows",
    license             = 'BSD 3-Clause',
    long_description    = open('README.md').read(),
    install_requires    = [
        'notebook'
    ],
)
