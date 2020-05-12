import os
from urllib import request

from .files import writeFile, readFile


def validateURL(url: str):
    if not url.startswith('https://') and not url.startswith('http://'):
        return 'http://' + url
    else:
        return url


# TODO: add progress bar later
def downloadWithUrl(url: str, filename: str, forceDownload: bool = False, progress: bool = None):
    validUrl = validateURL(url)

    if not os.path.exists(filename) or forceDownload:
        response = request.urlopen(validUrl)
        content = response.read()

        return writeFile(filename, content.decode())
    else:
        return readFile(filename)
