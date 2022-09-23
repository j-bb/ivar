qx.Mixin.define("dolab.fwk.helper.ArrayHelper", {
    members:  {
        isArray : function(testObject) {
            //            this.debug("isArray " + !(testObject.propertyIsEnumerable('length')));
            //            this.debug("isArray " + (typeof testObject === 'object'));
            //            this.debug("isArray " + (typeof testObject.length === 'number'));
            //            return testObject && !(testObject.propertyIsEnumerable('length')) && typeof testObject === 'object' && typeof testObject.length === 'number';
            return testObject && testObject != null && !(testObject.propertyIsEnumerable('length')) && typeof testObject === 'object';
        }
    }
});