qx.Class.define("dolab.fwk.helper.RuntimeVariableHelper", {
    extend : qx.core.Object,
    type : "singleton",

    construct : function() {
        this.base(arguments);
        this.map = {};
    },

    members :
    {
        map : null,

        set : function(key, value) {
            this.debug("RuntimeVariableHelper.set(" + key + ", " + value + ")");
            this.debug("--------- BEFORE debugObject ---------------");
            qx.dev.Debug.debugObject(value);
            this.debug("--------- AFTER debugObject ----------------");
            this.map.key = value;
        },

        has : function(key) {
            var value = this.get(key);
            return value != null;
        },

        get : function(key) {
            return this.map.key;
        }
    }
});