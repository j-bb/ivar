qx.Class.define("dolab.fwk.ui.explorer.AbstractExplorer", {
    extend: qx.ui.splitpane.Pane,

    construct: function() {
        this.base(arguments);
        this.setOrientation("horizontal");
        this.dataPanes = [];
        this.doChangePane = true;
        this._initUI();
        this.seleniumId = "explorer.splitpane";
    },

    members: {
        data: null,

        dataTree: null,
        rightPane: null,

        dataPanes: null,
        currentDataPane: null,
        doChangePane: null,
        currentItem: null,

        setData: function(rootData) {
            this.data = rootData;

            this.afterInitData(this.data);
        },

        afterInitData : function() {
            throw new Error("abstract method call. afterInitData() must be redefined. Init in this method the items to put inside the dataTree root folder.");
        },

        getData: function() {
            return this.data;
        },

        widgetToData: function() {
            if (this.currentDataPane != null) {
                this.currentDataPane.widgetToData();
            }
        },

        checkMandatoryValues: function() {
            return (this.currentDataPane == null) || (this.currentDataPane.checkMandatoryValues());
        },

        _initUI: function() {
            //------- Init the splitpane

            // right pane : container for a detail view that extends dolab.fwk.ui.AbstractPane
            this.rightPane = new qx.ui.container.Composite(new qx.ui.layout.Dock(5, 5));

            this.add(this._getInitLeftPane(), 0);
            this.add(this.rightPane, 1);
        },

        _changePane: function(item) { //absexpl
            // if item == null, the tree is empty or we're deleting an item, so there's no need to check mandatory values
            if (this.currentDataPane != null && item != null) {
                if (this.currentDataPane.checkMandatoryValues()) {
                    this.doChangePane = true;
                    this.currentDataPane.widgetToData();
                } else {
                    this.doChangePane = false;
                    if (this.currentItem != null && this.currentItem != item) {
                        this.currentItem.getTree().setSelection([this.currentItem]);
                    }
                }
            } else {
                this.doChangePane = true;
            }

            if (this.doChangePane && item != null) {
                var dataPane = this._getDataPane(item);

                if (dataPane != null) {
                    dataPane.resetWidgets();
                    dataPane.setData(item.getData(), this.data);

                    if (dataPane != this.currentDataPane) {
                        this.rightPane.removeAll();
                        this.rightPane.add(dataPane.getRootWidget());
                        this.currentDataPane = dataPane;
                    }
                }

                if (this.currentItem != null) {
                    this._currentItemChanged();
                }
                this.currentItem = item;
            }
        },

        _getDataPane: function(item) {
            if (this.dataPanes[item.classname] == null) {
                var pane = item.getNewPane();

                if (pane != null) {
                    this.dataPanes[item.classname] = pane;
                    this.dataPanes[item.classname].initUI();
                }
            }

            return this.dataPanes[item.classname];
        },

        _getInitLeftPane: function() {
            this.dataTree = new qx.ui.tree.VirtualTree();
            this.dataTree.setHideRoot(true);
            this.dataTree.setRootOpenClose(true);
            this.dataTree.setWidth(200);

            this._addChangePaneListener(this.dataTree);

            this.dataTree.setRoot(new qx.ui.tree.TreeFolder());
            this.dataTree.getRoot().setOpen(true);

            return this.dataTree;
        },

        _currentItemChanged: function() {
            // Do whatever you need in a subclass
        },

        _addChangePaneListener: function(treeWidget) {
            if (treeWidget != null) {
                treeWidget.addListener("changeSelection",
                        function(e) {
                            var newItem = e.getData()[0]; // selected item
                            this._changePane(newItem);

                            if (e.getOldData() != null) {
                                var oldItem = e.getOldData()[0];

                            }
                        }, this);

                treeWidget.addListener("removeItem",
                        function() {
                            this.currentDataPane.resetWidgets();
                            this.currentDataPane.setData(null, this.data);
                            this.currentItem = null;
                            if (treeWidget.getSelection().length == 0) {
                                // display a default pane
                                this.doDisplayDefaultPane();
                            }
                        }, this);
            }
        },

        doDisplayDefaultPane: function() {
            throw new Error("abstract method call. doDisplayDefaultPane() must be redefined. The default pane MUST NOT have mandatory widgets (or at least they have to be filled).");
        }
    }
});