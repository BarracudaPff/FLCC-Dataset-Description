import {
    JupyterLab,
    JupyterLabPlugin
} from '@jupyterlab/application';
import {ICommandPalette} from "@jupyterlab/apputils";
import {INotebookTracker} from "@jupyterlab/notebook";
import {
    Completer,
    CompleterModel,
    CompletionHandler
} from "@jupyterlab/completer";
import {Widget} from '@phosphor/widgets';
import {FullLineP3Connector} from "./connector";

namespace CommandIDs {
    export const invoke = 'code-completion:invoke';

    export const select = 'code-completion:select';

    export const invokeNotebook = 'code-completion:invoke-notebook';

    export const selectNotebook = 'code-completion:select-notebook';

    export const connect = 'code-completion:connect';

    export const category = 'Code Completion';
}

const extension: JupyterLabPlugin<void> = {
    id: 'jupyterlab-code-completion',
    autoStart: true,
    requires: [ICommandPalette, INotebookTracker],
    activate: (app: JupyterLab,
               palette: ICommandPalette,
               notebooks: INotebookTracker) => {
        console.log('JupyterLab extension jupyterlab-code-completion is activated!');

        const handlers: { [id: string]: CompletionHandler } = {};

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

        app.commands.addCommand(CommandIDs.invokeNotebook, {
            label: 'Show completions',
            execute: () => {
                const panel = notebooks.currentWidget;
                if (panel && panel.content.activeCell.model.type === 'code') {
                    return app.commands.execute(CommandIDs.invoke, {id: panel.id});
                }
            }
        });

        app.commands.addCommand(CommandIDs.selectNotebook, {
            label: 'Select statement',
            execute: () => {
                const id = notebooks.currentWidget && notebooks.currentWidget.id;

                if (id) {
                    return app.commands.execute(CommandIDs.select, {id});
                }
            }
        });

        notebooks.widgetAdded.connect(() => {
            console.log('notebooks.widgetAdded.connect');
            const cell = notebooks.currentWidget.content.activeCell;
            const editor = cell && cell.editor;
            //const session = panel.session;
            const parent = notebooks.currentWidget;
            //const connector = new CompletionConnector({session, editor});
            const connector = new FullLineP3Connector({editor});
            //const handler = manager.register({connector, editor, parent});

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
            notebooks.currentWidget.content.activeCellChanged.connect(() => {
                const editor = notebooks.currentWidget.content.activeCell.editor;
                handler.editor = editor;
                handler.connector = new FullLineP3Connector({editor});
            });
        });

        app.commands.addKeyBinding({
            command: CommandIDs.invokeNotebook,
            keys: ['Ctrl Space'],
            selector: `.jp-Notebook.jp-mod-editMode`
        });

        app.commands.addKeyBinding({
            command: CommandIDs.selectNotebook,
            keys: ['Enter'],
            selector: `.jp-Notebook .jp-mod-completer-active`
        });

        palette.addItem({command: CommandIDs.invokeNotebook, category: CommandIDs.category});
        palette.addItem({command: CommandIDs.selectNotebook, category: CommandIDs.category});
    }
};

export default extension;
