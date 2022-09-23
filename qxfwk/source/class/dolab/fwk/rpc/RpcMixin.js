/**
 * Created with IntelliJ IDEA.
 * User: Loïc Bresson (Pandore)
 * Date: Nov 16, 2009
 * Time: 11:21:02 AM
 */

qx.Mixin.define("dolab.fwk.rpc.RpcMixin", {

    // /!\ Important: the object that includes this mixin must extend qx.ui.core.Object (in order to be able to fire events)

    statics : {
        DEBUG : true,
        TIMEOUT : 500000
    },

    events : {
        "startRequest" : "qx.event.type.Data",
        "endRequest" : "qx.event.type.Data",
        "requestFailed" : "qx.event.type.Data"
    },

    members : {
        initRequest : null,

        setInitRequest : function(request) {
            this.initRequest = request;
        },

        addInitRequestParam : function(params) {
            if (this.initRequest.p == null || this.initRequest.p.length == 0) {
                this.initRequest.p = params.p;
            } else {
                for (var i = 0; i < params.p.length; i++) {
                    this.initRequest.p.push(params.p[i]);
                }
            }
        },

        send : function(message, request, whatToDo) {
            this._send(message, request.svc, request.m, request.p, whatToDo);
        },

        sendWithTimeout : function(message, request, whatToDo, timeout) {
            this._internalSend(message, request.svc, request.m, request.p, whatToDo, ".qxrpc", timeout);
        },

        _send : function(message, service, method, params, whatToDo) {
            this._internalSend(message, service, method, params, whatToDo, ".qxrpc", dolab.fwk.rpc.RpcMixin.TIMEOUT);
        },

        _internalSend : function(message, service, method, params, whatToDo, rpcUrl, timeout) {
            this.fireDataEvent("startRequest", message);

            if (service != null && method != null) {
                this.debug("OK, we'll handle it, service is '" + service + "', method is '" + method + "', params are '" + params);

                if (this.publicKey != null) {
                    this.debug("Will send RPC in RAW mode");
                    rpcUrl = rpcUrl + "?screen=" + this.publicKey + "&svc=" + service + "&method=" + method;
                }

                this.debug("=> RPC URL = "+rpcUrl);
                var rpc = new qx.io.remote.Rpc(rpcUrl, service);
                rpc.setTimeout(timeout);

                var that = this;
                var handler = function(result, exc) {
                    if (exc == null) { // Case OK
                        params.shift();
                        params.shift();
                        that.debug("Request completed : " + service + "." + method + "(" + params + ")");
                        if (dolab.fwk.rpc.RpcMixin.DEBUG) {
                            that.debug("Begin resp dump -------------------");
                            /*
                             * TODO try to improve formating in case of debug (or not) ...
                             qx.util.Json.BEAUTIFYING_INDENT = "\t";
                             qx.util.Json.BEAUTIFYING_LINE_END = "\n";
                             */
                            that.debug(result);
                            that.debug("End resp dump -------------------");
                        }

                        if (result && result.authError) {
                            that.warn("Authentification error : please login again!");
                            that.fireRequestFailed(service, method, params, whatToDo, result.authError, message, true);
                            that.windowManager.showLogin();
                        } else {
                            that.requestExecution(result, whatToDo);
                        }
                    } else {
                        var theUsedHandler = params.shift();
                        var theUsedMethod = params.shift();
                        that.debug("Request failed : " + service + "." + method + "(" + params + ") : " + exc);
                        if (dolab.fwk.rpc.RpcMixin.DEBUG) {
                            that.debug("Begin resp dump -------------------");
                            /*
                             * TODO try to improve formating in case of debug (or not) ...
                             qx.util.Json.BEAUTIFYING_INDENT = "\t";
                             qx.util.Json.BEAUTIFYING_LINE_END = "\n";
                             */
                            that.debug(result);
                            that.debug("End resp dump -------------------");
                        }

                        that.fireRequestFailed(service, method, params, whatToDo, exc, message, false);
                    }
                };
                //                rpc.callAsync(handler, method, params);
                if (params != null && params.length > 0) {
                    params.unshift(handler, method);
                } else {
                    params = [handler, method];
                }
                //                for (var i = 0; i < params.length; i++) {
                //                    this.debug("params[" + i + "]= " + params[i]);
                //                }
                qx.lang.Json.CONVERT_DATES = true;
                rpc.callAsync.apply(rpc, params);
                qx.lang.Json.CONVERT_DATES = false;
            } else {
                if (service == null) {
                    this.error("RpcMixin._send : service is null !");
                }
                if (method == null) {
                    this.error("RpcMixin._send : method is null !");
                }
                this.debug("Ko. Service is " + service + ", method is " + method + ", params are " + params);
                this.requestExecution(null, whatToDo);
            }
        },

        requestExecution : function(data, whatToDo) {
            if (data != null && data.securityError != null) {
                this.debug("SECURITY ERROR: please login.");
                qx.core.Init.getApplication().showLogin();
            } else if (whatToDo != null) {
                whatToDo.call(this, data);
            }

            this.fireDataEvent("endRequest", null);
        },

        fireRequestFailed : function(service, method, params, whatToDo, exception, message, relogin) {
            if (this.waitingOff) {
                this.waitingOff();
            }
            var requestObj = new Object();
            requestObj.service = service;
            requestObj.method = method;
            requestObj.params = params;
            requestObj.whatToDo = whatToDo;
            requestObj.exception = exception;
            requestObj.message = message;
            requestObj.reLogin = relogin;
            this.fireDataEvent("requestFailed", requestObj);
        }
    }
});