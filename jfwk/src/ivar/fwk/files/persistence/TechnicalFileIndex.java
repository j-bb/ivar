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

import java.io.File;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TechnicalFileIndex")
public class TechnicalFileIndex extends AbstractFileIndex {

    public static String TMP_DIR;
    public static String SAVE_DIR;

    public TechnicalFileIndex() {
        // Only serialize/unserialize
        super();
    }

    public TechnicalFileIndex(final String fileName) {
        super(fileName);
    }

    @Override
    public File getPersistentPath() {
        return new File(getSaveDir() + (path != null ? path : File.separator), fileName);
    }

    @Override
    protected String getTmpDir() {
        return TMP_DIR;
    }

    @Override
    protected String getSaveDir() {
        return SAVE_DIR;
    }
}
