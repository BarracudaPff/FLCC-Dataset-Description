from flask import Flask, request
from flask import jsonify
from .domain.charrnn.predictor import CharRnnPredictor

app = Flask(__name__)


@app.route('/complete', methods=['POST', 'GET'])
def complete():
    json = request.get_json()
    print("Received message: " + str(json))
    #context = json['context']
    return jsonify([
        "Hre ",
        "dasda ",
        "dasd a"
    ])
    # if context is not None:
    #    return jsonify(model.predict(context))


@app.route('/completion/python3', methods=['POST'])
def completePy3():
    if request.method == 'POST':
        return Python3CharRnnHandler().post(request.get_json())


class Python3CharRnnHandler():
    """
    Handler for code in Python3.
    """
    predictor: CharRnnPredictor = None

    def post(self, jsonBody):
        """
        POST request handler, get all completions from code.
        """
        code = jsonBody['code']
        offset = jsonBody['offset']
        token = jsonBody['token']
        some_list = self.complete(code, offset, token)
        return {'completions': some_list}

    # TODO implement this method
    def complete(self, code: str, offset: int, token: str):
        """
        Here something happens with code
        @:return - list of completions
        """
        if self.predictor is None:
            self.predictor = CharRnnPredictor()
        return self.predictor.predict(code, offset, token)


@app.route('/')
def hello():
    return "Hello World!"


@app.route('/shutdown', methods=['GET'])
def shutdown():
    func = request.environ.get('werkzeug.server.shutdown')
    func()
    return 'Server shutting down...'


if __name__ == '__main__':
    app.run()
