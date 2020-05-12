from src.config import subject
from src.utils.db import Connector
from src.utils.files import writeLinesFile
from src.utils.mail import MailNotifier


def dump_errors(email_notify, skipped_filename):
    with Connector() as connector:
        git_repos = connector.get_repositories_with_errors()

        writeLinesFile(skipped_filename, git_repos, mode='a', appendWithNewLine=True)

        MailNotifier(subject, email_notify).send_notification(
            f"Skipped {len(git_repos)} repositories: {git_repos}")
