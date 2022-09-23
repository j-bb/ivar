qx.Class.define("dolab.fwk.ui.Dialog", {

    extend : qx.ui.window.Window,

    construct: function(applicationName) {
        this.base(arguments);
        this.icon = dolab.fwk.ui.IconManager.getIcon("default-icon", "bigico");
    },

    members : {

        icon : null,

        open : function(parent, title, message, icon, buttonDesc) {
            this.setCaption(title);
            if (icon != null) {
                this.icon = dolab.fwk.ui.IconManager.getIcon(icon, "bigico");
            }
            this.setLayout(new qx.ui.layout.VBox(10));
            this.setModal(true);
            this.setShowClose(false);

            var firstLine = true;
            for (var i = 0, length = message.length; i < length; i++) {
                if (firstLine) {
                    this.add(new qx.ui.basic.Atom(message[i], this.icon));
                    firstLine = false;
                } else {
                    this.add(new qx.ui.basic.Atom(message[i]));
                }
            }

            for (var i = 0, buttonDescLength = buttonDesc.length; i < buttonDescLength; i++) {
                buttonDesc[i].cdeA = this.close;
                buttonDesc[i].cdeACtx = this;
            }
            this.add(dolab.fwk.ui.AbstractPane.getNewButtonsPanel(buttonDesc));
            if (parent != null) {
                this.addListenerOnce("resize", function(e) {
                    this.moveTocenter(parent.getRootPane());
                }, this);
            }
            this.base(arguments);
        },

        moveTocenter : function(parent) {
            var parentBounds = parent.getBounds();
            var bounds = this.getBounds();
            if (parentBounds != null && bounds != null) {
                this.moveTo(Math.floor((parentBounds.left + (parentBounds.width - parentBounds.left) / 2) - (bounds.width / 2)), Math.floor((parentBounds.top + (parentBounds.height - parentBounds.top) / 2) - (bounds.height / 2)));
            } else {
                this.debug("Dialog.moveToCenter : one of parentBounds or bounds is null !!!");
            }
        }
    }
});