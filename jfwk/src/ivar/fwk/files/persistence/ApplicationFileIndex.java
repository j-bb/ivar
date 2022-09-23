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
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ApplicationFileIndex")
public class ApplicationFileIndex extends AbstractFileIndex {

    public static String TMP_DIR = ApplicationDirtyConfig.getInstance().get("uploads-tmp-dir");
    public static String SAVE_DIR = ApplicationDirtyConfig.getInstance().get("uploads-save-dir");

    public ApplicationFileIndex() {
        super();
    }

    public ApplicationFileIndex(final String fileName) {
        super(fileName);
    }

    private boolean isImage() {
        return fileName.endsWith(".png") || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".gif");
    }

    private BufferedImage getImage() {
        BufferedImage img = null;
        if (isImage()) {
            try {
                img = ImageIO.read(getFileInstance());
            } catch (IOException e) {
                error(e);
            }
        }
        return img;
    }

    public Integer getImageWidth() {
        Integer result = null;
        // !!!
        // Wow Check that deeply : on server, there is no GUI and this is a dependency on java.awt !!!
        // !!!
        final BufferedImage img = getImage();
        if (img != null) {
            result = img.getWidth();
        }
        return result;
    }

    public Integer getImageHeight() {
        Integer result = null;
        final BufferedImage img = getImage();
        if (img != null) {
            result = img.getHeight();
        }
        return result;
    }

    @Override
    public String getTmpDir() {
        return TMP_DIR;
    }

    @Override
    protected String getSaveDir() {
        return SAVE_DIR;
    }
}
