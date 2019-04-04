import {
    JupyterLab, JupyterLabPlugin
} from '@jupyterlab/application';

import {
    ICommandPalette
} from '@jupyterlab/apputils';

import {
    Message
} from '@phosphor/messaging';

import {
    Widget
} from '@phosphor/widgets';

import {
    PageConfig, URLExt
} from "@jupyterlab/coreutils";

import {
    CodeCell,
} from "@jupyterlab/cells";

import {
    IEditorTracker
} from '@jupyterlab/fileeditor';

import {
    INotebookTracker
} from "@jupyterlab/notebook";

import '../style/index.css';


/**
 * An extension viewer.
 */
class CCWidget extends Widget {
    private tracker: INotebookTracker;
    private editorTracker: IEditorTracker;

    readonly div: HTMLDivElement;

    constructor(tracker: INotebookTracker, editorTracker: IEditorTracker) {
        super();

        this.id = 'jupyterlab-code-completion';
        this.title.label = 'Code Completion';
        this.title.closable = true;

        this.tracker = tracker;
        this.editorTracker = editorTracker;

        this.div = document.createElement('div');
        this.node.appendChild(this.div);

        this.div.insertAdjacentHTML('afterend', `<iframe class="github"></iframe>`);
    }

    private submitRequest(cmd: string, requestType: string) {
        let xhttp = new XMLHttpRequest();
        this.setCompletedTasks(xhttp);

        let baseUrl = PageConfig.getOption('baseUrl');
        let endpoint = URLExt.join(baseUrl, cmd);

        xhttp.open(requestType, endpoint, true);
        xhttp.setRequestHeader('Authorization', 'token ' + PageConfig.getToken());
        xhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhttp.send();
    }

    private setCompletedTasks(xhttp: XMLHttpRequest) {
        console.log('Try to connect');
        xhttp.onreadystatechange = () => {
            if (xhttp.readyState === xhttp.DONE && xhttp.status == 200) {
                console.log('Server is activated!\n' + xhttp.response.toString());
            }
        };
    };

    /**
     * Handle update requests for the widget.
     */
    onUpdateRequest(msg: Message): void {
        this.submitRequest('/hello', 'GET');
        document.querySelector(".github").setAttribute("src"
            , "http://www2.latech.edu/~acm/HelloWorld.shtml");
    }

    getCode() {
        console.log("Formatting something!");
        const editorWidget = this.editorTracker.currentWidget;

        if (editorWidget && editorWidget.content !== null && editorWidget.content.isVisible) {
            console.log("Formatting a file");
            const code = editorWidget.content.editor.model.value.text;
            this.logCode(code);
        } else if (this.tracker.activeCell instanceof CodeCell) {
            console.log("Formatting a notebook cell");
            const code = this.tracker.activeCell.model.value.text;
            this.logCode(code);
        } else {
            console.log("This doesn't seem like a code cell or a file...");
        }
    }

    private logCode(code: string) {
        console.log("Code is:\n" + code);
    }
}

/**
 * Activate the code completion widget extension.
 */
function activate(app: JupyterLab,
                  palette: ICommandPalette,
                  tracker: INotebookTracker,
                  editorTracker: IEditorTracker) {
    console.log('Code Completion extension is activated!');
    const category: string = 'Code Completion';

    // Declare a widget variable
    let widget: CCWidget;

    // Add an application command (HW Ref)
    const cmdOpen: string = 'cc:open';
    app.commands.addCommand(cmdOpen, {
        label: 'Hello World sample',
        execute: () => {
            if (!widget) {
                widget = new CCWidget(tracker, editorTracker);
                widget.update();
            }
            if (!widget.isAttached) {
                app.shell.addToMainArea(widget);
            } else {
                widget.update();
            }
            app.shell.activateById(widget.id);
        }
    });

    // Add an application command (Get code)
    const cmdGetCode: string = 'cc:get-code';
    app.commands.addCommand(cmdGetCode, {
        label: 'Get Code',
        execute: () => {
            if (!widget) {
                widget = new CCWidget(tracker, editorTracker);
            }
            widget.getCode();
        }
    });

    const bindings = [
        {
            selector: '.jp-Notebook.jp-mod-editMode',
            keys: ['Ctrl Alt B'],
            command: cmdGetCode
        },
        {
            selector: '.jp-Notebook.jp-mod-editMode',
            keys: ['Alt O'],
            command: cmdOpen
        },
    ];
    bindings.map(binding => app.commands.addKeyBinding(binding));

    palette.addItem({command: cmdGetCode, category: category});
    palette.addItem({command: cmdOpen, category: category});
}

/**
 * Initialization data for extension.
 */
const extension: JupyterLabPlugin<void> = {
    id: 'jupyterlab-code-completion',
    autoStart: true,
    requires: [ICommandPalette, INotebookTracker, IEditorTracker],
    activate: activate
};

export default extension;
