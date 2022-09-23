qx.Class.define("dolab.fwk.ui.table.Table", {

    extend : qx.ui.table.Table,
    include : dolab.fwk.ui.table.ResizeMixin,

    construct : function(dataModel) {
        //this.debug("----- Table constructor !!!");
        this.base(arguments, dataModel);
        this.set({
            height: 101,

            // minHeight is also set to 101 to avoid bugs when the table is inside a scrollpane
            // (100% CPU and freezing UI if the table was too small then sized-up).
            minHeight: 60
        });

        // If an "id" column is present, we use it to identify rows and keep the selection of the rows after sorting the table
        var idColumnIndex = this.getTableModel().getColumnIndexById("id");
        if (idColumnIndex != null && idColumnIndex >= 0) {

            // After this event the selection is cleared
            this.getTableModel().addListener("sorted", function(e) {
                this.onSortTable = true;
            }, this);

            // TODO: optimize
            // Currently the selectioned ids are saved each time the selection changes and if a sort happens,
            // we reselect the lines with the previously saved ids.
            // A better approach would be to listen an event like "beforeSort" or "columnHeaderClick" to save the selected ids
            // only before the table is sorted, but such an event doesn't exist for now (qooxdoo 0.8.3).
            this.getSelectionModel().addListener("changeSelection", function(e) {
                if (!this.onSortTable) { // classic selection change
                    var selections = this.getSelectionModel().getSelectedRanges();
                    if (selections != null) {
                        this.tableSelectionedIds = [];
                        for (var i = 0; i < selections.length; i++) {
                            for (var j = selections[i].minIndex; j <= selections[i].maxIndex; j++) {
                                this.tableSelectionedIds.push(this.getTableModel().getValueById("id", j));
                            }
                        }
                    }
                } else if (!this.reselecting && this.tableSelectionedIds != null && this.tableSelectionedIds.length > 0) {
                    // selection has been cleared automatically by qooxdoo after sorting the table
                    this.debug("reselecting rows after table sort");
                    this.reselecting = true; // we need another boolean because we will rechange the selection in this listener of "changeSelection"
                    for (var i = 0; i < this.getTableModel().getRowCount(); i++) {
                        if (this.tableSelectionedIds.indexOf(this.getTableModel().getValueById("id", i)) >= 0) {
                            this.getSelectionModel().addSelectionInterval(i, i);
                        }
                    }
                    var selections = this.getSelectionModel().getSelectedRanges();
                    if (selections != null && selections.length > 0) {
                        var visibleColumns = this.getTableColumnModel().getVisibleColumns();
                        if (visibleColumns != null && visibleColumns.length > 0) {
                            this.getPaneScroller(0).setFocusedCell(visibleColumns[0], selections[0].minIndex);
                            this.getPaneScroller(0).scrollCellVisible(visibleColumns[0], selections[0].minIndex);
                        }
                    }
                    this.onSortTable = false;
                    this.reselecting = false;
                }
            }, this);
        }

        if (this.getTableModel() != null) {
            this._resizeColumns();
        }
    },

    members : {
        onSortTable : false,
        reselecting : false,
        tableSelectionedIds : null,

        getNextPos : function(column) {
            var tableModel = this.getTableModel();
            var maxi = 0;
            for (var i = 0, len = tableModel.getRowCount(); i < len; i++) {
                //maxi = Math.max(maxi, tableModel[i][column]);
                maxi = Math.max(maxi, tableModel.getValue(column, i));
            }
            return maxi + 10;
        },

        setExclusiveBooleanColumn : function(booleanColNumber) {
            var booleanRenderer = new qx.ui.table.cellrenderer.Boolean();
            booleanRenderer.setIconFalse("decoration/form/radiobutton.png");
            booleanRenderer.setIconTrue("decoration/form/radiobutton-checked.png");
            this.getTableColumnModel().setDataCellRenderer(booleanColNumber, booleanRenderer);

            this.addListener("cellDblclick",
                    function(cellEvent) {
                        var col = cellEvent.getColumn();
                        if (col == booleanColNumber) {
                            var row = cellEvent.getRow();
                            var tableModel = this.getTableModel();
                            if (tableModel.getValue(col, row) == false) {
                                tableModel.setValue(col, row, true);

                                for (var r = 0, len = tableModel.getRowCount(); r < len; r++) {
                                    if (r != row && tableModel.getValue(col, r) == true) {
                                        tableModel.setValue(col, r, false);
                                    }
                                }
                            }
                        }
                    }, this);
        },

        // @Override
        _applyTableModel : function(model, old) {
            if (model !== old) {
                model.addListener("dataChanged", this._resizeColumns, this);
                model.addListener("metaDataChanged", this._resizeColumns, this);
            }
            this.base(arguments, model);
        },

        getSelectedId : function() {
            return this.getTableModel().getValue(0, this.getFocusedRow());
        },

        getSelectedIds : function() {
            var result = [];
            var tableModel = this.getTableModel();
            for (var i = tableModel.getRowCount() - 1; i >= 0; i--) {
                result.push(tableModel.getValue(0, i));
            }
            return result;
        },

        getMinId : function() {
            var minId = -1;
            var tableModel = this.getTableModel();
            for (var i = tableModel.getRowCount() - 1; i >= 0; i--) {
                var id = tableModel.getValue(0, i);
                minId = (id <= minId) ? id - 1 : minId;
            }

            return minId;
        },

        getModel : function() {
            return this.getTableModel().getData();
        },

        toCSV : function(filename, linkedPane) {
            var tableModel = this.getTableModel();
            var columnModel = this.getTableColumnModel();
            var dateFormat = new qx.util.format.DateFormat(qx.locale.Date.getDateFormat("short"));
            var data = tableModel.getData();
            var sentData = [];
            var headers = [];
            for (var j = 0, colIndex = 0, length = tableModel.getColumnCount(); j < length; j++) {
                if (!columnModel.isColumnVisible(j)) {
                    continue;
                }
                headers[colIndex] = tableModel.getColumnName(j);
                colIndex++;
            }

            for (var i = 0, length = data.length; i < length; i++) {
                var colIndex = 0;
                sentData[i] = [];
                for (var j = 0; j < data[i].length; j++) {
                    if (!columnModel.isColumnVisible(j)) {
                        continue;
                    }

                    if (data[i][j] == null) {
                        sentData[i][colIndex] = "null";
                    } else if (data[i][j].getDate) {
                        sentData[i][colIndex] = dateFormat.format(data[i][j]);
                    } else {
                        sentData[i][colIndex] = data[i][j];
                        sentData[i][colIndex] = sentData[i][colIndex].toString();
                    }
                    colIndex++;
                }
            }

            var request = {
                svc : "dolab.fwk.files.download.FileRequestController",
                m : "requestCSV",
                p : [headers, sentData]
            };
            if (!filename) {
                filename = "export";
            }

            var callback =
                    function(result) {
                        window.open("files/tables/" + filename + ".csv?key=" + result, "_blank");
                    };
            linkedPane.send("Requesting CSV file...", request, callback);
        }

        /*_resizeColumns : function() {
         this.debug("Resizing columns...");
         var tableModel = this.getTableModel();
         var offset = 10;
         var data = tableModel.getData();
         var dateFormat = new qx.util.format.DateFormat(qx.locale.Date.getDateFormat("short"));
         var lengths = [];
         for (var j = 0, length = tableModel.getColumnCount(); j < length; j++) {
         lengths[j] = qx.bom.Label.getTextSize(tableModel.getColumnName(j)).width;
         }

         if (data && data.length >= 1) {
         for(var i = 0; i < data.length; i++) {
         for (var j = 0; j < data[i].length; j++) {
         if (!data[i][j]) {
         continue;
         }
         var tmpLength = -1;
         if (data[i][j].getDate) {
         tmpLength = qx.bom.Label.getTextSize(dateFormat.format(data[i][j])).width;
         } else {
         tmpLength = qx.bom.Label.getTextSize(data[i][j].toString()).width;
         }
         lengths[j] = (lengths[j] > tmpLength) ? lengths[j] : tmpLength;
         }
         }
         }
         for (var i = 0; i < lengths.length; i++) {
         this.setColumnWidth(i, lengths[i] + offset);
         }
         }*/
    }

});


/* Another way :
 Hello Massimo,

 as far as i know there is no way to set this behavior in the API.

 The only thing i can think of is patching the qooxdoo table for
 yourself. You could change the variables
 var horNeeded = false;
 var verNeeded = false;
 to
 var horNeeded = true;
 var verNeeded = true;
 This variables are in the table class (qx.ui.table.Table) in the
 _updateScrollBarVisibility method (line 1784). With that change, you
 can make the scrollbars stay visible all the time.

 Best,
 Martin
 */