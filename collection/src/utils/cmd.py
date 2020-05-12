import os
import subprocess

from src.config import siva_folder, temp_folder, renameLimit, diff_filter


def _do_bash_command(bash_command):
    process = subprocess.Popen([bash_command], shell=True, stdout=subprocess.PIPE)
    output, _ = process.communicate()
    return output


def checkout_branch(branch: str):
    subprocess.call(
        (f"git --git-dir={siva_folder}/.git --work-tree={siva_folder}/ checkout " + branch).split(),
        stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT)


def head_branches() -> []:
    _branches = []
    for branch in _do_bash_command(f"git --git-dir={siva_folder}/.git branch").decode("utf-8").split("\n"):
        if branch.lstrip().startswith('HEAD/'):
            _branches.append(branch.lstrip())
    return _branches


def repository_by_branch(branch: str):
    assert branch.startswith('HEAD/')
    id = branch[5:]

    url = _do_bash_command(f"git --git-dir={siva_folder}/.git ls-remote --get-url {id}")[:-1].decode()
    return f"{url[19:]}"


def write_repository_history(repository):
    _do_bash_command(f"git --git-dir={siva_folder}/.git config diff.renameLimit {renameLimit}")
    _do_bash_command(f"git --git-dir={siva_folder}/.git log --pretty=%ct --name-status --diff-filter={diff_filter} "
                     f"> {temp_folder}/repositories/{repository}/METADATA")


def unpack_siva(siva_file) -> str:
    _do_bash_command(f"siva unpack {siva_file} ./{siva_folder}/.git")
    return siva_file.split('/')[-1][:-5]
