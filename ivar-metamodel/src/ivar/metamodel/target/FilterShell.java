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
package ivar.metamodel.target;

import ivar.helper.StringHelper;

public class FilterShell<F extends TargetFilter> {

    protected F staticFilter;
    protected F dynamicFilter;
    private String scenarioKeyname;

    public FilterShell(final String scenarioKeyname) {
        this.scenarioKeyname = scenarioKeyname;
    }

    public String getServerMethodName() {
        return StringHelper.getFirstCapitalized(scenarioKeyname);
    }

    public F getStaticFilter() {
        return staticFilter;
    }

    public void setStaticFilter(F staticFilter) {
        this.staticFilter = staticFilter;
    }

    public F getDynamicFilter() {
        return dynamicFilter;
    }

    public void setDynamicFilter(F dynamicFilter) {
        this.dynamicFilter = dynamicFilter;
    }

    public boolean hasStatic() {
        return staticFilter != null;
    }

    public boolean hasDynamic() {
        return dynamicFilter != null;
    }

    public String getScenarioKeyname() {
        return scenarioKeyname;
    }

    public void free() {
        if (dynamicFilter != null) {
            dynamicFilter.free();
            dynamicFilter = null;
        }
        if (staticFilter != null) {
            staticFilter.free();
            staticFilter = null;
        }
    }
}
