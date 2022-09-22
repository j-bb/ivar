qx.Class.define("dolab.fwk.ui.form.NumberField", {

    extend : qx.ui.form.TextField,
    include : [dolab.fwk.ui.form.FormFieldMixin],

    construct : function() {
        this.base(arguments);
        this.format = new qx.util.format.NumberFormat();
        this.setTextAlign("right");
    }, //construct

    members : {
        __minValue : null,
        __maxValue : null,
        __precision : null,
        __scale : null,
        __forceNonZero : false,

        setValue : function(value) {
            this.base(arguments, this.format.format(value));
        },

        setMaxValue : function(value) {
            this.__maxValue = value;
        },

        setMinValue : function(value) {
            this.__minValue = value;
        },

        setPrecision : function(val) {
            this.__precision = val;
        },

        setScale : function(val) {
            this.__scale = val;
        },

        setForce : function(flag) {
            if (flag === 1) {
                if (this.__minValue == null) {
                    this.__minValue = 0;
                } else {
                    this.__minValue = Math.max(0, this.__minValue);
                }
            } else if (flag === -1) {
                if (this.__maxValue == null) {
                    this.__maxValue = 0;
                } else {
                    this.__maxValue = Math.min(0, this.__maxValue);
                }
            } else if (flag === 0) {
                this.__forceNonZero = true;
            }
        },

        setBounds : function(min, max) {
            this.__minValue = min;
            this.__maxValue = max;
        },

        checkBounds : function(callingPane, fieldName) {
            var val = this.getValue();
            var result = true;

            this._hideError();

            if (val != null) {
                if (this.__maxValue !== null) {
                    if (val > this.__maxValue) {
                        result = false;
                        var message = "Value for \"" + fieldName + "\" is " + val + ", max value is " + this.__maxValue;
                        this._setErrorTitle("Value is too high!");
                        this._showError(message);
                    }
                }

                if (result && this.__minValue !== null) {
                    if (val < this.__minValue) {
                        result = false;
                        var message = "Value for \"" + fieldName + "\" is " + val + ", min value is " + this.__minValue;
                        this._setErrorTitle("Value is too low!");
                        this._showError(message);
                    }
                }

                if (result && this.__forceNonZero) {
                    if (val === 0) {
                        result = false;
                        var message = "Value 0 for \"" + fieldName + "\" is forbidden";
                        this._setErrorTitle("Value is zero!");
                        this._showError(message);
                    }
                }
            }

            return result;
        },

        checkLengths : function(callingPane, fieldName) {
            var result = true;
            var tester = /-?([0-9]+)(?:\.([0-9]+))?/;
            var value = this.getValue();

            if (value != null) {
                var matches = tester.exec(value);

                if (this.__scale != null) {
                    if (matches.length >= 3 && typeof matches[2] != 'undefined') {
                        if (matches[2].length > this.__scale) {
                            result = false;
                            var message = "There are " + matches[2].length + " digits on the right of the decimal separator in \"" + fieldName + "\"," +
                                    " expecting a maximum of " + this.__scale;
                            this._setErrorTitle("Too many digits!");
                            this._showError(message);
                        }
                    }
                }

                if (result && this.__precision != null) {
                    if (matches.length >= 3 && typeof matches[2] != 'undefined') {
                        var total = matches[1].length + matches[2].length;
                        if (total > this.__precision) {
                            result = false;
                            var message = "There are " + total + " significants digits in field \"" + fieldName + "\", expecting a maximum of " + this.__precision;
                            this._setErrorTitle("Too many digits!");
                            this._showError(message);
                        }
                    }
                }
            }
            return result;
        },

        /** @Override */
        getValue : function() {
            var original = this.base(arguments);
            if (original === null || original === "") {
                return null;
            }
            return this.format.parse(original);
        }

    }, //members

    destruct : function() {
        this._disposeObjects("format");
    } //destruct
});