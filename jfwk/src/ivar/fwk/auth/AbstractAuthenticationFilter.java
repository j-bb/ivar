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
package ivar.fwk.auth;

import ivar.common.AbstractObject;
import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.fwk.util.ServletHelper;
import ivar.helper.StopWatch;
import ivar.helper.StringHelper;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/* /!\ False good idea:
   Filter urls that matches "*" instead of "*.qxrpc" in the web.xml and maintain in the filter a list of the allowed urls
   (i.e. have a list of authorized urls instead of a list of filtered urls).
   There is actually a lot of urls needed when the application is loaded (since the login is in qooxdoo) :
   index.html, resource/ folder, class/ folder, script/ folder, etc.
   Further more, there are the source version and the build version
 */
public abstract class AbstractAuthenticationFilter extends AbstractObject implements Filter {

    private static final String LOGIN_TOKEN = "logintoken";
    private static final String ID = "\"id\":";
    private String filterName;
    private String differentiator;

//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        final String urls = filterConfig.getInitParameter("allowed-urls");
//        if (urls != null) {
//            final StringTokenizer tokenizer = new StringTokenizer(urls, ",");
//            while (tokenizer.hasMoreTokens()) {
//                allowedURLs.add(tokenizer.nextToken());
//            }
//        }
//    }
    abstract protected String getApplicationName();

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
        final StopWatch sw = new StopWatch();
        sw.start();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        filterName = filterConfig.getFilterName();
        log.info("Houston, we're in the servlet '" + filterName + "' init() method, please wait ...");
        differentiator = filterConfig.getInitParameter("differentiator");
        if (differentiator != null) {
            log.info("Found a differentiator : " + differentiator + "on application " + getApplicationName());
        } else {
            log.info("No differentiator found on application " + getApplicationName());
        }
        doInit(log);
        sw.stop();
        log.info(sw.getDeltaTimeTrace());
        log.info("OK, Houston, '" + filterName + "' ready for the mission !");
        log.emptyLine();
    }

    protected void doInit(final Logger log) {
    }

    protected final String getJsonId(final HttpServletRequest request) {
        String id = null;

        try {
            final String line = request.getReader().readLine();
            if (line != null) {
                final int idIndex = line.indexOf(ID);
                final int endIdIndex = line.indexOf(",", idIndex);
                id = line.substring(idIndex + 5, endIdIndex);
            } else {
                id = "-1";
            }
        } catch (IOException e) {
            // Nothing to do
            id = "-1";
        }
        return id;
    }

    @Override
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        getLogger().reset(System.out);
        final String clientTraceString = request.getRemoteAddr();
        getLogger().setClientTraceString(clientTraceString);
        getLogger().setApplicationKeyname((differentiator != null ? differentiator + "_" : "") + getApplicationName());

        beginBlock("[AuthenticationFilter] " + ServletHelper.getUrl3(request));

        final HttpSession session = request.getSession(false);
        debug("[AuthenticationFilter] Is there a session ? " + (session != null));
        if (session != null && session.getAttribute(LOGIN_TOKEN) != null && (session.getAttribute(LOGIN_TOKEN) instanceof LoginToken)) {
            final LoginToken loginToken = (LoginToken) session.getAttribute(LOGIN_TOKEN);
            if (loginToken.getLogin() != null && loginToken.getLogin().length() > 0) {
                debug("[AuthenticationFilter] Already logged as " + loginToken.getLogin() + " -> GREEN LIGHT");
                // TODO: add a "filterToken" into the session if the access is granted. The servlet will check before anything if
                // this token is present in the session. If not, this means the request has not passed through the filter, and the request will be rejected.
                // If the token is present, it will be immediately erased by the servlet.
                // This prevents to disable security by just removing the filter from the web.xml, since if a request has not been filtered, it will fail.
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                debug("[AuthenticationFilter] Already logged as " + loginToken.getLogin() + ", login should be null or empty -> YELLOW LIGHT => session invalidate.");
                error(request, response, session, "securityError");
//                final String id = getJsonId(request);
//                session.invalidate();
//                response.resetBuffer();
//                response.getWriter().print("{\"id\":" + id + ", \"result\": {\"securityError\":true} }");
//                response.flushBuffer();
            }
        } else {
            if (checkForPublicAccess(request, response, session)) {
                debug("[AuthenticationFilter] Not logged but public access-> GREEN LIGHT " + request.getQueryString());
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                debug("[AuthenticationFilter] Not logged -> RED LIGHT => session invalidate and goto login screen");
                error(request, response, session, "securityError");
            }
        }
//        } else {
//            // TODO ? Also add here the filterToken if a session exists
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
        endBlock("[AuthenticationFilter] " + request.getContextPath());
    }

    protected final void error(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session, final String message) throws IOException {
        error("[AuthenticationFilter] error " + message);
        final String id = getJsonId(request);
        if (session != null) {
            session.invalidate();
        }
        response.resetBuffer();
        response.getWriter().print("{\"id\":" + id + ", \"result\": {\"" + message + "\":true, \"authError\" : \"" + message + "\"} }");
        response.flushBuffer();
    }

    protected final boolean checkForPublicAccess(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws IOException {
        boolean result = false;

        beginBlock("[AuthenticationFilter] security check");

        final String params = request.getQueryString();
        final int numParams = StringHelper.count(params, '&') + 1;
        final StringTokenizer st = new StringTokenizer(params, "&", false);
        String screen = null;
        String serviceName = null;
        String methodName = null;
        for (int i = 1; i <= numParams; i++) {
            final String param = st.nextToken();
            final String key = StringHelper.getLeft(param, "=");
            final String value = StringHelper.getRight(param, "=");
            debug("[AuthenticationFilter] security check param " + 1 + " -> " + key + " : " + value);
            if ("svc".equals(key)) {
                serviceName = value;
            } else if ("method".equals(key)) {
                methodName = value;
            } else if ("screen".equals(key)) {
                screen = value;
            }
        }
        if (methodName == null || serviceName == null || screen == null) {
            error(request, response, session, "AuthenticationFilter security check failed : method and/or service and/or screen not there. methodName =" + methodName + ", serviceName=" + serviceName + ", screen=" + screen);
        } else {
            result = doCheckForPublicAccess(screen, serviceName, methodName);
        }

        endBlock("[AuthenticationFilter] security check. Result = " + result);
        return result;
    }

    protected abstract boolean doCheckForPublicAccess(final String screen, final String serviceName, final String methodName);

    @Override
    public final void destroy() {
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        log.info("Houston, we're in the filter '" + filterName + "' destroy() method, please wait ...");
        log.info("OK, Houston, '" + filterName + "' landed. End of the the mission !");
        log.emptyLine();
    }
}
