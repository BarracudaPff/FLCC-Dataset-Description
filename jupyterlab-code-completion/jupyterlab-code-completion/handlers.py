from notebook.utils import url_path_join as url_join
from notebook.base.handlers import APIHandler

def completions(code):
    return ["Here", "will", "be", "completion", "---"] + code.split()


class Python3Handler(APIHandler):
    def post(self):
        code = self.get_json_body()['code']
        some_list = completions(code)
        self.finish({'completions': some_list})


def setup_handlers(web_app):

    completion_handlers = [
        ("/completion/python3", Python3Handler)
    ]

    base_url = web_app.settings["base_url"]
    completion_handlers = [(url_join(base_url, x[0]), x[1]) for x in completion_handlers]

    web_app.add_handlers('.*$', completion_handlers)
