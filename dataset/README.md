# Create Dataset

## Create repository list

1. Download [latest version](https://golang.org/doc/install) of Go

2. Install [siva](https://github.com/src-d/go-siva)

    `go get -u gopkg.in/src-d/go-siva.v1/...`
3. Install [pga](https://github.com/src-d/datasets/tree/master/PublicGitArchive/pga)

    `go get github.com/src-d/datasets/PublicGitArchive/pga`
4. Install [pga-create](https://github.com/src-d/datasets/blob/master/PublicGitArchive/pga-create/README.md)

    `go get -v github.com/src-d/datasets/PublicGitArchive/pga-create`    
5. Download list of repositories and stars with latest url from [here](http://ghtorrent-downloads.ewi.tudelft.nl/mysql/.)
    
    `pga-create discover --url=http://ghtorrent-downloads.ewi.tudelft.nl/mysql/mysql-2019-06-01.tar.gz`
    
## Scan list of repositories

1. Insert github keys in `gh-keys.txt`. Better to use more than 10 keys

1. Scan latest data
    
    `TODO`
2. Count language and licences statistic

    `TODO`
3. Select languages and create file for extensions
    
    `TODO`

## Download 

Python scripts:

- python3 get_dataset.py - create dataset.
- python3 dump_error_reps.py - dump errored repositories from Borges.
- python3 prepare_repo_list.py - Prepare list for Borges, taking only __slice__ off all items to save memory.

Scripts:

- cleanUp.sh - Restart Borges, download and archive new part of dataset using Borges and siva files.
- loop script - Loop Python scripts. Suitable for get_dataset.py (*PGA* and *Borges*).
- restart.sh - Restart Borges, PSQL, RabbitMQ and their docker images.

