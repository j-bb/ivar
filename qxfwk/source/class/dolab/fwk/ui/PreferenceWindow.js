qx.Class.define("dolab.fwk.ui.PreferenceWindow", {

    extend: dolab.fwk.ui.AbstractPane,
    include: [dolab.fwk.rpc.RpcMixin],

    construct: function() {
        this.base(arguments);
    },

    members: {
        panesOpeningPref : null,

        afterInitData: function(data) {

        },

        // Override
        getCaption : function() {
            return "Composer preference";
        },

        // Override
        getIcon : function() {
            return "edt application";
        },

        // Override
        getSeleniumId : function() {
            return "composer.preference";
        },

        doInitUI: function() {
            // Buttons
            var buttonPane = dolab.fwk.ui.AbstractPane.getNewButtonsPanel([
                {
                    lbl:"Save",
                    cde:this.saveAction,
                    ctx:this,
                    keyLstnr:this.rootPane,
                    key:"Enter",
                    seleniumId: "saveButton"
                },
                {
                    lbl:"Cancel",
                    cde:this.cancelAction,
                    ctx:this,
                    keyLstnr:this.rootPane,
                    key:"Escape",
                    seleniumId: "cancelButton"
                }
            ]);

            this.contentPane.add(buttonPane, {edge: "south"});


            // Info
            var infoLabel = new qx.ui.basic.Label("<i>Changes in preference will take effect next time you start the application.</i>").set({rich: true});
            this.contentPane.add(infoLabel, {edge: "north"});

            // Main content
            var mainGrid = new qx.ui.layout.Grid(7, 7);
            mainGrid.setColumnFlex(1, 1);
            var mainContent = new qx.ui.container.Composite(mainGrid);
            var roww = 0;

            // preferences for the opening mode of panes: dock or floating windows
            var panesOpeningPrefLabel = new qx.ui.basic.Label("Panes opening mode:");
            this.panesOpeningPref = new dolab.fwk.ui.SelectBoxWithValuesHandling();
            var dockMode = new qx.ui.form.ListItem("dock");
            dockMode.setModel("dock");
            var floatingMode = new qx.ui.form.ListItem("floating windows");
            floatingMode.setModel("floating");
            this.panesOpeningPref.add(floatingMode);
            this.panesOpeningPref.add(dockMode);

            this.setWidgetValue(this.panesOpeningPref, qx.core.Init.getApplication().getPreferenceManager().getPreference("panes_opening_mode"));

            this.addWidget(mainContent, "panesOpeningMode", "l", panesOpeningPrefLabel, {row: roww, column: 0});
            this.addWidget(mainContent, "panesOpeningMode", "w", this.panesOpeningPref, {row: roww, column: 1});

            this.contentPane.add(mainContent, {edge: "center"});
        },

        saveAction: function() {
            this.debug("before set pane mode pref");
            qx.core.Init.getApplication().getPreferenceManager().setPreference("panes_opening_mode", this.getWidgetValue(this.panesOpeningPref));
            this.debug("before save prefs to server");
            qx.core.Init.getApplication().getPreferenceManager().savePreference(dolab.fwk.auth.AuthManager.getInstance().getUser());
            this.closeForEver();
        },

        cancelAction: function() {
            this.closeForEver();
        }


    }

});