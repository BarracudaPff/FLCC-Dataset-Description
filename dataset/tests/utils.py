import os
import shutil
import unittest
from unittest import TestCase

from src.files import dataDir

# Add files here so that they are not deleted during tests
_saveFiles = [
    "repositories.txt",
    "languages.txt",
    "gh-keys.txt",
    ".gitignore"
]


class BaseDataClass(TestCase):
    def tearDown(self) -> None:
        cleanData()


def cleanData():
    for file in os.listdir(dataDir):
        path = os.path.join(dataDir, file)
        if os.path.isfile(path) and os.path.basename(path) not in _saveFiles:
            os.unlink(path)
        elif os.path.isdir(path):
            shutil.rmtree(path)


if __name__ == '__main__':
    loader = unittest.TestLoader()
    start_dir = 'tests'
    suite = loader.discover(start_dir)

    runner = unittest.TextTestRunner()
    runner.run(suite)
