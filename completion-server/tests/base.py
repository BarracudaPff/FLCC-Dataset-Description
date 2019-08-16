import unittest
import urllib.request

from flask_testing import LiveServerTestCase

import app


class MyTest(LiveServerTestCase):

    def create_app(self):
        app.register_blueprint()
        app.app.config['TESTING'] = True
        app.app.config['LIVESERVER_PORT'] = 0
        return app.app

    def test_server_is_up_and_running(self):
        url = self.get_server_url() + '/v1/status'
        print(url)
        response = urllib.request.urlopen(url)

        self.assertEqual(response.code, 200)


if __name__ == '__main__':
    unittest.main()
