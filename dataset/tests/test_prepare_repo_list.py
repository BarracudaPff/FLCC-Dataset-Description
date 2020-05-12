from parameterized import parameterized

from src.prepare_repo_list import prepare_list
from src.utils.files import writeLinesFile, readLinesFile
from tests.utils import BaseDataTest

_rep_count = 2
_email_notify = "none"


class TestPrepareRepoList(BaseDataTest):

    @parameterized.expand([
        ["correct list", "test-repositories-list.txt", ["Lorem", "ipsum", "dolor", "sit", "amet,", "consectetur"], 4],
        ["1 item list", "test-repositories-list.txt", ["One item"], 0],
        ["0 item list", "test-repositories-list.txt", [], 0],
    ])
    def test_prepare_list(self, _, repo_list, repositories, expected_len):
        writeLinesFile(repo_list, repositories, appendWithNewLine=True)

        prepare_list(repo_list, _rep_count, _email_notify)

        new_repo_list = readLinesFile(repo_list)
        self.assertEqual(expected_len, len(new_repo_list))

    # noinspection PyUnusedLocal
    @parameterized.expand([
        ["None in filename", None, ["1", "2", "3", "4", "5"], TypeError],
        ["None in data", "test-repositories-list.txt", None, TypeError]
    ])
    def test_prepare_list_with_errors(self, _, repo_list, repositories, error):
        with self.assertRaises(error):
            writeLinesFile(repo_list, repositories, appendWithNewLine=True)
            prepare_list(repo_list, _rep_count, _email_notify)
