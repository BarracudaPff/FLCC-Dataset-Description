import os
import shutil
import sys
import time

from tqdm import tqdm

from src.config import max_files_in_folder, subject
from src.utils.files import writeLinesFile
from src.utils.mail import MailNotifier


def handle_huge_folder(path: [], log, position, disableTQDM=False) -> bool:
    root = '/'.join(path[:-1])
    old_folder = '/'.join(path)

    new_folder = max(list(map(int, os.listdir(root)))) + 1
    new_folder = os.path.join(root, str(new_folder))

    files = os.listdir(old_folder)

    if len(files) < max_files_in_folder:
        position -= 1
        return False

    left = len(files) - max_files_in_folder
    left = left if left > 0 else 0

    os.makedirs(new_folder)
    for f in tqdm(files[:max_files_in_folder], desc=root, position=position, disable=disableTQDM):
        pass
        shutil.move(os.path.join(old_folder, f), new_folder)

    log.append(f"Moved {max_files_in_folder} files {old_folder} -> {new_folder}. In folder left {left}")
    return left > 0


def calibrate(email, target_directory):
    notifier = MailNotifier(subject, email)
    log = []

    position = 0
    for root, dirs, files in tqdm(os.walk(target_directory), position=0):
        path = root.split('/')
        if path[-1] == '0':
            num = (len(os.listdir(root)))
            if num >= max_files_in_folder:
                while handle_huge_folder(path, log, position):
                    position += 1

    sys.stdout.flush()
    if len(log) > 0:
        writeLinesFile("moving-log-" + str(time.time()) + '.txt', log, appendWithNewLine=True)
    print('\n' * position)
    notifier.send_notification(log)
