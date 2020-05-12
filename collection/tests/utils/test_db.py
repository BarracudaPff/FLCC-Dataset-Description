from unittest import TestCase, mock

from src.utils.db import Connector


class TestExtensions(TestCase):

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
