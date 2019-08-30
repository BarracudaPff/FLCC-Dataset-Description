import argparse

import psycopg2

from src.mail import MailNotifier

subject = '''\
Subject: Dataset info From AWS Machine. Repositories with errors

>'''

parser = argparse.ArgumentParser(description='Dump skipped repositories from borges and PostgreSQL')
parser.add_argument('--target_directory', type=str, help='Path to file with skipped repositories',
                    default='data/skipped.txt')
parser.add_argument('--email_notify', type=str, help='Gmail to notify. Type none to skip', default='none')


def main(email_notify, target_directory):
    try:
        select_query = "SELECT (endpoints) FROM repositories WHERE status != 'fetched';"

        connection = psycopg2.connect(user="testing",
                                      password="testing",
                                      host="127.0.0.1",
                                      port="5432",
                                      database="testing")
        cursor = connection.cursor()
        cursor.execute(select_query)
        records = cursor.fetchall()

        git_repos = []
        for row in records:
            git_repos.append(row[0][0] + '\n')

        MailNotifier(subject, email_notify).send_notification(
            f"Skipped: {git_repos}\nTotal {git_repos.__len__()} repos")
        with open(target_directory, 'a') as f:
            f.writelines(git_repos)

        # closing database connection.
        if connection:
            cursor.close()
            connection.close()
            print("PostgreSQL connection is closed")
    except Exception as e:
        if email_notify != 'none':
            MailNotifier(subject, email_notify).send_error(e)


if __name__ == '__main__':
    args = parser.parse_args()
    main(args.email_notify, args.target_directory)
