import json
import os
import subprocess
import shutil
import ast

MAX_FILES_NUMBER = 150000

def do_bash_command(bash_command):
    process = subprocess.Popen(bash_command.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()
    return output

def is_py3_file(fileName):
    code_data = open(fileName).read()
    try:
        ast.parse(code_data)
        return True
    except SyntaxError:
        return False


def get_py3_files(directory):
    files_count = 0
    for root, dirs, files in os.walk(directory):
        path = root.split(os.sep)
        print((len(path) - 1) * '---', root )
        for file in files:
            if file.endswith(".py") and is_py3_file(file):
                print(file)
                files_count += 1
                shutil.copy(file, 'dataset_py3')
    return files_count

if __name__ == '__main__':
    
    os.system("pga list -l python -f json > list_repos.txt")
    with open("list_repos.txt", "r") as dsc:
        jsons = list(map(json.loads, dsc.readlines()))

    out_siva = [item for x in jsons for item in x['sivaFilenames']]
    #сокращенный список siva файлов для тестирования
    short_siva = out_siva[0:5]

    with open("list_siva.txt", "w+") as dsc:
        for item in short_siva:
            dsc.write("%s\n" % item)

    #скачали siva файлы
    os.system("cat list_siva.txt | pga get -i -o repos")

    # распаковка репозиториев и отбор py3 файлов
    files_total = 0
    for root, dirs, files in os.walk("repos"):
        path = root.split(os.sep)
        for file in files:
            if file.endswith(".siva"):
                os.system("mkdir siva")
                unpack_siva_command = "siva unpack " + root + "/" + file + " " + "./siva/.git"
                print(unpack_siva_command)
                os.system(unpack_siva_command)
                files_total += get_py3_files("siva")
                #удаление промежуточных данных
                shutil.rmtree("siva")
                if (files_total > MAX_FILES_NUMBER):
                    break
                    
    print(files_total,  " python3 files were selected")

