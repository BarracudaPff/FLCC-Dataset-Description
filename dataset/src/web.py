import os
from urllib import request
from urllib.error import URLError

from src.files import writeFile, readFile


def downloadWithUrl(url: str, filename: str, forceDownload: bool = False, progress: bool = None):
    try:
        if not os.path.exists(filename) or forceDownload:
            response = request.urlopen(url)
            content = response.read()

            return writeFile(filename, content.decode())
        else:
            return readFile(filename)
    except URLError as e:
        return None
