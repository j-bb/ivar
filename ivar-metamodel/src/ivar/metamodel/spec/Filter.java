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

import ivar.metamodel.MetaObject;
import java.util.List;
import java.util.Set;
import ivar.helper.CollectionFactory;

public class Filter extends MetaObject {

    private FilterModes mode = FilterModes.dynamicf;
    private Set<FilterElement> filterElements = CollectionFactory.newSetWithInsertionOrderPreserved();

    public void addElement(final FilterElement element) {
        filterElements.add(element);
    }

    public FilterModes getMode() {
        return mode;
    }

    public boolean isDynamic() {
        return FilterModes.dynamicf.equals(mode);
    }

    public boolean isStatic() {
        return FilterModes.staticf.equals(mode);
    }

    public void setMode(String mode) {
        mode = mode.toLowerCase();
        if (mode.startsWith("static")) {
            this.mode = FilterModes.staticf;
        } else if (mode.startsWith("dynamic")) {
            this.mode = FilterModes.dynamicf;
        }
    }

    public void setFilterElementParamName() {
        for (final FilterElement filterElement : filterElements) {
            filterElement.setParamName(filterElements);
        }
    }

    public boolean isChecked() {
        boolean result = true;
        for (final FilterElement filterElement : filterElements) {
            if (!filterElement.isBooleanOperator()) {
                final boolean c = filterElement.isChecked();
                if (!c) {
                    compileError("Unchecked filter element : " + filterElement.getKeyname() + " " + filterElement.getOperator() + " " + filterElement.getValue());
                }
                result = result && c;
            }
        }
        return result;
    }

    public Set<FilterElement> getFilterElements() {
        return filterElements;
    }

    public Filter getJPAClone() {
        return getJPAClone(new Filter());
    }

    protected Filter getJPAClone(Filter dst) {
        super.getJPAClone(dst);
        dst.mode = mode;

        dst.filterElements.clear();
        for (final FilterElement filterElement : filterElements) {
            dst.filterElements.add(filterElement.getJPAClone());
        }

        return dst;
    }

    public void free() {
        mode = null;
        for (FilterElement element : filterElements) {
            element.free();
        }
        filterElements.clear();
        filterElements = null;
    }

    public boolean hasRuntimeVariable() {
        boolean result = false;
        for (final FilterElement filterElement : filterElements) {
            if (filterElement.isRuntimeVariable()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public List<String> getRuntimeVariables() {
        List<String> result = CollectionFactory.newList();
        for (final FilterElement filterElement : filterElements) {
            if (filterElement.isRuntimeVariable()) {
                result.add(filterElement.getRuntimeVariable());
            }
        }
        return result;
    }
}
