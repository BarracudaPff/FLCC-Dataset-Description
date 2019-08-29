import argparse

from src.mail import MailNotifier

subject = '''\
Subject: Dataset info From AWS Machine. New repo list

>'''

parser = argparse.ArgumentParser(description='Get slice of repositories for downloading.')
parser.add_argument('--rep_count',      type=int,   help='Number of repositories to get from the general list.',
                    default=5000)
parser.add_argument('--email_notify',   type=str,   help='Gmail to notify. Type none to skip.',
                    default='none')
parser.add_argument('--temp_repo_list', type=str,   help='Path of file with temporary list of repositories.',
                    default='../data/repo_list.txt')
parser.add_argument('--reverse',        type=bool,  help='Reverse list of repositories',
                    default=True)
parser.add_argument('--repo_list',      type=str,
                    help='Path of file with full list of repositories. WARNING: list will be reduced.',
                    default='../data/rep-list-working.txt')


def prepare_repo_list(repo_list: str, temp_repo_list: str, rep_count: int, email_notify: str, reverse: bool):
    with open(repo_list, "r") as f:
        content = f.readlines()

    if reverse:
        with open(temp_repo_list, "w") as fw:
            fw.writelines(content[-rep_count:])
    else:
        with open(temp_repo_list, "w") as fw:
            fw.writelines(content[:rep_count])

    MailNotifier(subject, email_notify).send_notification(
        f"Was repos: {content.__len__()}. "
        f"Taking only {rep_count} reps. "
        f"Was become: {content[rep_count:].__len__()}.")

    with open(repo_list, "w") as fw:
        fw.writelines(content[rep_count:])


if __name__ == '__main__':
    args = parser.parse_args()
    prepare_repo_list(args.repo_list, args.temp_repo_list, args.rep_count, args.email_notify, args.reverse)
