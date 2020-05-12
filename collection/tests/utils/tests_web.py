from urllib import request
from urllib.error import URLError

from parameterized import parameterized

from src.utils.web import downloadWithUrl, validateURL
from . import BaseDataTest

_test_data = [
    ["correct url", "https://raw.githubusercontent.com/github/hub/master/.gitignore", ".ignore", None],
    ["url without https", "raw.githubusercontent.com/github/hub/master/.gitignore", ".ignore2", None],
    ["url with error", "https://error.error", "error.er", URLError],
    ["url is None", None, "error.er", AttributeError],
    ["filename is None", "error.error.error", None, TypeError]
]


class TestWeb(BaseDataTest):

    @parameterized.expand(_test_data)
    def test_downloadWithUrl(self, _, url, filename, errClass):
        try:
            content = downloadWithUrl(url, filename)

            self.assertGreater(len(content), 0)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    @parameterized.expand(_test_data)
    def test_downloadWithUrl_forceDownload(self, _, url, filename, errClass):
        try:
            content = downloadWithUrl(url, filename, forceDownload=True, progress=False)

            self.assertGreater(len(content), 0)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    @parameterized.expand(_test_data)
    def test_downloadWithUrl_progress(self, _, url, filename, errClass):
        try:
            content = downloadWithUrl(url, filename, forceDownload=False, progress=True)

            self.assertGreater(len(content), 0)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    @parameterized.expand([
        ["filename with https", "https://raw.githubusercontent.com/github/hub/master/.gitignore"],
        ["filename with http", "http://raw.githubusercontent.com/github/hub/master/.gitignore"],
        ["filename without any protocol", "raw.githubusercontent.com/github/hub/master/.gitignore"],
    ])
    def test_validateURL(self, _, url):
        validUrl = validateURL(url)
        response = request.urlopen(validUrl)
        content = response.read()

        self.assertGreater(len(content), 0)
