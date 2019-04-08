import {
    DataConnector,
    PageConfig,
    URLExt
} from "@jupyterlab/coreutils";

import {CompletionHandler} from "@jupyterlab/completer";

import {CodeEditor} from '@jupyterlab/codeeditor';

/**
 * A Python3 full line connector for completion handlers.
 */
export class FullLineP3Connector extends DataConnector<CompletionHandler.IReply,
    void,
    CompletionHandler.IRequest> {
    private _editor: CodeEditor.IEditor;

    /**
     * Create a new Python3 full line connector for completion requests.
     *
     * @param {FullLineP3Connector.IOptions} options - The options for the full line connector.
     */
    constructor(options: FullLineP3Connector.IOptions) {
        super();
        this._editor = options.editor;
    }

    /**
     * Fetch completion requests.
     *
     * @param request - The completion request text and details.
     * @returns  - The completion reply array of possible matches.
     */
    fetch(
        request: CompletionHandler.IRequest
    ): Promise<CompletionHandler.IReply> {
        return new Promise<CompletionHandler.IReply>(resolve => {
            const code = this._editor.model.value.text;
            Private.fetchMy(code, json => {
                const cursor = this._editor.getCursorPosition();
                const token = this._editor.getTokenForPosition(cursor);
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

/**
 * A namespace for Python3 full line connector statics.
 */
export namespace FullLineP3Connector {
    /**
     * The instantiation options for cell completion handlers.
     */
    export interface IOptions {
        /**
         * The session used by the context connector.
         */
        editor: CodeEditor.IEditor;
    }
}

/**
 * A namespace for Private functionality.
 */
namespace Private {

    /**
     * Get a list of completion hints from a server extension
     *
     * @param code - full code from editor
     * @param json - response from server extension
     */
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
