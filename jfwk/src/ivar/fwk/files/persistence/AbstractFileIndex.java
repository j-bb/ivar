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

import ivar.common.AbstractObject;
import ivar.fwk.util.Generated;
import ivar.helper.StringHelper;
import ivar.helper.io.IOHelperException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.persistence.*;
import lib.serialization.JSONSerializer;
import lib.serialization.JavaSerializer;

@MappedSuperclass
@Generated(origin = "files.json", partial = true)
public abstract class AbstractFileIndex extends AbstractObject {

    private static FileTypeMap mimeTypesMap = MimetypesFileTypeMap.getDefaultFileTypeMap();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    protected Long id = null;

    @Column(name = "file_name", nullable = false)
    @Basic
    protected String fileName;

    @Column(nullable = true)
    @Basic
    protected String path;

    @Column(nullable = true)
    @Basic
    protected String user;

    @Column(nullable = false)
    @Basic
    protected Boolean temporary = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "last_modified")
    protected Date lastModified = null;

    public AbstractFileIndex(final String fileName) {
        this.fileName = fileName;
        temporary = true;
        lastModified = new Date();
    }

    public AbstractFileIndex() {
        /* Only for serialize/unserialize */
        temporary = true;
        fileName = null;
    }

    public File getFile() {
        File result = getFileInstance();
        try {
            if (!result.exists()) {
                warning("[AbstractFileIndex] File doesn't exist : " + result.getAbsolutePath());
                result = null;
            }
        } catch (SecurityException e) {
            error("Could not access file (no read permission) : " + result.getAbsolutePath(), e, true);
            result = null;
        }
        return result;
    }

    protected File getFileInstance() {
        if (temporary) {
            return new File(getTmpDir(), id.toString() + getExtension());
        } else {
            return getPersistentPath();
        }
    }

    public File getPersistentPath() {
        return new File(getSaveDir() + (path != null ? path : File.separator), id.toString() + getExtension());
    }

    public String getUser() {
        return user;
    }

    public String getExtension() {
        return "." + StringHelper.getLastRight(fileName, ".");
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(final Date date) {
        lastModified = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        final File file = getFileInstance();
        long result;
        try {
            if (file.exists()) {
                result = file.length();
            } else {
                result = 0;
            }
        } catch (SecurityException e) {
            error("Could not access file (no read permission) : " + file.getAbsolutePath(), e, true);
            result = 0;
        }
        return result;
    }

    public String getMimeType() {
        final File file = getFileInstance();
        final String result;
        if (!file.exists()) {
            result = null;
        } else {
            result = mimeTypesMap.getContentType(file);
        }

        return result;
    }

    public Long getId() {
        return id;
    }

    public void nullifyId() {
        id = null;
    }

    public String getPath() {
        return path;
    }

    public boolean removeFile() {
        boolean result = false;
        File file = getFileInstance();
        if (file.exists()) {
            try {
                result = file.delete();
            } catch (SecurityException e) {
                result = false;
                error("Did not have permission to delete file : " + file.getAbsolutePath());
            }
        }
        return result;
    }

    public SerializableFileIndex getSerializableSelf() {
        return new SerializableFileIndex(this);
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void persist() {
        final File file = getFileInstance();
        if (file.exists()) {
            boolean result = file.renameTo(getPersistentPath());
            if (!result) {
                error("Tried to move file " + file.getAbsolutePath() + " to " + getPersistentPath().getAbsolutePath() + " but operation failed.");
            } else {
                final File debugFile = new File(file.getParent(), id + getExtension() + "-" + getClass().getSimpleName() + ".json");
                if (debugFile.exists()) {
                    debugFile.renameTo(new File(getPersistentPath().getParent(), debugFile.getName()));
                }
                this.setTemporary(false);
            }
        } else {
            error("Tried to persist file but this one doesn't exist anymore. id= " + id + " & name= " + fileName);
        }
    }

    public void setTemporary(final boolean temporary) {
        this.temporary = temporary;
    }

    public String asJsonData() {
        String result = null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> obj = (Map<String, Object>) new JavaSerializer().serialize(this);
            result = new JSONSerializer().serialize(obj);
        } catch (Exception e) {
            error("Error while serializing file index to json data", e);
        }
        return result;
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        final String result = getClass().getSimpleName() + " " + newLine
                + "\tid = " + (id != null ? id : "null") + newLine
                + "\tfileName = " + (fileName != null ? fileName : "null") + newLine
                + "\tuser = " + (user != null ? user : "null") + newLine
                + "\tpath = " + (path != null ? path : "null") + newLine
                + "\ttemporary = " + (temporary != null ? temporary : null);
        return result;
    }

    @Override
    public int hashCode() {
        if (id > Integer.MAX_VALUE) {
            return (int) (Integer.MIN_VALUE + (id - Integer.MAX_VALUE));
        } else {
            return id.intValue();
        }
    }

    @Override
    public boolean equals(final Object otherObject) {
        boolean result = false;
        if (otherObject != null && otherObject instanceof AbstractFileIndex) {
            result = ((AbstractFileIndex) otherObject).id == this.id;
        }
        return result;
    }

    protected abstract String getTmpDir();

    protected abstract String getSaveDir();

    public static void createDebugFile(final AbstractFileIndex fileIndex, final String path, final String name) throws IOHelperException {
        final File debugFile = new File(path, (name == null ? fileIndex.getId().toString() : name) + fileIndex.getExtension() + "-" + fileIndex.getClass().getSimpleName() + ".json");
        PrintWriter writer = null;

        try {
            debugFile.createNewFile();
        } catch (IOException e) {
            throw new IOHelperException("Error while trying to create debug file for " + fileIndex.getFileName() + "(id=" + fileIndex.getId() + ")", e);
        }
        try {
            writer = new PrintWriter(new FileWriter(debugFile));
        } catch (IOException e) {
            throw new IOHelperException("Error while trying to create debug file for " + fileIndex.getFileName() + "(id=" + fileIndex.getId() + ")", e);
        }

        if (writer != null) {
            writer.println(fileIndex.asJsonData());
            writer.close();
        }
    }
}
