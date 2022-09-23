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
package ivar.metamodel;

import ivar.helper.StringHelper;

public abstract class MetaObject extends IdentifiableObject {

    public String name;
    public String keyname;

    /**
     * This is a functional description. It will be used in the generated application could be tooltips ot help key.
     */
    private String documentation;

    /**
     * A comment is not visible to the end user in the generated application. However, tt may be read in the generated application by the role reviwer with meta
     * key
     */
    private String comment;

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
//        if (name.length() > DBHardCodedLimit.stdIdentifier) {
//            throw new IllegalKeynameException(ClassHelper.getShortName(getClass()) + ".setName : length error. Length must ne less than " + DBHardCodedLimit.stdIdentifier + " and " + name + " has " + name.length());
//        }
        this.name = name;
    }

    public final String getDocumentation() {
        return documentation;
    }

    public final void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public final String getComment() {
        return comment;
    }

    public final void setComment(String comment) {
        this.comment = comment;
    }

    public final String getKeyname() {
        return StringHelper.normalize(keyname);
    }

    public final String getKeynameFirstCapitalized() {
        return StringHelper.getFirstCapitalizedAndLoweredTheRest(keyname);
    }

    public final void setKeyname(String keyname) {
        keyname = StringHelper.normalize(keyname);
//        if (keyname.length() > DBHardCodedLimit.stdTechIdentifier) {
//            throw new IllegalKeynameException(ClassHelper.getShortName(getClass()) + ".setKeyname : length error. Length must ne less than " + DBHardCodedLimit.stdTechIdentifier + " and " + keyname + " has " + keyname.length());
//        }

        this.keyname = keyname;
    }

    /**
     * Clones all *non-transient* attributes of this instance, except id, and copies them into a destination instance. All transient attributes and the id of
     * the destination object will be preserved.
     *
     * @param dst The destination object, whose attributes will be filled with clones of the ones of this object.
     * @return the modified destination argument.
     */
    protected MetaObject getJPAClone(final MetaObject dst) {
        super.getJPAClone(dst);
        if (dst != null) {
            if (keyname != null) {
                dst.keyname = new String(keyname);
            } else {
                dst.keyname = null;
            }

            if (name != null) {
                dst.name = new String(name);
            } else {
                dst.name = null;
            }

            if (documentation != null) {
                dst.documentation = new String(documentation);
            } else {
                dst.documentation = null;
            }

            if (comment != null) {
                dst.comment = new String(comment);
            } else {
                dst.comment = null;
            }
        }

        return dst;
    }

    @Override
    public String toString() {
        return "MetaObject{"
                + " name='" + name + '\''
                + ", keyname='" + keyname + '\''
                + '}';
    }
}
