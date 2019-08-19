import json

from jsonschema import validate, ValidationError

from src.application.v1.exceptions.ApiException import ApiException

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


class CharRnnAction:
    def __init__(self, charRnnService):
        self.charRnnService = charRnnService

    def suggestions(self, jsonData: json) -> json:
        try:
            validate(instance=jsonData, schema=schema)

            code = jsonData["code"]
            cursorPos = jsonData["cursor_pos"]
            filename = jsonData["filename"]

            return self.charRnnService.getCompletions(code, cursorPos, filename)

        except ValidationError as e:
            exceptionData = {
                "message": e.message,
                "validator": e.validator,
                "value": e.validator_value
            }
            raise ApiException("Wrong parameters passed to char rnn", e, exceptionData, 500)
