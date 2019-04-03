import json
import os
import subprocess
import shutil
import ast
import re
import datetime

MAX_FILES_NUMBER = 150000


def do_bash_command(bash_command):
    process = subprocess.Popen(bash_command.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()
    return output


def is_py3_file(file_name):
    try:
        code_data = open(file_name, 'r', encoding='latin-1').read()
        ast.parse(code_data)
        return True
    except:
        return False


def copy_file(file, file_name, destination):
    new_name = file_name.replace('/', '_')
    if not os.path.exists(destination + new_name):
        shutil.copy(file_name, destination + new_name)
    else:
        # change name if file exists
        now = str(datetime.datetime.now())[:19]
        now = now.replace(":", "_").replace(" ", "_")
        base, extension = os.path.splitext(file)
        changed_name = os.path.join(base.replace('/', '_') + "_" + now + extension)
        shutil.copy(file_name, destination + changed_name)


def copy_py3_files(directory):
    files_count = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_name = os.path.join(root, file)
            if file.endswith(".py") and is_py3_file(file_name):
                files_count += 1
                copy_file(file, file_name, "dataset_py3/")
    return files_count


def checkout_branch():
    branches = do_bash_command("git --git-dir=siva/.git branch")
    if branches:
        branches_list = branches.decode("utf-8").split("\n")
        branch = re.findall(r"[A-Za-z0-9-/]+", branches_list[0])
        os.system("git --git-dir=siva/.git --work-tree=siva/ checkout " + branch[0])
        return True
    else:
        return False


# delete temporary data
def remove_temp():
    shutil.rmtree("repos")
    os.remove("list_siva.txt")
    

def unpack_siva_file(dir_name, root, file):
    os.system("mkdir " + dir_name)
    os.system("siva unpack " + root + "/" + file + " " + "./" + dir_name + "/.git")


def unpack_and_select_py3_files(out_siva):

    py3_files_total = 0
    i = 0
    while i in range(0, len(out_siva)):
        # downloading 500 files at a time
        short_siva = out_siva[i:i + 500]
        i += 500
        with open("list_siva.txt", "w+") as dsc:
            for item in short_siva:
                dsc.write("%s\n" % item)

        os.system("cat list_siva.txt | pga get -i -o repos")
        
        for root, dirs, files in os.walk("repos"):
            for file in files:
                if file.endswith(".siva"):
                    unpack_siva_file(dir_name="siva", root=root, file=file)
                   
                    # copying py3 files
                    if checkout_branch():
                        py3_files_total += copy_py3_files("siva")
                        
                    # deleting temporary data
                    shutil.rmtree("siva")
                    
                    print(py3_files_total, "files saved out of ", MAX_FILES_NUMBER)

                    if (py3_files_total > MAX_FILES_NUMBER):
                        remove_temp()
                        return py3_files_total
        remove_temp()
    return py3_files_total


if __name__ == '__main__':
    
    # selecting python repositories from PGA
    os.system("pga list -l python -f json > list_repos.txt")
    with open("list_repos.txt", "r") as dsc:
        jsons = list(map(json.loads, dsc.readlines()))
    
    # extracting siva file names from general repositories info
    out_siva = [item for x in jsons for item in x['sivaFilenames']]
    
    os.system("mkdir dataset_py3")
    
    files_total = unpack_and_select_py3_files(out_siva)
    os.remove("list_repos.txt")
    print(files_total, " python3 files were selected")
    if files_total > MAX_FILES_NUMBER:
        print("max files number was reached")
    else:
        print("max files number was not reached")




