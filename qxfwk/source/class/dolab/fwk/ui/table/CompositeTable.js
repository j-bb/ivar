qx.Class.define("dolab.fwk.ui.table.CompositeTable", {

    extend : qx.ui.container.Composite,

    construct : function(table, jumps, minijumps, name, linkedPane, withToolBar) {
        this.base(arguments, new qx.ui.layout.Dock());

        this.__table = table;
        this.__jumpsToolbar = jumps;
        this.__minijumpsToolbar = minijumps;
        this.__name = name;
        this.__linkedPane = linkedPane;
        this.__csvWithFirstColumn = false;

        if (table != null) {
            this.add(this.__table, {edge : "center"});
        }

        this.__initJumpsContainer();

        if (jumps != null) {
            this.__jumpsContainer.add(this.__jumpsToolbar);
        }

        this.__addCSVMinijump();
        this.__jumpsContainer.add(this.__minijumpsToolbar);

        this.add(this.__jumpsContainer, {edge : "north"});

        if (withToolBar) {
            this.__toolbarLayout();
        }
    },

    members : {
        __table : null,
        __jumpsToolbar : null,
        __minijumpsToolbar : null,
        __jumpsContainer : null,
        __linkedPane : null,
        __csvWithFirstColumn : null,

        getTable : function() {
            return this.__table;
        },

        getLinkedPane : function() {
            return this.__linkedPane;
        },

        setCsvWithFirstColumn : function(withFirst) {
            this.__csvWithFirstColumn = withFirst;
        },

        __toolbarLayout : function() {
            this.__jumpsContainer.setAlignX("right");
            this.__jumpsContainer.setAllowStretchX(true, true);
            this.__jumpsToolbar.setAllowStretchX(true, true);
            this.__jumpsToolbar.setAlignX("right");
            this.__jumpsToolbar.add(new qx.ui.toolbar.Separator());
            this.__minijumpsToolbar.setAlignX("right");
            this.__minijumpsToolbar.setAllowStretchX(true, true);
            this.__minijumpsToolbar.setAppearance("toolbar");
        },

        __initJumpsContainer : function() {
            this.__jumpsContainer = new qx.ui.container.Composite();
            this.__jumpsContainer.setLayout(new qx.ui.layout.HBox(0));
            this.__jumpsContainer.setAlignX("right");
            this.__jumpsContainer.setAllowGrowX(false);
        },

        __addCSVMinijump : function() {
            var icon = dolab.fwk.ui.IconManager.getIcon("csv export", "jump", "quartz");
            var csvButton = new qx.ui.toolbar.Button(null, icon);

            csvButton.setToolTip(new qx.ui.tooltip.ToolTip("Export as CSV", icon));
            csvButton.addListener("click",
                    function() {
                        this.__table.toCSV(this.__name, this.__linkedPane);
                    }, this);

            if (!this.__minijumpsToolbar) {
                this.__minijumpsToolbar = new qx.ui.toolbar.ToolBar();
                this.__minijumpsToolbar.setAppearance("widget");
                this.__minijumpsToolbar.add(csvButton);
                csvButton.setAlignY("middle");
            } else {
                this.__minijumpsToolbar.add(new qx.ui.toolbar.Separator());
                this.__minijumpsToolbar.add(csvButton);
            }
        }
    }
});