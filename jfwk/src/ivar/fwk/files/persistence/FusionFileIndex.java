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
package ivar.fwk.files.persistence;

import ivar.fwk.util.ApplicationDirtyConfig;
import java.io.File;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FusionFileIndex")
public class FusionFileIndex extends AbstractFileIndex {

    private static String SAVE_DIR = ApplicationDirtyConfig.getInstance().get("fusion-results-dir");

    public FusionFileIndex() {
        super();
    }

    public FusionFileIndex(final String fileName) {
        super(fileName);
        setPath(null);
        setUser("SYSTEM");
        setTemporary(true);
    }

    @Override
    public File getFileInstance() {
        return new File(getTmpDir(), getFileName());
    }

    @Override
    protected String getTmpDir() {
        return getSaveDir();
    }

    @Override
    protected String getSaveDir() {
        if (SAVE_DIR == null) {
            SAVE_DIR = ApplicationDirtyConfig.getInstance().get("fusion-results-dir");
        }
        return SAVE_DIR;
    }
}
