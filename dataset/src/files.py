import json
import os

dataDir = '../data'


def writeFile(filename: str, data):
    with open(_unite(filename), 'w') as w:
        w.write(data)
    return data


def readFile(filename: str):
    if not os.path.exists(_unite(filename)):
        return None

    with open(_unite(filename), 'r') as r:
        return r.read()


def writeLinesFile(filename: str, data, appendWithNewLine: bool = False):
    if appendWithNewLine:
        for i in range(len(data)):
            data[i] = data[i] + '\n'

    with open(_unite(filename), 'w') as w:
        w.writelines(data)


def readLinesFile(filename: str):
    if not os.path.exists(_unite(filename)):
        return None

    with open(_unite(filename), 'r') as w:
        data = w.readlines()
    for i in range(len(data)):
        data[i] = data[i][:-1]
    return data


def writeJsonFile(filename: str, data: json):
    with open(_unite(filename), 'w') as w:
        json.dump(data, w)


def readJsonFile(filename: str) -> json:
    if not os.path.exists(_unite(filename)):
        return None

    with open(_unite(filename), 'r') as r:
        return json.load(r)


def _unite(filename: str):
    return os.path.join(dataDir, filename)
