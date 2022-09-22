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
package ivar.fwk;

import ivar.common.logger.Logger;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractServlet extends HttpServlet {

    public void init() throws ServletException {
        super.init();
    }

    protected void debug(Logger log, HttpServletRequest request) {

        log.beginBlock("Debug HTTP info");
        log.info("QueryString is " + request.getQueryString());
        log.info("ContextPath is " + request.getContextPath());
        log.info("AuthType is " + request.getAuthType());
        log.info("PathInfo is " + request.getPathInfo());
        log.info("PathTranslated is " + request.getPathTranslated());
        log.beginBlock("----- Parameter DUMP begin ---");
        Enumeration<?> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final Object parameterName = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(parameterName.toString());
            if (parameterValues == null || parameterValues.length == 0) {
                log.info(parameterName + " : No value");
            } else {
                for (int i = 0; i < parameterValues.length; i++) {
                    final String parameterValue = parameterValues[i];
                    log.info("[" + i + "] " + parameterName + " : " + parameterValue);
                }
            }
        }
        log.endBlock("----- Parameter DUMP end ---");

        log.beginBlock("----- Attribute DUMP begin ---");

        Enumeration<?> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            Object attribuneName = attributeNames.nextElement();
            Object attributeValue = request.getAttribute(attribuneName.toString());
            log.info(attribuneName + " : " + attributeValue);
        }
        log.endBlock("----- Attribute DUMP end ---");

        log.beginBlock("----- Header DUMP begin ---");
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            Object headerName = headerNames.nextElement();
            Object headerValue = request.getHeader(headerName.toString());
            log.info(headerName + " : " + headerValue);
        }
        log.endBlock("----- Header DUMP end ---");
        log.endBlock("Debug HTTP info");
    }
}
