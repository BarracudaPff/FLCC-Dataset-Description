import argparse
import os
import shutil
import time

from tqdm import tqdm

from files import writeLinesFile
from src.mail import MailNotifier

subject = '''\
Subject: Dataset info From AWS Machine. Repositories with errors

>'''

parser = argparse.ArgumentParser(description='Dump skipped repositories from borges and PostgreSQL')
parser.add_argument('--target_directory', type=str, help='Dataset directory root')
parser.add_argument('--email_notify', type=str, help='Gmail to notify. Type none to skip', default='none')

max_files_in_folder = 100


def handleHugeFolder(path: [], log):
    root = '/'.join(path[:-1])
    results = list(map(int, os.listdir(root)))
    old_folder = '/'.join(path)
    new_folder = max(results) + 1

    new_folder = os.path.join(root, str(new_folder))

    os.makedirs(new_folder)

    files = os.listdir(old_folder)
    for f in tqdm(files, desc=root):
        shutil.move(os.path.join(old_folder, f), new_folder)

    log.append(f"Moved {len(files)} files {old_folder} -> {new_folder}")


def main(email, target_directory):
    notifier = MailNotifier(subject, email)
    log = []

    for root, dirs, files in os.walk(target_directory):
        path = root.split('/')
        if path[-1] == '0':
            num = (len([name for name in os.listdir(root)]))
            if num > max_files_in_folder:
                handleHugeFolder(path, log)

    if len(log) > 0:
        writeLinesFile("moving-log-" + str(time.time()) + '.txt', log, appendWithNewLine=True)
    notifier.send_notification(log)


if __name__ == '__main__':
    args = parser.parse_args()
    main(args.email_notify, args.target_directory)
