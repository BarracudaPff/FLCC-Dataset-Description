import yaml

from files import writeJsonFile, readLinesFile
from web import downloadWithUrl


def initExtensions(languages: [] = None):
    if languages is None:
        languages, data = _getAllLanguages()
    else:
        _, data = _getAllLanguages()

    extensions = {}

    for language in languages:
        languageExtensions = data.get(language).get('extensions')
        if languageExtensions is None:
            continue
        for extension in languageExtensions:
            extensions[extension] = f"{language}/{extension}/0"

    writeJsonFile("extensions.json", extensions)
    print(f"Initialized {len(extensions)} extensions for {len(languages)} languages")
    return extensions


def _getAllLanguages() -> ([], dict):
    content = downloadWithUrl(
        'https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml',
        'languages.yml',
        True)
    try:
        data = yaml.safe_load(content)
        languages = [language for language in data]
        return languages, data
    except yaml.YAMLError as exc:
        print(exc)


if __name__ == '__main__':
    languages = readLinesFile("languages.txt")
    initExtensions(languages)
