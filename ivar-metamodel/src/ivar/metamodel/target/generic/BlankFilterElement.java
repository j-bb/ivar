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
package ivar.metamodel.target.generic;

import ivar.metamodel.spec.FilterElement;
import ivar.metamodel.spec.FilterOperators;
import java.util.List;

public class BlankFilterElement {

    private FilterElement filterElement;

    public BlankFilterElement(final FilterElement filterElement) {
        this.filterElement = filterElement;
    }

    public List<BusinessObjectAttribute> getAssociatedBusinessObjectAttributesPath() {
        return filterElement.getAssociatedBusinessObjectAttributesPath();
    }

    public String getAssociatedBusinessObjectAttributesPathAsString() {
        return filterElement.getAssociatedBusinessObjectAttributesPathAsString();
    }

    public Integer getPos() {
        return filterElement.getPos();
    }

    public String getKeyname() {
        return filterElement.getKeyname();
    }

    public FilterOperators getOperator() {
        return filterElement.getOperator();
    }

    public String getValue() {
        return filterElement.getValue();
    }

    public String getParamName() {
        return filterElement.getParamName();
    }

    public boolean isBooleanOperator() {
        return filterElement.isBooleanOperator();
    }

    public boolean isBooleanValue() {
        return filterElement.isBooleanValue();
    }

    public boolean hasValue() {
        return filterElement.hasValue();
    }

    public DataSet getDataset() {
        return filterElement.getDataset();
    }

    public boolean isDataset() {
        return filterElement.isDataset();
    }

    public boolean isMandatory() {
        return filterElement.isMandatory();
    }

    public void free() {
        filterElement = null;
    }

    public boolean isRuntimeVariable() {
        return filterElement.isRuntimeVariable();
    }

    public String getRuntimeVariable() {
        return filterElement.getRuntimeVariable();
    }
}
