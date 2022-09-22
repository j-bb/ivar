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

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.metamodel.spec.Filter;
import ivar.metamodel.spec.FilterElement;
import ivar.metamodel.spec.FilterModes;
import ivar.metamodel.target.TargetFilter;
import java.util.List;
import java.util.Set;

public class BlankFilter extends AbstractObject implements TargetFilter {

    private Filter filter;

    private Set<BlankFilterElement> filterElements = CollectionFactory.newSetWithInsertionOrderPreserved();

    public BlankFilter(final Filter filter) {
        this.filter = filter;
        for (final FilterElement filterElement : filter.getFilterElements()) {
            filterElements.add(new BlankFilterElement(filterElement));
        }
    }

    public boolean hasRuntimeVariable() {
        return filter.hasRuntimeVariable();
    }

    public List<String> getRuntimeVariables() {
        return filter.getRuntimeVariables();
    }

    public FilterModes getMode() {
        return filter.getMode();
    }

    public boolean isDynamic() {
        return FilterModes.dynamicf.equals(filter.getMode());
    }

    public String getName() {
        return filter.getName();
    }

    public String getDocumentation() {
        return filter.getDocumentation();
    }

    public String getComment() {
        return filter.getComment();
    }

    public String getKeyname() {
        return filter.getKeyname();
    }

    public Set<BlankFilterElement> getFilterElements() {
        return filterElements;
    }

    public void free() {
        filter = null;

        filterElements.clear();
        filterElements = null;
    }
}
