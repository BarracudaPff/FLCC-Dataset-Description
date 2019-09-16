from unittest import TestCase, mock

from src.utils.db import Connector


class TestExtensions(TestCase):

    @mock.patch("psycopg2.connect")
    def test_get_repository_by_siva(self, mock_connect):
        siva = "1c0259ae455dc613be2ee12320c1572afc820f6a"
        repo = "thedillonb/http-shutdown"

        query_result = [(siva, ["https://github.com/" + repo])]
        mock_connect.return_value.cursor.return_value.fetchall.return_value = query_result

        with Connector() as con:
            rep = con.get_repository_by_siva(siva)

        self.assertEqual(repo + '/' + siva, rep)

    @mock.patch("psycopg2.connect")
    def test_get_repositories_with_errors(self, mock_connect):
        repositories = [
            "https://github.com/plexinc-plugins/Services.bundle",
            "https://github.com/CISecurity/OVALRepo",
            "https://github.com/IIJ-NetBSD/netbsd-src"
        ]

        query_result = [([rep],) for rep in repositories]

        mock_connect.return_value.cursor.return_value.fetchall.return_value = query_result

        with Connector() as con:
            rep = con.get_repositories_with_errors()

        self.assertEqual(repositories, rep)
