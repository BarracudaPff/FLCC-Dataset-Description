import {
    JupyterLab, JupyterLabPlugin, ILayoutRestorer
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

import '../style/index.css';


/**
 * An extension viewer.
 */
class CCWidget extends Widget {
    constructor() {
        super();

        this.id = 'jupyterlab-code-completion';
        this.title.label = 'Code Completion';
        this.title.closable = true;

        this.div = document.createElement('div');
        this.node.appendChild(this.div);

        this.div.insertAdjacentHTML('afterend', `<iframe class="github"></iframe>`);
    }

    readonly div: HTMLDivElement;

    /**
     * Handle update requests for the widget.
     */
    onUpdateRequest(msg: Message): void {
        document.querySelector(".github").setAttribute("src"
            , "http://www2.latech.edu/~acm/HelloWorld.shtml");
    }
}

/**
 * Activate the code completion widget extension.
 */
function activate(app: JupyterLab, palette: ICommandPalette, restorer: ILayoutRestorer) {
    console.log('Code Completion extension is activated!');

    // Declare a widget variable
    let widget: CCWidget;

    // Add an application command
    const command: string = 'cc:open';
    app.commands.addCommand(command, {
        label: 'Hello World sample',
        execute: () => {
            if (!widget) {
                widget = new CCWidget();
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

    palette.addItem({command, category: 'Hello World sample'});
}

/**
 * Initialization data for extension.
 */
const extension: JupyterLabPlugin<void> = {
    id: 'jupyterlab-code-completion',
    autoStart: true,
    requires: [ICommandPalette, ILayoutRestorer],
    activate: activate
};

export default extension;