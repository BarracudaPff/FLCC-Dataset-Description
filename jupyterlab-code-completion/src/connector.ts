import {
    ServerConnection
} from '@jupyterlab/services'

import {
    DataConnector
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
            Private.getCompletions(code, json => {
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
     * Response from server extension
     */
    export interface IResponse {
        completions: string[]
    }

    /**
     * Get a list of completion hints from a server extension
     *
     * @param code - full code from editor
     * @param json - response from server extension
     */
    export function getCompletions(code: string, json: (json: IResponse) => void) {
        const settings = ServerConnection.makeSettings();
        const url = new URL("completion/python3", settings.baseUrl);
        let request: RequestInit = {
            method: 'POST',
            body: JSON.stringify({
                code
            }),
        };
        ServerConnection
            .makeRequest(url.toString(), request, settings)
            .then(response => response.json())
            .then(json);
    }
}
