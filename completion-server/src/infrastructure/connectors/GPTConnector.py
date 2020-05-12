from . import Connector


class GPTConnector(Connector):

    def __init__(self, model_name):
        super().__init__(model_name)

    def connect(self, params, model):
        print(f'GPT connected with {params} and {model}')
