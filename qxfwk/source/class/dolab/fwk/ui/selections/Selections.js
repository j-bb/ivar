qx.Interface.define("dolab.fwk.ui.selections.Selections", {
    events : {
        changeSelection : "qx.event.type.Event"
    },
    members : {
        /**
         * get value or collection of values selected by the user (using the
         * provided value key)
         */
        getValue : function() {
        },

        /**
         * set selection to the unique value represented by data given as param
         *
         * @param value
         */
        setValue : function(value) {
        },

        /**
         * Key that determines which key is significative as the value in the
         * data's model
         *
         * @param key
         */
        setValueKey : function(key) {
        },

        /**
         * Key that determines which key is significative as the label in the
         * data's model
         *
         * @param key
         */
        setLabelKey : function(key) {
        },

        /**
         * Key that determines which key is significative as the value in the
         * data's model when it is selected. If not present,
         * labelWhenSelected = labelKey
         *
         * @param key
         */
        setLabelWhenSelected : function(key) {
        },

        /**
         * Using the value key (or the id if none is provided), determine which
         * items must be shown as selected.
         *
         * @param selection
         */
        setSelected : function(selection) {
        },

        /**
         * Set underlying model (containing data that can be manipulated through
         * the widget)
         *
         * @param model
         */
        setModel : function(model) {
        },

        /**
         * Update/modify a data already present in the model.
         *
         * @param data
         */
        updateModel : function(data) {
        },

        /**
         * Add a new data to the model
         *
         * @param data
         */
        addToModel : function(data) {
        },

        /**
         * Remove a data from the model
         */
        removeFromModel : function() {
        },

        /**
         * Get underlying model
         */
        getModel : function() {

        }, // also in table

        /**
         * Return the first selected id.
         */
        getSelectedId : function() {
        }, // also in table

        /**
         * Return id of all data marked as selected.
         */
        getSelectedIds : function() {
        }, // also in table

        /**
         * Return a negative id that is lesser to all other ids present in the
         * model.
         */
        getMinId : function() {
        } // also in table
    }
});