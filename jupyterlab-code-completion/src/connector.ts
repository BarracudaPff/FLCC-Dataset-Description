import {
    DataConnector,
    PageConfig,
    URLExt
} from "@jupyterlab/coreutils";
import {CompletionHandler} from "@jupyterlab/completer";
import {CodeEditor} from '@jupyterlab/codeeditor';

export class FullLineP3Connector extends DataConnector<CompletionHandler.IReply,
    void,
    CompletionHandler.IRequest> {
    private _editor: CodeEditor.IEditor;

    constructor(options: FullLineP3Connector.IOptions) {
        super();
        this._editor = options.editor;
    }

    fetch(
        request: CompletionHandler.IRequest
    ): Promise<CompletionHandler.IReply> {
        return new Promise<CompletionHandler.IReply>(resolve => {
            const code = this._editor.model.value.text;
            Private.fetchMy(code, json => {
                const cursor = this._editor.getCursorPosition();
                const token = this._editor.getTokenForPosition(cursor);
                console.log(json.completions);
                resolve({
                    start: token.offset,
                    end: token.offset + token.value.length,
                    matches: json.completions,
                    metadata: {}
                })
            })
        })
    }
}

export namespace FullLineP3Connector {

    export interface IOptions {

        editor: CodeEditor.IEditor;
    }
}

namespace Private {

    export function fetchMy(code: string, json: (json: any) => void) {
        const url = URLExt.join(PageConfig.getOption('baseUrl'), '/completion/python3');

        fetch(url, {
            method: 'POST',
            body: JSON.stringify({
                code
            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(response => response.json())
            .then(json);
    }
}
