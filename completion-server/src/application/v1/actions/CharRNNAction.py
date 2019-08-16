import json

from jsonschema import validate, ValidationError

from src.application.v1.exceptions import ApiException
from src.domain.services import CharRNNService

# TODO(Move to config)
schema = {
    "type": "object",
    "properties": {
        "code": {"type": "string"},
        "cursor_pos": {"type": "number"},
        "filename": {"type": "string"},
    },
    "required": [
        "filename",
        "cursor_pos",
        "code"
    ]
}


def char_rnn_action(json_data: json) -> json:
    try:
        validate(instance=json_data, schema=schema)

        code = json_data["code"]
        cursor_pos = json_data["cursor_pos"]
        filename = json_data["filename"]

        # result = charRnnService.getCompletions(code, cursor_pos, filename)
        return ['one two ', 'three']

    except ValidationError as e:
        exception_data = {
            "message": e.message,
            "validator": e.validator,
            "value": e.validator_value
        }
        raise ApiException("Wrong parameters passed to char rnn", e, exception_data, 500)
