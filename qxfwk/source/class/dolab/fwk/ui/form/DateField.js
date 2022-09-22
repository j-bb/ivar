qx.Class.define("dolab.fwk.ui.form.DateField", {

    extend : qx.ui.form.DateField,
    include : [dolab.fwk.ui.form.FormFieldMixin],

    construct : function() {
        this.base(arguments);
    },

    members : {
        __before : null,
        __after : null,

        setBefore : function(date) {
            this.__before = date;
        },

        setAfter : function(date) {
            this.__after = date;
        },

        checkBounds : function(callingPane, fieldName) {
            var result = true;
            var value = this.getValue();
            var format = qx.util.format.DateFormat.getDateInstance();

            this._hideError();

            if (value != null) {
                if (this.__before != null) {
                    if (this.__before.getTime() < value.getTime()) {
                        result = false;
                        var message = "Value for " + fieldName + " is " + format.format(value) + ", but was meant to be before " + format.format(this.__before);
                        this._setErrorTitle("Value is too high!");
                        this._showError(message);
                    }
                }

                if (result && this.__after != null) {
                    if (this.__after.getTime() > value.getTime()) {
                        result = false;
                        var message = "Value for " + fieldName + " is " + format.format(value) + ", but was meant to be after " + format.format(this.__after);
                        this._setErrorTitle("Value is too low!");
                        this._showError(message);
                    }
                }
            }

            return result;
        }
    }
});