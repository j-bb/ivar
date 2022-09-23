qx.Class.define("dolab.fwk.ui.AbstractLandingZonePane", {

    extend : dolab.fwk.ui.AbstractPane,

    construct: function(application) {
        this.base(arguments);

        this.application = application;
    },

    members: {
        application : null,
        labels : null,
        html: null,

        getIcon : function() {
            return "application";
        },

        getCaption : function() {
            return "Landing zone";
        },

        afterInitData : function(data) {
        },

        doInitWindow : function() {
            this.getWin().addListenerOnce("beforeClose", function() {
                this.contentPane.remove(this.html);
                // this.html.setLayoutParent(null);
                qx.html.Element.flush();
                this.html.dispose();
            }, this);

            this.getWin().maximize();
        },

        doInitUI : function() {
            this.contentPane.setLayout(new qx.ui.layout.Basic());
            //            this.contentPane.setBackgroundColor("#FFFFFF");

            var desktopDeco = new qx.ui.decoration.MBackgroundColor("#F3F3F3");
            desktopDeco.setBackgroundImage(dolab.fwk.ui.IconManager.getIcon("background", "png"));

            this.contentPane.setDecorator(desktopDeco);


            this.html = new qx.ui.embed.Html("<div id=holder></div><script src=\"resource/" + this.doGetLandingZoneResourcePath() + "\" type=\"text/javascript\" charset=\"utf-8\"></script>");
            this.html.setWidth(600);
            this.html.setHeight(500);

            this.contentPane.add(this.html, {left: 0, top: 0});

            this.makeLabelsCircle();
        },

        makeLabelsCircle: function() {
            var centerX = 35;
            var centerY = 40;
            var radius = 500;
            var radiusSquare = radius * radius;
            var decHeight = 20;

            var y = radius;

            this.labelwidth = 280;

            for (var i = 0; i < this.labels.length; i++,y -= decHeight) {
                this.createLabel(i, y, radiusSquare, centerX, centerY);
            }

        },

        createLabel : function(i, y, radiusSquare, centerX, centerY) {
            var x = Math.round(Math.sqrt(radiusSquare - y * y));
            var j = i;

            var labelValue = this.labels[j].val;
            // 7 is for <i></i> length
            if (labelValue.length - 7 > 40) {
                labelValue = labelValue.substring(0, 37 + 7) + "...";
            }

            var label = new qx.ui.basic.Label().set({value: "<b onmouseover=\"style.textDecoration='underline'\" onmouseout=\"style.textDecoration='none'\">" + labelValue + "</b>", rich: true});

            var labelX = centerX + x;
            var labelY = centerY + y;

            label.setHeight(15);
            label.setWidth(this.labelwidth);
            this.labelwidth += 10;

            // Need to be done *after* setHeight
            label.setBackgroundColor("#55AFE8");

            label.addListener("click", function() {
                this.labels[j].action.call(this.application);
            }, this);

            this.contentPane.add(label, {left: labelX, top: labelY});
        },

        // NO leading '/'
        doGetLandingZoneResourcePath: function() {
            throw new Error("abstract method call. doGetLandingZoneResourcePath() must be redefined.");
        }
    }
});