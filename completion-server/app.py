import logging

from flask import Flask, request

from src.application.v1.api import api

app = Flask(__name__)
app.config.from_pyfile('config.cfg')

logging.basicConfig(format="%(asctime)s\t:\t%(levelname)s\t:\t%(name)s\t:\t%(message)s")
logging.getLogger().setLevel(logging.DEBUG)


# TODO (Remove later)
@app.route('/shutdown', methods=['GET'])
def shutdown():
    func = request.environ.get('werkzeug.server.shutdown')
    func()
    return 'Server shutting down...'


if __name__ == '__main__':
    app.register_blueprint(api, url_prefix='/v1')
    app.debug = True
    app.run()
