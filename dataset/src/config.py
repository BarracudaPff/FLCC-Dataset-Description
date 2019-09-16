import os

statistic_folder = 'statistics'
dataDir = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../data/')
max_files_in_folder = 100

# Temporary fields
temp_folder = 'temp'
siva_folder = 'siva'
list_siva = 'siva.txt'
list_siva_temp = 'list-siva-temp.txt'
temp_repo_list = 'data/temp-repo-list.txt'

# TODO: add password and email here for email logging
password = "None"
sender_email = "None"
subject = '''\
Subject: Dataset info.

>'''

# PSQL For Borges
db_user = "testing"
db_password = "testing"
db_host = "127.0.0.1"
db_port = 5432
db_database = "testing"
