qx.Class.define("dolab.fwk.ui.tabview.Page", {

    extend : qx.ui.tabview.Page,

    construct : function(pane) {
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
                    this.setLabel(e.getData());
                }, this);

        this.pane.addListener("modalityChange",
                function(e) {
                    var tabview = this.getLayoutParent();
                    this.debug(tabview);
                    //this.setModal(e.getData());
                }, this);
    },

    members : {
        pane : null,
        requestDone : null,
        ready : null
    }
});