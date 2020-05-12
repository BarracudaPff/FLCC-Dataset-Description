from abc import ABC, abstractmethod


class Connector(ABC):
    @abstractmethod
    def __init__(self, model_name):
        params = "models/data/" + model_name
        model = "models/" + model_name
        self.connect(params, model)

    @abstractmethod
    def connect(self, params, model):
        pass
