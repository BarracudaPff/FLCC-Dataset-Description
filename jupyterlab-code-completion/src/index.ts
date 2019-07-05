import {
    JupyterFrontEndPlugin,
    JupyterFrontEnd
} from '@jupyterlab/application';

import {
    ICommandPalette
} from "@jupyterlab/apputils";

import {
    INotebookTracker
} from "@jupyterlab/notebook";

import {
    Completer,
    CompleterModel, CompletionConnector,
    CompletionHandler
} from "@jupyterlab/completer";

import {
    ServerConnection
} from '@jupyterlab/services';

import {
    Widget
} from '@phosphor/widgets';
import {
    FullLineP3Connector
} from "./connector";

/**
 * The command IDs and category used by the code completion plugin
 */
namespace CommandIDs {
    export const invoke = 'code-completion:invoke';

    export const select = 'code-completion:select';

    export const invokeNotebook = 'code-completion:invoke-notebook';

    export const selectNotebook = 'code-completion:select-notebook';

    export const connect = 'code-completion:connect';

    export const category = 'Code Completion';
}

function checkCompletionServer(result: (isRunning: boolean) => void) {
    const settings = ServerConnection.makeSettings();
    const url = new URL("completion/status", settings.baseUrl);
    let request: RequestInit = {
        method: 'GET'
    };
    ServerConnection
        .makeRequest(url.toString(), request, settings)
        .then(response => response.json())
        .then(it => {
            try {
                result(it.status);
            } catch (e) {
                result(false);
            }
        });

}

/**
 * A plugin providing full line code completion for notebooks.
 */
const extension: JupyterFrontEndPlugin<void> = {
    id: 'jupyterlab-code-completion',
    autoStart: true,
    requires: [ICommandPalette, INotebookTracker],
    activate: (app: JupyterFrontEnd,
               palette: ICommandPalette,
               notebooks: INotebookTracker) => {
        console.log('JupyterLab extension jupyterlab-code-completion is activated!!');

        checkCompletionServer(isRunning => console.log("Server running status: " + isRunning));

        // Create a handler for each notebook that is created.
        const handlers: { [id: string]: CompletionHandler } = {};

        //Invoke completions
        app.commands.addCommand(CommandIDs.invoke, {
            execute: args => {
                let id = args && (args['id'] as string);
                if (!id) {
                    return;
                }

                const handler = handlers[id];
                if (handler) {
                    handler.invoke();
                }
            }
        });

        //Select completion
        app.commands.addCommand(CommandIDs.select, {
            execute: args => {
                let id = args && (args['id'] as string);
                if (!id) {
                    return;
                }

                const handler = handlers[id];
                if (handler) {
                    handler.completer.selectActive();
                }
            }
        });

        //Invoke completion in Notebook
        app.commands.addCommand(CommandIDs.invokeNotebook, {
            label: 'Show completions',
            execute: () => {
                const panel = notebooks.currentWidget;
                if (panel && panel.content.activeCell.model.type === 'code') {
                    return app.commands.execute(CommandIDs.invoke, {id: panel.id});
                }
            }
        });

        //Select completion in Notebook
        app.commands.addCommand(CommandIDs.selectNotebook, {
            label: 'Select statement',
            execute: () => {
                const id = notebooks.currentWidget && notebooks.currentWidget.id;

                if (id) {
                    return app.commands.execute(CommandIDs.select, {id});
                }
            }
        });


        // Create a handler for each notebook that is created.
        notebooks.widgetAdded.connect((sender, panel) => {
            console.log("connect 1st");
            const cell = panel.content.activeCell;
            const editor = cell && cell.editor;
            const session = panel.session;
            const parent = panel;
            const connector = new CompletionConnector({session, editor});

            const model = new CompleterModel();
            const completer = new Completer({editor, model});
            const handler = new CompletionHandler({completer, connector});
            const id = parent.id;

            // Hide the widget when it first loads.
            completer.hide();

            // Associate the handler with the parent widget.
            handlers[id] = handler;

            // Set the handler's editor.
            handler.editor = editor;

            // Attach the completer widget.
            Widget.attach(completer, document.body);

            // Listen for parent disposal.
            parent.disposed.connect(() => {
                delete handlers[id];
                model.dispose();
                completer.dispose();
                handler.dispose();
            });

            // Listen for active cell changes.
            panel.content.activeCellChanged.connect((sender, cell) => {
                const editor = cell && cell.editor;
                handler.editor = editor;
                handler.connector = new FullLineP3Connector({editor});
            });
        });

        // Set ctrl space key command for notebook completion invoke command.
        app.commands.addKeyBinding({
            command: CommandIDs.invokeNotebook,
            keys: ['Ctrl Space'],
            selector: `.jp-Notebook.jp-mod-editMode`
        });

        // Set enter key for notebook completion select command.
        app.commands.addKeyBinding({
            command: CommandIDs.selectNotebook,
            keys: ['Enter'],
            selector: `.jp-Notebook .jp-mod-completer-active`
        });

        //add main commands to palette
        palette.addItem({command: CommandIDs.invokeNotebook, category: CommandIDs.category});
        palette.addItem({command: CommandIDs.selectNotebook, category: CommandIDs.category});
    }
};

export default extension;
