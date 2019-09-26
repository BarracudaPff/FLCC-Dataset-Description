import argparse
import os

import main
from calibrate_dataset import calibrate
from config import subject, dataDir
from dump_error_reps import dump_errors
from get_dataset import Dataset
from prepare_repo_list import prepare_list
from statistic import statistic
from utils.exts import extensions
from utils.mail import MailNotifier


class SubParsersFormatter(argparse.RawDescriptionHelpFormatter):
    def _format_action(self, action):
        parts = super(argparse.RawDescriptionHelpFormatter, self)._format_action(action)
        if action.nargs == argparse.PARSER:
            parts = "\n".join(parts.split("\n")[1:])
        return parts


parser = argparse.ArgumentParser(formatter_class=SubParsersFormatter, description="Main")

base_parser = argparse.ArgumentParser(description="The parent parser", add_help=False)
base_parser.add_argument('--target_directory', type=str, help='Dataset directory root',
                         default='dataset')
base_parser.add_argument('--email_notify', type=str, help='Gmail to notify. Type none to skip',
                         default='none')

subparsers = parser.add_subparsers(dest='subparser')

# A dataset command
dataset_parser = subparsers.add_parser('dataset', help='Get dataset for Languages', parents=[base_parser])
dataset_parser.add_argument('--sivas_folder', type=str, default='/tmp/root-repositories/',
                            help='Directory where are sivas')
dataset_parser.add_argument('--slice', type=int, default=500,
                            help='Amount of files from sivas_folder')
dataset_parser.add_argument('--languages_file', type=str, default='languages.txt',
                            help='Path to file with list of languages')
dataset_parser.add_argument('-mode', type=str, action="store", choices=['pga', 'borges'], required=True,
                            help='Use PGA')

# A statistic command
statistic_parser = subparsers.add_parser('statistic', help='Get statistic for downloaded dataset',
                                         parents=[base_parser])

# A prepare list command
list_parser = subparsers.add_parser('prepare_list', help='Extract slice of repositories to downloading and compress',
                                    parents=[base_parser])
list_parser.add_argument('--rep_count', type=int, default=5000,
                         help='Number of repositories to get from the general list.')
list_parser.add_argument('--repo_list', type=str, default='data/rep-list-working.txt',
                         help='Path to file with full list of repositories. WARNING: list will be reduced.')

# A dump errors command
errors_parser = subparsers.add_parser('dump_errors', help='Dump repositories that caused the download error',
                                      parents=[base_parser])
errors_parser.add_argument('--skipped_file', type=str, default='skipped.txt',
                           help='File with list of skipped repositories.')

# A calibrate command
calibrate_parser = subparsers.add_parser('calibrate', help='Calibrate dataset')


def _dataset(args: argparse.Namespace):
    languages_file = args.languages_file

    if os.path.exists(dataDir + languages_file):
        exts = extensions(args.languages_file)
    else:
        print(f"File {languages_file} with language list wasn't found. Downloading all possible languages")
        exts = extensions(None)

    dataset = Dataset(exts, args.slice, args.target_directory, args.email_notify)
    if args.mode == 'pga':
        dataset.pga()
    elif args.mode == 'borges':
        dataset.borges(args.sivas_folder)
    else:
        raise Exception


def _statistic(args: argparse.Namespace):
    statistic(args.email_notify, args.target_directory)


def _prepare_list(args: argparse.Namespace):
    prepare_list(args.repo_list, args.rep_count, args.email_notify)


def _dump_errors(args: argparse.Namespace):
    dump_errors(args.email_notify, args.skipped_file)


def _calibrate(args: argparse.Namespace):
    calibrate(args.email_notify, args.target_directory)


if __name__ == '__main__':
    _args = parser.parse_args()
    _subparser = vars(_args)['subparser']
    if _subparser is not None:
        try:
            getattr(main, '_' + _subparser)(_args)
        except Exception as error:
            MailNotifier(subject, _args.email_notify).send_error(error)
            raise error
