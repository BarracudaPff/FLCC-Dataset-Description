# Description of dataset

A dataset of source files from GitHub for Code Completion

## Table of content
- [Structure](#structure)
    - [Languages](#languages)
    - [Extensions](#extensions)
    - [Repositories](#repositories)
        - [files.json](#filesjson)
        - [paths.json](#pathsjson)
        - [METADATA](#metadata)
- [Download](#download)
- [Extra](#extra)
    - [Statistic](#statistic)
    - [Scripts](#scripts)
    - [Spikes](#spikes)



    
## Structure

Dataset structure is:
```
dataset/v3
        ├── languages 
        │   ├── <Language> 
        │   ... ├── <Extension>  
        │       ... ├── <Author> 
        │           ... ├── <Repository>
        │               ... ├── <Init Hash>
        │                   ... ├── <Files>
        │                       └── ...
        │
        └── repositories   
            ├── <Author> 
            ... ├── <Repository>
                ... ├── <Init Hash>
                    ... ├── files.json
                        ├── paths.json
                        └── METADATA
```

Dataset split for 2 data-parts, languages and repositories.
**All extensions includes dot.**

Naming rule for all source files:

`<New_name> = <Old_name>_<Unix_time><.Extension><sup>*</sup>`  
`btree_test.go -> btree_test_1569435301125374618.go`


&nbsp;
## Languages

All languages first grouped by **Language**, then by **Extension**.              
`./languages/HTML/.html/...`

Then every folder sorted by **Author**, **Repository** and **Init Hash**. Here, **Init Hash** means hash for first commit in branch, if repository has multiple branches with unrelated histories.  
`.../google/gvisor/deb7ecf1e46862d54f4b102f2d163cfbcfc37f3b/...`

Dataset includes 103 most popular languages in Github with all possible extensions. Full list of them contains in [languages.txt](languages.txt)

&nbsp;
### Extensions

Languages downloaded with all possible extensions. Full list in JSON format contains in [extensions.json](extensions.json)

> Generated using [languages.yml](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml) from [github/linguist](https://github.com/github/linguist)


&nbsp;
## Repositories

All repositories are sorted just as languages by **Author**, **Repository** and **Init Hash**.  
`.../google/gvisor/deb7ecf1e46862d54f4b102f2d163cfbcfc37f3b/...`

Folders has only 3 files: `files.json`, `paths.json` and `METADATA`


#### files.json

To control the number of files in the repository and determine the primary language (by amount) we have `files.json` where is how many files with a certain extension are located.  
**Schema:**
```json5
{
  "<Language>/<Extension>": "<Amount>"
}
```
**Example:**
```json5
{
  "Go/.go": 3,
  "Python/.py": 10,
}
```

&nbsp;
#### paths.json

To find out real path of file and it's filename we have `paths.json`, where there is real path for every saved file.  
**Schema:**
```json5
{
  "<Filename_in_dataset>": "<Real_filename_with_path>"
}
```
**Example:**
```json5
{
  "btree_1569435301124687101.go": "btree.go", 
  "btree_mem_1569435301125123970.go": "btree_mem.go", 
  "btree_test_1569435301125374618.go": "btree_test.go",
}
```

&nbsp;
#### METADATA

To find out history for every file in repository, we have `METADATA`, where stored all files'commit history in plaintext. 
Actually, it's just `git log --pretty=%ct --name-status --diff-filter=ACDMR`.

Every commit separated by double new line (`\n\n`)
**Schema:**
```
<Mod>       <Filename>
<Unix_timestamp>
```
**Example:**
```bash
M       go.mod
1553186610

A       go.mod
1534174272

M       btree.go
M       btree_test.go
1506445921
```



&nbsp;
## Download
Full dataset slitted by parts

**Repositories with more than 50 stars**

| Part |Link | Compressed size | Full size | Amount of files | Amount of repositories |
| ---- |---- | --------------- | --------- | --------------- | ---------------------- |
| 1 | will be approximately in 8 PM (CET)                                                                          | ?          | 511 GB| 32665248  |157367 |
| 2 | [link](https://5k-dataset.s3.amazonaws.com/v3/dataset-open-50-more-2.tar.gz) | 56.8 GB    | ?     | ?         |       |


&nbsp;
## Extra
### Spikes
1. For some projects it's impossible to find out name of repository and author. In such case, author name is **dev**, repository name is **null** and init hash is **random uuid**.
So all lost projects placed in `dev/null/<uuid>`

2. Some files caused Exception "too long filename" and were renamed without filename to `<Unix_time><Extension>`. It's still possible to find real name out in paths.json
