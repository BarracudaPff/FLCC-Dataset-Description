import os
import unittest

test_dir = os.path.dirname(os.path.abspath(__file__))
tests_dir = os.path.join(test_dir, 'utils')

if __name__ == '__main__':
    loader = unittest.TestLoader()
    suite = loader.discover(test_dir)
    loader.discover(tests_dir)

    runner = unittest.TextTestRunner()
    runner.run(suite)
