import json
import os
import time

from tqdm import tqdm

from src.config import subject, calibrate_folder
from src.utils.files import writeLinesFile
from src.utils.mail import MailNotifier


def _log_files(repos, root_reps, root_langs):
    log = []

    for rep in tqdm(repos, desc="Getting files"):
        with open(os.path.join(root_reps, rep, "files.json"), 'r') as r:
            content = json.load(r)

        for lang, must in content.items():
            real = len(os.listdir(os.path.join(root_langs, lang, rep)))
            if must != real:
                log.append(rep)

    print(f"Found {len(log)} errors in files.json")
    return list(set(log))


def _log_paths(repos, root_reps, root_langs):
    log = []

    for rep in tqdm(repos, desc="Getting paths"):
        with open(os.path.join(root_reps, rep, "paths.json"), 'r') as r:
            paths = json.load(r)
        with open(os.path.join(root_reps, rep, "files.json"), 'r') as r:
            files = json.load(r)

        for path, _ in files.items():
            for f in os.listdir(os.path.join(root_langs, path, rep)):
                if f not in paths:
                    log.append(rep)

    print(f"Found {len(log)} errors in paths.json")
    return log


def _check_for_branches(root_reps, only_master):
    log = []
    for user in tqdm(os.listdir(root_reps), desc="Checking branches"):
        for rep in os.listdir(os.path.join(root_reps, user)):
            full_path = os.path.join(root_reps, user, rep)
            if len(os.listdir(full_path)) < 1:
                log.append(rep)
                continue

            if only_master:
                if len(os.listdir(full_path)) != 1 or not os.path.isdir(os.path.join(full_path, "master")):
                    log.append(rep)
            else:
                found = False
                for hash in os.listdir(full_path):
                    if hash == 'master':
                        found = True
                if not found:
                    log.append(rep)

    print(f"Found {len(log)} errors in branches")
    return log


# noinspection PyShadowingBuiltins
def _get_all_repos(target_directory):
    repos = []

    for user in tqdm(os.listdir(target_directory), desc="Getting repos"):
        for rep in os.listdir(os.path.join(target_directory, user)):
            for hash in os.listdir(os.path.join(target_directory, user, rep)):
                repository = user + '/' + rep + '/' + hash
                repos.append(repository)

    print(f"Found {len(repos)} repositories")
    return repos


def calibrate(email, target_directory, only_master):
    notifier = MailNotifier(subject, email)
    root_reps = os.path.join(target_directory, 'repositories')
    root_langs = os.path.join(target_directory, 'languages')

    repos = _get_all_repos(root_reps)
    files = _log_files(repos, root_reps, root_langs)
    paths = _log_paths(repos, root_reps, root_langs)
    branches = _check_for_branches(root_reps, only_master)

    if len(paths) > 0:
        remove_answer = input(f"Remove {len(paths)} files not listed in paths.json? (Y/n) ").lower() in ["yes", 'y']

        if remove_answer:
            for file in tqdm(paths, desc="Remove corrupted files"):
                os.remove(file)

    timestamp = str(time.time_ns())

    if len(files) > 0:
        writeLinesFile(f"{calibrate_folder}/calibration-files-{timestamp}.txt", files)
        notifier.send_notification(f"Total amount of errors with files: {len(files)}")
    if len(paths) > 0:
        writeLinesFile(f"{calibrate_folder}/calibration-paths-{timestamp}.txt", paths)
        notifier.send_notification(f"Total amount of errors with paths: {len(paths)}")
    if len(branches) > 0:
        writeLinesFile(f"{calibrate_folder}/calibration-branches-{timestamp}.txt", branches)
        notifier.send_notification(f"Total amount of errors with branches: {len(branches)}")
