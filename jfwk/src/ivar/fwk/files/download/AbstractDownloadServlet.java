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
package ivar.fwk.files.download;

import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.fwk.AbstractServlet;
import ivar.fwk.files.persistence.ApplicationFileIndexController;
import ivar.fwk.files.persistence.FusionFileIndexController;
import ivar.helper.StopWatch;
import ivar.helper.io.IOHelper;
import ivar.helper.io.IOHelperException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractDownloadServlet extends AbstractServlet {

    private final ApplicationFileIndexController fileIndexController = new ApplicationFileIndexController();
    private final FusionFileIndexController fusionFileController = new FusionFileIndexController();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        final Logger log = LoggerDefaultImplementation.getInstance();

        log.beginBlock("Ok Houston, we're entering the method service() of the servlet " + getClass().getSimpleName() + ".");
        doService(log, request, response);
        log.endBlock("Ok Houston, we're exiting the method service() of the servlet " + getClass().getSimpleName() + "... Done.");
    }

    @Override
    public void init() throws ServletException {
        final ServletConfig servletConfig = getServletConfig();
        final StopWatch sw = new StopWatch();
        sw.start();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        final String servletName = getServletName();
        log.info("Houston, we're in the servlet '" + servletName + "' init() method, please wait ...");
        super.init();
        sw.stop();
        log.info(sw.getDeltaTimeTrace());

        final String differentiator = servletConfig.getInitParameter("differentiator");
        if (differentiator != null) {
            log.info("Found a differentiator : " + differentiator + " in " + getServletTitle());
        } else {
            log.info("No differentiator found in " + getServletTitle());
        }
        log.info("OK, Houston, '" + servletName + "' ready for the mission !");
        log.emptyLine();
    }

    abstract protected void doService(final Logger log, final HttpServletRequest request, final HttpServletResponse response) throws ServletException;

    abstract protected String getServletTitle();

    protected void printMessageToUser(final HttpServletResponse rsp, final String message, final Logger log) {
        rsp.setContentType("text/html");
        rsp.reset();
        try {
            rsp.getWriter().print("<html><head><title>Orbiter Engine :: " + getServletTitle() + "</title></head><body>" + message + "</body></html>");
            rsp.getWriter().close();
        } catch (IOException e) {
            logIOError(log, "Houston, we encountered an error while trying to log an error ! " + message, e);
        }
    }

    protected void logIOError(final Logger log, final Exception e) {
        log.error("Houston, we encountered an error while trying to set the response content for the client.", e, true);
    }

    protected void logIOError(final Logger log, final String message, final Exception e) {
        log.error(message, e, true);
    }

    protected void writeFileToOutputStream(final File file, final OutputStream out) throws IOHelperException {
        IOHelper.writeFileToOutputStream(file, out);
    }
}
