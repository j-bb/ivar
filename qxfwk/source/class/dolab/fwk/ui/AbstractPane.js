qx.Class.define("dolab.fwk.ui.AbstractPane", {

    extend : qx.core.Object,
    type : "abstract",
    include: [dolab.fwk.ui.FormMixin, dolab.fwk.rpc.RpcMixin],

    statics : {

        getNewButtonsPanel : function(buttonDesc) {
            var buttonPane = new qx.ui.container.Composite(new qx.ui.layout.HBox(5, "right"));
            for (var i = 0, buttonDescLength = buttonDesc.length; i < buttonDescLength; i++) {
                var currentDesc = buttonDesc[i];

                var button = new qx.ui.form.Button(currentDesc.lbl, (currentDesc.ico ? dolab.fwk.ui.IconManager.getIcon(currentDesc.ico, "icon", "fwdw_icons") : null));
                button.addListener("execute", dolab.fwk.ui.AbstractPane.getButtonPanelListener(currentDesc));

                if (currentDesc.key != null && currentDesc.keyLstnr != null) {
                    button.keyListener = currentDesc.keyLstnr.addListener("keydown", dolab.fwk.ui.AbstractPane.getButtonPanelKeyListener(currentDesc));
                }
                if (currentDesc.seleniumId != null) {
                    button.seleniumId = currentDesc.seleniumId;
                    if (buttonPane.buttons == null) {
                        buttonPane.buttons = [];
                    }
                    buttonPane.buttons[currentDesc.seleniumId] = button;
                }
                if (currentDesc.tooltip != null) {
                    button.setToolTip(new qx.ui.tooltip.ToolTip(currentDesc.tooltip));
                }
                buttonPane.add(button);
            }

            return buttonPane;
        },

        getButtonPanelKeyListener : function(currentDesc) {
            return function(e) {
                var key = e.getKeyIdentifier();
                if (key == currentDesc.key && (!currentDesc.silencer || !currentDesc.silencer.isSilenced(currentDesc.key))) {
                    dolab.fwk.ui.AbstractPane.getButtonPanelListener(currentDesc).call();
                }
            };
        },

        getButtonPanelListener : function(currentDesc) {
            return function() {
                if (currentDesc.cdeB != null && currentDesc.cdeBCtx != null) {
                    currentDesc.cdeB.call(currentDesc.cdeBCtx);
                }
                if (currentDesc.cde != null && currentDesc.ctx != null) {
                    currentDesc.cde.call(currentDesc.ctx);
                }
                if (currentDesc.cdeA != null && currentDesc.cdeACtx != null) {
                    currentDesc.cdeA.call(currentDesc.cdeACtx);
                }
            };
        }
    },

    construct: function() {
        this.base(arguments);
        this.contentPane = new qx.ui.container.Composite(new qx.ui.layout.Dock(5, 5));
        this.subPanes = [];
        this.subPane = false;
        this.rootPane = null;

        this.addListener("startRequest",
                function(e) {
                    if (!this.subPane) {
                        // e.data contains a String which is the message
                        this.waitingOn(e.getData());
                    }
                }, this);

        this.addListener("endRequest",
                function(e) {
                    if (!this.subPane) {
                        this.waitingOff();
                    }
                    this.afterInit();
                }, this);
        this.addListener("requestFailed", this.ackRequestFailed, this);
        this.silentKeys = [];
    },

    events : {
        "newTitle" : "qx.event.type.Data",
        "modalityChange" : "qx.event.type.Data"
    },

    members : {
        win : null,
        rootPane : null,
        contentPane : null,
        subPanes : null,
        subPane : null,
        title : null,
        silentKeys : null,

        showUserMessage : function (title, messages) {
            var dialog = new dolab.fwk.ui.Dialog();
            var okCallback = function() {
                this.contentPane.setEnabled(true);
            };

            dialog.open(this, title, messages, null, [
                {
                    lbl : "OK",
                    cde : okCallback,
                    ctx : this,
                    keyLstnr : dialog,
                    key : "Enter"
                }
            ]);
        },

        addSilentKey : function(key) {
            if (!this.isSilenced(key)) {
                this.silentKeys.push(key);
            }
        },

        isSilenced : function(key) {
            return qx.lang.Array.contains(this.silentKeys, key);
        },

        removeSilentKey : function(key) {
            if (this.isSilenced(key)) {
                qx.lang.Array.remove(this.silentKeys, key);
            }
        },

        setWindowManager : function(windowManager) {
            this.windowManager = windowManager;

            for (var i = 0, length = this.subPanes.length; i < length; i++) {
                var aSubPane = this.subPanes[i];
                aSubPane.setWindowManager(windowManager);
                aSubPane.subPane = true;
            }
        },

        getBounds : function() {
            if (this.rootPane != null) {
                return this.rootPane.getBounds();
            }
        },

        setModal : function(modal) {
            this.fireDataEvent("modalityChange", modal == true);
        },

        setTitle : function(title) {
            this.title = title;
            this.fireDataEvent("newTitle", title);
        },

        getTitle : function() {
            return this.title;
        },

        setRootPane : function(container) {
            //            this.debug("////////// " + this.getCaption() + " setRootPane()");
            this.rootPane = container;
        },

        getRootPane : function() {
            return this.rootPane;
        },

        getIcon : function() {
            return "application";
        },

        getCaption : function() {
            throw new Error("abstract method call. getCaption() must be redefined.");
        },

        initWaitingPane : function() {
            var waiterPaneLayout = new qx.ui.layout.VBox();
            waiterPaneLayout.setAlignX("center");
            this.waitingPane = new qx.ui.container.Composite(waiterPaneLayout);

            this.waitingAtom = new qx.ui.basic.Label("...");
            this.waitingWaiter = new qx.ui.basic.Image(dolab.fwk.ui.IconManager.getIcon("waiter", "gif"));

            this.waitingPane.add(this.waitingAtom);
            this.waitingPane.add(this.waitingWaiter);
        },

        setWaitingText : function(message) {
            this.waitingAtom.setValue(message);
        },

        initWindow : function() {
            this.doInitWindow();
            for (var i = 0, length = this.subPanes.length; i < length; i++) {
                this.subPanes[i].initWindow();
            }
        },

        doInitWindow : function() {
        },

        initUI : function() {
            //            this.debug("////////// " + this.getCaption() + " initUI()");
            if (!this.subPane) {
                this.rootPane.setLayout(new qx.ui.layout.Dock());
                this.waitingOn("Building UI ...");
            }
            for (var i = 0, length = this.subPanes.length; i < length; i++) {
                this.subPanes[i].initUI();
            }

            this.doInitUI();
        },

        doInitUI : function() {
            throw new Error("abstract method call. doInitUI() must be redefined.");
        },

        traceObject : function(object) {
            if (object == null) {
                return "object is null !";
            } else {
                var trace = "{ ";
                var first = true;
                //TODO switch to classical for(i=0; ; i++ ... ???
                for (var key in object) {
                    if (first) {
                        first = false;
                    } else {
                        trace += ", ";
                    }
                    trace += key + " : \"" + object[key] + "\"";
                }
                trace += " }";
                return trace;
            }
        },

        closeForEver : function() {
            this.windowManager.closeWin(this);
        },

        handleException : function(exception) {
            this.debug("[EXC] name = " + exception.name);
            this.debug("[EXC] desc = " + exception.description);
            this.debug("[EXC] file = " + exception.fileName + " line = " + exception.lineNumber);
        },

        initData : function() {
            //            this.debug("////////// " + this.getCaption() + " initData()");
            if (this.initRequest != null && this.initRequest.svc != null && this.initRequest.m != null) {
                //                this.debug("[DBG] init request = " + this.traceObject(this.initRequest));
                this._send("Loading ...", this.initRequest.svc, this.initRequest.m, this.initRequest.p,
                        function(data) {
                            this.afterInitData(data);
                            // The following is already done in the _send() method
                            // this.waitingOff();
                            // this.afterInit();
                        });
            } else {
                this.afterInitData(null);
                if (!this.subPane) {
                    this.waitingOff();
                }
                this.afterInit();
            }

            for (var i = 0, length = this.subPanes.length; i < length; i++) {
                this.subPanes[i].initData(null, null);
            }

            //            this.debug("In the initData of AbstractPane case where NO REQUEST");
        },

        afterInitData : function(data) {
            throw new Error("abstract method call. afterInitData() must be redefined.");
        },

        afterInit : function() {
            this.doAfterInit();
        },

        doAfterInit : function() {
        },

        waitingOn : function(message) {
            if (!this.subPane) {
                this.waiting = true;
                if (this.win != null) {
                    this.win.ready = false;
                }
                this.setWaitingText(message);
                var bounds = null;
                if (this.rootPane != null) {
                    bounds = this.rootPane.getBounds();
                }
                this.rootPane.removeAll();
                this.rootPane.add(this.waitingPane, {edge: "center"});
                if (bounds != null) {
                    this.rootPane.setWidth(bounds.width);
                    this.rootPane.setHeight(bounds.height);
                }
            }
        },

        waitingOff : function() {
            if (!this.subPane) {
                this.rootPane.removeAll();
                if (this.scrollPane == null) {
                    this.scrollPane = new qx.ui.container.Scroll();
                    this.scrollPane.getChildControl("scrollbar-x").addListener("changeVisibility", this.changeScrollPanePaddingX, this);
                    this.scrollPane.getChildControl("scrollbar-y").addListener("changeVisibility", this.changeScrollPanePaddingY, this);
                    this.contentPane.set({padding: 1});
                    this.scrollPane.add(this.contentPane);
                }
                this.rootPane.add(this.scrollPane, {edge: "center"});

                if (this.win != null) {
                    this.win.ready = true;
                }
                this.waiting = false;
            }
            this.scrollPaneSize();
        },

        scrollPaneSize : function() {
            if (this.scrollPane != null) {
                this.scrollPane.setHeight(null);
                this.scrollPane.setWidth(null);
            }
        },

        // Add padding inside of the scrollpane if scrollbars are visible
        changeScrollPanePaddingX : function() {
            if (this.scrollPane._isChildControlVisible("scrollbar-x")) {
                //this.debug("scrollbar-x visible");
                this.scrollPane.setContentPaddingBottom(5);
            } else {
                //this.debug("scrollbar-x hidden");
                this.scrollPane.setContentPaddingBottom(0);
            }
        },

        changeScrollPanePaddingY : function() {
            if (this.scrollPane._isChildControlVisible("scrollbar-y")) {
                //this.debug("scrollbar-y visible");
                this.scrollPane.setContentPaddingRight(5);
            } else {
                //this.debug("scrollbar-y hidden");
                this.scrollPane.setContentPaddingRight(0);
            }
        },

        isSubPane : function() {
            return this.subPane;
        },

        isWaiting : function() {
            return this.waiting;
        },

        isDotDecimalSeparator : function() {
            return qx.locale.Number.getDecimalSeparator() == ".";
        },

        ackRequestFailed : function(e) {
            var reqObj = e.getData();

            if (reqObj && reqObj.reLogin) { // Authentication error / session timeout
                this.windowManager.addListenerOnce("hideLogin", function() {
                    this.retryRequest(reqObj);
                }, this);
            } else {
                var infoLabel = new qx.ui.basic.Label("An error has occured!");
                var errZone = null;
                if (reqObj) { // RpcMixin requestFailed event
                    errZone = new qx.ui.form.TextArea(reqObj.exception.toString());
                } else { // Spec upload error
                    errZone = new qx.ui.form.TextArea(e.logs);
                }
                var detailsLabel = new qx.ui.basic.Label("Show details >>");
                var buttonPane = new qx.ui.container.Composite(new qx.ui.layout.HBox(10, "center"));
                var retryButton = new qx.ui.form.Button("Retry", dolab.fwk.ui.IconManager.getIcon("refresh", "icon", "fwdw_icons"));
                var cancelButton = new qx.ui.form.Button("Cancel", dolab.fwk.ui.IconManager.getIcon("cancel", "icon", "fwdw_icons"));

                detailsLabel.setTextColor("#000099");

                detailsLabel.addListenerOnce("click", function (e) {
                    errZone.setVisibility("visible");
                }, this);
                cancelButton.addListener("execute", this.closeForEver, this);
                if (reqObj) {
                    retryButton.addListener("execute", function (e) {
                        this.retryRequest(reqObj);
                    }, this);

                    buttonPane.add(retryButton);
                }

                buttonPane.add(cancelButton);

                errZone.setSelectable(true);
                errZone.setReadOnly(true);
                errZone.setVisibility("hidden");

                this.rootPane.removeAll();
                var newLayout = new qx.ui.layout.Grid(5, 5);

                newLayout.setRowFlex(2, 1);
                newLayout.setColumnFlex(0, 1);

                newLayout.setRowAlign(0, "left", "middle");
                newLayout.setRowAlign(1, "center", "middle");
                newLayout.setRowAlign(3, "right", "middle");

                this.rootPane.setLayout(newLayout);

                this.rootPane.add(infoLabel, {row: 0, column: 0});
                this.rootPane.add(buttonPane, {row: 1, column: 0});
                this.rootPane.add(errZone, {row: 2, column: 0});
                this.rootPane.add(detailsLabel, {row: 3, column: 0});
            }
        },

        retryRequest : function(requestObj) {
            this.rootPane.setLayout(new qx.ui.layout.Dock());
            this._send(requestObj.message, requestObj.service, requestObj.method, requestObj.params, requestObj.whatToDo);
        }
    }
});