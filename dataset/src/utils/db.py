import psycopg2

from src.config import db_user, db_password, db_host, db_port


# noinspection SqlNoDataSourceInspection,SqlResolve
class Connector:
    def __init__(self, user=db_user, password=db_password, host=db_host, port=db_port):
        self.connection = psycopg2.connect(user=user, password=password, host=host, port=port)

    pass

    def get_repository_by_siva(self, siva: str) -> []:
        query = f"SELECT repository_references.init, repositories.endpoints " \
            f"FROM repository_references INNER JOIN repositories " \
            f"ON repository_references.repository_id = repositories.id " \
            f"WHERE repository_references.init ='{siva}' GROUP BY repository_references.init, endpoints"

        cursor = self.connection.cursor()
        cursor.execute(query)
        records = cursor.fetchall()

        cursor.close()
        return f"{records[0][1][0][19:]}/{records[0][0]}"

    def get_repositories_with_errors(self) -> []:
        select_query = "SELECT (endpoints) FROM repositories WHERE status != 'fetched';"

        cursor = self.connection.cursor()
        cursor.execute(select_query)
        records = cursor.fetchall()

        cursor.close()

        return [row[0][0] for row in records]

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        if self.connection:
            self.connection.close()
