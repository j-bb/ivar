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
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TableExportServlet extends AbstractDownloadServlet {

    private static ItemDeposit<String> csvDeposit = new ItemDeposit<String>();

    @Override
    protected void doService(final Logger log, final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String requestKey = request.getParameter("key");
        if (requestKey == null) {
            log.error("Request to download servlet with null key / no key");
            printMessageToUser(response, "Sorry, your request was not properly formatted. Please try again.", log);
        } else {
            sendCSV(requestKey, log, response);
        }
    }

    private void sendCSV(final String key, final Logger log, final HttpServletResponse response) {
        log.beginBlock("Trying to send CSV to user with key : " + key);
        final String result = getFile(key);
        if (result == null) {
            log.error("Request for CSV with key: " + key + " not found in files map");
            printMessageToUser(response, "Sorry, your file was not found in our directories. Please try to issue your request once again.", log);
        } else {
            log.info("Found CSV file, writing content in servlet output.");
            try {
                response.setContentType("text/csv");
                PrintWriter pw = response.getWriter();
                pw.print(result);
            } catch (IOException e) {
                log.error("Error while trying to print CSV file in servlet response with key " + key);
                printMessageToUser(response, "Sorry, an error has occured while trying to output the file. Please try again.", log);
            }
        }
        log.endBlock("Trying to send CSV to user with key : " + key);
    }

    public static String addFile(String fileContent) {
        return csvDeposit.addItem(fileContent);
    }

    private String getFile(String key) {
        return csvDeposit.getItem(key);
    }

    @Override
    protected String getServletTitle() {
        return getClass().getSimpleName();
    }
}
