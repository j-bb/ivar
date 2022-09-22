qx.Class.define("dolab.fwk.ui.form.TextArea", {
    extend : qx.ui.form.TextArea,
    include : [dolab.fwk.ui.form.FormFieldMixin, dolab.fwk.ui.form.TextualFieldMixin],

    construct : function(value) {
        this.base(arguments, value);
        this.setMaxLength(300);
    }
});