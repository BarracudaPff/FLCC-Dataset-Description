import os
import time

from tqdm import tqdm

from src.config import subject, statistic_folder
from src.utils.files import writeLinesFile, readLinesFile, count_all_files
from src.utils.mail import MailNotifier


class Statistic:
    def __init__(self, email, target_directory):
        self.notifier = MailNotifier(subject, email)
        self.target_directory = target_directory

    def stat_languages(self, languages_file):
        logLang = ["Folder, number or files"]
        languages = readLinesFile(languages_file)

        for language in tqdm(languages):
            directory = os.path.join(self.target_directory, 'languages', language)
            count = count_all_files(directory, None)
            logLang.append(f"{language}, {count}\n")

        if len(logLang) > 0:
            writeLinesFile(f"{statistic_folder}/stat-languages-{str(time.time_ns())}.csv", logLang)
            self.notifier.send_notification(logLang)

    def stat_extensions(self, extensions):
        logExt = ["Folder, number or files"]

        for ext, path in tqdm(extensions.items()):
            directory = os.path.join(self.target_directory, 'languages', path)
            count = count_all_files(directory, ext)
            logExt.append(f"{path}, {count}\n")

        if len(logExt) > 0:
            writeLinesFile(f"{statistic_folder}/statistic-extensions-{str(time.time_ns())}.csv", logExt)
            self.notifier.send_notification(logExt)

    def stat_repositories(self):
        logRepos = []
        total = 0

        for user in tqdm(os.listdir(self.target_directory)):
            for rep in os.listdir(os.path.join(self.target_directory, user)):
                url = f"https://github.com/{user}/{rep}/\n"
                total += len(os.listdir(os.path.join(self.target_directory, user, rep)))
                logRepos.append(url)

        if len(logRepos) > 0:
            writeLinesFile(f"{statistic_folder}/statistic-repositories-{str(time.time_ns())}.txt", logRepos)
            self.notifier.send_notification(f"Total amount of repositories with different branches: {total}")

    def gather_statistic(self, modes, languages_file, exts):
        if 'languages' in modes:
            self.stat_languages(languages_file)
        if 'extensions' in modes:
            self.stat_extensions(exts)
        if 'repositories' in modes:
            self.stat_repositories()
