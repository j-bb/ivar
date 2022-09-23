qx.Mixin.define("dolab.fwk.ui.form.FormFieldMixin", {
    construct : function() {
        this.__errorWidget = new qx.ui.basic.Atom(null, dolab.fwk.ui.IconManager.getIcon("warning", "icon", "fwdw_icons"));
        this.__errorWidget.setTextColor("#FF0000");
        this._hideError();
    },

    members : {
        __errorMessage : null,
        __errorWidget : null,

        setErrorMessage : function(message) {
            this.__errorMessage = message;
            this.__errorWidget.setToolTipText(message);
        },

        getErrorWidget : function() {
            return this.__errorWidget;
        },

        _showError : function(message) {
            if (!this.__errorMessage) {
                this.__errorWidget.setToolTipText(message);
            }
            this.__errorWidget.setVisibility("visible");
        },

        _hideError : function() {
            this.__errorWidget.setVisibility("excluded");
        },

        _setErrorTitle : function(title) {
            this.__errorWidget.setLabel(title);
        },

        _getErrorMessage : function() {
            return this.__errorMessage;
        }
    }
});