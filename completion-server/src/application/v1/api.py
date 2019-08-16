import logging

from flask import Blueprint, request

from src.application.v1.actions.CharRNNAction import char_rnn_action
from src.application.v1.actions.StatusAction import getStatus
from src.application.v1.exceptions.ApiException import ApiException

api = Blueprint('api', __name__)


@api.route('status')
def complete_char_rnn1():
    return getStatus()


@api.route('complete/charrnn', methods=['POST'])
def completeCharRnn():
    json = request.get_json()
    answer = char_rnn_action(json)
    return answer


@api.route('complete/gpt', methods=['POST'])
def completeGpt():
    pass


@api.errorhandler(ApiException)
def allExceptionHandler(error):
    logging.exception(error)
    return error.json, error.code
