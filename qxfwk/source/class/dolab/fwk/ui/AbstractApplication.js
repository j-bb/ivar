qx.Class.define("dolab.fwk.ui.AbstractApplication", {

    extend : qx.core.Object,
    type : "abstract",
    include : [dolab.fwk.ui.WindowManager],

    construct : function() {
        this.base(arguments);
        this.waiter = new qx.ui.basic.Image(dolab.fwk.ui.IconManager.getIcon("waiter", "gif"));
        this.container = new qx.ui.container.Composite(new qx.ui.layout.Dock());
    },

    events : {
        "hideLogin" : "qx.event.type.Event"
    },

    statics : {
        init : function() {
            var logger = new qx.core.Object();
            logger.debug("Entering init method in AbstractApplication");

            /* Change values in default locale */
            var localeManager = qx.locale.Manager.getInstance();
            var dateFormats = {
                cldr_date_format_short : "dd/MM/yy",
                cldr_date_format_medium : "d MMM y",
                cldr_date_format_long : "d MMMM y",
                cldr_date_format_full : "EEEE, d MMMM y"
            };
            localeManager.addLocale("C", dateFormats);
            logger.debug("User locale: " + localeManager.getLocale());
            localeManager.setLocale("C");
            logger.debug("Set locale: " + localeManager.getLocale());

            logger.debug("Leaving init method in AbstractApplication");
        }
    },

    members : {
        loginWindow : null,
        UIinitialized : false,
        container : null,
        waiter : null,
        qooXdooApplication : null,
        statuscontainer : null,
        desktop : null,
        tabview : null,
        windowManager : null,
        //        landingzonePane : null,

        computeMaxWidthHeight : function() {
            this.debug("computeMaxWidthHeight");
            //var bounds = qx.core.Init.getApplication().getRoot().getBounds();
            var bounds = this.desktop.getBounds();
            if (bounds != null) {
                this.debug("!!!!! computeMaxWidthHeight : DESKTOP BOUNDS left:" + bounds.left + " width:" + bounds.width + " top:" + bounds.top + " height:" + bounds.height);
                this.maxWidth = bounds.width;
                this.maxHeight = bounds.height;
                this.debug("!!!!! computeMaxWidthHeight : dolab.fwk.ui.AbstractApplication construct maxX= " + this.maxWidth + ", maxY= " + this.maxHeight);
            } else {
                this.debug("!!!!! computeMaxWidthHeight : DESKTOP BOUNDS IS NULL !!!!!");
                var rootBounds = qx.core.Init.getApplication().getRoot().getBounds();
                if (rootBounds != null) {
                    this.debug("!!!!! computeMaxWidthHeight : computing maxX and maxY using root bounds");
                    this.maxWidth = rootBounds.width;
                    this.maxHeight = rootBounds.height - 55; // -35 for toolbar and -15 for status bar
                    this.debug("!!!!! computeMaxWidthHeight : dolab.fwk.ui.AbstractApplication construct maxX= " + this.maxWidth + ", maxY= " + this.maxHeight);
                } else {
                    this.debug("!!!!! computeMaxWidthHeight : ROOT BOUNDS IS NULL !!!!! Cannot compute maxX and maxY");
                }
            }
        },

        showLogin : function() {
            if (this.container != null) {
                this.hideApplicationContainer();
            }

            if (this.loginWindow == null) {
                this.loginWindow = new dolab.fwk.ui.LoginWindow(this.doGetApplicationName());
            }
            qx.core.Init.getApplication().getRoot().add(this.loginWindow);
            this.loginWindow.open();
            this.loginWindow.focusUserField();
        },

        /**
         * Call this method with reInit = true if the user has changed beetween 2 logins.
         * If the user has changed, roles might have changed too, so we need to reconstruct the application menus for the new roles
         * @param reInit (boolean) If true, closes all windows (AbtractPanes) and reconstructs the application UI, else just displays what was previously here
         */
        hideLogin : function(reInit) {
            this.debug("hide login");
            if (this.loginWindow != null) {
                qx.core.Init.getApplication().getRoot().remove(this.loginWindow);
            }

            if (reInit || !this.UIinitialized) {
                // TODO: dispose previous objects of the abtractapplication instance
                // (this.container, this.desktop, this.windowManager, AbstractPanes, etc).
                var preferenceManager = this.doGetPreferenceManager();
                preferenceManager.initPreference(dolab.fwk.auth.AuthManager.getInstance().getUser(),
                        function() {
                            dolab.fwk.ui.WindowManager.PANES_OPENING_MODE = preferenceManager.getPreference("panes_opening_mode");
                            this.init();
                        }, this);
            }

            //TODO : WARNING potential bug due to // : what if preferencesManager come back too late ? addApplicationContainerToRoot() will be called without preferences initialized correctly. 

            if (this.container != null) {
                this.addApplicationContainerToRoot();
            }

            this.fireEvent("hideLogin");
        },

        doGetPreferenceManager : function() {
            throw new Error("Method doGetPreferenceManager is abstract in dolab.fwk.ui.AbstractApplication.");
        },

        // Init UI for the application: menus, background, abtractpanes desktop, etc
        // These are then added to the root.
        init: function() {
            this.debug("The decimal separator is \'" + qx.locale.Number.getDecimalSeparator() + "\'");
            var qooXdooApplication = qx.core.Init.getApplication();
            this.UIinitialized = true;
            var root = qx.core.Init.getApplication().getRoot();
            root.setNativeContextMenu(false);
            //this.container = new qx.ui.container.Composite(new qx.ui.layout.Dock());

            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "dock") {
                this.initContainerForDock();
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                this.initContainerForFloatingWindows();
            } else if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "raw") {
                this.debug("RAW mode");
            } else {
                throw new Error(dolab.fwk.ui.WindowManager.PANES_OPENING_MODE + " is not a valid opening mode for panes. Value of dolab.fwk.ui.WindowManager.PANES_OPENING_MODE must be \"floating\" or \"dock\"");
            }

            this.statuscontainer = new qx.ui.container.Composite(new qx.ui.layout.Dock()).set({backgroundColor: "#8CCBFF"});

            var atom = new qx.ui.basic.Atom(this.doGetApplicationName() + " - copyright Digital Orbiter - Compiled on \"" + this.getCompileTimeTrace()[0] + "\" - composer version : " + dolab.fwk.auth.AuthManager.getInstance().getVersion());
            this.statuscontainer.add(atom, {edge: "west"});

            var userInfo = new qx.ui.basic.Atom("You are logged as " + dolab.fwk.auth.AuthManager.getInstance().getUser() + ".");
            var roles = dolab.fwk.auth.AuthManager.getInstance().getRoles();
            var tooltipText = null;
            if (roles != null && roles.length > 0) {
                tooltipText = "Your roles are: " + roles + ".";
            } else {
                tooltipText = "You have no role.";
            }
            userInfo.setToolTip(new qx.ui.tooltip.ToolTip(tooltipText));
            this.statuscontainer.add(userInfo, {edge: "east"});

            this.container.add(this.statuscontainer, {edge: "south"});

            //var menuDeco = new qx.ui.decoration.Background("#4796FF");

            var menu = new qx.ui.toolbar.ToolBar();
            menu.setAppearance("toolbar-dolab");
            //menu.setDecorator(menuDeco);
            //menu.getAppearance().getDecorator().setBackgroundImage("decoration/toolbar/toolbar-gradient-blue.png");

            var menuIconButton = new qx.ui.toolbar.Button("", dolab.fwk.ui.IconManager.getIcon(this.doGetMenuIcon(), "png"));
            menuIconButton.setAppearance("toolbar-dolab-button");
            menuIconButton.addListener("execute",
                    function() {
                        this.doGetMenuIconFunction();
                    }
                    , this);
            menu.add(menuIconButton);

            this.addApplicationMenu(menu);
            this.doAddMenu(menu);
            this.container.add(menu, {edge: "north"});
            //            this.container.add(new qx.ui.basic.Image(this.doGetCentralReportURL()), {edge: "north"});

            root.add(this.container, {edge: 0});

            if (dolab.fwk.ui.WindowManager.PANES_OPENING_MODE == "floating") {
                this.computeMaxWidthHeight();
                this.desktop.addListener("resize", this.computeMaxWidthHeight, this);
            }

            this.doAfterInit();

            //            this.pollServerTimerID = qx.util.TimerManager.getInstance().start(this.keepAlive, 5000, this, null, 3000);
        },

        initContainerForDock : function() {
            this.tabview = new qx.ui.tabview.TabView("bottom");

            this.container.add(this.tabview, {edge: "center"});
        },

        initContainerForFloatingWindows : function() {
            this.windowManager = new qx.ui.window.Manager();
            this.desktop = new qx.ui.window.Desktop(this.windowManager);

            var desktopDeco = new qx.ui.decoration.MBackgroundColor("#F3F3F3");
            desktopDeco.setBackgroundImage(dolab.fwk.ui.IconManager.getIcon("background", "png"));
            this.desktop.setDecorator(desktopDeco);

            this.container.add(this.desktop, {edge: "center"});
        },

        doGetMenuIcon : function() {
            throw new Error("abstract method call. doGetMenuIcon() must be redefined.");
        },

        doGetMenuIconFunction : function() {
            throw new Error("abstract method call. doGetMenuIcon() must be redefined.");
        },

        doGetCentralReportURL : function() {
            throw new Error("abstract method call. doGetCentralReportURL() must be redefined.");
        },

        doGetApplicationName : function() {
            throw new Error("abstract method call. doGetApplicationName() must be redefined.");
        },

        doAfterInit : function() {
            throw new Error("abstract method call. doAfterInit() must be redefined.");
        },

        getCompileTimeTrace : function() {
            return ["Information not set"];
        },

        keepAlive : function(userData, timerId) {
            if (this.pollServerTimerID == timerId) {
                this.pollServer();
            }
        },

        pollServer : function() {
            this.debug("[pollServer !]");
            this.doPollServer();
        },

        doPollServer : function() {
        },

        addApplicationContainerToRoot : function() {
            qx.core.Init.getApplication().getRoot().add(this.container, {edge: 0});
            //            this.container.setEnabled(true);
            //            this.container.setVisibility("visible");
        },

        hideApplicationContainer : function() {
            //            this.container.setEnabled(true);
            //            this.container.setVisibility("excluded");
            var rootContainer = qx.core.Init.getApplication().getRoot();
            if (this.container != null && rootContainer.indexOf(this.container) > 0) {
                rootContainer.remove(this.container);
            }
        },

        menuFunction : function() {
            window.open("http://www.digitalorbiter.com?targapp=" + this.doGetApplicationKeyName() + "&name=" + this.doGetApplicationName(), "_blank");
        },

        doAboutMenuFunction : function() {
            var dialog = new dolab.fwk.ui.Dialog();

            dialog.open(null, "About " + this.doGetApplicationName(), this.getCompileTimeTrace(), null, [
                {
                    lbl:"Ok",
                    ctx:this,
                    keyLstnr:dialog,
                    key:"Enter"
                }
            ]);
        },

        addApplicationMenu : function(menu) {
            var applicationMenu = new qx.ui.menu.Menu();
            applicationMenu.setAppearance("menu-dolab");

            var openPrefButton = new qx.ui.menu.Button("User preference", "");
            openPrefButton.seleniumId = "openPref";
            openPrefButton.setAppearance("menu-dolab-button");
            openPrefButton.addListener("execute",
                    function() {
                        this.openWin(new dolab.fwk.ui.PreferenceWindow);
                    }, this);
            applicationMenu.add(openPrefButton);

            var aboutButton = new qx.ui.menu.Button("About " + this.doGetApplicationName(), "", null);
            aboutButton.setAppearance("menu-dolab-button");
            aboutButton.addListener("execute", this.doAboutMenuFunction, this);
            applicationMenu.add(aboutButton);

            var separator = new qx.ui.menu.Separator();
            separator.setAppearance("menu-dolab-separator");
            applicationMenu.add(separator);

            var aboutButton = new qx.ui.menu.Button("About", "", null);
            aboutButton.setAppearance("menu-dolab-button");
            aboutButton.addListener("execute", this.menuFunction, this);
            applicationMenu.add(aboutButton);

            var applicationButtonMenu = new qx.ui.toolbar.MenuButton(this.doGetApplicationName());
            applicationButtonMenu.setAppearance("toolbar-dolab-menubutton");
            applicationButtonMenu.setMenu(applicationMenu);
            menu.add(applicationButtonMenu);


            this.doAddApplicationMenu(applicationMenu);
        },

        doAddApplicationMenu : function(applicationMenu) {
        },

        doAddMenu : function(menu) {
            throw new Error("abstract method call. doAddMenu() must be redefined.");
        },

        newLandingZonePaneAction : function() {
            //            if (this.landingZonePane == null) {
            //                this.landingZonePane = this.doGetNewLandingZonePane();
            //                this.openWin(this.landingZonePane);
            //
            //                this.landingZonePane.getWin().addListenerOnce("close",
            //                        function() {
            //                            this.landingZonePane = null;
            //                        }, this);
            //            } else {
            //                this.windowManager.bringToFront(this.landingZonePane.getWin());
            //            }
        },

        doGetNewLandingZonePane : function() {
            throw new Error("abstract method call. doGetNewLandingZonePane() must be redefined.");
        }
    }
});