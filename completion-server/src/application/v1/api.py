import logging

from flask import Blueprint, request

from src.application.v1.actions.CharRNNAction import CharRnnAction
from src.application.v1.actions.GPTAction import GPTAction
from src.application.v1.actions.StatusAction import StatusAction
from src.application.v1.exceptions.ApiException import ApiException

api = Blueprint('api', __name__)


@api.route('status')
def status(statusAction: StatusAction):
    return statusAction.getStatus()


@api.route('complete/charrnn', methods=['POST'])
def completeCharRnn(charRnnAction: CharRnnAction):
    json_data = request.get_json()
    return charRnnAction.suggestions(json_data)


@api.route('complete/gpt', methods=['POST'])
def completeGpt(gptAction: GPTAction):
    json_data = request.get_json()
    return {'gpt': 'todo'}


@api.errorhandler(ApiException)
def allExceptionHandler(error):
    logging.exception(error)
    return error.json, error.code
