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
package ivar.metamodel.target.java;

public class FetchPlanElement {

    private String type;
    private String name;
    private boolean isTransient;

    public FetchPlanElement(final String type, final String name) {
        this(type, name, false);
    }

    public FetchPlanElement(final String type, final String name, final boolean transience) {
        setType(type);
        setName(name);
        setTransient(transience);
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(final boolean isTransient) {
        this.isTransient = isTransient;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(final Object other) {
        boolean result = false;
        if (other != null) {
            if (other instanceof FetchPlanElement) {
                final FetchPlanElement otherElem = (FetchPlanElement) other;
                result = name == otherElem.name && type == otherElem.type;
            }
        }
        return result;
    }
}
