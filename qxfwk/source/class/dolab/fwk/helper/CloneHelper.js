qx.Class.define("dolab.fwk.helper.CloneHelper", {
    type : "static",

    statics : { // "Static functions can access other static functions directly through the this keyword."
        // http://manual.qooxdoo.org/1.3.x/pages/core/oo_feature_summary.html#accessing-static-members
        /**
         * Take a native javascript object and return a clone of it created by deep copy.
         * @param obj a native javascript object or array.
         * @param exclusion an array of string containing keys that should be excluded from the resulting object.
         * @return The clone
         */
        cloneObject : function(obj, exclusion) {
            if (exclusion == null) {
                exclusion = [];
            }
            var result = null;
            if (qx.lang.Type.isArray(obj)) {
                result = [];
                for (var i = 0, len = obj.length; i < len; i++) {
                    result[i] = this.cloneObject(obj[i], exclusion);
                }
            } else if (qx.lang.Type.isObject(obj) && !qx.lang.Type.isDate(obj)) {
                result = this.__cloneObject(obj, exclusion);
            } else {
                result = obj;
            }
            return result;
        },

        __cloneObject : function(obj, exclusion) {
            var result = {};
            for (var key in obj) {
                if (!qx.lang.Array.contains(exclusion, key)) {
                    result[key] = this.cloneObject(obj[key], exclusion);
                }
            }
            return result;
        }
    }
});