import json


class ApiException(Exception):
    def __init__(self, message, error, data: json, code: int):
        super().__init__(message)
        self.code = code
        self.error = error
        self.json = {
            "error": {
                "message": message,
                "issue": data,
                "code": code,
                "type": error.__class__.__name__
            }
        }
