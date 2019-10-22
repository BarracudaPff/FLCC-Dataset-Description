import json
import os
import shutil

import glob2
from tqdm import tqdm

from src.config import dataDir, temp_folder


def count_all_files(root, extension):
    count = 0
    if extension is not None:
        for root, dirs, files in os.walk(root):
            for file in files:
                base, ext = os.path.splitext(file)
                if ext == extension:
                    count += 1
    else:
        count = sum([len(files) for r, d, files in os.walk(root)])
    return count


def select_all_files_with_extension(root, extension):
    if extension is None:
        raise TypeError("Extension can't be None")
    files = glob2.glob(root + f"/**/*{extension}")
    return files


def move_all_files_from_temp(target: str, temp: str):
    for root, dirs, files in tqdm(os.walk(temp), position=0):
        for file in files:
            file_name = os.path.join(root, file)
            if os.path.isfile(file_name):
                move = os.path.join(target, file_name[len(temp) + 1:])
                directories = os.path.dirname(move)
                mkdir(directories)
                shutil.copy(file_name, move)
    try:
        root_dir = temp_folder.split('/')[0]
        shutil.rmtree(root_dir)
    except FileNotFoundError:
        pass


def mkdir(dir):
    os.makedirs(dir, exist_ok=True)


def writeFile(filename: str, data):
    new_file = _unite(filename)
    mkdir(os.path.dirname(new_file))

    with open(new_file, 'w') as w:
        w.write(data)
    return data


def readFile(filename: str):
    new_file = _unite(filename)

    if not os.path.exists(new_file):
        return None

    with open(new_file, 'r') as r:
        return r.read()


def writeLinesFile(filename: str, data: [], appendWithNewLine: bool = False, mode='w'):
    new_file = _unite(filename)
    mkdir(os.path.dirname(new_file))

    if appendWithNewLine:
        dataWithNewLine = [dat + '\n' for dat in data]
    else:
        dataWithNewLine = [dat for dat in data]
        dataWithNewLine.append('\n')

    with open(new_file, mode) as w:
        w.writelines(dataWithNewLine)


def readLinesFile(filename: str):
    new_file = _unite(filename)

    if not os.path.exists(new_file):
        return None

    with open(new_file, 'r') as w:
        data = w.readlines()
    for i in range(len(data)):
        data[i] = data[i][:-1]
    return data


def writeJsonFile(filename: str, data: json, dataFolder=True):
    if dataFolder:
        new_file = _unite(filename)
    else:
        new_file = filename
    mkdir(os.path.dirname(new_file))

    with open(new_file, 'w') as w:
        json.dump(data, w)


def readJsonFile(filename: str) -> json:
    if not os.path.exists(_unite(filename)):
        return None

    with open(_unite(filename), 'r') as r:
        return json.load(r)


def _unite(filename: str):
    return os.path.join(dataDir, filename)
