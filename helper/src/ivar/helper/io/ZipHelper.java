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
package ivar.helper.io;

import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import java.io.*;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipHelper {

    private static final int BUFFER_SIZE = IOHelper.BUFFER_SIZE;
    private static final boolean DEBUG = false;

    /**
     * Unzip all entries in zipfile which names match the given regular expression. Warning/possible improvement : regexp "file\\.txt" will not extract entry in
     * nested folder (e.g. a/b/file.txt)
     *
     * @param zipFile Path to zip file that must be processed
     * @param outputFolder Path to directory where matching entries should be extracted
     * @param regExp A regular expression to match against entries names.
     * @return A set containing all extracted files
     * @throws IOException If an error occurs when reading the zipfile or writing the output file(s)
     */
    public static Set<File> unzipMatching(final String zipFile, final String outputFolder, final String regExp) throws IOException {
        final ZipFile zf;
        final Set<File> result = CollectionFactory.newSet();
        zf = new ZipFile(zipFile);
        try {
            final Enumeration<? extends ZipEntry> zipEnum = zf.entries();
            String dir = ".";

            if (outputFolder != null && outputFolder.length() > 1) {
                dir = outputFolder.replace('\\', File.separatorChar);
            }

            if (dir.charAt(dir.length() - 1) != File.separatorChar) {
                dir += File.separator;
            }
            while (zipEnum.hasMoreElements()) {
                final ZipEntry item = (ZipEntry) zipEnum.nextElement();
                if (item.isDirectory() && item.getName().matches(regExp)) { // Directory
                    File newdir = new File(dir + item.getName());
                    try {
                        IOHelper.mkdirs(newdir.getAbsolutePath());
                    } catch (IOHelperException e) {
                        throw new IOException(e);
                    }
                    result.add(newdir);
                } else if (item.getName().matches(regExp)) { // File
                    final File itemDir = new File(dir + item.getName()).getParentFile();
                    try {
                        IOHelper.mkdirs(itemDir.getAbsolutePath());
                    } catch (IOHelperException e) {
                        throw new IOException(e);
                    }
                    final String prefix = item.getName().contains(File.separator) ? StringHelper.getLastRight(item.getName(), File.separator) : item.getName();
                    final File newFile = File.createTempFile(prefix, null, itemDir);
                    InputStream is = null;
                    BufferedOutputStream out = null;
                    try {
                        is = zf.getInputStream(item);
                        int count;
                        byte data[] = new byte[BUFFER_SIZE];
                        out = new BufferedOutputStream(new FileOutputStream(newFile), BUFFER_SIZE);
                        while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (out != null) {
                                out.flush();
                                out.close();
                            }
                        } catch (IOException e) {
                            // Nothing we can do
                        }
                    }
                    result.add(newFile);
                }
            }
        } finally {
            try {
                if (zf != null) {
                    zf.close();
                }
            } catch (IOException e) {
                // Nothing we can do
            }
        }
        return result;
    }

    public static void unzip(final String zipfile, final String outputfolder, final String applicationName) throws IOException {
        unzipMatching(zipfile, outputfolder, ".*");
    }
}
