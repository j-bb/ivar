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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IOHelper {

    /*
     * Benchmark indicates that 128kb is optimal buffer size with ByteBuffer and FileChannel for reading.
     * 16kb is better for BufferedInputStream reading byte arrays.
     * http://nadeausoftware.com/articles/2008/02/java_tip_how_read_files_quickly
     */
    public static final int BUFFER_SIZE = 128 * 1024;

    public static boolean mkdirs(final String path) throws IOHelperException {
        boolean result = false;
        final File dir = new File(path);
        try {
            result = dir.mkdirs();
        } catch (SecurityException e) {
            throw new IOHelperException("Unable to create directory because of access restrictions", e);
        }
        return result;
    }

    public static File writeInputStreamToFile(final InputStream in, final String location) throws IOHelperException {
        return writeInputStreamToFile(in, new File(location));
    }

    public static File writeInputStreamToFile(final InputStream in, final File file) throws IOHelperException {
        BufferedInputStream dataStream = null;
        final byte[] arr = new byte[BUFFER_SIZE];
        ByteBuffer buf = ByteBuffer.wrap(arr);
        FileChannel chan = null;
        boolean fileExists = true;
        try {
            fileExists = file.exists();
        } catch (SecurityException e) {
            throw new IOHelperException(e);
        }
        if (fileExists) {
            throw new IOHelperException("Destination file already exists, aborting writing process.");
        } else {
            try {
                IOHelper.mkdirs(file.getParent());
                file.createNewFile();
                try {
                    chan = new FileOutputStream(file).getChannel();
                    try {
                        dataStream = new BufferedInputStream(in, IOHelper.BUFFER_SIZE);
                        int read;
                        long total = 0;
                        while ((read = dataStream.read(arr)) > 0) {
                            total += read;
                            chan.write(buf);
                            buf.clear();
                        }
                        chan.truncate(total);
                    } catch (IOException e) {
                        throw new IOHelperException(e);
                    } finally {
                        try {
                            if (dataStream != null) {
                                dataStream.close();
                            }
                            if (chan != null) {
                                chan.close();
                            }
                        } catch (IOException e) {
                            // Nothing to do...
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new IOHelperException(e);
                } catch (SecurityException e) {
                    throw new IOHelperException(e);
                }
            } catch (IOException e) {
                throw new IOHelperException(e);
            } catch (SecurityException e) {
                throw new IOHelperException(e);
            }
        }
        return file;
    }

    public static long writeFileToLocation(final File file, final String path) throws IOHelperException {
        FileOutputStream strm = null;
        final long result;
        try {
            strm = new FileOutputStream(path);
            result = writeFileToOutputStream(file, strm);
        } catch (IOHelperException e) {
            throw e;
        } catch (IOException e) {
            throw new IOHelperException("Error while trying to open a file stream for " + file.getAbsolutePath(), e);
        } finally {
            try {
                strm.close();
            } catch (IOException e) {
                // Nothing to do...
            }
        }
        return result;
    }

    public static long writeFileToOutputStream(final File file, final OutputStream out) throws IOHelperException {
        /* This method uses java.nio and is thread-safe. */
        FileInputStream fis = null;
        long total = 0;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            final String message = "IOHelper, unable to find file " + file.getAbsolutePath();
            throw new IOHelperException(message, e);
        }
        if (fis != null) {
            final FileChannel chan = fis.getChannel();
            final byte[] array = new byte[IOHelper.BUFFER_SIZE];
            final ByteBuffer bb = ByteBuffer.wrap(array);
            int read;
            try {
                while ((read = chan.read(bb)) > 0) {
                    out.write(array, 0, read);
                    bb.clear();
                    total += read;
                }
            } catch (IOException e) {
                final String message = "IOHelper, error while reading file " + file.getAbsolutePath() + " or during writing to outputstream";
                throw new IOHelperException(message, e);
            } finally {
                try {
                    if (chan != null) {
                        chan.close();
                    }
                } catch (IOException e) {
                    // Nothing we can do
                }
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    // Nothing we can do
                }
            }
        }
        return total;
    }
}
