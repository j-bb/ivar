/*
    Java JSON RPC
    RPC Java POJO

    This library is dual-licensed under the GNU Lesser General Public License (LGPL) and the Eclipse Public License (EPL).
    Check http://qooxdoo.org/license

    This library is also licensed under the Apache license.
    Check http://www.apache.org/licenses/LICENSE-2.0

    Contribution:
    This contribution is provided by Jean-Baptiste Briaud
 */

package lib.rpc;

import lib.serialization.JSONSerializer;
import lib.serialization.JavaSerializer;
import lib.serialization.SerializationException;
import org.json.JSONException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRpcServlet extends HttpServlet {
    private static final String DOPOST_RESPONSE_CONTENTTYPE = "text/plain; charset=UTF-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String DOPOST_REQUEST_CONTENTTYPE = "application/json";
    private static final String DOGET_RESPONSE_CONTENTTYPE = "text/javascript; charset=UTF-8";
    private static final String SCRIPT_TRANSPORT_ID = "_ScriptTransport_id";
    private static final String SCRIPT_TRANSPORT_DATA = "_ScriptTransport_data";

    private static final int READ_BUFFER_SIZE = 8192;

    /*
    * Error codes
    */
    // origin of error
    protected static final int ERROR_FROM_SERVER = 1;
    protected static final int ERROR_FROM_METHOD = 2;

    // error codes for errors that come from server (origin = 1 = ERROR_FROM_SERVER)
    protected static final int ILLEGAL_SERVICE = 1;
    protected static final int SERVICE_NOT_FOUND = 2;
    protected static final int METHOD_NOT_FOUND = 4;
    protected static final int PARAMETER_MISMATCH = 5;
    protected static final int PERMISSION_DENIED = 6;
    protected static final int INTERNAL_SERVER_ERROR = 500;

    protected static final String ACCESS_DENIED_RESULT = "alert('Access denied. Please make sure that your browser sends correct referer headers.')";


    /**
     * The referrer checking method used for RPC calls.
     */
    protected static int referrerCheck;

    protected static final int REFERRER_CHECK_STRICT = 0;
    protected static final int REFERRER_CHECK_DOMAIN = 1;
    protected static final int REFERRER_CHECK_SESSION = 2;
    protected static final int REFERRER_CHECK_PUBLIC = 3;
    protected static final int REFERRER_CHECK_FAIL = 4;

    protected static final String SESSION_REFERRER_KEY = "_qooxdoo_rpc_referrer";


    protected JSONSerializer jsonSerializer = null;
    protected JavaSerializer javaSerializer = null;
    protected RemoteCallUtils remoteCallUtils = null;
    private static final String UTF_8 = "UTF-8";

    /**
     * Initializes this servlet.
     *
     * @param config the servlet config.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.jsonSerializer = doGetJsonSerializer();
        this.javaSerializer = doGetJavaSerializer();
        this.remoteCallUtils = new RemoteCallUtils(javaSerializer);

        final String referrerCheckName = config.getInitParameter("referrerCheck");
        if ("strict".equals(referrerCheckName)) {
            referrerCheck = REFERRER_CHECK_STRICT;
        } else if ("domain".equals(referrerCheckName)) {
            referrerCheck = REFERRER_CHECK_DOMAIN;
        } else if ("session".equals(referrerCheckName)) {
            referrerCheck = REFERRER_CHECK_SESSION;
        } else if ("public".equals(referrerCheckName)) {
            referrerCheck = REFERRER_CHECK_PUBLIC;
        } else if ("fail".equals(referrerCheckName)) {
            referrerCheck = REFERRER_CHECK_FAIL;
        } else {
            referrerCheck = REFERRER_CHECK_STRICT;
            log("No referrer checking configuration found. Using strict checking as the default.");
        }
    }

    protected JSONSerializer doGetJsonSerializer() {
        return new JSONSerializer();
    }

    protected JavaSerializer doGetJavaSerializer() {
        return new JavaSerializer();
    }

    /**
     * Possibility to modify here the map produced from the JSON request.
     * The modification must be done in the same map instance since it's the one that will be used.
     *
     * @param request
     */
    protected void doModifyRequestMap(Map<String, Object> request) {
        // Empty. Override this method if necessary.
    }

    /**
     * Possibility to modify here the result of the invoked method.
     * The modification must be done in the same instance since it's the one that will be used.
     *
     * @param result The result of the invoked method, that has been prepared for JSON serialization
     *               result can be :
     *               - a Map
     *               - a List
     *               - a String
     *               - a primitive wrapped into an object (Integer, Boolean, Character, etc.)
     *               - null
     *               Only Map and List results should be modified
     */
    protected void doModifyMethodResult(Object result) {
        // Empty. Override this method if necessary.
    }

    protected String handleRPC(final HttpServletRequest httpRequest, final String requestString) throws ServletException {
        return doHandleRPC(httpRequest, requestString);
    }

    protected String doHandleRPC(final HttpServletRequest httpRequest, final String requestString, final Object... extraParams) throws ServletException {
        final Map<String, Object> requestMap = extractRequestMap(requestString);

        Map<String, Object> responseIntermediateObject;
        try {
            beforeCallService(httpRequest, requestMap);
            final Object methodResult = executeRequest(requestMap, extraParams);
            responseIntermediateObject = buildResponse(requestMap, methodResult, null);
        } catch (RpcException e) {
            responseIntermediateObject = buildResponse(requestMap, e);
        } catch (RemoteException e) {
            responseIntermediateObject = buildResponse(requestMap, e);
        }
        // TODO: what does happen in case of other exception?

        return jsonSerializer.serialize(responseIntermediateObject);
    }

    protected final void beforeCallService(final HttpServletRequest httpRequest, final Map<String, Object> requestMap) throws RpcException {
        final String serviceName = (String) requestMap.get("service");
        final String methodName = (String) requestMap.get("method");
        final List<Object> methodParams = (List<Object>) requestMap.get("params");

        doBeforeCallService(httpRequest, serviceName, methodName, methodParams);
    }

    protected void doBeforeCallService(final HttpServletRequest httpRequest, final String serviceName, final String methodName, final List<Object> methodParams) throws RpcException {
    }

    protected String getRequestString(final HttpServletRequest request) throws IOException {
        String requestString = null;

        InputStream is = null;
        Reader reader = null;

        try {
            is = request.getInputStream();
            reader = new InputStreamReader(is, UTF_8);

            final StringBuilder sb = new StringBuilder(READ_BUFFER_SIZE);
            char[] readBuffer = new char[READ_BUFFER_SIZE];
            int length;
            while ((length = reader.read(readBuffer)) != -1) {
                sb.append(readBuffer, 0, length);
            }
            requestString = sb.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Nothing we can do
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Nothing we can do
                }
            }
        }

        return requestString;
    }

    protected Map<String, Object> extractRequestMap(final String requestString) throws ServletException {
        Object requestIntermediateObject;
        try {
            requestIntermediateObject = jsonSerializer.unserialize(requestString);
        } catch (JSONException e) {
            throw new ServletException("Unable to read request", e);
        }

        final Map<String, Object> requestMap = (Map<String, Object>) requestIntermediateObject;
        doModifyRequestMap(requestMap);
        return requestMap;
    }

    protected Object executeRequest(final Map<String, Object> requestIntermediateObject, final Object... extraArgs) throws RpcException, RemoteException {
        final String serviceName = (String) requestIntermediateObject.get("service");
        final String methodName = (String) requestIntermediateObject.get("method");
        final List<Object> methodParams = (List<Object>) requestIntermediateObject.get("params");

        return callProcedure(serviceName, methodName, methodParams, extraArgs);
    }

    protected Object callProcedure(final String service, final String method, final List<Object> args, final Object... extraArgs) throws RpcException, RemoteException {
        Remote serviceInstance;
        try {
            serviceInstance = remoteCallUtils.getServiceInstance(service, null);
        } catch (ClassNotFoundException e) {
            throw new RpcException(ERROR_FROM_SERVER, SERVICE_NOT_FOUND, "Service " + service + " not found", e);
        } catch (IllegalAccessException e) {
            throw new RpcException(ERROR_FROM_SERVER, ILLEGAL_SERVICE, "Illegal service " + service, e);
        } catch (InstantiationException e) {
            throw new RpcException(ERROR_FROM_SERVER, null, "Could not load service " + service, e);
        } catch (ClassCastException e) {
            throw new RpcException(ERROR_FROM_SERVER, null, "Could not load service " + service, e);
        }

        Object methodResult;
        try {
            methodResult = remoteCallUtils.callCompatibleMethod(serviceInstance, method, args, extraArgs);
        } catch (NoSuchMethodException e) {
            throw new RpcException(ERROR_FROM_SERVER, METHOD_NOT_FOUND, "Method " + method + " not found", e);
        } catch (IllegalAccessException e) {
            throw new RpcException(ERROR_FROM_SERVER, METHOD_NOT_FOUND, "Method " + method + " not found", e);
        } catch (InvocationTargetException e) {
            throw new RpcException(ERROR_FROM_METHOD, null, "An exception was raised by the call to method " + method + "(...)", e);
        }

        return methodResult;
    }

    protected Map<String, Object> buildResponse(final Map<String, Object> request, final Object methodResult, final Map<Class, List<String>> wantedFields) {
        Map<String, Object> response;

        try {
            final Object result = javaSerializer.serialize(methodResult, wantedFields);
            doModifyMethodResult(result);

            response = new HashMap<String, Object>(3);
            response.put("id", request.get("id"));
            response.put("error", null);
            response.put("result", result);
        } catch (SerializationException e) {
            response = buildResponse(request, new RpcException(ERROR_FROM_SERVER, INTERNAL_SERVER_ERROR, "Unable to serialize method result.", e));
        }

        return response;
    }

    protected Map<String, Object> buildResponse(final Map<String, Object> request, final RpcException exception) {
        final Map<String, Object> response = new HashMap<String, Object>(3);

        final Map<String, Object> error = new HashMap<String, Object>(6);
        error.put("origin", exception.getOrigin());
        error.put("code", exception.getErrorCode());

        error.put("message", exception.getClass().getName() + ": " + exception.getMessage());
        error.put("origMessage", exception.getMessage());
        error.put("class", exception.getClass().getName());

        final Throwable cause = exception.getCause();
        if (cause != null) {
            error.put("cause", convertException(cause));
        }

        response.put("id", request.get("id"));
        response.put("error", error);
        response.put("result", null);

        return response;
    }

    protected Map<String, Object> buildResponse(final Map<String, Object> request, final RemoteException exception) {
        final Map<String, Object> response = new HashMap<String, Object>(3);

        final Map<String, Object> error = new HashMap<String, Object>(6);
        error.put("origin", ERROR_FROM_METHOD);
        error.put("code", null);

        error.put("message", exception.getClass().getName() + ": " + exception.getMessage());
        error.put("origMessage", exception.getMessage());
        error.put("class", exception.getClass().getName());

        final Throwable cause = exception.getCause();
        if (cause != null) {
            error.put("cause", convertException(cause));
        }

        response.put("id", request.get("id"));
        response.put("error", error);
        response.put("result", null);

        return response;
    }

    protected Map<String, Object> convertException(Throwable exception) {
        final Map<String, Object> error = new HashMap<String, Object>(4);

        error.put("message", exception.getClass().getName() + ": " + exception.getMessage());
        error.put("origMessage", exception.getMessage());
        error.put("class", exception.getClass().getName());

        final Throwable cause = exception.getCause();
        if (cause != null) {
            error.put("cause", convertException(cause));
        }

        return error;
    }


    /**
     * Checks the referrer based on the configured strategy
     *
     * @param request the incoming request.
     * @return whether or not the request should be granted.
     */
    protected boolean checkReferrer(HttpServletRequest request) {
        if (referrerCheck == REFERRER_CHECK_PUBLIC) {
            return true;
        }
        if (referrerCheck == REFERRER_CHECK_FAIL) {
            return false;
        }
        String referrer = request.getHeader("Referer");
        if (referrer == null) {
            return false;
        }
        if (referrerCheck == REFERRER_CHECK_STRICT) {
            String contextURL = getContextURL(request);
            return referrer.startsWith(contextURL);
        }
        if (referrerCheck == REFERRER_CHECK_DOMAIN) {
            String domainURL = getDomainURL(request);
            return referrer.startsWith(domainURL);
        }
        if (referrerCheck == REFERRER_CHECK_SESSION) {
            // find the domain part of the referrer
            int colonIndex = referrer.indexOf(":");
            if (colonIndex == -1) {
                return false;
            }
            int referrerLength = referrer.length();
            int i;
            for (i = colonIndex + 1; ; ++i) {
                if (i >= referrerLength) {
                    return false;
                }
                if (referrer.charAt(i) != '/') {
                    break;
                }
            }
            int slashIndex = referrer.indexOf("/", i + 1);
            if (slashIndex == -1) {
                return false;
            }
            String referrerDomain = referrer.substring(0, slashIndex + 1);
            HttpSession session = request.getSession();
            String oldReferrerDomain = (String) session.getAttribute(SESSION_REFERRER_KEY);
            if (oldReferrerDomain == null) {
                session.setAttribute(SESSION_REFERRER_KEY, referrerDomain);
            } else {
                if (!oldReferrerDomain.equals(referrerDomain)) {
                    return false;
                }
            }
            return true;
        }
        throw new IllegalStateException("Internal error: unknown referrer checking configuration");
    }

    protected String getContextURL(HttpServletRequest request) {
        // reconstruct the start of the URL
        StringBuffer contextURL = new StringBuffer();
        String scheme = request.getScheme();
        int port = request.getServerPort();

        contextURL.append(scheme);
        contextURL.append("://");
        contextURL.append(request.getServerName());
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            contextURL.append(':');
            contextURL.append(request.getServerPort());
        }
        contextURL.append(request.getContextPath());

        return contextURL.toString();
    }


    protected String getDomainURL(HttpServletRequest request) {
        // reconstruct the start of the URL
        StringBuffer domainURL = new StringBuffer();
        String scheme = request.getScheme();
        int port = request.getServerPort();

        domainURL.append(scheme);
        domainURL.append("://");
        domainURL.append(request.getServerName());
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            domainURL.append(':');
            domainURL.append(request.getServerPort());
        }
        domainURL.append('/');

        return domainURL.toString();
    }


    /**
     * Remote method execution. The method name and parameters are expected
     * in JSON format in the request body.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @throws ServletException thrown when executing the method or writing the response fails.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String result = null;
        final String contentType = request.getHeader(CONTENT_TYPE);

        if (!checkReferrer(request) || contentType == null || !contentType.startsWith(DOPOST_REQUEST_CONTENTTYPE)) {
            result = ACCESS_DENIED_RESULT;
        } else {
            String requestString = null;
            try {
                requestString = getRequestString(request);
            } catch (IOException e) {
                throw new ServletException("Cannot read request", e);
            }
            result = handleRPC(request, requestString);
        }

        response.setContentType(DOPOST_RESPONSE_CONTENTTYPE);
        try {
            final Writer responseWriter = response.getWriter();
            responseWriter.write(result);
        } catch (IOException e) {
            throw new ServletException("Cannot write response", e);
        }
    }


    /**
     * Returns context path information to the client (as a JavaScript hash
     * map).
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @throws ServletException thrown when writing the response fails.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String result;
        response.setContentType(DOGET_RESPONSE_CONTENTTYPE);

        if (!checkReferrer(request)) {
            result = ACCESS_DENIED_RESULT;
        } else {
            final String jsTransportId = request.getParameter(SCRIPT_TRANSPORT_ID);
            if (jsTransportId != null) {
                final String requestString = request.getParameter(SCRIPT_TRANSPORT_DATA);
                final String res = handleRPC(request, requestString);

                result = "qx.io.remote.ScriptTransport._requestFinished(\"" + jsTransportId + "\", " + res + ");";
            } else {
                result = makeJavaScriptServerInfo(request);
            }
        }
        try {
            final Writer responseWriter = response.getWriter();
            responseWriter.write(result);
        } catch (IOException e) {
            throw new ServletException("Cannot write response", e);
        }
    }

    private String makeJavaScriptServerInfo(final HttpServletRequest request) {
        // sessions from the original code have been replaced by "null".
        return "if (!qx || !qx.core || !qx.core.ServerSettings) {" +
                "qx.OO.defineClass(\"qx.core.ServerSettings\");" +
                "}" +
                "qx.core.ServerSettings.serverPathPrefix = \"" + getContextURL(request) + "\";" +
                "qx.core.ServerSettings.serverPathSuffix = \";" +
                "jsessionid=null\";" +
                "qx.core.ServerSettings.sessionTimeoutInSeconds = null;" +
                "qx.core.ServerSettings.lastSessionRefresh = (new Date()).getTime();";
    }
}