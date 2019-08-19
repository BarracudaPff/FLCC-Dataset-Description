import os

from flask import Flask
from injector import Module, singleton

from src.application.v1.actions.CharRNNAction import CharRnnAction
from src.application.v1.actions.GPTAction import GPTAction
from src.application.v1.actions.StatusAction import StatusAction
from src.domain.services.CharRNNService import CharRNNService
from src.domain.services.GPTService import GPTService

ROOT = os.path.dirname(os.path.abspath(__file__))


class AppModule(Module):
    def configure(self, binder):
        app = binder.injector.get(Flask)

        charModelPath = app.config['MODEL_CHAR_RNN_CPYTHON']
        charParamPath = app.config['DATA_CHAR_RNN_CPYTHON']
        charRnnService = self._configureCharRNNService(charParamPath, charModelPath)

        gptModelPath = app.config['MODEL_GPT']
        gptService = self._configureGPTService(gptModelPath)

        charRnnAction = CharRnnAction(charRnnService)
        gptAction = GPTAction(gptService)
        statusAction = StatusAction()

        # Services
        binder.bind(GPTService      , to=gptService     , scope=singleton)
        binder.bind(CharRNNService  , to=charRnnService , scope=singleton)

        # Actions
        binder.bind(StatusAction    , to=statusAction   , scope=singleton)
        binder.bind(GPTAction       , to=gptAction      , scope=singleton)
        binder.bind(CharRnnAction   , to=charRnnAction  , scope=singleton)

    @staticmethod
    def _configureCharRNNService(params_path, model_path) -> CharRNNService:
        # init path for parameters and model
        params_path = os.path.join(ROOT, params_path)
        model_path = os.path.join(ROOT, model_path)

        # TODO (Download model here if we want)
        assert os.path.exists(params_path) and os.path.exists(model_path), \
            'Params and model not found. See README.md to download them'
        return CharRNNService(params_path, model_path)

    @staticmethod
    def _configureGPTService(modelPath):
        modelPath = os.path.join(ROOT, modelPath)

        # TODO (Download model here if we want)
        assert os.path.exists(modelPath), \
            'Params and model not found. See README.md to download them'

        return GPTService(modelPath)
