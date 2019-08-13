import time

from ..charrnn.main import ModelConnector
from ..mainPredictor import Predictor

_param_path = 'data/tf-rnn-parameters.json'
_model_path = 'models/char-rnn.net'


# TODO Add cache for input code
class CharRnnPredictor(Predictor):

    def __init__(self):
        self.connector = ModelConnector(_param_path, _model_path, max_branches=25, max_chars=40)

    def predict(self, code: str, offset: int, token: str) -> [str]:
        t1 = time.time()
        answers = self.connector.get_suggestions(code, token)
        print(f"Time to predict: {time.time() - t1}")
        return answers
