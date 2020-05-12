from src.utils.exts import extensions
from src.utils.files import writeLinesFile
from tests.utils import BaseDataTest

_filename = "test-languages.txt"


class TestExtensions(BaseDataTest):

    def test_correctInitExtensions(self):
        languages = ["Go", "Java", "CSS"]
        writeLinesFile(_filename, languages, appendWithNewLine=True)
        _extensions = extensions(_filename)

        if languages is not None:
            languagesFromPath = [_extensions[extension].split('/')[0] for extension in _extensions]
            for language in languages:
                self.assertIn(language, languagesFromPath)

    def test_wrongInitExtensions(self):
        languages = ["Go", "TotallyNotJava", "TotallyNotCSS"]
        writeLinesFile(_filename, languages, appendWithNewLine=True)

        with self.assertRaises(TypeError):
            extensions(_filename)
