import yaml

from .files import writeJsonFile, readLinesFile
from .web import downloadWithUrl


def extensions(languages_file):
    if languages_file is None:
        languages, data = _getAllLanguages()
    else:
        languages = readLinesFile(languages_file)
        _, data = _getAllLanguages()

    _extensions = {}

    for language in languages:
        try:
            languageExtensions = data.get(language).get('extensions')
            if languageExtensions is None:
                continue
            for extension in languageExtensions:
                _extensions[extension] = f"{language}/{extension}"
        except AttributeError:
            raise TypeError(f"Wrong language name: {language}")

    writeJsonFile("extensions.json", _extensions)
    print(f"Initialized {len(_extensions)} extensions for {len(languages)} languages")
    return _extensions


def _getAllLanguages() -> ([], dict):
    content = downloadWithUrl(
        'https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml',
        'languages.yml',
        False)
    data = yaml.safe_load(content)
    languages = [language for language in data]
    return languages, data
