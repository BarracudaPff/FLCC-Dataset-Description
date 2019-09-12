import os

from parameterized import parameterized

from src import config
from src.calibrate_dataset import handle_huge_folder, calibrate
from src.config import dataDir
from src.utils.files import mkdir, writeFile
from tests.utils import BaseDataTest

_folder = "HTML/.hmtl"


class TestMailNotifier(BaseDataTest):

    def setUp(self) -> None:
        config.max_files_in_folder = 100

    def _create_folder(self, amount, folder):
        path = os.path.join(dataDir, folder, '0')
        mkdir(path)
        for i in range(amount):
            writeFile(f"{folder}/0/test-file-{i}.hmtl", f"test-{i}")

        self.assertEqual(len(os.listdir(path)), amount)

        return path.split('/')

    @parameterized.expand([
        ["amount more max in folder", 110, {"0": 10, "1": 100}],
        ["amount less max in folder", 90, {"0": 90}]
    ])
    def test_handle_huge_folder(self, _, total_amount, distribution):
        log = []
        path = self._create_folder(total_amount, _folder)

        handle_huge_folder(path, log, 0, disableTQDM=True)

        for item in distribution.items():
            num = len(os.listdir(f"{dataDir}/{_folder}/{item[0]}"))
            self.assertEqual(num, item[1])

        self.assertEqual(len(log), len(distribution.items()) - 1)

    @parameterized.expand([
        ["amount more max in folder", 110, {"0": 10, "1": 100}],
        ["amount less max in folder", 90, {"0": 90}],
        ["amount less max in folder", 227, {"0": 27, "1": 100, "2": 100}]
    ])
    def test_calibrate(self, _, total_amount, distribution):
        self._create_folder(total_amount, 'dataset/' + _folder)
        target_dir = os.path.join(dataDir, 'dataset')

        calibrate("none", target_dir)

        for item in distribution.items():
            num = len(os.listdir(f"{dataDir}/dataset/{_folder}/{item[0]}"))
            self.assertEqual(num, item[1])
