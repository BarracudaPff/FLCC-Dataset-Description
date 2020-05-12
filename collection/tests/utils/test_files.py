from parameterized import parameterized

from src.utils.files import *
from . import BaseDataTest, DeepDataTest


# Simple write/read
class TestWriteReadFile(BaseDataTest):
    @parameterized.expand([
        ["simple data", "test.txt", "Lorem ipsum dolor sit amet, consectetur", None],
        ["data with folders", "test/with/folder.py", "Lorem ipsum dolor sit amet, consectetur", None],
        ["file without extension", "test", "Lorem ipsum dolor sit amet, consectetur", None],
        ["None in filename", None, "Lorem ipsum dolor sit amet, consectetur", TypeError],
        ["None in data", "test.none", None, TypeError],
    ])
    def test_write_read_file(self, _, filename, data, errClass):
        try:
            writeFile(filename, data)
            content = readFile(filename)
            self.assertEqual(content, data)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    def test_read_missing_file(self):
        content = readFile("test.txt")
        self.assertIsNone(content)


# Json write/read
class TestWriteReadJsonFile(BaseDataTest):

    @parameterized.expand([
        ["simple json data", "test.json", {"test": 123}, None],
        ["data with folders", "test/with/folder.txt", {"test": "data"}, None],
        ["None in filename", None, {"test": "someData"}, TypeError],
        ["None in data", "test.none", None, TypeError],
    ])
    def test_write_read_json_file(self, _, filename, data, errClass):
        try:
            writeJsonFile(filename, data)
            content = readJsonFile(filename)
            self.assertEqual(content, data)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    def test_read_missing_json_file(self):
        content = readJsonFile("test.json")
        self.assertIsNone(content)


# Array write/read
class TestWriteReadArrayFile(BaseDataTest):
    test_data = [
        ["simple array data", "test.json", ["line", "another line", "", "end"], None],
        ["array with different types", "test.json", ["line", "with", "", 1, bool], TypeError],
        ["None in filename", None, ["Simple", "line"], TypeError],
        ["None in data", "test.none", None, TypeError],
    ]

    @parameterized.expand(test_data)
    def test_writeLinesFile_without_new_line(self, _, filename, data, errClass):
        try:
            writeLinesFile(filename, data, appendWithNewLine=False)
            content = readLinesFile(filename)
            self.assertEqual(["".join(data)], content)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    @parameterized.expand(test_data)
    def test_writeLinesFile_with_new_line(self, _, filename, data, errClass):
        try:
            writeLinesFile(filename, data, appendWithNewLine=True)
            content = readLinesFile(filename)
            self.assertEqual(data, content)
        except Exception as e:
            self.assertIs(e.__class__, errClass)

    def test_read_missing_lines_file(self):
        content = readFile("test.txt")
        self.assertIsNone(content)


class TestDeepFiles(DeepDataTest):
    def test_select_all_files_with_extension(self):
        self._assert_files(self.root)

    def test_move_all_files_from_temp(self):
        target = os.path.join(dataDir, 'test-target')

        move_all_files_from_temp(target, self.root)

        self._assert_files(target)

    def _assert_files(self, path):
        files = select_all_files_with_extension(path, self.extension)
        for file in files:
            self.assertTrue(os.path.exists(file))

        expected = self.files_in_folder * self.amount
        self.assertEqual(expected, len(files))


class TestFileUtils(BaseDataTest):

    @parameterized.expand([
        ["create one dir", "test"],
        ["create multiple dirs", "test/for/folder/s"],
        ["create dir with slash", "test/with/last/slash/"]
    ])
    def test_mkdir(self, _, path):
        data_path = os.path.join(dataDir, path)

        self.assertFalse(os.path.exists(data_path))
        mkdir(data_path)
        self.assertTrue(os.path.exists(data_path))

    def test_mkdir_None_path(self):
        with self.assertRaises(TypeError):
            mkdir(None)

    @parameterized.expand([
        ["None in root", None, ".test"],
        ["None in extension", dataDir, None]
    ])
    def test_select_all_files_with_extension_Nones(self, _, root, extension):
        with self.assertRaises(TypeError):
            select_all_files_with_extension(root, extension)
