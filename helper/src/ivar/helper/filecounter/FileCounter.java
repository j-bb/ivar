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
package ivar.helper.filecounter;

import java.util.Map;
import java.util.Set;
import ivar.helper.CollectionFactory;

public class FileCounter {

    private Map<FileCategories, Set<String>> files = CollectionFactory.newMapWithInsertionOrderPreserved();

    public void addFile(final FileCategories fileCategory, final String file) {
        //debug("[FILE] [" + fileCategory + "] " + file);
        Set<String> filesFromCategory = files.get(fileCategory);
        if (filesFromCategory == null) {
            filesFromCategory = CollectionFactory.newSetWithInsertionOrderPreserved();
            files.put(fileCategory, filesFromCategory);
        }
        if (filesFromCategory.contains(file)) {
            throw new RuntimeException("FileCounter : duplicate file : " + file + " in category " + fileCategory.toString());
        } else {
            filesFromCategory.add(file);
        }
    }

    public Map<FileCategories, Set<String>> getFiles() {
        return files;
    }

    public void free() {
        files.clear();
        files = null;
    }
}
