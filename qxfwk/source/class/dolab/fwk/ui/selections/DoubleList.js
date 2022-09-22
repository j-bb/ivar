qx.Class.define("dolab.fwk.ui.selections.DoubleList", {
    extend : qx.ui.container.Composite,
    include : [dolab.fwk.rpc.RpcMixin],
    implement : dolab.fwk.ui.selections.Selections,

    construct : function(labelKey, valueKey) {
        this.base(arguments, new qx.ui.layout.HBox(5));
        this.__availables = new qx.ui.form.List();
        this.__selected = new qx.ui.form.List();
        var addButton = new qx.ui.form.Button(">>");
        var remButton = new qx.ui.form.Button("<<");
        this.__buttons = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
        this.__buttons.add(addButton);
        this.__buttons.add(remButton);
        this.__buttons.getLayout().setAlignY("middle");
        this.add(this.__availables);
        this.add(this.__buttons);
        this.add(this.__selected);

        remButton.addListener("execute", this._removeSelection, this);
        addButton.addListener("execute", this._addSelection, this);
        this.__availables.addListener("changeSelection", function() {
            this.__availables.resetme = false;
            if (this.__selected.resetme) {
                this.__selected.resetSelection();
                this.fireEvent("changeSelection");
                this.__availables.resetme = true;
                this.__selected.resetme = true;
            }
        }, this);
        this.__selected.addListener("changeSelection", function() {
            this.__selected.resetme = false;
            if (this.__availables.resetme) {
                this.__availables.resetSelection();
                this.fireEvent("changeSelection");
                this.__availables.resetme = true;
                this.__selected.resetme = true;
            }
        }, this);

        if (labelKey) {
            this.setLabelKey(labelKey);
            this.setLabelWhenSelected(labelKey);
        } else {
            this.setLabelKey("label");
            this.setLabelWhenSelected("label");
        }
        this.setValueKey(valueKey);

        this.__availables.resetme = true;
        this.__selected.resetme = true;
    },

    events : {
        "changeSelection" : "qx.event.type.Event"
    },

    members : {
        __availables : null,
        __selected : null,
        __buttons : null,
        __labelKey : null,
        __labelWhenSelected : null,
        __valueKey : null,
        __model : null,

        setLabelKey : function(key) {
            this.__labelKey = key;
        },

        setValueKey : function(key) {
            this.__valueKey = key;
        },

        setModel : function(model) {
            for (var i = 0; i < model.length; i++) {
                this._addToAvailables(model[i][this.__labelKey], model[i]);
            }
            this.__model = model;
        },

        getModel : function() {
            return this.__model;
        },

        updateModel : function(data) {
            var children = this.__availables.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel().id == data.id) {
                    children[i].setLabel(String(data[this.__labelKey]));
                    for (var j = 0; j < this.__model.length; j++) {
                        if (this.__model[j].id == data.id) {
                            this.__model[j] = qx.lang.Object.mergeWith(this.__model[j], data, true);
                            children[i].setModel(this.__model[j]);
                            break;
                        }
                    }
                    break;
                }
            }

            children = this.__selected.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel().id == data.id) {
                    children[i].setLabel(String(data[this.__labelWhenSelected]));
                    for (var j = 0; j < this.__model.length; j++) {
                        if (this.__model[j].id == data.id) {
                            this.__model[j] = qx.lang.Object.mergeWith(this.__model[j], data, true);
                            children[i].setModel(this.__model[j]);
                            break;
                        }
                    }
                    break;
                }
            }
        },

        removeFromModel : function(id) {
            var children = this.__availables.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel().id == id) {
                    this.__availables.remove(children[i]);
                    for (var j = 0; j < this.__model.length; j++) {
                        if (this.__model[j].id == id) {
                            this.__model.splice(j, 1);
                            break;
                        }
                    }
                    break;
                }
            }

            children = this.__selected.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel().id == id) {
                    this.__selected.remove(children[i]);
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

        getSelection : function() {
            var children = this.__selected.getChildren();
            var result = [];
            if (children != null) {
                for (var i = 0; i < children.length; i++) {
                    if (this.__valueKey == null) {
                        result.push(children[i].getModel());
                    } else {
                        result.push(children[i].getModel()[this.__valueKey]);
                    }
                }
            }
            return result;
        },

        setSelected : function(selection) {
            if (this.__model != null) {
                this.__unselectAll();

                var valueKey = (this.__valueKey ? this.__valueKey : "id");
                var availables = this.__availables.getChildren();

                for (var i = 0; i < selection.length; i++) {
                    var found = false;
                    for (var j = 0; j < availables.length; j++) {
                        if (selection[i][valueKey] == availables[j].getModel()[valueKey]) {
                            availables[j].setLabel(String(availables[j].getModel()[this.__labelWhenSelected]));
                            this.__selected.add(availables[j]);
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
        },

        setLabelWhenSelected : function(key) {
            this.__labelWhenSelected = key;
        },

        __unselectAll : function() {
            var selected = this.__selected.getChildren();
            for (var i = 0; i < selected.length; i++) {
                var child = selected[i];
                child.setLabel(String(child.getModel()[this.__labelKey]));
                this.__selected.remove(child);
                this.__availables.add(child);
            }
        },

        getSelectedValues : function() {
            return this.getSelection();
        },

        setValue : function(value) {
            this.setSelected([value]);
        },

        getSelectedId : function() {
            var selection = this.__selected.getSelection();
            if (selection != null && selection.length > 0) {
                return selection[0].getModel().id;
            } else {
                selection = this.__availables.getSelection();
                if (selection != null && selection.length > 0) {
                    return selection[0].getModel().id;
                }
            }
            return null;
        },

        getSelectedIds : function() {
            var result = [];
            var selected = this.__selected.getChildren();
            for (var i = selected.length - 1; i >= 0; i--) {
                result.push(selected[i].getModel().id);
            }
            return result;
        },

        getMinId : function() {
            var minId = -1;
            var children = this.__availables.getChildren();
            for (var i = 0; i < children.length; i++) {
                var candidate = children[i].getModel().id;
                minId = (candidate <= minId) ? candidate - 1 : minId;
            }
            children = this.__selected.getChildren();
            for (var i = 0; i < children.length; i++) {
                var candidate = children[i].getModel().id;
                minId = (candidate <= minId) ? candidate - 1 : minId;
            }
            return minId;
        },

        addToModel : function(data) {
            this.__model.push(data);
            this._addToSelected(String(data[this.__labelWhenSelected]), data);
        },

        setDecorator : function(decorator) {
            this.__availables.setDecorator(decorator);
            this.__selected.setDecorator(decorator);
        },

        getDecorator : function() {
            return this.__availables.getDecorator();
        },

        resetDecorator : function() {
            this.__availables.resetDecorator();
            this.__selected.resetDecorator();
        },

        _addToAvailables: function(label, value) {
            this.__availables.add(new qx.ui.form.ListItem(label, null, value));
        },

        _addToSelected : function(label, value) {
            this.__selected.add(new qx.ui.form.ListItem(label, null, value));
            this.fireEvent("changeSelection");
        },

        _addSelection : function() {
            this.__moveSelected(this.__availables, this.__selected, this.__labelWhenSelected);
            this.fireEvent("changeSelection");
        },

        _removeSelection : function() {
            this.__moveSelected(this.__selected, this.__availables, this.__labelKey);
        },

        __moveSelected : function(from, to, labelKey) {
            var selection = from.getSelection();
            if (selection != null && selection.length > 0) {
                var item = selection[0];
                item.setLabel(String(item.getModel()[labelKey]));
                from.remove(item);
                to.add(item);
            }
        },

        setReadOnly : function(ro) {
            this.__selected.setReadOnly(ro);
            this.__availables.setReadOnly(ro);
        }
    }
});