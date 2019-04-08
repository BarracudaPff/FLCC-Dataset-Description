from .handlers import setup_handlers


def _jupyter_server_extension_paths():
    return [{
        "module": "jupyterlab-code-completion"
    }]

def load_jupyter_server_extension(nb_app):
    setup_handlers(nb_app.web_app)
