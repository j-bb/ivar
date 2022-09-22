qx.Class.define("dolab.fwk.ui.selections.CheckBoxes", {
    extend : qx.ui.container.Composite,
    implement : dolab.fwk.ui.selections.Selections,
    include : [dolab.fwk.rpc.RpcMixin],

    construct : function(labelKey, valueKey) {
        this.base(arguments);
        this.__boxList = [];
        this.__layout = new qx.ui.layout.Flow(5);
        this.setLayout(this.__layout);
        if (labelKey != null) {
            this.__labelKey = labelKey;
        } else {
            this.__labelKey = "label";
        }

        this.__valueKey = valueKey;
    },

    properties : {
        multiple : {
            check : "Boolean",
            init : true,
            apply : "_applyMultiple"
        }
    },

    events : {
        "changeSelection" : "qx.event.type.Event"
    },

    members : {
        __model : null,
        __boxList : null,
        __labelKey : null,
        __valueKey : null,
        showNullValues : false,

        setModel : function(model) {
            this._clean();
            this._build(model);
            this.__model = model;
        },

        getModel : function() {
            return this.__model;
        },

        updateModel : function(data) {
            for (var i = 0; i < this.__boxList.length; i++) {
                if (this.__boxList[i].getModel().id == data.id) {
                    this.__boxList[i].setModel(data);
                    this.__boxList[i].setLabel(data[this.__labelKey]);
                    for (var j = 0; j < this.__model.length; j++) {
                        if (this.__model[j].id == data.id) {
                            this.__model[j] = qx.lang.Object.mergeWith(this.__model[j], data, true);
                            this.__boxList[i].setModel(this.__model[j]);
                            break;
                        }
                    }
                    break;
                }
            }
        },

        setLabelKey : function(key) {
            this.__labelKey = key;
        },

        setValueKey : function(key) {
            this.__valueKey = key;
        },

        _clean : function() {
            this.__model = null;
            for (var i = 0; i < this.__boxList.length; i++) {
                this.__boxList[i].removeListenerById(this.__boxList[i].uniquenessListenerId);
                this.__boxList[i].destroy();
            }
            this.__boxList = [];
        },

        _build : function(model) {
            for (var i = 0; i < model.length; i++) {
                var entry = model[i];
                var value = ((this.__valueKey == null) ? entry : entry[this.__valueKey]);

                if (value == null && !this.showNullValues) {
                    continue;
                }
                var widget = this._addToView(entry);

                if (entry.defaultValue) {
                    widget.setValue(true);
                }
            }
        },

        _applyMultiple : function(value, old) {
            if (value && !old) {
                for (var i = 0; i < this.__boxList.length; i++) {
                    this.__addUniquenessListener(this.__boxList[i]);
                }
            } else if (!value && old) {
                for (var i = 0; i < this.__boxList.length; i++) {
                    this.__boxList[i].removeListenerById(this.__boxList[i].uniquenessListenerId);
                }
            }
        },

        __addUniquenessListener : function(widget) {
            widget.uniquenessListenerId = widget.addListener("click", function() {
                for (var j = 0; j < this.__boxList.length; j++) {
                    if (this.__boxList[j] != widget) {
                        this.__boxList[j].setValue(false);
                    }
                }
            }, this);
        },

        getSelectedValues : function() {
            var selection = this.getSelection();
            var result = null;
            if (!this.isMultiple()) {
                if (selection.length > 0) {
                    if (this.__valueKey != null) {
                        result = selection[0][this.__valueKey];
                    } else {
                        result = selection[0];
                    }
                } else {
                    return null;
                }
            } else {
                result = [];
                if (this.__valueKey != null) {
                    for (var i = selection.length - 1; i >= 0; i--) {
                        result.push(selection[i][this.__valueKey]);
                    }
                } else {
                    for (var i = selection.length - 1; i >= 0; i--) {
                        result.push(selection[i]);
                    }
                }
            }
            return result;
        },

        getSelectedId : function() {
            var value = this.getValue();
            if (value && this.isMultiple()) {
                return value[0].id;
            } else if (value) {
                return value.id;
            } else {
                return null;
            }
        },

        getSelectedIds : function() {
            var value = this.getValue();
            var result = [];
            if (value !== null) {
                if (this.isMultiple()) {
                    for (var i = 0; i < value.length; i++) {
                        result.push(value[i].id);
                    }
                } else {
                    result.push(value.id);
                }
            }

            return result;
        },

        getMinId : function() {
            var minId = -1;
            var children = this.__boxList;
            for (var i = 0; i < children.length; i++) {
                var candidate = children[i].getModel().id;
                minId = (candidate <= minId) ? candidate - 1 : minId;
            }

            return minId;
        },

        addToModel : function(data) {
            this.__model.push(data);
            this._addToView(data);
            this.setSelected([data]);
        },

        _addToView : function(data) {
            var widget = new qx.ui.form.CheckBox(data[this.__labelKey]);
            widget.setModel(data);
            this.__boxList.push(widget);
            this.add(widget);
            if (!this.isMultiple()) {
                this.__addUniquenessListener(widget);
            }
            widget.addListener("click", function() {
                this.fireEvent("changeSelection");
            }, this);
            return widget;
        },

        removeFromModel : function(id) {
            for (var i = 0; i < this.__boxList.length; i++) {
                if (this.__boxList[i].getModel().id == id) {
                    this.remove(this.__boxList[i]);
                    this.__boxList.splice(i, 1);
                    for (var j = 0; j < this.__model.length; j++) {
                        if (this.__model[j].id == id) {
                            this.__model.splice(j, 1);
                            break;
                        }
                    }
                    break;
                }
            }
        },

        setValue : function(value) {
            for (var i = 0; i < this.__boxList.length; i++) {
                var valueKey = (this.__valueKey ? this.__valueKey : "id");
                if (this.__boxList[i].getModel()[valueKey] === value) {
                    this.__boxList[i].setValue(true);
                    if (this.isMultiple()) {
                        break;
                    }
                } else if (!this.isMultiple()) {
                    this.__boxList[i].setValue(false);
                }
            }
        },

        getSelection : function() {
            var result = [];
            for (var i = 0; i < this.__boxList.length; i++) {
                if (this.__boxList[i].getValue() === true) {
                    result.push(this.__boxList[i].getModel());
                }
            }
            return result;
        },

        setLabelWhenSelected : function(key) {
            return;
        },

        setReadOnly : function(ro) {
            for (var i = this.__boxList.length - 1; i >= 0; i--) {
                this.__boxList[i].setReadOnly(ro);
            }
        },

        setSelected : function(selection) {
            if (this.__model != null) {
                var valueKey = (this.__valueKey ? this.__valueKey : "id");
                for (var i = 0; i < selection.length; i++) {
                    var found = false;
                    for (var j = 0; j < this.__boxList.length; j++) {
                        if (this.__boxList[j].getModel()[valueKey] === selection[i][valueKey]) {
                            this.__boxList[j].setValue(true);
                            found = true;
                            if (this.isMultiple()) {
                                break;
                            }
                        } else if (!this.isMultiple()) {
                            this.__boxList[j].setValue(false);
                        }
                    }
                    if (!found) {
                        this.addToModel(selection[i]);
                    } else {
                        this.fireEvent("changeSelection");
                    }
                }
            } else {
                this.addListenerOnce("endRequest", function() {
                    this.setSelected(selection);
                }, this);
            }
        }
    }
});