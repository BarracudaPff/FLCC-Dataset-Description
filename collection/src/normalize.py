import ast
import os
import time

from tqdm import tqdm

from src.config import subject
from src.utils.converters import CommentsConverter, CSTConverter
from src.utils.files import select_all_files_with_extension
from src.utils.mail import MailNotifier

convert_state = ["c", "i"]
comments_con = CommentsConverter()
import_con = CSTConverter("i")


def normalize(email: str, target_directory: str, convert_modes: []):
    notifier = MailNotifier(subject, email)
    if len([item for item in convert_modes if item not in convert_state]) != 0:
        notifier.send_error(ValueError("Wrong convert state, must be one of " + str(convert_state)))

    files = select_all_files_with_extension(target_directory, '.py')
    filename = f"err_normalize-{time.time()}.txt"
    err_file = open(filename, "a")
    notifier.send_notification(f"Errors will be logged to {filename}")

    for file in tqdm(os.listdir(files)):
        try:
            if 'i' in convert_modes:
                import_con.convert_file(file)
            if 'c' in convert_modes:
                comments_con.convert_file(file)
        except KeyboardInterrupt:
            print('Aborted with KeyboardInterrupt')
            break
        except:
            err_file.write("Problem while converting:" + file + '\n')
            err_file.flush()
        try:
            ast.parse(open(file).read())
        except KeyboardInterrupt:
            print('Aborted with KeyboardInterrupt')
            break
        except:
            err_file.write("Damaged:" + file + '\n')
            err_file.flush()

    notifier.send_notification(f"Dataset normalised with {len(open(filename, 'a').readlines())} damaged files")
