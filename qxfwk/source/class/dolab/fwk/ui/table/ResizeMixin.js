qx.Mixin.define("dolab.fwk.ui.table.ResizeMixin", {
    members : {
        _resizeColumns : function() {
            this.debug("Resizing columns...");
            var tableModel = this.getTableModel();
            var offset = 10;
            var data = tableModel.getData();
            var dateFormat = new qx.util.format.DateFormat(qx.locale.Date.getDateFormat("short"));
            var lengths = [];
            var columnModel = this.getTableColumnModel();

            for (var j = 0, length = tableModel.getColumnCount(); j < length; j++) {
                lengths[j] = qx.bom.Label.getTextSize(tableModel.getColumnName(j)).width;
            }

            if (data && data.length >= 1) {
                for (var i = 0; i < data.length; i++) {
                    for (var j = 0; j < data[i].length; j++) {
                        if (!data[i][j]) {
                            continue;
                        }
                        var tmpLength = -1;
                        var cellRenderer = columnModel.getDataCellRenderer(j);
                        if (qx.lang.Type.isDate(data[i][j])) {
                            tmpLength = qx.bom.Label.getTextSize(dateFormat.format(data[i][j])).width;
                        } else {
                            tmpLength = qx.bom.Label.getHtmlSize(data[i][j].toString()).width;
                        }
                        lengths[j] = (lengths[j] > tmpLength) ? lengths[j] : tmpLength;
                    }
                }
            }
            for (var i = 0; i < lengths.length; i++) {
                var cellRenderer = columnModel.getDataCellRenderer(i);
                if (!(cellRenderer instanceof qx.ui.table.cellrenderer.Image ||
                        cellRenderer instanceof qx.ui.table.cellrenderer.Html)) {
                    this.setColumnWidth(i, lengths[i] + offset);
                }
            }
        }
    }
});