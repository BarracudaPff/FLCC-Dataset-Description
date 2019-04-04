from notebook.utils import url_path_join
from notebook.base.handlers import IPythonHandler


class HelloWorldHandler(IPythonHandler):
    def get(self):
        self.finish('Hello, world!')


def _jupyter_server_extension_paths():
    return [{
        "module": "jupyterlab-code-completion"
    }]


def load_jupyter_server_extension(nb_server_app):
    """
    Called when the extension is loaded.

    Args:
        nb_server_app (NotebookWebApplication): handle to the Notebook webserver instance.
    """
    web_app = nb_server_app.web_app
    host_pattern = '.*$'

    base_url = web_app.settings['base_url']

    web_app.add_handlers(host_pattern, [
        (url_path_join(base_url, '/hello'), HelloWorldHandler)
    ])
