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
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import ivar.metamodel.spec.StepConstraint;
import ivar.metamodel.spec.StepTypes;
import ivar.helper.oo.ClassHelper;

public class BusinessObjectType extends AbstractObject {

    private String name;
    private boolean builtIn = false;
    private StepConstraint constraints;
    private BusinessObject businessObject;
    private Step step;

    public BusinessObjectType(final Step step, BusinessObjectAttribute businessObjectAttribute) {
        this.step = step;
        if (step.getStepConstraint().isScenarioReference()) {
            final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
            setName(referencedScenario.getData());
        } else {
            setName(step.getStepConstraint().getType());
        }
        constraints = step.getStepConstraint();

        if (!businessObjectAttribute.isDataset()) {
            try {
                StepTypes.valueOf(name);
            } catch (IllegalArgumentException e) {
                // Nothing to do, it mean that it is not a primitive constraints.
            }
            builtIn = constraints.isAtomic();
        } else {
            builtIn = true;
        }
    }

    /**
     * Return true if type is one of text, integer, ... bolis type.
     */
    // TODO optimise algo. build table to store result, ...
    public boolean isBuiltIn() {
        return builtIn;
    }

    public boolean isTemporal() {
        return StepTypes.Date.toString().equals(getType()) || StepTypes.DateTime.toString().equals(getType()) || StepTypes.Time.toString().equals(getType());
    }

    public boolean isText() {
        final String type = getType();
        return isBuiltIn() && (type.equals("Text") || type.equals("Multitext") || type.equals("Formatedtext"));
    }

    public String getType() {
        if (step.getStepConstraint().isScenarioReference()) {
            final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
            return referencedScenario.getData();
        } else {
            return constraints.getType();
        }
    }

    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    private void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public void setBusinessObject(final BusinessObject businessObject) {
        this.businessObject = businessObject;
    }

    public BusinessObject getBusinessObject() {
        //TODO clean that exception when we'll have money
        if (isBuiltIn() && businessObject != null) {
            throw new RuntimeException("Inconsistency error " + ClassHelper.getShortName(getClass()) + " : businessObject attribute is set while isBuiltIn is true.");
        }
        return businessObject;
    }

    public boolean equals(BusinessObjectType obj) {
        boolean result = false;
        if (name != null) {
            result = name.equals(obj.getName());
        }
        return result;
    }

    public void free() {
        constraints.free();
        constraints = null;
        businessObject = null;
        step = null;
    }
}
