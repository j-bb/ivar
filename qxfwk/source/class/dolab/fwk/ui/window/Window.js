qx.Class.define("dolab.fwk.ui.window.Window", {

    extend : qx.ui.window.Window,

    construct : function(pane) {
        this.setStatus("Instanciating ...");
        this.base(arguments);
        this.pane = pane;

        this.pane.addListener("startRequest",
                function(e) {
                    this.requestDone = false;
                }, this);

        this.pane.addListener("endRequest",
                function(e) {
                    this.requestDone = true;
                }, this);

        this.pane.addListener("newTitle",
                function(e) {
                    this.setCaption(e.getData());
                }, this);

        this.pane.addListener("modalityChange",
                function(e) {
                    this.setModal(e.getData());
                }, this);

        this.setShowClose(true);
        this.setShowMaximize(true);
        this.setShowMinimize(true);
        this.setAllowClose(true);
        this.setAllowMaximize(true);
        this.setAllowMinimize(true);
    },

    members : {
        pane : null,
        requestDone : null,
        ready : null
    }
});