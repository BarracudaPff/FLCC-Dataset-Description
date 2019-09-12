import unittest

if __name__ == '__main__':
    loader = unittest.TestLoader()
    start_dir = 'tests'
    suite = loader.discover(start_dir)
    loader.discover('tests/utils')

    runner = unittest.TextTestRunner()
    runner.run(suite)
