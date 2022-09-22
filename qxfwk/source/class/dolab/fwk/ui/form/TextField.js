qx.Class.define("dolab.fwk.ui.form.TextField", {
    extend : qx.ui.form.TextField,
    include : [dolab.fwk.ui.form.TextualFieldMixin],

    construct : function(value) {
        this.base(arguments, value);
        // 2014
        // this.setMaxLength(30);
    }
});