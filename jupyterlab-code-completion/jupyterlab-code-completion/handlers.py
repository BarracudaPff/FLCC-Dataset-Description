from notebook.utils import url_path_join as url_join
from notebook.base.handlers import APIHandler


class Python3Handler(APIHandler):
    """
    Handler for code in Python3.
    """

    def post(self):
        """
        POST request handler, get all completions from code.
        """
        code = self.get_json_body()['code']
        some_list = self.completions(code)
        self.finish({'completions': some_list})

    # TODO implement this method
    def completions(self, code):
        """
        Here something happens with code
        @:return - list of completions
        """
        return ["Here", "will", "be", "completion", "---"] + code.split()


def setup_handlers(web_app):
    """
    Setups all of the completions command handlers.
    """

    # posible expand here
    completion_handlers = [
        ("/completion/python3", Python3Handler)
    ]

    # add the base url to our paths
    base_url = web_app.settings["base_url"]
    completion_handlers = [(url_join(base_url, x[0]), x[1]) for x in completion_handlers]

    web_app.add_handlers('.*$', completion_handlers)
