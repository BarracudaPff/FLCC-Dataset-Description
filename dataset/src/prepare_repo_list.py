from src.config import temp_repo_list, subject
from src.utils.files import readLinesFile, writeLinesFile
from src.utils.mail import MailNotifier


def prepare_list(repo_list: str, rep_count: int, email_notify: str):
    content = readLinesFile(repo_list)

    writeLinesFile(temp_repo_list, content[:rep_count])

    MailNotifier(subject, email_notify).send_notification(
        f"Was repos: {content.__len__()}. "
        f"Taking only {rep_count} reps. "
        f"Become repos: {content[rep_count:].__len__()}.")

    writeLinesFile(repo_list, content[rep_count:], appendWithNewLine=True)
