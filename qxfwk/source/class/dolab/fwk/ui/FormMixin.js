qx.Mixin.define("dolab.fwk.ui.FormMixin", {

    construct: function() {
        this.widgets = [];
    },

    members: {

        widgets : null,
        redDecorator : null,

        setWidgetValue : function(widget, value) {
            if (value != null) {
                widget.setValue(String(value));
            } else {
                widget.setValue("");
            }
        },

        getWidgetValue : function(widget, stringcase) {
            var value = widget.getValue();
            if (value != null) {
                if (stringcase == "u") {
                    value = value.toUpperCase();
                } else if (stringcase == "l") {
                    value = value.toLowerCase();
                } else if (stringcase == "c") {
                    value = value.charAt(0).toUpperCase() + value.substring(1, value.length).toLowerCase();
                }
                if (value === "") {
                    value = null;
                }
            }
            return value;
        },

        getTableCellValue : function(tableModel, row, column) {
            var value = tableModel.getValue(column, row);
            if (value === "") {
                value = null;
            }
            return value;
        },

        getTableRowValue : function(tableModel, row) {
            var row = tableModel.getData()[row];
            for (var i = row.length - 1; i >= 0; i--) {
                if (row[i] == "") {
                    row[i] == null;
                }
            }
            return row;
        },

        // TODO: remove isMandatory optional parameter and replace it with an object which will contain all optional params
        // -> the function thus becomes addWidget : function(container, fieldKey, widgetKey, widget, layoutInfo, optParams)
        // example of an optParams: { isMandatory: true, seleniumId:"id" }
        addWidget : function(container, fieldKey, widgetKey, widget, layoutInfo, isMandatory) {
            // TODO manage the memory leak here. Make sure to
            // release that complex data structure when window
            // is destroyed.
            if (this.widgets == null) {
                this.widgets = [];
            }
            if (this.widgets[fieldKey] == null) {
                this.widgets[fieldKey] = [];
            }
            this.widgets[fieldKey][widgetKey] = {w : widget, l : layoutInfo};
            if (isMandatory != null) {
                this.widgets[fieldKey][widgetKey].mandatory = isMandatory;
            }
            // this.widgets[fieldKey]['c'] = container;
            container.add(widget, layoutInfo);
        },

        addCompositeTable : function(container, fieldKey, widgetKey, widget, layoutInfo, isMandatory) {
            if (this.widgets == null) {
                this.widgets = [];
            }
            if (this.widgets[fieldKey] == null) {
                this.widgets[fieldKey] = [];
            }
            this.widgets[fieldKey][widgetKey] = {w : widget.getTable(), l : layoutInfo};
            if (isMandatory != null) {
                this.widgets[fieldKey][widgetKey].mandatory = isMandatory;
            }
            container.add(widget, layoutInfo);
        },

        getAllFieldWidgets : function(fieldKey) {
            return this.widgets[fieldKey];
        },

        getWidget : function(fieldKey, widgetKey) {
            this.debug("*** getWidget " + fieldKey + " , " + widgetKey);
            return this.widgets[fieldKey][widgetKey];
        },

        checkMandatoryValues : function() {
            var result = true;

            if (this.widgets != null) {
                //TODO switch to classical for(i=0; ; i++ ... ???
                for (var fieldKey in this.widgets) {
                    if (this.widgets[fieldKey]['w'] != null && this.widgets[fieldKey]['w'].mandatory != null && this.widgets[fieldKey]['w'].mandatory) {
                        var widget = this.widgets[fieldKey]['w'].w;
                        if (widget != null && (widget.getValue() == null || widget.getValue() == "")) {
                            this.highlightWidget(fieldKey, true);
                            result = false;
                        } else {
                            this.highlightWidget(fieldKey, false);
                        }
                    }
                }
            }
            return result;
        },

        highlightWidget : function(fieldKey, highlight) {
//            if (this.redDecorator == null) {
//                this.redDecorator = [];
//            }
//            var formLine = this.widgets[fieldKey];
//            if (formLine != null) {
//                var formLineWidget = formLine['w'];
//                if (formLineWidget != null) {
//                    var widget = formLineWidget.w;
//                    if (widget != null) {
//                        var decorator = qx.theme.manager.Decoration.getInstance().resolve(widget.getDecorator());
//                        if (decorator instanceof qx.ui.decoration.Beveled) {
//                            if (highlight == true) {
//                                if (this.redDecorator['Beveled'] == null) {
//                                    this.redDecorator['Beveled'] = new qx.ui.decoration.Beveled().set({outerColor : "#FF0000", backgroundColor : decorator.getBackgroundColor()});
//                                }
//                                widget.setDecorator(this.redDecorator['Beveled']);
//                            } else {
//                                widget.resetDecorator();
//                            }
//                        } else if (decorator instanceof qx.ui.decoration.Single) {
//                            if (highlight == true) {
//                                if (this.redDecorator['Single'] == null) {
//                                    this.redDecorator['Single'] = new qx.ui.decoration.Single().set({color : "#FF0000", width : 1});
//                                }
//                                widget.setDecorator(this.redDecorator['Single']);
//                            } else {
//                                widget.resetDecorator();
//                            }
//                        } else if (widget instanceof qx.ui.form.SelectBox) {
//                            if (highlight) {
//                                if (this.redDecorator['SelectBox'] == null) {
//                                    this.redDecorator['SelectBox'] = qx.theme.manager.Decoration.getInstance().resolve("button-error");
//                                }
//                                widget.setDecorator(this.redDecorator['SelectBox']);
//                            } else {
//                                widget.resetDecorator();
//                            }
//                        } else {
//                            if (highlight) {
//                                if (this.redDecorator['Shadow'] == null) {
//                                    this.redDecorator['Shadow'] = new qx.ui.decoration.Single().set({color : "#FF0000", width : 1});
//                                }
//                                widget.setShadow(this.redDecorator['Shadow']);
//                            } else {
//                                widget.resetShadow();
//                            }
//                        }
//                    } else {
//                        this.error("Error on FormMixin.highlightWidget : this.widgets[ " + fieldKey + "]['w'].w is null !");
//                    }
//                } else {
//                    this.error("Error on FormMixin.highlightWidget : this.widgets[ " + fieldKey + "]['w'] is null !");
//                }
//            } else {
//                this.error("Error on FormMixin.highlightWidget : this.widgets[ " + fieldKey + "] is null !");
//            }
        },

        traceDecorator : function() {
            var theme = qx.theme.manager.Decoration.getInstance().getTheme();

            var decorations = theme.decorations;

            this.debug("--- TRACE decoration ---");
            //TODO switch to classical for(i=0; ; i++ ... ???
            for (var key in decorations) {
                this.debug(key);
            }
            this.debug("--- TRACE decoration ---");
        }
    }
});