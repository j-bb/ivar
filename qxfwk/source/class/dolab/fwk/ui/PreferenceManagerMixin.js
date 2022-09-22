qx.Mixin.define("dolab.fwk.ui.PreferenceManagerMixin", {
    include : [dolab.fwk.rpc.RpcMixin],

    construct : function() {
        this.preference = [];
        this.initDefaultValues();
    },

    members : {
        initService : null,
        updateService : null,
        preference : null,
        defaultValues : null,

        initDefaultValues : function() {
            this.defaultValues = [];
            this.defaultValues["panes_opening_mode"] = "dock";
        },

        getPreference : function(prefName) {
            var result = null;
            var i = 0;
            for (i = 0; i < this.preference.length; i++) {
                if (this.preference[i].pref_name == prefName) {
                    result = this.preference[i].pref_value;
                    break;
                }
            }
            if (i == this.preference.length) { // We do not test "result == null" in case the value of the pref is explicitly "null"
                result = this.getDefaultPreference(prefName);
            }
            return result;
        },

        getDefaultPreference : function(prefName) {
            return this.defaultValues[prefName];
        },

        setPreference : function(prefName, prefValue) {
            var prefAlreadyExists = false;
            for (var i = 0; i < this.preference.length; i++) {
                if (this.preference[i].pref_name == prefName) {
                    this.preference[i].pref_value = prefValue;
                    prefAlreadyExists = true;
                    break;
                }
            }
            if (!prefAlreadyExists) {
                this.preference.push({pref_name: prefName, pref_value: prefValue});
            }
        },

        initPreference : function(login, whatToDo, context) {
            this.debug("initservice = " + this.initService);
            this.send("Initializing user preference...", {svc:this.initService, m:"getPrefShowAppUserPreferences", p:[login, false, null]},
                    function(data) {
                        if (data.length == 1) {
                            this.preference = data[0].preference;
                        } else {
                            throw new Error("Unable to find preference for user " + login);
                        }

                        if (whatToDo != null) {
                            whatToDo.call(context);
                        }
                    });
        },

        savePreference : function(login) {
            this.send("Initializing user preference...", {svc:this.updateService, m:"updatePreference", p:[login, this.preference]},
                    function(data) {
                        this.preference = data.preference;
                    });
        },

        setUpdateService : function(service) {
            this.updateService = service;
        },

        setInitService : function(service) {
            this.initService = service;
        }
    }
});