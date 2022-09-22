qx.Mixin.define("dolab.fwk.ui.form.TextualFieldMixin", {
    include : [dolab.fwk.ui.form.FormFieldMixin],
    construct : function() {
    },

    members : {
        __regex : null,
        __maxLength : null,

        setRegex : function(regExp) {
            this.__regex = regExp;
        },

// 2014
//        setMaxLength : function(len) {
//            this.__maxLength = len;
//        },

        getNormalizedValue : function() {
            var val = this.getValue();
            if (val === "") {
                val = null;
            }
            return val;
        },

        checkConstraints : function(callingPane, fieldName) {
            var val = this.getNormalizedValue();
            var result = true;

            this._hideError();

            if (val != null) {
                if (this.__maxLength != null) {
                    if (val.length > this.__maxLength) {
                        result = false;
                        var message = "Value for field " + fieldName + " contains " + val.length +
                                " characters, but the maximum allowed is " + this.__maxLength;
                        this._setErrorTitle("Too long!");
                        this._showError(message);
                    }
                }
                if (result && this.__regex != null) {
                    result = this.__regex.test(val);
                    if (!result) {
                        var message = "Incorrect format for field " + fieldName;
                        this._setErrorTitle("Wrong format!");
                        this._showError(message);
                    }
                }
            }

            return result;
        }
    }
});