qx.Class.define("dolab.contrib.uploadwidget.UploadFormRegistrar", {
    extend : qx.core.Object,

    construct : function() {
        this.base(arguments);
        this.__formsList = [];
    },

    events : {
        "allComplete" : "qx.event.type.Event"
    },

    properties : {
        count : { apply : "__applyCount", init : 0}
    },

    members : {
        __formsList : null,

        addForm : function(form, uploadField) {
            var found = false;
            for (var i = this.__formsList.length - 1; i >= 0; i--) {
                if (this.__formsList[i].toHashCode() == form.toHashCode()) {
                    this.warn("Upload form " + form.toString() + " was already added to this registrar.");
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.debug("Adding form " + form.toString() + " to registrar.");
                form.add(uploadField);
                form.setAttachedField(uploadField);
                this.__formsList.push(form);

                if (form.getValue() == null) {
                    this.debug("Form has no init value, we'll need to send it.");
                    this.incrementCount();
                }

                form.addListener("completed",
                        function() {
                            this.debug("An upload form has completed its request. count = " + (this.getCount() - 1));
                            this.debug("Iframe content (newly created id) :" + form.getIframeTextContent());
                            form.setId(form.getIframeTextContent());
                            this.decrementCount();
                        }, this);
                var idSetListener = form.addListener("idSet",
                        function() {
                            this.debug("Registrar detected an id was set (probably during the after init in an update scenario). No need to send anything.");
                            this.decrementCount();
                        }, this);
                uploadField.addListener("changeFieldValue",
                        function() {
                            this.debug("User has selected a new file in an upload form.");
                            form.removeListenerById(idSetListener);
                            if (form.getValue() != null) {
                                this.debug("Form had an init value, reinitializing it.");
                                form.resetValue();
                                this.incrementCount();
                            }
                        }, this);
            }
        },

        recount : function() {
            this.debug("Recounting forms that should still be sent");
            this.setCount(0);
            for (var i = this.__formsList.length - 1; i >= 0; i--) {
                if (this.__formsList[i].getValue() == null) {
                    this.incrementCount();
                }
            }
            this.debug("Found " + this.getCount() + " forms that have not been sent.");
        },

        sendAll : function(listener) {
            var allSent = true;
            this.debug("Performing send on all " + this.__formsList.length + " forms in registrar");
            if (this.getCount() == 0) {
                this.fireEvent("allComplete");
            } else {
                for (var i = this.__formsList.length - 1; i >= 0; i--) {
                    if (this.__formsList[i].getValue() == null) {
                        allSent = allSent && this.__formsList[i].send();
                    }
                }
            }

            if (!allSent) {
                this.removeListenerById(listener);
            }
        },

        __applyCount : function(value) {
            if (this.getCount() == 0) {
                this.debug('All uploads done, firing "allComplete" event');
                this.fireEvent("allComplete");
            }
        },

        incrementCount : function() {
            this.setCount(this.getCount() + 1);
            return this.getCount();
        },

        decrementCount : function() {
            this.setCount(this.getCount() - 1);
            return this.getCount();
        }
    }
});