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

import ivar.metamodel.spec.Scenario;
import java.util.List;
import ivar.helper.CollectionFactory;

public class FetchPath {

    private FetchPathType type;
    private Scenario scenario;
    private List<BusinessObjectAttribute> attributes;
    private String path;
    private List<BusinessObjectAttribute> fileAttributes;

    public FetchPath(final FetchPathType type, final Scenario scenario, final String path) {
        this.type = type;
        this.scenario = scenario;
        this.path = path;
        attributes = CollectionFactory.newList();
        fileAttributes = CollectionFactory.newList();
    }

    public FetchPathType getType() {
        return type;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<BusinessObjectAttribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(final BusinessObjectAttribute boa) {
        attributes.add(boa);
    }

    public String getKey() {
        return "scenario=" + scenario.keyname + ":type=" + type.getAsString() + ":path=" + path;
    }

    public void free() {
        attributes.clear();
        attributes = null;
        type = null;
    }

    public boolean hasFileAttributes() {
        return fileAttributes != null && fileAttributes.size() > 0;
    }

    public List<BusinessObjectAttribute> getFileAttributes() {
        return fileAttributes;
    }

    public void addFileAttribute(final BusinessObjectAttribute attr) {
        fileAttributes.add(attr);
    }
}
