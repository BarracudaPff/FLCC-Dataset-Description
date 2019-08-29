import os
import shutil
from unittest import TestCase

from files import dataDir

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
