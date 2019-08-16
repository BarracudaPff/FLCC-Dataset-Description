from flask import jsonify


def getStatus() -> jsonify:
    return jsonify({"status": True})
