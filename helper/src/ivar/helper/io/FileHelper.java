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


   This code was partially taken from http://www.dreamincode.net/code/snippet1443.htm
   This is no more accessible at the time of publication, so I cannot credit the initial author.
 */
package ivar.helper.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {

    /**
     * This function will copy files or directories from one location to another. note that the source and the destination must be mutually exclusive. This
     * function can not be used to copy a directory to a sub directory of itself. The function will also have problems if the destination files already exist.
     *
     * @param src -- A File object that represents the source for the copy
     * @param dest -- A File object that represnts the destination for the copy.
     * @throws java.io.IOException if unable to copy.
     */
    public static void copyFiles(File src, File dest) throws IOException {
        if (src.getName().equals(".svn")
                || src.getName().equals(".cvs")
                || src.getName().equals(".DS_Store")
                || src.getName().startsWith("__MAC")
                || src.getName().startsWith("._")) {
            return;
        }
        //Check to ensure that the source is valid...
        if (!src.exists()) {
            throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
        } else if (!src.canRead()) { //check to ensure we have rights to the source...
            throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
        }
        //is this a directory copy?
        if (src.isDirectory()) {
            if (!dest.exists()) { //does the destination already exist?
                //if not we need to make it exist if possible (note this is mkdirs not mkdir)
                try {
                    if (!IOHelper.mkdirs(dest.getAbsolutePath())) {
                        throw new IOException("copyFiles: Could not create directory: " + dest.getAbsolutePath() + ".");
                    }
                } catch (IOHelperException e) {
                    throw new IOException("copyFiles: Could not create directory: " + dest.getAbsolutePath() + ".", e);
                }
            }
            //get a listing of files...
            String list[] = src.list();
            //copy all the files in the list.
            for (String aList : list) {
                File dest1 = new File(dest, aList);
                File src1 = new File(src, aList);
                copyFiles(src1, dest1);
            }
        } else {
            //This was not a directory, so lets just copy the file
            if (dest.exists()) {
                dest.delete();
            }
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[IOHelper.BUFFER_SIZE]; //Buffer 4K at a time (you can change this).
            int bytesRead;
            try {
                //open the files for input and output
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest, false);
                //while bytesRead indicates a successful read, lets write...
                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) { //Error copying file...
                IOException wrapper = new IOException("copyFiles: Unable to copy file: "
                        + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            } finally { //Ensure that the files are closed (if they were open).
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fin.close();
                }
            }
        }
    }

    /**
     * work with file and dir is now recursive in case folder contains folders.
     *
     * @param file
     * @return true if directory deleted, false otherwise.
     */
    public static boolean deleteDirectory(final File file) {
        boolean result = true;
        // If it's a file
        if (!file.isDirectory()) {
            result = file.delete();
        } else {
            final File[] files = file.listFiles();
            for (final File subfile : files) {
                result = result && deleteDirectory(subfile);
            }
            result = result && file.delete();
        }
        return result;
    }

    /**
     * Will create pathName if not existing.
     *
     * @param pathName
     * @return the File instance corresponding to the pathName
     * @throws java.io.IOException
     */
    public static File ensurePath(final String pathName) throws IOException {
        final File path = new File(pathName);
        if (!path.exists()) {
            try {
                if (!IOHelper.mkdirs(pathName)) {
                    throw new IOException("FileHelper.ensurePath: unable to create at least one folder in the path `" + pathName + "`");
                }
            } catch (IOHelperException e) {
                throw new IOException("FileHelper.ensurePath: unable to create because of access restriction `" + pathName + "`", e);
            }
        }
        if (!path.isDirectory()) {
            throw new IOException("FileHelper.ensurePath: '" + pathName + "' must be a folder.");
        }
        if (!path.canRead()) {
            throw new IOException("FileHelper.ensurePath: '" + pathName + "' cannot read.");
        }
        if (!path.canWrite()) {
            throw new IOException("FileHelper.ensurePath: '" + pathName + "' cannot write.");
        }
        return path;
    }
}
