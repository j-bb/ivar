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
package ivar.metamodel.spec;

import ivar.metamodel.IdentifiableObject;

public class DatasetData extends IdentifiableObject {

    private String value;
    private boolean defaultValue = false;
    private boolean isNull = false;
    private Integer position = 0;

    public DatasetData() {
    }

    public DatasetData(final String value) {
        this(value, false);
    }

    public DatasetData(final String keyname, final boolean defaultValue) {
        this.value = keyname;
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public boolean isNull() {
        return isNull;
    }

    public DatasetData getJPAClone() {
        return getJPAClone(new DatasetData());
    }

    protected DatasetData getJPAClone(DatasetData dst) {
        super.getJPAClone(dst);
        if (value != null) {
            dst.value = new String(value);
        } else {
            dst.value = null;
        }

        dst.defaultValue = defaultValue;
        dst.isNull = isNull;
        dst.position = position;

        return dst;
    }
}
