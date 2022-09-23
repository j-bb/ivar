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
 */package ivar.metamodel.target.generic.ui;

import ivar.metamodel.spec.Step;
import java.util.Collection;
import ivar.helper.CollectionFactory;

public class ScreenFieldGroup extends ScreenField {

    public ScreenFieldGroup(final Step step) {
        super(step);
    }

    /**
     * A Collection of AbstractField.
     */
    private Collection<ScreenField> fields = CollectionFactory.newList();

    private ScreenField parent = null;

    public Collection<ScreenField> getFields() {
        return fields;
    }

    public ScreenField getParent() {
        return parent;
    }

    public void addField(ScreenField screenField) {
        fields.add(screenField);
    }

    /**
     * @param screenField
     */
    public void setParent(ScreenField screenField) {
        parent = screenField;
    }
}
