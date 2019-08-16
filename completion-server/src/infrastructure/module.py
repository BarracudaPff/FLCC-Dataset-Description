import os

from flask import Flask
from injector import Module, singleton

from src.domain.services.CharRNNService import CharRNNService

charRnnService = None


class AppModule(Module):
    def configure(self, binder):
        app = binder.injector.get(Flask)
        self.root = app.root_path
        model_path = app.config['MODEL_CHAR_RNN_CPYTHON']
        param_path = app.config['DATA_CHAR_RNN_CPYTHON']
        charRnnService = self.configureCharRNNService(model_path, param_path)
        binder.bind(CharRNNService, to=charRnnService, scope=singleton)

    def configureCharRNNService(self, params_path, model_path) -> CharRNNService:
        # init path for parameters and model
        params_path = os.path.join(self.root, params_path)
        model_path = os.path.join(self.root, model_path)

        # TODO (Download model here if we want)
        assert os.path.exists(params_path) and os.path.exists(model_path), \
            'Params and model not found. See README.md to download them'
        return CharRNNService(params_path, model_path)
