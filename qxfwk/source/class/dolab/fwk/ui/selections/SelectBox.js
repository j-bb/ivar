qx.Class.define("dolab.fwk.ui.selections.SelectBox", {

    extend : dolab.fwk.ui.SelectBoxWithValuesHandling,
    implement : dolab.fwk.ui.selections.Selections,
    include : dolab.fwk.rpc.RpcMixin,

    construct : function(labelKey, valueKey, withBlank) {
        this.base(arguments);
        this.removeListener("mousewheel", this._onMouseWheel, this);

        if (labelKey == null) {
            this.__labelKey = "label";
        } else {
            this.__labelKey = labelKey;
        }
        this.__labelWhenSelected = this.__labelKey;
        this.__valueKey = valueKey;
        this.__withBlank = withBlank;

        this.addListener("changeSelection", this._onChangeSelection, this);
        this.setAllowGrowY(false);
    },

    members : {
        __model : null,
        __labelKey : null,
        __labelWhenSelected : null,
        __valueKey : null,
        __withBlank : null,

        setModel : function(model) {
            this.removeAll();
            if (this.__withBlank) {
                var blankItem = new qx.ui.form.ListItem("", null, null);
                this.add(blankItem);
                this.setSelection([blankItem]);
            }
            for (var i = 0; i < model.length; i++) {
                this._addModelObject(model[i]);
            }
            this.__model = model;
        },

        getModel : function() {
            return this.__model;
        },

        updateModel : function(data) {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel() && children[i].getModel().id == data.id) {
                    if (children[i] == this.getSelection()[0]) {
                        children[i].setLabel(String(data[this.__labelWhenSelected]));
                    } else {
                        children[i].setLabel(String(data[this.__labelKey]));
                    }
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
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel() && children[i].getModel().id == id) {
                    this.remove(children[i]);
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

        setLabelKey : function(key) {
            this.__labelKey = key;
        },

        getSelectedId : function() {
            var selection = this.getSelection();
            if (selection != null && selection.length > 0) {
                var model = selection[0].getModel();
                if (model) {
                    return model.id;
                }
            }
            return null;
        },

        getSelectedIds : function() {
            var id = this.getSelectedId();
            return (id === null) ? [] : [id];
        },

        getMinId : function() {
            var minId = -1;
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                if (children[i].getModel()) {
                    var candidate = children[i].getModel().id;
                    minId = (candidate <= minId) ? candidate - 1 : minId;
                }
            }

            return minId;
        },

        addToModel : function(data) {
            this.__model.push(data);
            this._addModelObject(data);
            this.setSelected([data]);
        },

        setValueKey : function(key) {
            this.__valueKey = key;
        },

        // @Override
        getSelectedValues : function() {
            var value = this.getValue();
            if (this.__valueKey != null) {
                value = value[this.__valueKey];
            }
            return value;
        },

        _addModelObject : function(obj) {
            var item = new qx.ui.form.ListItem(obj[this.__labelKey], null, obj);
            this.add(item);
            if (obj.defaultValue) {
                this.setSelection([item]);
            }
        },

        _onChangeSelection : function(e) {
            var data = e.getData();
            var old = e.getOldData();

            if (old && old[0] && old[0].getModel()) {
                old[0].setLabel(String(old[0].getModel()[this.__labelKey]));
            }

            if (data && data[0] && data[0].getModel()) {
                data[0].setLabel(String(data[0].getModel()[this.__labelWhenSelected]));
            }
        },

        setLabelWhenSelected : function(key) {
            this.__labelWhenSelected = key;
        },

        setSelected : function(selection) {
            if (this.__model != null) {
                var children = this.getChildren();
                var valueKey = (this.__valueKey ? this.__valueKey : "id");

                for (var i = 0; i < selection.length; i++) {
                    var found = false;
                    for (var j = 0; j < children.length; j++) {
                        if (children[j].getModel() === null) {
                            continue;
                        }
                        if (selection[i][valueKey] == children[j].getModel()[valueKey]) {
                            this.setSelection([children[j]]);
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