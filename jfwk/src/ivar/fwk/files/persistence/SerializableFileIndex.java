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

import java.io.Serializable;
import java.util.Date;

public class SerializableFileIndex implements Serializable {

    private String fileName;

    private Date uploadDate;

    private Long fileSize;

    private Integer height;

    private Integer width;

    public SerializableFileIndex(final AbstractFileIndex fileIndex) {
        fileName = fileIndex.getFileName();
        uploadDate = fileIndex.getLastModified();
        fileSize = fileIndex.getFileSize();
        if (fileIndex instanceof ApplicationFileIndex) {
            ApplicationFileIndex appFileIndex = (ApplicationFileIndex) fileIndex;
            height = appFileIndex.getImageHeight();
            width = appFileIndex.getImageWidth();
        }
    }

    public SerializableFileIndex() {
        // This ctor is useful only for unserialization.
    }
}
