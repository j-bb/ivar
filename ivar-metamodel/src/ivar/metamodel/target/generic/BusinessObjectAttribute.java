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
import ivar.metamodel.spec.Rule;
import ivar.metamodel.spec.RuleException;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;

public class BusinessObjectAttribute extends AbstractObject implements OnScreenDataAtom {

    private String databasename;
    private String name;
    private String doc;
    private BusinessObjectType type;
    private String min;
    private String max;
    private Integer minInt;
    private Integer maxInt;
    private boolean toMany = false;
    private boolean calculated = false;
    private boolean mandatory = false;
    private Set<Step> steps = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Step originatorStep;
    private BusinessObject businessObject;
    private boolean isDataset = false;
    private DataSet dataset;
    private String columnName;
    private Step step;

    public BusinessObjectAttribute(final Scenario scenario, final Step step, final BusinessObject businessObject) {
        this.step = step;
        name = step.getKeyname();
        calculated = step.isCalculated();
        isDataset = step.getStepConstraint().isDataset();
        if (isDataset) {
            dataset = step.getStepConstraint().getReferencedDataset();
            debug(scenario.getKeyname() + "." + getName() + " is a reference to dataset of " + dataset.getKeyname());
        }

        type = new BusinessObjectType(step, this);

        setMin(step.getMin());
        setMax(step.getMax());
        setMandatory(step.isMandatory());

        this.businessObject = businessObject;
        addStep(step);
        this.originatorStep = step;

        databasename = step.getTechkeyname();

        debug(toString() + ", builtIn= " + getType().isBuiltIn());
        debug("detecting a new attribute : " + getType() + " " + getName() + " for " + businessObject.getFullName());
    }

    public String getBusinessObjectName() {
        return businessObject.getNameFirstCapitalized();
    }

    ///////////
    /// BEGIN smelling section for templates
    ///////////
    // TODO clean that big hack who smell. This is because either ScreenField and BusinessObjectAttribute must be able to go throw include-formular-fields.vm
    public final boolean hasJumps() {
        return false;
    }

    public final String getStepTypeForDebug() {
        return type.getType();
    }

    public final boolean hasTooltip() {
        return false;
    }

    ///////////
    /// END smelling section for templates
    ///////////
    public String getTypeName() {
        if (step.getStepConstraint().isScenarioReference()) {
            final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
            return referencedScenario.getData();
        } else {
            return type.getName().toLowerCase();
        }
    }

    public String getTypeFirstCapitalized() {
        if (step.getStepConstraint().isScenarioReference()) {
            final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
            return StringHelper.getFirstCapitalized(referencedScenario.getData());
        } else {
            return StringHelper.getFirstCapitalized(type.getName());
        }
    }

    public void addStep(final Step step) {
        this.steps.add(step);
        final String techkeyname = step.getTechkeyname();
        if (techkeyname != null && techkeyname.length() > 0) {
            if (databasename != null && databasename.length() > 0) {
                if (!databasename.equals(techkeyname)) {
                    error("techkeyname must be " + databasename + " found " + techkeyname + " in step " + step.getKeyname(), true);
                } else {
                    databasename = techkeyname;
                }
            } else {
                databasename = techkeyname;
            }
        }
    }

    private Step getArtificialStepForDefaultSearchScreen(final boolean showDebug) throws RuleException {
        // TODO proper "merge" to get the most expressive step for generic search.
        final Step result = originatorStep.getJPAClone();
        result.setSynthetic();
        final Map<String, Rule> intermediateResult = CollectionFactory.newMap();
        fillIntermediateMapOfRules(intermediateResult, result, showDebug);

        // This is to ensure that no duplicate rule will be added.
        if (showDebug) {
            beginBlock("ensure that no duplicate rule had been added");
        }
        for (final Step step : steps) {
            fillIntermediateMapOfRules(intermediateResult, step, showDebug);
        }
        if (showDebug) {
            endBlock();
        }

        result.setRules(intermediateResult);

        return result;
    }

    private void fillIntermediateMapOfRules(final Map<String, Rule> intermediateResult, final Step step, final boolean showDebug) {
        if (showDebug) {
            debug("Control for getArtificialStepForDefaultSearchScreen() : BO attribute " + getType().getName() + " " + getName() + " --step type--> " + step.getKeyname() + " " + step.getStepConstraint());
        }
        final Collection<Rule> stepRules = step.getRules();
        for (Rule rule : stepRules) {
            intermediateResult.put(rule.getKeyname(), rule);
        }
    }

    public BusinessObjectType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getKeyname() {
        return getName();
    }

    public String getNameFirstCapitalized() {
        return StringHelper.getFirstCapitalized(getName());
    }

    public boolean isBuiltIn() {
        return getType().isBuiltIn();
    }

    public boolean isCollection() {
        return maxInt > 1;
    }

    public boolean isToMany() {
        return isCollection() && toMany;
    }

    public boolean isToOne() {
        return isCollection() && !toMany;
    }

    public void setToMany(boolean toMany) {
        this.toMany = toMany;
        debug(getBusinessObject().getFullName() + "." + getKeyname() + " -> " + getType().getBusinessObject().getFullName() + " setToMany(" + toMany + ")");
    }

    public String getMin() {
        return min;
    }

    private void setMin(String min) {
        this.min = min;
        this.minInt = Integer.decode(min);
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
        if ("*".equals(max)) {
            maxInt = Integer.MAX_VALUE;
        } else {
            maxInt = Integer.decode(max);
        }
    }

    public void setMinInt(Integer minInt) {
        this.minInt = minInt;
        this.min = minInt.toString();
    }

    public void setMaxInt(Integer maxInt) {
        this.maxInt = maxInt;
        if (maxInt == Integer.MAX_VALUE) {
            max = "*";
        } else {
            this.max = maxInt.toString();
        }
    }

    public int getMinInt() {
        return minInt;
    }

    public int getMaxInt() {
        return maxInt;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getDoc() {
        return doc;
    }

    public void addDoc(final String doc) {
        if (doc != null) {
            if (this.doc == null) {
                this.doc = doc;
            } else {
                this.doc += "\n" + doc;
            }
        }
    }

    public Integer getMaxLengthConstraint() {
        return getMaxNumericConstraint("length");
    }

    public Integer getMaxScaleConstraint() {
        return getMaxNumericConstraint("scale");
    }

    public Integer getMaxPrecisionConstraint() {
        return getMaxNumericConstraint("precision");
    }

    public boolean isHash() {
        boolean result = false;
        for (final Step step : steps) {
            if (step.hasConstraint("hash")) {
                result = true;
                break;
            }
        }

        return result;
    }

    public boolean isUnique() {
        boolean res = false;
        for (final Step step : steps) {
            if (step.hasConstraint("unique") && step.getConstraint("unique").equals(true)) {
                res = true;
                break;
            }
        }
        return res;
    }

    private Integer getMaxNumericConstraint(final String name) {
        Integer res = null;
        for (final Step step : steps) {
            final Integer stepLength = (Integer) step.getConstraint(name);
            if (stepLength != null && (res == null || stepLength > res)) {
                res = stepLength;
            }
        }

        return res;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public boolean isUpToStar() {
        return getMax().equals("*");
    }

    public DataSet getDataset() {
        return dataset;
    }

    public int getMaxDatasetLength() {
        return dataset.getDataset().getMaxDataLength();
    }

    public boolean isDataset() {
        return isDataset;
    }

    public String getCalculatedRuleCode() {
        return originatorStep.getCalculatedRule().getValue();
    }

    public BusinessObject getBusinessObject() {
        return businessObject;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Set<Step> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        final boolean documented = getDoc() != null && getDoc().length() > 1;
        return "(" + min + ".." + max + ") " + type + " " + name + ", documented= " + documented;
    }

    @Override
    public boolean isScreenReference() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isScenarioReference();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    public boolean isFile() {
        return originatorStep.getStepConstraint().isFile();
    }

    @Override
    public boolean isText() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isText();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isPassword() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isPassword();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isBoolean() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isBoolean();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isNumber() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isNumber();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isMultitext() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isMultitext();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isFormatedtext() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isFormatedtext();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isInteger() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isInteger();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isReal() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isReal();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isAmount() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isAmount();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isPercentile() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isPercentile();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isDate() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isDate();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isTime() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isTime();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public boolean isDateTime() {
        try {
            return getArtificialStepForDefaultSearchScreen(false).getStepConstraint().isDateTime();
        } catch (RuleException e) {
            error(e);
            return false;
        }
    }

    @Override
    public String getDebugInfo() {
        try {
            return getKeyname() + "  (I'm a BusinessObjectAttribute) " + getArtificialStepForDefaultSearchScreen(false).getStepConstraint().toString();
        } catch (RuleException e) {
            error(e);
            return getKeyname() + " (I'm a BusinessObjectAttribute) error during getting constraint debug info : " + e.getMessage();
        }
    }

    public void free() {
        type.free();
        type = null;
        steps.clear();
        steps = null;
        originatorStep = null;
        businessObject = null;
        if (dataset != null) {
            dataset.free();
            dataset = null;
        }
        step = null;
    }
}
