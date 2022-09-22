qx.Mixin.define("dolab.fwk.ui.WindowManager", {

    statics: {
        /**
         * Values of PANES_OPENING_MODE can be "dock" or "floating"
         */
        PANES_OPENING_MODE: "dock"
    },

    construct: function() {
        this.seleniumIds = [];
        this.maxX = null;
        this.maxY = null;
    },

    members : {
        nextX : 20,
        nextXinit : 50,
        nextY : 20,
        nextYinit : 50,
        seleniumIds : null,
        maxX : null,
        maxY : null,

        closeWin : function(pane) {
            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                this.closeForDock(pane);
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                this.closeFloatingWindow(pane);
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "raw") {
                this.debug("RAW mode");
                pane.waitingOff();
            } else {
                throw new Error(dolab.fwk.ui.WindowManager.PANES_OPENING_MODE + " is not a valid opening mode for panes. Value of dolab.fwk.ui.WindowManager.PANES_OPENING_MODE must be \"floating\" or \"dock\"");
            }

            pane.setRootPane(null);
            pane.dispose();
        },

        closeFloatingWindow : function(pane) {
            var win = pane.getRootPane();
            if (win) {
                win.close();
                win.removeAll();
            }
            //win.dispose();
        },

        closeForDock : function(pane) {
            var page = pane.getRootPane();
            if (page) {
                this.tabview.remove(page);
                page.removeAll();
            }
            //page.dispose();
        },

        setSeleniumId: function (win, seleniumId) {
            if (this.seleniumIds[seleniumId] == null) {
                this.seleniumIds[seleniumId] = 0;
            } else {
                this.seleniumIds[seleniumId]++;
            }
            win.seleniumId = seleniumId + "-" + this.seleniumIds[seleniumId];
        },

        /* !!! BEWARE !!! (for Selenium users)
         *
         * If window contains a grid layout with non contiguous rows or if params.boundX and params.boundY are
         * set, with selenium you might not be able to select a row inside the table of another window after opening&closing
         * this window (the row will be "half-selected": in modern theme it will be "light blue" colored, and
         * the table selection manager will not see this row among the selected ones. Best example is:
         * open finder => open&close create application window => select an application inside the finder.
         */
        openWin : function(pane, params, title) {
            if (params != null) {
                if (params.initRequest != null && (params.initRequest.m != null || params.initRequest.svc != null)) {
                    throw new Error("WindowManager.openWin called with params.initRequest more than just p: element");
                }
                if (params.initRequest != null && params.initRequest.p == null) {
                    throw new Error("WindowManager.openWin called with params.initRequest that contains no p: element");
                }
                if (params.initRequest != null && params.initRequest.p != null) {
                    pane.addInitRequestParam(params.initRequest);
                }
            }

            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                this.openInDock(pane, title);
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                this.openInFloatingWindow(pane, params, title);
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "raw") {
                this.openRaw(pane, params, title);
            } else {
                throw new Error(dolab.fwk.ui.WindowManager.PANES_OPENING_MODE + " is not a valid opening mode for panes. Value of dolab.fwk.ui.WindowManager.PANES_OPENING_MODE must be \"floating\" or \"dock\"");
            }
        },

        overrideWin : function(srcPane, newPane, params, title) {
            var srcRootPane = srcPane.rootPane;
            newPane.setWindowManager(this);
            newPane.initWaitingPane();
            this.statuscontainer.add(this.waiter, {edge:"east"});
            srcRootPane.ready = false;
            srcRootPane.requestDone = true;

            if (newPane.getSeleniumId != null) {
                this.setSeleniumId(srcRootPane, newPane.getSeleniumId());
            }

            var icon = newPane.getIcon();
            if (icon == null) {
                icon = dolab.fwk.ui.IconManager.getIcon("application", "win");
            } else {
                icon = dolab.fwk.ui.IconManager.getIcon(icon, "win");
            }
            srcRootPane.setIcon(icon);

            var caption;
            if (title != null) {
                caption = title;
            } else {
                caption = newPane.getCaption();
            }
            newPane.setTitle(caption);

            srcRootPane.removeAll();

            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                srcRootPane.pane = newPane;
                srcRootPane.setLabel(caption);
                this.tabview.setSelection([srcRootPane]);
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                srcRootPane.setCaption(caption);
            }

            newPane.initWindow();
            newPane.setRootPane(srcRootPane);
            newPane.initUI();
            newPane.initData();

            var that = this;
            newPane.closeForEver =
                    function() {
                        srcRootPane.ready = false;
                        this.setRootPane(null);
                        this.contentPane.destroy();
                        this.contentPane = null;
                        srcRootPane.removeAll();
                        if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                            srcRootPane.pane = srcPane;
                        }

                        if (srcPane.getSeleniumId != null) {
                            that.setSeleniumId(srcRootPane, srcPane.getSeleniumId());
                        }
                        srcPane.setRootPane(srcRootPane);

                        if (!srcPane.scrollPane) {
                            srcPane.scrollPane = new qx.ui.container.Scroll();
                            srcPane.scrollPane.getChildControl("scrollbar-x").addListener("changeVisibility", srcPane.changeScrollPanePaddingX, srcPane);
                            srcPane.scrollPane.getChildControl("scrollbar-y").addListener("changeVisibility", srcPane.changeScrollPanePaddingY, srcPane);
                        }

                        srcPane.scrollPane.add(srcPane.contentPane);
                        srcRootPane.add(srcPane.scrollPane, {edge : "center"});
                        srcRootPane.setIcon(srcPane.getIcon() ?
                                dolab.fwk.ui.IconManager.getIcon(srcPane.getIcon(), "win") :
                                dolab.fwk.ui.IconManager.getIcon("application", "win"));
                        if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                            srcRootPane.setCaption(srcPane.getTitle());
                        } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                            srcRootPane.setLabel(srcPane.getTitle());
                        }
                        srcRootPane.ready = true;
                    };

            srcRootPane.ready = true;
            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                srcRootPane.setStatus("Ready.");
            }
            this.statuscontainer.remove(this.waiter);
        },

        openInFloatingWindow : function(pane, params, title) {
            var x = null;
            var y = null;

            if (params != null && (params.boundX != null || params.boundY != null)) {
                if (params.boundX != null) {
                    x = params.boundX;

                    if (params.boundY != null) { // boundX and boundY
                        y = params.boundY;
                    } else { // only boundX
                        this.nextY = this.nextY + 20;
                        if (this.nextY + 300 > this.maxY) {
                            this.nextY = this.nextYinit;
                        }
                        x = this.nextY;
                    }
                } else { // only boundY
                    y = params.boundY;

                    this.nextX = this.nextX + 20;
                    if (this.nextX + 300 > this.maxWidth) {
                        this.nextX = this.nextXinit;
                    }
                    x = this.nextY;
                }
            } else { // no boundX && no boundY
                this.nextX = this.nextX + 20;
                this.nextY = this.nextY + 20;
                if (this.nextY + 300 > this.maxHeight) {
                    this.nextY = this.nextYinit;
                    this.nextXinit = this.nextXinit + 50;
                    this.nextX = this.nextXinit;
                }

                if (this.nextX + 300 > this.maxWidth) {
                    this.nextY = this.nextYinit;
                    this.nextXinit = 50;
                    this.nextX = this.nextXinit;
                }
                x = this.nextX;
                y = this.nextY;
            }

            pane.setWindowManager(this);
            //qx.dev.Debug.debugObject(this);
            pane.initWaitingPane();

            this.statuscontainer.add(this.waiter, {edge:"east"});

            var win = new dolab.fwk.ui.window.Window(pane);
            win.ready = false;
            win.requestDone = true;

            if (pane.getSeleniumId != null) {
                this.setSeleniumId(win, pane.getSeleniumId());
            }

            var icon = pane.getIcon();
            if (icon == null) {
                icon = dolab.fwk.ui.IconManager.getIcon("application", "win");
            } else {
                icon = dolab.fwk.ui.IconManager.getIcon(icon, "win");
            }
            win.setIcon(icon);

            var caption;
            if (title != null) {
                caption = title;
            } else {
                caption = pane.getCaption();
            }
            pane.setTitle(caption);
            win.setCaption(caption);
            win.setMaxHeight(this.maxHeight - y);
            win.setMaxWidth(this.maxWidth - x);
            pane.initWindow();

            this.desktop.add(win);
            win.open();
            win.moveTo(x, y);

            pane.setRootPane(win);
            pane.initUI();
            //this.debug("!!!!! BEFORE INIT DATA maxX= " + maxX + ", maxY= " + maxY);
            pane.initData();
            //            this.debug("In the windowsManager.open()");
            //pane.checkWindowOverflood();

            this.statuscontainer.remove(this.waiter);
            win.setStatus("Ready.");
            win.ready = true;
        },

        openInDock : function(pane, title) {
            pane.setWindowManager(this);
            //qx.dev.Debug.debugObject(this);
            pane.initWaitingPane();

            this.statuscontainer.add(this.waiter, {edge:"east"});

            var page = new dolab.fwk.ui.tabview.Page(pane);
            page.setShowCloseButton(true);
            page.ready = false;
            page.requestDone = true;

            if (pane.getSeleniumId != null) {
                this.setSeleniumId(page, pane.getSeleniumId());
            }

            var icon = pane.getIcon();
            if (icon == null) {
                icon = dolab.fwk.ui.IconManager.getIcon("application", "win");
            } else {
                icon = dolab.fwk.ui.IconManager.getIcon(icon, "win");
            }
            page.setIcon(icon);

            var caption;
            if (title != null) {
                caption = title;
            } else {
                caption = pane.getCaption();
            }
            pane.setTitle(caption);
            page.setLabel(caption);
            pane.initWindow();

            this.tabview.add(page);
            this.tabview.setSelection([page]);

            pane.setRootPane(page);
            pane.initUI();
            pane.initData();
            //            this.debug("In the windowsManager.open()");

            this.statuscontainer.remove(this.waiter);
            page.ready = true;
        },

        openRaw : function(pane, title) {
            var root = qx.core.Init.getApplication().getRoot();
            var contentPane = new qx.ui.container.Composite();
            pane.setWindowManager(this);
            //qx.dev.Debug.debugObject(this);
            pane.initWaitingPane();
            root.add(contentPane, {edge: 0});
            this.ready = false;
            this.requestDone = true;

            if (pane.getSeleniumId != null) {
                /// !!! this.setSeleniumId(page, pane.getSeleniumId());
            }
            var caption;
            if (title != null) {
                caption = title;
            } else {
                caption = pane.getCaption();
            }
            pane.setTitle(caption);
            pane.initWindow();
            pane.setRootPane(contentPane);
            pane.initUI();
            pane.initData();
            //            this.debug("In the windowsManager.open()");
            this.ready = true;
        }
    }
});