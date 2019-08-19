import os

from flask import Flask
from injector import Module, singleton

from src.application.v1.actions.CharRNNAction import CharRnnAction
from src.application.v1.actions.GPTAction import GPTAction
from src.application.v1.actions.StatusAction import StatusAction
from src.domain.services.CharRNNService import CharRNNService
from src.domain.services.GPTService import GPTService
from src.infrastructure.connectors.CharRnnConnector import CharRnnConnector

ROOT = os.path.dirname(os.path.abspath(__file__))


class AppModule(Module):
    def configure(self, binder):
        app = binder.injector.get(Flask)
        self.testing = app.testing

        gptModelPath    = app.config['MODEL_GPT']
        charModelPath   = app.config['MODEL_CHAR_RNN_CPYTHON']
        charParamPath   = app.config['DATA_CHAR_RNN_CPYTHON']
        maxBranches     = app.config['MAX_CHARS']
        maxChars        = app.config['MAX_BRANCHES']

        charRnnConnector = self._configureCharRNNService(charParamPath, charModelPath, maxBranches, maxChars)

        gptService = self._configureGPTService(gptModelPath)
        charRnnService = CharRNNService(charRnnConnector)

        statusAction = StatusAction()
        gptAction = GPTAction(gptService)
        charRnnAction = CharRnnAction(charRnnService)

        # Connectors
        binder.bind(CharRnnConnector, to=charRnnConnector, scope=singleton)

        # Services
        binder.bind(GPTService      , to=gptService     , scope=singleton)
        binder.bind(CharRNNService  , to=charRnnService , scope=singleton)

        # Actions
        binder.bind(StatusAction    , to=statusAction   , scope=singleton)
        binder.bind(GPTAction       , to=gptAction      , scope=singleton)
        binder.bind(CharRnnAction   , to=charRnnAction  , scope=singleton)

    def _configureCharRNNService(self, paramsPath, modelPath, maxBranches, maxChars) -> CharRnnConnector:
        # init path for parameters and model
        paramsPath = os.path.join(ROOT, paramsPath)
        modelPath = os.path.join(ROOT, modelPath)

        # TODO (Download model here if we want)
        if self.testing:
            assert os.path.exists(paramsPath) and os.path.exists(modelPath), \
                'Params and model not found. See README.md to download them'
        return CharRnnConnector(paramsPath, modelPath, maxBranches, maxChars)

    def _configureGPTService(self, modelPath) -> GPTService:
        modelPath = os.path.join(ROOT, modelPath)

        # TODO (Download model here if we want)

        if self.testing:
            assert os.path.exists(modelPath), \
                'Params and model not found. See README.md to download them'

        return GPTService(modelPath)
