import argparse
import os
import re
import shutil
import subprocess
import time
from distutils.dir_util import copy_tree

import glob2
from tqdm import tqdm

from extensions import initExtensions
from files import readLinesFile, writeLinesFile
from mail import MailNotifier

subject = '''\
Subject: Dataset info From AWS Machine. Info about downloading dataset

>'''

parser = argparse.ArgumentParser(description='Get dataset for Python 3')
parser.add_argument('--target_directory', type=str, help='Directory to save Python3 file')
parser.add_argument('--sivas_folder', type=str, help='Directory where are sivas')
parser.add_argument('--email_notify', type=str, help='Gmail to notify. Type none to skip', default='none')
parser.add_argument('--reverse', type=bool, help='Reverse list of repositories', default=False)
parser.add_argument('--use_pga', type=bool, help='Use PGA', default=False)
parser.add_argument('--slice', type=int, help='Amount of files from sivas_folder', default=500)

temp_folder = 'temp'
list_siva = 'list-siva.txt'
list_siva_temp = 'list-siva-temp.txt'
hide_output = ' > /dev/null'

languages = readLinesFile("languages.txt")
extensions = initExtensions(languages)


def do_bash_command(bash_command):
    process = subprocess.Popen(bash_command.split(), stdout=subprocess.PIPE)
    output, _ = process.communicate()
    return output


def checkout_branch():
    branches = do_bash_command("git --git-dir=siva/.git branch")
    if branches:
        branches_list = branches.decode("utf-8").split("\n")
        branch = re.findall(r"[A-Za-z0-9-/]+", branches_list[0])
        os.system("git --git-dir=siva/.git --work-tree=siva/ checkout " + branch[0])
        return True
    else:
        return False


def copy_one_file(file, file_name, destination):
    """Copy one file to the target directory
        and solving the problem of identical file names.
    """
    base, ext = os.path.splitext(file)
    if ext in extensions:
        new_name = (base + '_' + str(time.time()) + ext)
        sortedDestination = extensions[ext]
        shutil.copy(file_name, destination + '/' + sortedDestination + '/' + new_name)


def copy_py3_files_from_dir(directory):
    """Checking on which Python version the file is written.
       If Python 3 than copy the file to the target directory.
    """
    files_count = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_name = os.path.join(root, file)
            if os.path.isfile(file_name):
                files_count += 1
                copy_one_file(file, file_name, temp_folder + '/')
    return files_count


def unpack_and_select_files(out_siva):
    """Downloading siva files (500 at a time).
       Unpacking siva file, checkout git branch and selecting all python 3 files.
       Copying all Python 3 files to the targer directory and counting saved files.

       out_siva -- names of siva files, that contain Githib repos written in Python fon the PGA dataset.
    """

    py3_files_total = 0
    repos_num = 0
    for file in tqdm(out_siva):
        os.system("siva unpack " + file + " ./" + "siva" + "/.git" + hide_output)
        new_files_count = 0
        # copying py3 files
        if checkout_branch():
            new_files_count = copy_py3_files_from_dir("siva")
        repos_num += 1

        # deleting the directory with siva files we have already used
        shutil.rmtree("siva")

        py3_files_total = len(
            [f for f in os.listdir(temp_folder) if os.path.isfile(os.path.join(temp_folder, f))])
        py3_files_total += new_files_count
        os.remove(file)
    return py3_files_total, repos_num


def select_all_siva_files(root):
    files = glob2.glob(root + '/**/*.siva')
    return files


def move_all_files_from_temp(target: str, temp: str):
    copy_tree(target, temp)
    shutil.rmtree(temp_folder)


def pga(slice, email, target_directory):
    content = readLinesFile(list_siva)

    temp_siva = content[:slice]

    writeLinesFile(list_siva_temp, temp_siva)

    os.system(f"cat {list_siva_temp} | pga get -i -o repos")
    files_total, repos_total = unpack_and_select_files(select_all_siva_files('repos'))

    if os.path.exists("repos"):
        shutil.rmtree("repos")

    move_all_files_from_temp(temp_folder, target_directory)

    writeLinesFile(list_siva, content[slice:])

    MailNotifier(subject, email).send_notification(
        f"Left sivas: {content[slice:].__len__()}"
        f" Total files: {files_total} and add repos: {repos_total}")


def mkdir(dir):
    if not os.path.exists(dir):
        os.makedirs(dir)


def borges(slice, email, reverse, target_directory, sivas_folder):
    notifier = MailNotifier(subject, email)
    out_siva = select_all_siva_files(sivas_folder)
    total = out_siva.__len__()

    if reverse:
        out_siva = out_siva[-slice:]
    else:
        out_siva = out_siva[:slice]

    notifier.send_notification(f"Started download. "
                               f"Total siva count: {total}. "
                               f"Taking only {out_siva[:slice].__len__()} sivas")

    files_total, repos_total = unpack_and_select_files(out_siva)

    move_all_files_from_temp(temp_folder, target_directory)

    notifier.send_notification(
        f"Finnished. Total files:{files_total} and add repos: {repos_total}")


if __name__ == '__main__':
    args = parser.parse_args()
    mkdir(temp_folder)
    mkdir(args.target_directory)
    for key in extensions:
        mkdir(os.path.join(temp_folder, extensions[key]))
    try:
        if args.use_pga:
            pga(args.slice, args.email_notify, args.target_directory)
        else:
            borges(args.slice, args.email_notify, args.reverse, args.target_directory, args.sivas_folder)
    except Exception as error:
        MailNotifier(subject, args.email_notify).send_error(str(error))
        raise error
