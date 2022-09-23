qx.Class.define("dolab.contrib.uploadwidget.UploadForm", {
    extend : dolab.contrib.uploadwidget.UploadForm,

    construct :  function(name, url, encoding) {
        this.base(arguments, name, url, encoding);
        this.setParameter("user", dolab.fwk.auth.AuthManager.getInstance().getUser());
        this.setLayout(new qx.ui.layout.VBox(5));
    },

    events : {
        "idSet" : "qx.event.type.Event"
    },

    members : {
        __id : null,
        __attachedField : null,
        __extensions : null,

        getValue : function() {
            return this.__id;
        },

        resetValue : function() {
            this.__id = null;
        },

        setId : function(id) {
            var check = new RegExp(/[0-9]+/);
            if (!check.test(id)) {
                if (id == "null") {
                    id = null;
                } else {
                    throw "Unexpected id in upload form, probably an error from the server.";
                }
            }
            var fireEvent = (this.__id == null && id != null);
            this.__id = id;
            if (fireEvent) {
                this.fireEvent("idSet");
            }
        },

        setAttachedField : function(field) {
            this.__attachedField = field;
        },

        getAttachedField : function() {
            return this.__attachedField;
        },

        setExtensions : function(extensionArray) {
            this.__extensions = extensionArray;
        },


// 2014
//        setName : function(name) {
//            if (this.__attachedField != null) {
//                this.__attachedField.getTextField().setValue(name);
//            }
//        },

        /** @Override */
        send : function() {
            var validated = true;
            if (!this.__attachedField) {
                throw "No attached field to form " + this.toHashCode();
            }

            if (this.__extensions != null && this.__extensions.length > 0) {
                validated = false;
                var value = this.__attachedField.getTextField().getValue();
                if (value == null) {
                    validated = true;
                } else {
                    for (var i = this.__extensions.length - 1; i >= 0; i--) {
                        if (dolab.fwk.helper.StringHelper.endsWith(value, this.__extensions[i])) {
                            validated = true;
                            break;
                        }
                    }
                }
            }

            if (validated) {
                this.base(arguments); // super.send();
            } else {
                var dialog = new dolab.fwk.ui.Dialog();
                var buttonDesc =
                        [
                            {lbl:"Ok", ctx: dialog, cde: function() {
                                this.close();
                            }, seleniumId : "okButton"}
                        ];
                dialog.open(null, "File type mismatch", ["File must be of one of the following types: ", this.__extensions.join(", ")], null, buttonDesc);
            }

            return validated;
        }
    }
});