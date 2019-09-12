# Create Dataset

Module to get dataset of source files from Github.
Using pga-create and latest gh-torrent

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

## Download 

Python scripts:

For easier use write `./main.sh <arguments>`

To run scripts use `./scripts/<some-script.sh>`

### Python script options
| Options           | Description                                                   |
| ----------------- | ------------------------------------------------------------- |
| `dataset`         | Get dataset for Languages                                     |
| `statistic`       | Get statistic for downloaded dataset                          |
| `prepare_list`    | Extract slice of repositories to downloading and compress     |
| `dump_errors`     | Dump repositories that caused the download error              |
| `calibrate`       | Calibrate dataset                                             |

### Scripts
| Script            | Short Description                                                                           |
| ----------------- | ------------------------------------------------------------------------------------- |
| `cleanUp.sh`      | Restart Borges, download and archive new part of dataset using Borges and siva files  |
| `restart.sh`      | Restart Borges, PSQL, RabbitMQ and their docker images                                |
| `loop.sh`         | script - Loop Python scripts. Suitable for get_dataset.py (*PGA* and *Borges*)        |
| `coutn-siva.sh`   | Count files in specific folders                                                       |
| `tar.sh`          | Archive folder with tar gz                                                            |
