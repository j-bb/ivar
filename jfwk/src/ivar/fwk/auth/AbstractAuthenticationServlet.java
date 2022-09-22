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

import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.helper.StopWatch;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lib.rpc.JsonRpcServlet;

public abstract class AbstractAuthenticationServlet extends JsonRpcServlet {

    // 1 hour
    protected static final int SESSION_TIMEOUT = 3600;

    protected String differentiator;

    abstract protected String getApplicationName();

    protected void doAfterInit() throws ServletException {
    }

    @Override
    public final void init(ServletConfig servletConfig) throws ServletException {
        final StopWatch sw = new StopWatch();
        sw.start();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        final String name = servletConfig.getServletName();
        log.info("Houston, we're in the servlet '" + name + "' init() method, please wait ...");
        differentiator = servletConfig.getInitParameter("differentiator");
        if (differentiator != null) {
            log.info("Found a differentiator : " + differentiator + "on application " + getApplicationName());
        } else {
            log.info("No differentiator found on application " + getApplicationName());
        }
        super.init(servletConfig);
        doAfterInit();
        sw.stop();
        log.info(sw.getDeltaTimeTrace());
        log.info("OK, Houston, '" + name + "' ready for the mission !");
        log.emptyLine();
    }

    protected void invalidateSession(final HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            session = null;
        }
    }
}
