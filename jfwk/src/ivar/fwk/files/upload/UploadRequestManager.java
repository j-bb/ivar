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
package ivar.fwk.files.upload;

import ivar.common.AbstractObject;
import ivar.common.logger.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadRequestManager extends AbstractObject {

    private DiskFileItemFactory fileFactory;
    private long maxSize;
    private long maxRequestSize;

    public UploadRequestManager(final String tempDir, final long maxFileSize, final long maxRequestSize) {
        beginBlock("Entering ctor in " + getClass().getSimpleName());
        fileFactory = new DiskFileItemFactory();
        fileFactory.setRepository(new File(tempDir));
        maxSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;

        info("If files are above " + fileFactory.getSizeThreshold() + " bytes, they will be written on disk, otherwise they'll be stored in memory.");
        info("Maximum file size is " + maxFileSize + " kB, maximum request size is " + maxRequestSize + " kB");
        endBlock("Leaving ctor in " + getClass().getSimpleName());
    }

    public List<FileItem> parseRequest(final HttpServletRequest request, final Logger log) throws FileUploadException {
        final ServletFileUpload servletFileUpload = new ServletFileUpload(fileFactory);
        servletFileUpload.setFileSizeMax(maxSize * 1024L);
        servletFileUpload.setSizeMax(maxRequestSize * 1024L);
        List<FileItem> requestItems = null;
        try {
            requestItems = servletFileUpload.parseRequest(request);
        } catch (FileUploadException e) {
            log.error("An error has occured while trying to parse the upload request", e, true);
            requestItems = null;
            throw e;
        }

        return requestItems;
    }

    public FileItemIterator getIterator(final HttpServletRequest request, final Logger log) {
        final ServletFileUpload servletFileUpload = new ServletFileUpload();
        FileItemIterator result = null;
        try {
            result = servletFileUpload.getItemIterator(request);
        } catch (IOException e) {
            log.error("Could not get a file item iterator for this request", e, true);
            result = null;
        } catch (FileUploadException e) {
            log.error("Could not get a file item iterator for this request", e, true);
            result = null;
        }
        return result;
    }

    public boolean isMultipartRequest(final HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(request);
    }
}
