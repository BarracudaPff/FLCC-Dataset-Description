import logging
import os
from urllib.request import urlopen

from flask import Flask
from injector import Module, singleton

from src.application.v1.actions.CharRNNAction import CharRnnAction
from src.application.v1.actions.GPTAction import GPTAction
from src.application.v1.actions.StatusAction import StatusAction
from src.domain.services.CharRNNService import CharRNNService
from src.domain.services.GPTService import GPTService
from src.infrastructure.connectors.CharRnnConnector import CharRnnConnector
from src.infrastructure.connectors.GPTConnector import GPTConnector

ROOT = os.path.dirname(os.path.abspath(__file__))


class AppModule(Module):
    def configure(self, binder):
        app = binder.injector.get(Flask)

        models = app.config['models']
        maxBranches = app.config['maxBranches']
        maxChars = app.config['maxChars']

        charRnnConnector = CharRnnConnector(_setupModels(models['CharRNN']), maxBranches, maxChars)
        gptConnector = GPTConnector(_setupModels(models['GPT']))

        gptService = GPTService(gptConnector)
        charRnnService = CharRNNService(charRnnConnector)

        statusAction = StatusAction()
        gptAction = GPTAction(gptService)
        charRnnAction = CharRnnAction(charRnnService)

        # Connectors
        binder.bind(CharRnnConnector, to=charRnnConnector   , scope=singleton)
        binder.bind(GPTConnector    , to=gptConnector       , scope=singleton)

        # Services
        binder.bind(GPTService      , to=gptService         , scope=singleton)
        binder.bind(CharRNNService  , to=charRnnService     , scope=singleton)

        # Actions
        binder.bind(StatusAction    , to=statusAction       , scope=singleton)
        binder.bind(GPTAction       , to=gptAction          , scope=singleton)
        binder.bind(CharRnnAction   , to=charRnnAction      , scope=singleton)


def _setupModels(config) -> str:
    mainInst = config['main']
    for instance in config['instances']:
        instanceConfig = config['instances'][instance]
        _downloadFile(instanceConfig['model'], 'models/' + instance     , instanceConfig['reload'])
        _downloadFile(instanceConfig['data'] , 'models/data/' + instance, instanceConfig['reload'])
    return mainInst


def _downloadFile(url, filename, reload):
    if url is None:
        return

    if not reload and os.path.isfile(filename):
        return

    response = urlopen(url).read()
    with open(filename, 'wb') as writer:
        writer.write(response)
    logging.info(f"File {filename} downloaded from {url}")
