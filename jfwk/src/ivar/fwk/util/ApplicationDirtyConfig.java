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
package ivar.fwk.util;

import ivar.common.AbstractObject;
import ivar.fwk.files.persistence.ApplicationFileIndex;
import ivar.helper.CollectionFactory;
import ivar.helper.io.IOHelper;
import ivar.helper.io.IOHelperException;
import java.io.File;
import java.util.Map;

/*
 * SPRING would be better suited and cleaner, but
 * we lack time and money to do this right...
 */
public class ApplicationDirtyConfig extends AbstractObject {

    private static final ApplicationDirtyConfig INSTANCE = new ApplicationDirtyConfig();
    private static final String FILE_SEP = System.getProperty("file.separator");

    private Map<String, String> configMap = CollectionFactory.newMap();

    private ApplicationDirtyConfig() {
    }

    public void put(final String key, final String value) {
        info("Inserting key [" + key + "] in config with value [" + value + "]");
        configMap.put(key, value);
    }

    public String get(final String key) {
        return configMap.get(key);
    }

    public void initFusionFolders(final String fusionRootDir) throws IOHelperException {
        final String fusionResultsDir = fusionRootDir + FILE_SEP + "fusion";
        final String templatesDir = fusionRootDir + FILE_SEP + "technical" + FILE_SEP + "templates";

        IOHelper.mkdirs(fusionResultsDir);
        IOHelper.mkdirs(new File(templatesDir, "tmp").getAbsolutePath());

        put("fusion-results-dir", fusionResultsDir);
        put("fusion-templates-dir", templatesDir);
    }

    public void initUploadFolders(final String uploadsRootDir) throws IOHelperException {
        final String technicalTmpDir = uploadsRootDir + FILE_SEP + "techtmp";
        final String uploadsSaveDir = uploadsRootDir + FILE_SEP + "docs";
        final String uploadsTmpDir = uploadsRootDir + FILE_SEP + "tmp";

        IOHelper.mkdirs(technicalTmpDir);
        IOHelper.mkdirs(uploadsTmpDir);

        final File saveDir = new File(uploadsSaveDir);
        if (!saveDir.exists()) {
            IOHelper.mkdirs(uploadsSaveDir);
        }

        if (!saveDir.canExecute()) {
            try {
                saveDir.setExecutable(true);
            } catch (SecurityException e) {
                error("Could not set execute rights on save directory in " + getClass().getSimpleName() + ": " + uploadsSaveDir);
            }
        }

        if (!saveDir.canRead()) {
            try {
                saveDir.setReadable(true);
            } catch (SecurityException e) {
                error("Could not set read rights on save directory in " + getClass().getSimpleName() + ": " + uploadsSaveDir);
            }
        }

        if (!saveDir.canWrite()) {
            try {
                saveDir.setWritable(true);
            } catch (SecurityException e) {
                error("Could not set write rights on save directory in " + getClass().getSimpleName() + ": " + uploadsSaveDir);
            }
        }
        final Long freeBytes = saveDir.getFreeSpace();
        info("There are " + freeBytes + " bytes left on this partition, which is " + (freeBytes / 1024L) + " kilobytes, or " + (freeBytes / (1024L * 1024L)) + " megabytes");

        put("uploads-save-dir", uploadsSaveDir);
        put("uploads-tmp-dir", uploadsTmpDir);
        put("technical-tmp-dir", technicalTmpDir);
        ApplicationFileIndex.SAVE_DIR = get("uploads-save-dir");
        ApplicationFileIndex.TMP_DIR = get("uploads-tmp-dir");
    }

    public static ApplicationDirtyConfig getInstance() {
        return INSTANCE;
    }
}
