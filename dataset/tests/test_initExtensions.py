from src.extensions import initExtensions
from utils import BaseDataClass


class TestExtensions(BaseDataClass):

    def test_correctInitExtensions(self):
        languages = ["Go", "Java", "CSS"]
        extensions = initExtensions(languages)
        if languages is not None:
            languagesFromPath = [extensions[extension].split('/')[0] for extension in extensions]
            for language in languages:
                self.assertIn(language, languagesFromPath)

    def test_wrongInitExtensions(self):
        languages = ["Go", "TotallyNotJava", "TotallyNotCSS"]
        try:
            initExtensions(languages)
        except Exception as e:
            self.assertEqual(self.failureException, AssertionError)
