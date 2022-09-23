qx.Class.define("dolab.fwk.ui.explorer.AbstractTreeItem", {

    extend: qx.ui.tree.core.AbstractTreeItem,
    type: "abstract",

    /*
     *****************************************************************************
     CONSTRUCTOR
     *****************************************************************************
     */

    construct: function(data) {
        this.base(arguments);

        this.data = data;

        this.addListener("contextmenu", function(e) {
            e.stopPropagation();
        }, this);
    },


    members: {
        data: null,

        getData: function() {
            return this.data;
        },

        getNewPane: function() {
            throw new Error("abstract method call. getNewPane() must be redefined.");
        },

        _deleteThis: function(thisType) {
            this.debug("_deleteThis");
            var parent = this.getParent();

            var parentDataArray = null;

            // delete in data
            if (thisType == null || thisType == "scenario" || thisType == "dataset") {
                parentDataArray = parent.getData();
            } else if (thisType == "step") {
                parentDataArray = parent.getData().steps;
            } else if (thisType == "rule") {
                parentDataArray = parent.getData().rules;
            }

            for (var i = 0; parentDataArray[i] != this.data && i < parentDataArray.length; i++);

            if (i < parentDataArray.length) {
                parentDataArray.splice(i, 1); // delete one item at the i-th element
            }
            this.data = null;

            this.debug("_deleteThis avant UI");

            // delete in UI
            parent.remove(this);
        },

        _deleteAllChildren: function() {
            // delete in data
            this.data.splice(0, this.data.length);

            // delete in UI
            this.removeAll();
        }
    }
});