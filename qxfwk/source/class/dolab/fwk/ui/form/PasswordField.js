qx.Class.define("dolab.fwk.ui.form.PasswordField", {
    extend : qx.ui.form.PasswordField,
    include : [dolab.fwk.ui.form.TextualFieldMixin],

    construct : function(value) {
        this.base(arguments, value);
        // 2014
        // this.setMaxLength(30);
    }
});