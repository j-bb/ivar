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
package ivar.fwk.util.property;

import java.io.File;

public class PropertyDesc {

    private final String name;
    private final PropertyType type;
    private final String desc;

    private final boolean mandatory;
    private final boolean mustexists;

    public PropertyDesc(String name, boolean mandatory, PropertyType type, boolean mustexists, String desc) {
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.mandatory = mandatory;
        this.mustexists = mustexists;
    }

    public boolean isValid(final String value) {
        switch (this.type) {
            case file:
                return isValidFile(value);
            case folderAbsolute:
                return isValidAbsoluteFolder(value);
            case folderRelative:
                return isValidRelativeFolder(value);
            case number:
                return isValidNumber(value);
            case string:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidFile(final String value) {
        boolean result = true;
        final File file = new File(value);
        if (isMustexists()) {
            result = file.exists() && file.canRead();
        }
        return result;
    }

    private boolean isValidAbsoluteFolder(String value) {
        boolean result = true;
        final File file = new File(value);
        if (isMustexists()) {
            result = file.exists() && file.canRead() && file.isDirectory() && file.isAbsolute();
        }
        return result;
    }

    private boolean isValidRelativeFolder(String value) {
        boolean result = true;
        final File file = new File(value);
        if (isMustexists()) {
            result = file.exists() && file.canRead() && file.isDirectory() && !file.isAbsolute();
        }
        return result;
    }

    private boolean isValidNumber(final String value) {
        boolean result = true;
        try {
            final int i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            result = false;
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public PropertyType getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isMustexists() {
        return mustexists;
    }

    @Override
    public String toString() {
        return "PropertyDesc{" + "name=" + name + ", type=" + type + ", desc=" + desc + ", mandatory=" + mandatory + ", mustexists=" + mustexists + '}';
    }
}
