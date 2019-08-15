import os

from flask import Flask, request, jsonify

from gpt import init_model, Parameters


def default_params() -> Parameters:
    return Parameters(
        length=20,
        max_context=500,
        temperature=.8,
        batch_size=10
    )

print(os.path.abspath("."))
model = init_model("model_snapshot/model-best.pt", default_params())

app = Flask(__name__)


@app.route('/complete', methods=['POST'])
def complete():
    if request.method == 'POST':
        json = request.get_json()
        print("Received message: " + str(json))
        context = json['context']
        if context is not None:
            return jsonify(model.predict(context))
