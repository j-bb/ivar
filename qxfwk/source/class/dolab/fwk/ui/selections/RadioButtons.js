qx.Class.define("dolab.fwk.ui.selections.RadioButtons", {
    extend : qx.ui.form.RadioButtonGroup,
    include : [dolab.fwk.rpc.RpcMixin],
    implement : dolab.fwk.ui.selections.Selections,

    construct : function(labelKey, valueKey) {
        this.base(arguments, new qx.ui.layout.Flow(5, 5));
        this.__boxList = [];
        if (labelKey == null) {
            this.__labelKey = "label";
        } else {
            this.__labelKey = labelKey;
        }

        this.__valueKey = valueKey;
    },

    members : {
        __model : null,
        __boxList : null,
        showNullValues : false,
        __labelKey : null,
        __valueKey : null,

        setModel : function(model) {
            this.__model = model;
            this._clean();
            this._build();
        },

        getModel : function() {
            return this.__model;
        },

        updateModel : function(data) {
            for (var i = 0; i < this.__boxList.length; i++) {
                if (this.__boxList[i].getModel().id == data.id) {
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
            for (var i = 0; i < this.__boxList.length; i++) {
                this.__boxList[i].destroy();
            }
            this.__boxList = [];
        },

        _build : function() {
            for (var i = 0; i < this.__model.length; i++) {
                var entry = this.__model[i];
                var value = this.__valueKey != null ? entry[this.__valueKey] : entry;

                if (value == null && !this.showNullValues) {
                    continue;
                }
                var widget = new qx.ui.form.RadioButton(entry[this.__labelKey]);
                widget.setModel(entry);
                this.__boxList.push(widget);
                this.add(widget);
                if (entry.defaultValue) {
                    this.setSelection([widget]);
                }
            }
        },

        getSelectedValues : function() {
            var selection = this.getSelection();
            if (selection.length > 0) {
                if (this.__valueKey != null) {
                    return selection[0].getModel()[this.__valueKey];
                } else {
                    return selection[0].getModel();
                }
            } else {
                return null;
            }
        },

        getSelectedId : function() {
            if (this.getValue() != null) {
                return this.getValue().id;
            } else {
                return null;
            }
        },

        getSelectedIds : function() {
            var id = this.getSelectedId();
            return (id === null) ? [] : [id];
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
            var widget = new qx.ui.form.RadioButton(data[this.__labelKey]);
            widget.setModel(data);
            this.__boxList.push(widget);
            this.add(widget);
            this.setSelected([data]);
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
                    break;
                }
            }
        },

        setLabelWhenSelected : function(key) {
            return;
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
                            break;
                        }
                    }
                    if (!found) {
                        this.addToModel(selection[i]);
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