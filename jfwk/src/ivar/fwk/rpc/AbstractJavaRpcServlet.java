/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.fwk.rpc;

import ivar.common.logger.FullLogger;
import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.fwk.jpa.EntityManagerFactory;
import ivar.fwk.util.ApplicationDirtyConfig;
import ivar.fwk.util.ServletHelper;
import ivar.helper.StopWatch;
import ivar.helper.StringHelper;
import ivar.helper.io.IOHelperException;
import ivar.helper.oo.ClassHelper;
import java.rmi.RemoteException;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import lib.rpc.JsonRpcServlet;
import lib.rpc.RpcException;
import lib.serialization.JavaSerializer;

public abstract class AbstractJavaRpcServlet extends JsonRpcServlet {

    private static final List<String> EXCLUDE_FROM_SERIALIZATION = new ArrayList<String>() {
        {
            add("pcInheritedFieldCount");
            add("pcFieldNames");
            add("pcFieldTypes");
            add("pcFieldFlags");
            add("pcPCSuperclass");
            add("pcVersionInit");
            add("serialVersionUID");
            add("READ_WRITE_OK");
            add("LOAD_REQUIRED");
            add("READ_OK");
            add("CHECK_READ");
            add("MEDIATE_READ");
            add("CHECK_WRITE");
            add("MEDIATE_WRITE");
            add("SERIALIZABLE");
            add("DESERIALIZED");
        }
    };
    private static final JavaSerializer JAVA_SERIALIZER = new JavaSerializer(EXCLUDE_FROM_SERIALIZATION, "^class\\$L.*", null, null);

    private String servletName;
    private String differentiator;

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        final StopWatch sw = new StopWatch();
        sw.start();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        servletName = servletConfig.getServletName();
        log.info("Houston, we're in the servlet '" + servletName + "' init() method, please wait ...");
        super.init(servletConfig);
        log.beginBlock("JPA getting the Entity manager ...");
        EntityManagerFactory.getInstance();
        log.endBlock();
        sw.stop();
        log.info(sw.getDeltaTimeTrace());

        // Init param
        log.emptyLine();
        log.beginBlock("Parsing servlet init param ...");
        final Enumeration<String> paramNames = getInitParameterNames();
        final ApplicationDirtyConfig config = ApplicationDirtyConfig.getInstance();
        while (paramNames.hasMoreElements()) {
            final String elem = paramNames.nextElement();
            final String value = getInitParameter(elem);
            config.put(elem, value);
            if ("uploads-files-dir".equals(elem)) {
                log.beginBlock("Setting up upload dir " + value);
                try {
                    config.initUploadFolders(value);
                } catch (IOHelperException e) {
                    throw new ServletException(e);
                }
                log.endBlock("Setting up upload dir. Done.");
            } else if ("fusion-files-dir".equals(elem)) {
                log.beginBlock("Setting up fusion file dir " + value);
                try {
                    config.initFusionFolders(value);
                } catch (IOHelperException e) {
                    throw new ServletException(e);
                }
                log.endBlock("Setting up fusion file dir. Done.");
            } else if ("differentiator".equals(elem)) {
                differentiator = value;
                if (differentiator != null) {
                    log.info("Found a differentiator : " + differentiator + "on application " + getApplicationName());
                } else {
                    log.info("No differentiator found on application " + getApplicationName());
                }
            } else {
                log.warning("Init param ignored " + elem + " : " + value);
            }
        }
        log.endBlock("Parsing servlet init param. Done.");

        log.emptyLine();
        log.info("OK, Houston, '" + servletName + "' ready for the mission !");
        log.emptyLine();
    }

    protected abstract String getApplicationName();

    @Override
    protected String handleRPC(final HttpServletRequest request, final String requestString) throws ServletException {
        final StopWatch sw = new StopWatch();
        sw.start();
        final FullLogger log = LoggerDefaultImplementation.getInstance();
        log.reset(System.out);
        final String clientTraceString = request.getRemoteAddr();
        log.setClientTraceString(clientTraceString);
        log.setApplicationKeyname((differentiator != null ? differentiator + "_" : "") + getApplicationName());

        // Only for debugging the block/endblock appa
        int tabValueForControl = log.getTabValue();
        final int sizeOfRequestToLog = Math.min(120, requestString.length());
        boolean isCut = requestString.length() > sizeOfRequestToLog;
        final String requestForLog = requestString.substring(0, sizeOfRequestToLog - 1) + (isCut ? "... [CUT] : " : " : ") + request.getRequestURI();

        log.beginBlock("[HandleRPC] " + requestForLog);
        log.info("[HandleRPC] " + ServletHelper.getUrl3(request));

        log.beginBlock("Creating a new ControllerContext for " + requestForLog);
        ControllerContext cc = null;
        try {
            cc = new ControllerContext();
        } catch (Throwable t) {
            final String message = "[HandleRPC] error during ControlerContext creation";
            log.error(message, t, true);
            throw new ServletException(message, t);
        }
        log.endBlock("Creating a new ControllerContext : done.");

        String result = null;
        try {
            final Map<String, Object> requestMap = extractRequestMap(requestString);

            Map<String, Object> responseIntermediateObject;

            try {
                beforeCallService(request, requestMap);
                final Object methodResult = executeRequest(requestMap, cc);
                responseIntermediateObject = buildResponse(requestMap, methodResult, cc.getAllFields());
            } catch (RpcException e) {
                log.error("[HandleRPC] RPCException ", e);
                responseIntermediateObject = buildResponse(requestMap, e);
            } catch (RemoteException e) {
                log.error("[HandleRPC] RemoteException ", e, true);
                responseIntermediateObject = buildResponse(requestMap, e);
            }

            try {
                result = jsonSerializer.serialize(responseIntermediateObject);
            } catch (Throwable t) {
                final String message = "[HandleRPC] serialization error " + ClassHelper.getShortName(t.getClass().getName());
                log.error(message, t, true);
                throw new ServletException(t);
            }
        } finally {
            cc.close();
            sw.stop();
            log.info(sw.getDeltaTimeTrace());
            log.endBlock("[handleRPC] " + requestForLog);
        }

        if (tabValueForControl != log.getTabValue()) {
            log.warning("[DEV] : Houston, we noticed a beginBlock()/endBlock() control issue (waiting for " + tabValueForControl + " but found " + log.getTabValue() + "). Check for some missing endBlock(). Don't worry if you see that, the server it 100% OK.");
        }

        return result;
    }

    @Override
    protected void doBeforeCallService(final HttpServletRequest httpRequest, final String serviceName, final String methodName, final List<Object> methodParams) throws RpcException {
        final Logger log = LoggerDefaultImplementation.getInstance();
        final String url = httpRequest.getQueryString();
        log.beginBlock("RPC security check " + url);

        final String params = url;
        final int numParams = StringHelper.count(params, '&') + 1;
        final StringTokenizer st = new StringTokenizer(params, "&", false);
        boolean serviceChecked = false;
        boolean methodChecked = false;
        for (int i = 1; i <= numParams; i++) {
            final String param = st.nextToken();
            final String key = StringHelper.getLeft(param, "=");
            final String value = StringHelper.getRight(param, "=");
            log.debug("RPC security check param " + 1 + " -> " + key + " : " + value);
            if ("svc".equals(key)) {
                serviceChecked = true;
                if (!serviceName.equals(value)) {
                    throw new RpcException(-1, 406, "RPC security check failed : service name differs" + serviceName + " != " + value);
                } else {
                    log.debug("RPC security check. Service OK " + value);
                }
            }
            if ("method".equals(key)) {
                methodChecked = true;
                if (!methodName.equals(value)) {
                    throw new RpcException(-1, 406, "RPC security check failed : method name differs" + methodName + " != " + value);
                }
            } else {
                log.debug("RPC security check. Method OK " + value);
            }
        }
        if (!methodChecked && !serviceChecked) {
            log.debug("RPC security check : no param.");
        } else {
            if (!methodChecked || !serviceChecked) {
                throw new RpcException(-1, 406, "RPC security check failed : method and/or service not checked. methodChecked =" + methodChecked + ", serviceChecked=" + serviceChecked);
            }
        }
        log.endBlock("RPC security check");
    }

    @Override
    protected JavaSerializer doGetJavaSerializer() {
        return JAVA_SERIALIZER;
    }

    @Override
    public void destroy() {
        super.destroy();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        log.info("Houston, we're in the servlet '" + servletName + "' destroy() method, please wait ...");
        log.info("OK, Houston, '" + servletName + "' landed. End of the the mission !");
        log.emptyLine();
    }
}
