import psycopg2

from src.config import subject, db_user, db_password, db_host, db_port, db_database
from src.utils.files import writeLinesFile
from src.utils.mail import MailNotifier


def dump_errors(email_notify, skipped_filename):
    select_query = "SELECT (endpoints) FROM repositories WHERE status != 'fetched';"

    connection = psycopg2.connect(user=db_user,
                                  password=db_password,
                                  host=db_host,
                                  port=db_port,
                                  database=db_database)
    cursor = connection.cursor()
    cursor.execute(select_query)
    records = cursor.fetchall()

    git_repos = [row[0][0] for row in records]

    writeLinesFile(skipped_filename, git_repos, mode='a', appendWithNewLine=True)

    MailNotifier(subject, email_notify).send_notification(
        f"Skipped {len(git_repos)} repositories: {git_repos}")

    # closing database connection.
    if connection:
        cursor.close()
        connection.close()
