import os
import shutil
import time

from tqdm import tqdm

from src.config import temp_folder, subject, siva_folder
from src.utils.files import mkdir, move_all_files_from_temp, \
    select_all_files_with_extension, writeJsonFile
from src.utils.mail import MailNotifier
from utils.cmd import repository_by_branch, checkout_branch, head_branches, unpack_siva, write_repository_history

modes = ['files', 'paths', 'histories']


class Dataset:

    def __init__(self, extensions, slice, target_directory, email_notify, arg_modes, only_master):
        self.extensions = extensions
        self.notifier = MailNotifier(subject, email_notify)
        self.target_directory = target_directory
        self.slice = slice
        self.only_master = only_master

        for mode in arg_modes:
            exec(f"self.{mode} = {(mode in arg_modes)}")

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

            if self.paths:
                files_paths[new_name.split('/')[-1]] = file_name.split('/', 1)[1]

            if self.files and sortedDestination in files_data:
                files_data[sortedDestination] += 1
            else:
                files_data[sortedDestination] = 1
            return 1
        else:
            return 0

    def copy_files_from_dir(self, directory, repository):
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

        if self.paths:
            writeJsonFile(f"{temp_folder}/repositories/{repository}/paths.json", files_paths, dataFolder=False)
        if self.files:
            writeJsonFile(f"{temp_folder}/repositories/{repository}/files.json", files_data, dataFolder=False)
        if self.histories:
            write_repository_history(repository)

        return files_count

    def unpack_and_select_files(self, out_siva):
        total_files = 0
        repos_num = 0
        for file in tqdm(out_siva):
            siva_name = unpack_siva(file)
            new_files_count = 0

            # copying files
            for branch in head_branches():
                checkout_branch(branch)
                repo = repository_by_branch(branch)
                if self.only_master:
                    repo += "/master"
                else:
                    repo += '/' + siva_name
                if repo is None:
                    continue
                if self.only_master and os.path.exists(os.path.join(self.target_directory + repo)):
                    continue
                new_files_count = self.copy_files_from_dir(siva_folder, repo)
                repos_num += 1
            total_files += new_files_count

            # deleting the directory with siva files we have already used
            shutil.rmtree(siva_folder)
            os.remove(file)
        return total_files, repos_num
