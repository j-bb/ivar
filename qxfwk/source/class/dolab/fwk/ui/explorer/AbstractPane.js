qx.Class.define("dolab.fwk.ui.explorer.AbstractPane", {

    extend: qx.core.Object,
    type: "abstract",
    include: [dolab.fwk.ui.FormMixin, dolab.fwk.rpc.RpcMixin],

    construct: function() {
        this.base(arguments);
    },

    members: {
        root: null,
        data: null,

        initUI : function() {
            this.doInitUI();
        },

        doInitUI : function() {
            throw new Error("abstract method call. doInitUI() must be redefined.");
        },

        getRootWidget : function() {
            return this.root;
        },

        resetWidgets : function() {
            throw new Error("abstract method call. emptyContent() must be redefined.");
        },

        setData : function(nodeData, rootData) {
            throw new Error("abstract method call. setData(nodeData, rootData) must be redefined.");
        },

        widgetToData : function() {
            throw new Error("abstract method call. widgetToData() must be redefined.");
        }
    }
});