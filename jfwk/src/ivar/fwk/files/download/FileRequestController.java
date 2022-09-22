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
import ivar.fwk.files.persistence.ApplicationFileIndex;
import ivar.fwk.files.persistence.ApplicationFileIndexController;
import ivar.fwk.rpc.ControllerContext;
import ivar.helper.StringHelper;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class FileRequestController implements Remote {

    public FileRequestToken requestUploadedFile(final ControllerContext cc, final Integer id) throws RemoteException {
        ApplicationFileIndex fileIndex = new ApplicationFileIndexController().getFileIndexById(cc, id);
        FileRequestToken result = null;
        if (fileIndex != null) {
            cc.addTransientField(FileRequestToken.class, "fileName");
            cc.addTransientField(FileRequestToken.class, "key");
            result = new FileRequestToken(fileIndex.getId().toString(), fileIndex.getFileName());
        }
        return result;
    }

    public String requestCSV(final ControllerContext cc, final String[] headers, final String[][] values) throws RemoteException {
        final Logger log = cc.getLogger();
        final StringBuilder builder = new StringBuilder();
        log.beginBlock("Entering method requestCSV() in " + getClass().getSimpleName());
        log.beginBlock("Manipulating table data to store it as a CSV file...");

        for (int i = 0, length = headers.length; i < length; i++) {
            builder.append("\"");
            builder.append(StringHelper.quoteQuotes(headers[i]));
            builder.append("\"");
            builder.append(((i < length - 1) ? ";" : ""));
        }
        builder.append("\n");
        for (final String[] row : values) {
            for (int i = 0, length = row.length; i < length; i++) {
                final String cell = row[i];
                if (StringHelper.isDouble(cell)) {
                    builder.append(cell);
                    builder.append(((i < length - 1) ? ";" : ""));
                } else {
                    builder.append("\"");
                    builder.append(StringHelper.quoteQuotes(cell));
                    builder.append("\"");
                    builder.append(((i < length - 1) ? ";" : ""));
                }
            }
            builder.append("\n");
        }
        log.info("File is " + builder.length() + " characters long.");
        log.endBlock("Manipulating table data to store it as a CSV file... Done.");
        final String result = TableExportServlet.addFile(builder.toString());
        log.info("Stored CSV file content in files map with key " + result);
        log.endBlock("Exiting method requestCSV() in " + getClass().getSimpleName());
        return result;
    }
}
