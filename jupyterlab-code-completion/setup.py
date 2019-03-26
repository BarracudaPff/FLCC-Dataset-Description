from distutils.core import setup
from setuptools import find_packages

setup(
    name='jupyterlab-code-completion',
    version='0.1.0',
    description='A Jupyter Notebook server extension',
    packages=find_packages(),
    author='BarracudaPff',
    license='BSD 3-Clause',
    long_description=open('README.md').read(),
    install_requires=[
        'notebook'
    ],
)