qx.Class.define("dolab.fwk.ui.SelectBoxWithValuesHandling", {

    extend : qx.ui.form.SelectBox,

    construct : function() {
        this.base(arguments);
        this.removeListener("mousewheel", this._onMouseWheel, this);
    },

    members : {
        getValue : function() {
            var selection = this.getSelection();
            if (selection.length > 0) {
                return selection[0].getModel();
            } else {
                throw new Error("No item is selected in the SelectBoxWithValuesHandling.");
            }
        },

        setValue : function(value) {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel() == value) {
                    this.setSelection([children[i]]);
                    break;
                }
            }
        }
    }
});