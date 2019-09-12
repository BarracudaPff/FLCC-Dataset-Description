import os
import re
import shutil
import subprocess
import time

from tqdm import tqdm

from config import temp_folder, hide_output, list_siva, list_siva_temp, subject
from utils.exts import extensions as exts
from utils.files import readLinesFile, writeLinesFile, dataDir, mkdir, move_all_files_from_temp, \
    select_all_files_with_extension
from utils.mail import MailNotifier

extensions = exts(None)


def do_bash_command(bash_command):
    process = subprocess.Popen(bash_command.split(), stdout=subprocess.PIPE)
    output, _ = process.communicate()
    return output


def checkout_branch():
    branches = do_bash_command("git --git-dir=siva/.git branch")
    if branches:
        branches_list = branches.decode("utf-8").split("\n")
        branch = re.findall(r"[A-Za-z0-9-/]+", branches_list[0])
        subprocess.call(("git --git-dir=siva/.git --work-tree=siva/ checkout " + branch[0]).split(),
                        stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT)
        # s.system("git --git-dir=siva/.git --work-tree=siva/ checkout " + branch[0] + hide_output)
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
        shutil.copy(file_name, destination + sortedDestination + '/' + new_name)
        return 1
    else:
        return 0


def copy_py3_files_from_dir(directory):
    """Checking on which Python version the file is written.
       If Python 3 than copy the file to the target directory.
    """
    files_count = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_name = os.path.join(root, file)
            if os.path.isfile(file_name):
                files_count += copy_one_file(file, file_name, temp_folder + '/')
    return files_count


def unpack_and_select_files(out_siva):
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

        py3_files_total += new_files_count
        os.remove(file)
    return py3_files_total, repos_num


def pga(slice, email, target_directory):
    # TODO add creation of siva list
    content = readLinesFile(list_siva)

    temp_siva = content[:slice]

    writeLinesFile(list_siva_temp, temp_siva, appendWithNewLine=True)

    os.system(f"cat {dataDir + list_siva_temp} | pga get -i -o repos")
    files_total, repos_total = unpack_and_select_files(select_all_files_with_extension('repos', '.siva'))

    if os.path.exists("repos"):
        shutil.rmtree("repos")

    move_all_files_from_temp(target_directory, temp_folder)

    writeLinesFile(list_siva, content[slice:])

    MailNotifier(subject, email).send_notification(
        f"Left sivas: {content[slice:].__len__()}"
        f" Total files: {files_total} and add repos: {repos_total}")


def borges(slice, email, reverse, target_directory, sivas_folder):
    notifier = MailNotifier(subject, email)
    out_siva = select_all_files_with_extension(sivas_folder, '.siva')
    total = out_siva.__len__()

    if reverse:
        out_siva = out_siva[-slice:]
    else:
        out_siva = out_siva[:slice]

    notifier.send_notification(f"Started download. "
                               f"Total siva count: {total}. "
                               f"Taking only {out_siva[:slice].__len__()} sivas")

    files_total, repos_total = unpack_and_select_files(out_siva)

    move_all_files_from_temp(target_directory, temp_folder)

    notifier.send_notification(
        f"Finnished. Total files:{files_total} and add repos: {repos_total}")


def dataset(target_directory, email_notify, sivas_folder, mode, reverse):
    mkdir(temp_folder)
    mkdir(target_directory)

    for key in extensions:
        mkdir(os.path.join(temp_folder, extensions[key]))
    if mode == 'pga':
        pga(slice, email_notify, target_directory)
    elif mode == 'borges':
        borges(slice, email_notify, reverse, target_directory, sivas_folder)
    else:
        raise Exception
