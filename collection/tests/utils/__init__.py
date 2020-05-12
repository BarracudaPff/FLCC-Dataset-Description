import os
import shutil
from unittest import TestCase

from src.config import dataDir
from src.utils.files import writeFile

# Add files here so that they are not deleted during tests
_saveFiles = [
    "repositories.txt",
    "languages.txt",
    "gh-keys.txt",
    ".gitignore"
]


def composed(*decs):
    def deco(f):
        for dec in reversed(decs):
            f = dec(f)
        return f

    return deco


class BaseDataTest(TestCase):
    def tearDown(self) -> None:
        _cleanData()


class DeepDataTest(BaseDataTest):
    path = '0'
    extension = '.test'

    amount = 3
    files_in_folder = 5

    def setUp(self) -> None:
        deep_path = os.path.join(self.path, self.path)
        deeper_path = os.path.join(deep_path, self.path)
        self.root = os.path.join(dataDir, self.path)

        for i in range(self.files_in_folder):
            writeFile(f"{self.root}/test-file-{i}{self.extension}", f"Test case {i}")

        for i in range(self.files_in_folder):
            writeFile(f"{deep_path}/test-file-{i}{self.extension}", f"Test case {i}")

        for i in range(self.files_in_folder):
            writeFile(f"{deeper_path}/test-file-{i}{self.extension}", f"Test case {i}")


def _cleanData():
    for file in os.listdir(dataDir):
        path = os.path.join(dataDir, file)
        if os.path.isfile(path) and os.path.basename(path) not in _saveFiles:
            os.unlink(path)
        elif os.path.isdir(path):
            shutil.rmtree(path)
