import os

from src.config import dataDir, statistic_folder
from src.utils.files import readLinesFile
from src.statistic import statistic
from tests.utils import DeepDataTest


class TestStatistic(DeepDataTest):
    def test_statistic(self):
        statistic('none', self.root)

        stat_folder = os.path.join(dataDir, statistic_folder)

        for file in os.listdir(stat_folder):
            content = readLinesFile(os.path.join(stat_folder, file))

            # +1 because of new line in the end of file
            self.assertEqual(self.amount + 1, len(content))

        self.assertEqual(1, len(os.listdir(stat_folder)))

    def test_statistic_None(self):
        with self.assertRaises(TypeError):
            statistic('none', None)
