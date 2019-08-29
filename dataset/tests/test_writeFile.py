from files import *
from utils import BaseDataClass


# TODO: Unite tests
class TestWriteFile(BaseDataClass):
    # Simple write/read
    def test_writeReadFile(self):
        data = "here some data as string\nWith new lines"
        writeFile("test.txt", data)
        content = readFile("test.txt")
        self.assertEqual(data, content)

    def test_readMissingFile(self):
        content = readFile("test.txt")
        self.assertIsNone(content)

    # Json write/read
    def test_writeReadJsonFile(self):
        data = {
            "data": "here simple\ndata",
            "number": 1,
            "bool": True
        }
        writeJsonFile("test.json", data)
        content = readJsonFile("test.json")
        self.assertEqual(data, content)

    def test_readMissingJsonFile(self):
        content = readJsonFile("test.json")
        self.assertIsNone(content)

    # Array write/read
    def test_writeLinesWithNewLineFile(self):
        data = ["line", 'another line', "", "end"]
        writeLinesFile("test.txt", data, appendWithNewLine=True)
        content = readLinesFile("test.txt")
        self.assertEqual(data, content)

    # Array write/read
    def test_writeLinesWithoutNewLineFile(self):
        data = ["line", 'another line', "", "end"]
        writeLinesFile("test.txt", data, appendWithNewLine=False)
        content = readLinesFile("test.txt")
        self.assertEqual([''.join(data)], content)

    def test_readMissingLinesFile(self):
        content = readFile("test.txt")
        self.assertIsNone(content)
