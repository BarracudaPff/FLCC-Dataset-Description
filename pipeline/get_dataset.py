import json
import os
import subprocess
import shutil
import ast
import re
import datetime

MAX_FILES_NUMBER = 150000
target_directory = '150K_dataset'


def do_bash_command(bash_command):
    process = subprocess.Popen(bash_command.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()
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
    

def unpack_siva_file(dir_name, root, file):
    os.system("mkdir " + dir_name)
    os.system("siva unpack " + root + "/" + file + " " + "./" + dir_name + "/.git")


def is_py3_file(file_name):
    try:
        code_data = open(file_name, 'r', encoding='latin-1').read()
        ast.parse(code_data)
        return True
    except:
        return False


def copy_one_file(file, file_name, destination):
    """Copy one file to the target directory 
        and solving the problem of identical file names.
    """
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


def copy_py3_files_from_dir(directory):
    """Checking on which Python version the file is written. 
       If Python 3 than copy the file to the target directory.
    """
    files_count = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_name = os.path.join(root, file)
            if file.endswith(".py") and is_py3_file(file_name):
                files_count += 1
                copy_one_file(file, file_name, target_directory + '/')
                os.system("find " + target_directory + " -type f | wc -l")
    return files_count


def remove_temp(extra_files_number = 0):
    """Delete temporary data"""
    
    if (extra_files_number):
        files = os.listdir(target_directory)
        for x in files[:extra_files_number]:
            os.remove(target_directory + "/" + x)
        os.remove("list_repos.txt")
        
    shutil.rmtree("repos")
    os.remove("list_siva.txt")

    
def unpack_and_select_py3_files(out_siva):
    """Downloading siva files (500 at a time).
       Unpacking siva file, checkout git branch and selecting all python 3 files.
       Copying all Python 3 files to the targer directory and counting saved files.
       
       out_siva -- names of siva files, that contain Githib repos written in Python fon the PGA dataset.
    """
    
    py3_files_total = 0
    repos_num = 0
    i = 0
    while i in range(0, len(out_siva)):
        # downloading 500 siva files at a time
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
                    new_files_count = 0
                    # copying py3 files
                    if checkout_branch():
                   
                        new_files_count = copy_py3_files_from_dir("siva")
                        if new_files_count:
                            repos_num +=1
                    
                    # deleting the directory with 500 siva files we have alredy used
                    shutil.rmtree("siva")
                    
                    py3_files_total = len([f for f in os.listdir(target_directory) if os.path.isfile(os.path.join(target_directory, f))])
                    print(py3_files_total, "files saved out of", MAX_FILES_NUMBER)
                    print(repos_num, "repos saved")
                    if (py3_files_total > MAX_FILES_NUMBER):
                        # detete extra files
                        extra_files_number = py3_files_total - MAX_FILES_NUMBER
                        remove_temp(extra_files_number)
                        return py3_files_total, repos_num
        remove_temp()
    return py3_files_total, repos_num


if __name__ == '__main__':
    
    # selecting python repositories from PGA
    os.system("pga list -l python -f json > list_repos.txt")
    with open("list_repos.txt", "r") as dsc:
        jsons = list(map(json.loads, dsc.readlines()))
    
    # extracting siva file names from general repositories info
    out_siva = [item for x in jsons for item in x['sivaFilenames']]
    
    os.system("mkdir " + target_directory)
    
    files_total, repos_total = unpack_and_select_py3_files(out_siva)
    
    # Print the stats
    print(repos_total, " repos were selected")
    print(files_total, " python3 files were selected")
    if files_total > MAX_FILES_NUMBER:
        print("required files number was reached")
    else:
        print("required files number was not reached")
