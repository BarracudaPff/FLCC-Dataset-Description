import os
from abc import ABC, abstractmethod


class Predictor(ABC):
    @abstractmethod
    def predict(self, code: str, offset: str, token: str):
        pass
