import logging
import time

from src.domain.charrnn.main import ModelConnector
from src.domain.charrnn.utils.utils import getOldPrefix


class CharRNNService:
    def __init__(self, params_name, model_name):
        self.connector = ModelConnector(params_name, model_name, max_branches=25, max_chars=40)

    def getCompletions(self, code, cursor_pos, filename):
        oldPrefix, oldSuffix = getOldPrefix(code, cursor_pos)
        filename = filename

        t1 = time.time()
        answers = self.connector.get_suggestions(code, code)
        logging.info(f"Time to predict: {time.time() - t1}")
        return {"answers": answers}
