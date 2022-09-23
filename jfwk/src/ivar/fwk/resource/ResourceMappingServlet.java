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
package ivar.fwk.resource;

import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.fwk.AbstractServlet;
import ivar.helper.StopWatch;
import ivar.helper.io.IOHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceMappingServlet extends AbstractServlet {

    private static final boolean DEBUG = false;
    private static final int BUFFER_SIZE = IOHelper.BUFFER_SIZE;

    private File resourceDirectory;
    private String servletName;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        final StopWatch sw = new StopWatch();
        sw.start();
        final Logger log = LoggerDefaultImplementation.getInstance().reset(System.out);
        servletName = servletConfig.getServletName();
        log.info("Houston, we're in the servlet '" + servletName + "' init() method, please wait ...");
        super.init(servletConfig);
        final String resourceDirectory = getInitParameter("resourceDirectory");
        if (resourceDirectory == null) {
            throw new ServletException("The servlet parameter 'resourceDirectory' is mandatory");
        }
        this.resourceDirectory = new File(resourceDirectory);
        if (!this.resourceDirectory.exists()) {
            throw new ServletException("'" + resourceDirectory + "' doesn't exist");
        }
        if (!this.resourceDirectory.isDirectory()) {
            throw new ServletException("'" + resourceDirectory + "' isn't a directory");
        }
        log.info("Valid ressource folder found : " + this.resourceDirectory.getAbsolutePath());
        log.info("OK, Houston, '" + servletName + "' ready for the mission !");
        log.emptyLine();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String src = request.getPathInfo();
        final Logger log;
        if (DEBUG) {
            log = LoggerDefaultImplementation.getInstance().reset(System.out);
        }
        if (src == null) {
            response.sendError(500, servletName + " ressource name is null");
        } else {
            final ServletContext context = getServletConfig().getServletContext();
            final String MIMEType = context.getMimeType(src);
            if (MIMEType != null) {
                response.setContentType(MIMEType);
            }

            final File srcFile = new File(resourceDirectory, src);
            if (DEBUG) {
                log.debug("[" + servletName + "] " + "Request for file : " + resourceDirectory.getAbsolutePath() + src);
            }
            if (!srcFile.isFile()) {
                response.sendError(404, "Ressource doesn't exist or is not a file : " + resourceDirectory.getAbsolutePath() + src);
            } else {
                // TODO BufferedInputStream ??
                final FileInputStream in = new FileInputStream(srcFile);
                try {
                    final OutputStream out = response.getOutputStream();
                    final byte[] buffer = new byte[BUFFER_SIZE];
                    int nbRead;
                    int size = 0;
                    while ((nbRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, nbRead);
                        size += nbRead;
                    }
                    response.setContentLength(size);
                } catch (IOException e) {
                    response.sendError(500, "IOException when trying to read " + srcFile.getAbsolutePath() + " " + e.getMessage());
                } finally {
                    in.close();
                }
            }
        }
    }
}
