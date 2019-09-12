import os
import time

from tqdm import tqdm

from src.config import subject, statistic_folder
from src.utils.files import writeLinesFile
from src.utils.mail import MailNotifier


def statistic(email, target_directory):
    notifier = MailNotifier(subject, email)
    log = ["Folder, number or files"]

    for root, dirs, files in tqdm(os.walk(target_directory)):
        path = root.split('/')
        if os.path.isdir(root) and path[-1].isdigit():
            log.append(f"{root}, {(len([name for name in os.listdir(root)]))}")

    if len(log) > 0:
        writeLinesFile(f"{statistic_folder}/stat-{str(time.time())}.csv", log, appendWithNewLine=True)
        notifier.send_notification(log)
