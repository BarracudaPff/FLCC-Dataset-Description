import os
import shutil
import subprocess
import time
import sys

from tqdm import tqdm

from src.config import temp_folder, subject, siva_folder
from src.utils.db import Connector
from src.utils.files import mkdir, move_all_files_from_temp, \
    select_all_files_with_extension, writeJsonFile
from src.utils.mail import MailNotifier


def do_bash_command(bash_command):
    process = subprocess.Popen([bash_command], shell=True, stdout=subprocess.PIPE)
    output, _ = process.communicate()
    return output


def checkout_branch():
    branches = do_bash_command(f"git --git-dir={siva_folder}/.git branch")
    if branches:
        branches_list = branches.decode("utf-8").split("\n")
        checkout = branches_list[0]
        for branch in branches_list:
            if branch.lstrip().startswith('HEAD'):
                checkout = branch
        subprocess.call(
            (f"git --git-dir={siva_folder}/.git --work-tree={siva_folder}/ checkout " + checkout).split(),
            stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT)
        return True
    else:
        return False


class Dataset:

    def __init__(self, extensions, slice, target_directory, email_notify):
        self.extensions = extensions
        self.notifier = MailNotifier(subject, email_notify)
        self.target_directory = target_directory
        self.slice = slice
        mkdir(temp_folder + '/languages')
        mkdir(temp_folder + '/repositories')
        mkdir(target_directory)

        for key in extensions:
            mkdir(os.path.join(temp_folder + '/languages', extensions[key]))

    # TODO Temporary out of work
    def pga(self):
        # TODO add creation of siva list
        # do_bash_command("pga list -l python -f json > list_repos.json")
        # with open("list_repos.json", "r") as dsc:
        #     content = list(map(json.loads, dsc.readlines()))
        #
        # out_siva = [item for x in content[:self.slice] for item in x['sivaFilenames']]
        #
        # writeLinesFile(list_siva_temp, out_siva, appendWithNewLine=True)
        #
        # os.system(f"cat {dataDir + list_siva_temp} | pga get -i -o repos")

        files_total, repos_total = self.unpack_and_select_files(select_all_files_with_extension('repos', '.siva'))

        if os.path.exists("repos"):
            shutil.rmtree("repos")

        move_all_files_from_temp(self.target_directory, temp_folder)

        self.notifier.send_notification(
            # f"Left sivas: {len(content[self.slice:])}"
            f" Total files: {files_total} and add repos: {repos_total}")

    def borges(self, sivas_folder):
        full_siva = select_all_files_with_extension(sivas_folder, '.siva')

        out_siva = full_siva[:self.slice]

        if len(out_siva) == 0:
            print(f"No files in {sivas_folder} folder")
            return

        self.notifier.send_notification(f"Started download. "
                                        f"Total siva count: {len(full_siva)}. "
                                        f"Taking only {len(out_siva)} sivas")

        files_total, repos_total = self.unpack_and_select_files(out_siva)
        move_all_files_from_temp(self.target_directory, temp_folder)

        self.notifier.send_notification(f"Finnished. Add files: {files_total} and sivas: {repos_total}\n")

    def copy_file(self, file, file_name, destination, repository, files_data: dict, files_paths: dict):
        """Copy one file to the target directory
            and solving the problem of identical file names.
        """
        base, ext = os.path.splitext(file)
        if ext in self.extensions:
            new_name = (base + '_' + str(time.time_ns()) + ext)
            sortedDestination = self.extensions[ext]
            new_name = os.path.join(destination, sortedDestination, repository, new_name)

            mkdir(os.path.join(destination, sortedDestination, repository))
            try:
                shutil.copy(file_name, new_name)
            except OSError:
                tqdm.write(new_name)
                new_name = (str(time.time_ns()) + ext)
                new_name = os.path.join(destination, sortedDestination, repository, new_name)
                shutil.copy(file_name, new_name)

            files_paths[new_name.split('/')[-1]] = file_name.split('/', 1)[1]

            if sortedDestination in files_data:
                files_data[sortedDestination] += 1
            else:
                files_data[sortedDestination] = 1
            return 1
        else:
            return 0

    def copy_py3_files_from_dir(self, directory, repository):
        """Checking on which Python version the file is written.
           If Python 3 than copy the file to the target directory.
        """
        files_count = 0
        files_data = {}
        files_paths = {}
        for root, dirs, files in os.walk(directory):
            for file in files:
                file_name = os.path.join(root, file)
                if os.path.isfile(file_name):
                    files_count += self.copy_file(file, file_name, temp_folder + '/languages', repository,
                                                  files_data, files_paths)

        writeJsonFile(f"{temp_folder}/repositories/{repository}/paths.json", files_paths, dataFolder=False)
        writeJsonFile(f"{temp_folder}/repositories/{repository}/files.json", files_data, dataFolder=False)
        do_bash_command(f"git --git-dir={siva_folder}/.git config diff.renameLimit 999999")
        do_bash_command(f"git --git-dir={siva_folder}/.git log --pretty=%ct --name-status --diff-filter=ACDMR "
                        f"> {temp_folder}/repositories/{repository}/METADATA")

        return files_count

    def unpack_and_select_files(self, out_siva):
        total_files = 0
        repos_num = 0
        with Connector() as con:
            for file in tqdm(out_siva):
                do_bash_command(f"siva unpack {file} ./{siva_folder}/.git")
                new_files_count = 0

                siva_name = file.split('/')[-1][:-5]
                repository = con.get_repository_by_siva(siva_name)

                # copying files
                if checkout_branch():
                    if repository is None:
                        shutil.rmtree(siva_folder)
                        #shutil.move(file, '/tmp/skipped-repos/'+siva_name+'.siva')
                        continue
                    new_files_count = self.copy_py3_files_from_dir(siva_folder, repository)
                    repos_num += 1

                total_files += new_files_count

                # deleting the directory with siva files we have already used
                shutil.rmtree(siva_folder)
                os.remove(file)
        return total_files, repos_num
