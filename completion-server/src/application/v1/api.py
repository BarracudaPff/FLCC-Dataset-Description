import logging

from flask import Blueprint, request

from src.application import char_rnn_action
from src.application.v1.actions.StatusAction import getStatus
from src.application.v1.exceptions import ApiException
from src.domain.services import CharRNNService

api = Blueprint('api', __name__)


@api.route('status')
def complete_char_rnn1():
    return getStatus()


@api.route('complete/charrnn', methods=['POST'])
def complete_char_rnn():
    charRnnService: CharRNNService
    json = request.get_json()
    answer = char_rnn_action(json, charRnnService)
    return answer


@api.route('complete/gpt', methods=['POST'])
def complete_char_rnn1():
    pass


@api.errorhandler(ApiException)
def all_exception_handler(error):
    logging.exception(error)
    return error.json, error.code
