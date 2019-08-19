import logging
import time

from src.infrastructure.charrnn.utils.utils import getOldPrefix


class CharRNNService:
    def __init__(self, connector):
        self.connector = connector

    def getCompletions(self, code, cursor_pos, filename):
        oldPrefix, oldSuffix = getOldPrefix(code, cursor_pos)
        filename = filename

        t1 = time.time()
        answers = self.connector.get_suggestions(code, code)
        logging.info(f"Time to predict: {time.time() - t1}")
        return {"answers": answers}
