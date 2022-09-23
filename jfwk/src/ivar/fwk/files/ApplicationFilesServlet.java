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
package ivar.fwk.files;

import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.fwk.files.download.AbstractDownloadServlet;
import ivar.fwk.files.persistence.ApplicationFileIndex;
import ivar.fwk.files.persistence.ApplicationFileIndexController;
import ivar.fwk.files.upload.UploadRequestManager;
import ivar.fwk.rpc.ControllerContext;
import ivar.fwk.util.ApplicationDirtyConfig;
import ivar.helper.CollectionFactory;
import ivar.helper.io.IOHelper;
import ivar.helper.io.IOHelperException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

public class ApplicationFilesServlet extends AbstractDownloadServlet {

    private static long MAX_FILE_SIZE_IN_KB = 10 * 1024 * 1024; // 10 GB
    private static long MAX_REQUEST_SIZE = MAX_FILE_SIZE_IN_KB + 1024;
    private static String TECH_TMP_DIR = ApplicationDirtyConfig.getInstance().get("technical-tmp-dir");

    private static final boolean DEBUG = true;

    private UploadRequestManager uploadRequestManager;
    private ApplicationFileIndexController fileIndexController = new ApplicationFileIndexController();

    @Override
    public void init() throws ServletException {
        uploadRequestManager = new UploadRequestManager(TECH_TMP_DIR, MAX_FILE_SIZE_IN_KB, MAX_REQUEST_SIZE);
    }

    protected void doService(final Logger log, final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        if (uploadRequestManager.isMultipartRequest(request)) {
            doPost(request, response, log);
        } else {
            final String requestKey = request.getParameter("key");
            if (requestKey == null) {
                log.error("Request to download servlet with null key / no key");
                printMessageToUser(response, "Sorry, your request was not properly formatted. Please try again.", log);
            } else {
                sendUploadedFile(requestKey, log, response);
            }
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        doPost(req, resp, LoggerDefaultImplementation.getInstance());
    }

    private void sendUploadedFile(final String key, final Logger log, final HttpServletResponse resp) {
        try {
            final Long keyNum = Long.parseLong(key);
            final ApplicationFileIndex fileIndex = fileIndexController.getFileIndexById(new ControllerContext(), keyNum);
            if (fileIndex != null) {
                final File f = fileIndex.getFile();
                if (f != null && f.exists()) {
                    writeFileToOutputStream(f, resp.getOutputStream());
                }
            }

        } catch (IOHelperException e) {
            log.error(e, true);
        } catch (IOException e) {
            log.error(e, true);
        }
    }

    private ApplicationFileIndex createUploadedFileFileIndex(final FileItem actualFile, final Map<String, String> formFields, final Logger log) {
        ApplicationFileIndex fileIndex = null;
        if (actualFile != null && actualFile.getSize() > 0) {
            fileIndex = new ApplicationFileIndex(actualFile.getName());
            if (formFields.containsKey("businessObject") && formFields.containsKey("field")) {
                log.info("Computing path for the new file index using scenario and step");
                fileIndex.setPath(File.separator + formFields.get("businessObject") + File.separator + formFields.get("field"));
                log.info("Computed path is : ." + fileIndex.getPath());
            } else {
                // ???
                log.error("Missing fields in received form: businessObject is present?" + formFields.containsKey("businessObject") + ". field? " + formFields.containsKey("field"));
            }
            if (formFields.containsKey("user")) {
                fileIndex.setUser(formFields.get("user"));
                log.info("Uploading user is: " + fileIndex.getUser());
            } else {
                // ???
                log.error("Missing field in received form: user is not present");
            }

            try {
                fileIndex = fileIndexController.createInCreate(new ControllerContext(), fileIndex);
            } catch (RemoteException e) {
                log.error("An error has occured while trying to persist the new file entry in the database");
                fileIndex = null;
            }
        } else {
            log.error("No file provided in upload form or file has size 0");
        }
        return fileIndex;
    }

    private ApplicationFileIndex writeUploadOnFileSystem(final ApplicationFileIndex fileIndex, final FileItem actualFile, final Logger log) {
        ApplicationFileIndex result = null;
        log.info("Writing file on filesystem");
        try {
            IOHelper.mkdirs(ApplicationFileIndex.SAVE_DIR + fileIndex.getPath());
            final File resultFile = writeFileToLocation(actualFile, new File(ApplicationFileIndex.TMP_DIR, fileIndex.getId().toString() + fileIndex.getExtension()).getAbsolutePath(), log);
            if (resultFile == null) {
                log.beginBlock("Removing file index from database since the write operation has failed...");
                try {
                    fileIndexController.deleteInDelete(new ControllerContext(), fileIndex.getId());
                } catch (RemoteException e) {
                    log.error(e, true);
                }
                log.endBlock("Removing file index from database since the write operation has failed... Done.");
            } else {
                if (DEBUG) {
                    createDebugFile(fileIndex, log);
                }
                result = fileIndex;
            }
        } catch (IOHelperException e) {
            log.error("Could not create path hierarchy for " + fileIndex.getPath(), e, true);
            log.beginBlock("Removing file index from database since the write operation has failed...");
            try {
                fileIndexController.deleteInDelete(new ControllerContext(), fileIndex.getId());
            } catch (RemoteException e2) {
                log.error(e2, true);
            }
            log.endBlock("Removing file index from database since the write operation has failed... Done.");
        }

        return result;
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp, final Logger log) {
        // TODO : Use streaming API (with getIterator) instead of standard method with parseRequest
        // This would avoid copying the file twice, once in the TECH_TMP_DIR and then in the SAVE_DIR.
        List<FileItem> fileItems = null;
        FileItem actualFile = null;
        final Map<String, String> formFields = CollectionFactory.newMap();
        log.beginBlock("In doHandlePost() in servlet " + getClass().getSimpleName());
        try {
            fileItems = uploadRequestManager.parseRequest(req, log);
        } catch (FileUploadException e) {
            // No need to log since error has already been logged by parseRequest().
            fileItems = null;
        }

        if (fileItems != null) {
            for (FileItem fi : fileItems) {
                if (fi.isFormField()) {
                    log.info("Found simple parameter " + fi.getFieldName() + " with value " + fi.getString());
                    formFields.put(fi.getFieldName(), fi.getString());
                } else {
                    log.info("Found file " + fi.getName());
                    actualFile = fi;
                }
            }

            final ApplicationFileIndex fileIndex = createUploadedFileFileIndex(actualFile, formFields, log);

            if (fileIndex != null) {
                writeUploadResponse(writeUploadOnFileSystem(fileIndex, actualFile, log), resp, log);
            } else {
                writeUploadResponse(null, resp, log);
            }
        } else {
            log.error("No file items (form fields or files) in upload request. Maybe parsing has failed earlier.");
            writeUploadResponse(null, resp, log);
        }
        log.endBlock("Out doHandlePost() in servlet " + getClass().getSimpleName());
    }

    @Override
    protected String getServletTitle() {
        return getClass().getSimpleName();
    }

    private void writeUploadResponse(final ApplicationFileIndex fileIndex, final HttpServletResponse resp, final Logger log) {
        try {
            if (fileIndex != null) {
                resp.getWriter().print(fileIndex.getId());
            } else {
                resp.getWriter().print("null");
            }
        } catch (IOException e) {
            log.error(e, true);
        }
    }

    protected File writeStreamToLocation(final InputStream is, final String location, final Logger log) {
        File result = null;
        try {
            result = IOHelper.writeInputStreamToFile(is, location);
        } catch (IOHelperException e) {
            log.error("An error occurred when trying to write stream content to " + location, e);
        }
        return result;
    }

    protected File writeFileToLocation(final FileItem file, final String location, final Logger log) {
        File result = null;
        try {
            result = writeStreamToLocation(file.getInputStream(), location, log);
        } catch (IOException e) {
            log.error("An error occured when trying to open an input stream for file " + file.getName(), e, true);
            result = null;

        }
        return result;
    }

    private void createDebugFile(final ApplicationFileIndex fileIndex, final Logger log) {
        try {
            ApplicationFileIndex.createDebugFile(fileIndex, ApplicationFileIndex.TMP_DIR, null);
        } catch (IOHelperException e) {
            log.error("An error occured while trying to write a debug file for file index: " + fileIndex.getId(), e, true);
        }
    }
}
