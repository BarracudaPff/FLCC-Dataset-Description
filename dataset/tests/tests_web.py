import os

from parameterized import parameterized

from utils import cleanData, BaseDataClass
from web import downloadWithUrl


class TestWeb(BaseDataClass):

    @parameterized.expand([
        ["good url", "https://raw.githubusercontent.com/github/hub/master/.gitignore", ".ignore", False],
        ["url with error", "https://error.error", "error.er", True]
    ])
    def test_withProgressBar(self, _, url, filename, isError):
        for force in [False, True]:
            content = downloadWithUrl(url, filename, forceDownload=force, progress=True)

            if isError:
                self.assertIsNone(content)
            else:
                self.assertEqual(len(content) > 0, not isError)

    @parameterized.expand([
        ["good url", "https://raw.githubusercontent.com/github/hub/master/.gitignore", ".ignore", False],
        ["url with error", "https://error.error", "error.er", True]
    ])
    def test_withoutProgressBar(self, _, url, filename, isError):
        for force in [False, True]:
            content = downloadWithUrl(url, filename, forceDownload=force, progress=False)

            if isError:
                self.assertIsNone(content)
            else:
                print(os.listdir('../data'))
                self.assertEqual(len(content) > 0, not isError)

    def tearDown(self) -> None:
        cleanData()
