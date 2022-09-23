qx.Class.define("dolab.fwk.ui.LoginWindow", {

    extend : qx.ui.window.Window,
    include : [dolab.fwk.rpc.RpcMixin],

    events : {
        "loginDone" : "qx.event.type.Data"
    },

    construct: function(applicationName) {
        this.applicationName = applicationName;
        this.base(arguments);

        this.doInitUI();
        this.addListener("requestFailed", this.ackRequestFailed);
        this.errorLabel.addListener("appear", this.initErrorFade, this);
    },

    members: {
        login : null,
        password : null,
        errorLabel : null,
        applicationName : null,

        doInitUI : function() {
            this.setModal(true);

            this.setShowClose(false);
            this.setShowMaximize(false);
            this.setShowMinimize(false);


            this.setAllowClose(false);
            this.setAllowMaximize(false);
            this.setAllowMinimize(false);
            this.setResizable(false);

            this.setCaption("Please login");
            // Warning IconManager must be there before first login
            this.setIcon(dolab.fwk.ui.IconManager.getIcon("login", "win"));

            this.seleniumId = "loginWindow";

            this.addListenerOnce("appear", function() {
                this.center();
            }, this);

            this.setLayout(new qx.ui.layout.Dock(7, 7));

            var buttonPane = dolab.fwk.ui.AbstractPane.getNewButtonsPanel([
                {
                    lbl:"Login",
                    cde:this.loginAction,
                    ctx:this,
                    keyLstnr:this,
                    key:"Enter",
                    seleniumId: "loginButton",
                    tooltip: "Login into " + this.applicationName
                }
            ]);

            this.errorLabel = new qx.ui.basic.Label("Bad login or password.");
            this.errorLabel.setTextColor("#ff0000");
            this.add(this.errorLabel, {edge: "north"});
            this.errorLabel.setVisibility("excluded");

            var form = new qx.ui.container.Composite(new qx.ui.layout.Grid(5, 5));

            form.add(new qx.ui.basic.Label("User: "), {row:0, column:0});
            this.login = new qx.ui.form.TextField();
            this.login.seleniumId = "login";
            form.add(this.login, {row:0, column:1});

            form.add(new qx.ui.basic.Label("Password: "), {row:1, column:0});
            this.password = new qx.ui.form.PasswordField();
            this.password.seleniumId = "password";
            form.add(this.password, {row:1, column:1});

            this.add(form, {edge: "center"});
            this.add(buttonPane, {edge: "south"});
        },

        focusUserField: function() {
            this.login.focus();
        },

        loginAction : function() {
            // TODO take only the first xx char AND replace the new value in the field
            if (this.login.getValue() == null || this.login.getValue() == "") {
                this.login.focus();
            } else if (this.password.getValue() == null || this.password.getValue() == "") {
                this.password.focus();
            } else {
                this.setEnabled(false);
                var hp = dolab.fwk.helper.StringHelper.SHA1(this.password.getValue());
                this._internalSend("Logging in ...", "Login", "login", [this.login.getValue(), hp], this.afterLoginRequest, ".auth", 60000);
            }
        },

        afterLoginRequest : function(data) {
            this.password.setValue("");
            this.setEnabled(true);
            if (data != null && data.logged == true) {
                var oldUser = dolab.fwk.auth.AuthManager.getInstance().getUser();
                dolab.fwk.auth.AuthManager.getInstance().setRoles(data.roles);
                dolab.fwk.auth.AuthManager.getInstance().setUser(this.login.getValue());
                dolab.fwk.auth.AuthManager.getInstance().setVersion(data.composerVersion);
                this.login.setValue("");
                this.errorLabel.setVisibility("excluded");
                // If the user has changed, we need to reconstruct the UI for the roles of the new user
                qx.core.Init.getApplication().hideLogin(oldUser != dolab.fwk.auth.AuthManager.getInstance().getUser());
                oldUser = "";
                dolab.fwk.helper.RuntimeVariableHelper.getInstance().set("currentuser", data.user);
                this.fireDataEvent("loginDone", null);
            } else {
                this.handleError("Bad login or password.");
            }
        },

        ackRequestFailed : function(e) {
            this.handleError(e.getData().exception.toString().substr(0, 70));
        },

        handleError : function(errLabelValue) {
            if (errLabelValue != null) {
                this.errorLabel.setValue(errLabelValue);
            }
            this.login.setValue("");
            this.password.setValue("");
            this.errorLabel.setVisibility("visible");
//            var shakeEffect = new qx.fx.effect.combination.Shake(this.getContainerElement().getDomElement());
//            shakeEffect.addListenerOnce("finish",
//                    function() {
//                        shakeEffect.resetState();
//                        shakeEffect.cancel();
//                        shakeEffect = null;
                        this.setEnabled(true);
                        this.login.focus();
//                    }, this);
//            shakeEffect.start();
        },

        initErrorFade : function() {
//            var fadeEffect = new qx.fx.effect.core.Fade(this.errorLabel.getContainerElement().getDomElement());
//            fadeEffect.setFrom(1.0);
//            fadeEffect.setTo(0.1);
//            fadeEffect.setDelay(2.0);
//            fadeEffect.setDuration(4.0);
//            fadeEffect.addListenerOnce("finish", function() {
//                this.reinitFadeLevel(this.errorLabel);
//            }, this);
//            fadeEffect.start();
        },

        reinitFadeLevel : function (w) {
//            var fadeEffect = new qx.fx.effect.core.Fade(w.getContainerElement().getDomElement());
//            fadeEffect.setFrom(0);
//            fadeEffect.setTo(1);
//            fadeEffect.setDuration(0.001);
//            fadeEffect.setDelay(0);
//            fadeEffect.addListener("finish", function() {
                w.setVisibility("excluded");
//            });
//            fadeEffect.start();
        }
    }
});