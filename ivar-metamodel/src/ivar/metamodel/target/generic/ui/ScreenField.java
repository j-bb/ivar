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
package ivar.metamodel.target.generic.ui;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.spec.*;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.DataSet;
import ivar.metamodel.target.generic.OnScreenDataAtom;
import java.util.Collection;
import java.util.Set;

public class ScreenField extends AbstractObject implements OnScreenDataAtom {

    /*
   Ajouter une collection de ScreenField correspondant aux EmbededStep dans les scenario.
   Tout ScreenfIeld de la meme collection (par exemple street, code, town qui appartiennent a Address)
   doivent avoir un rendu graphique avec un TitledBorder.
     */
    private Step step;

    private String rule;

    private String keyname;

    private StepConstraint constraints;

    private Collection<ScreenField> dependencies = CollectionFactory.newList();
    private Collection<SumDescriptor> sums = CollectionFactory.newList();

    private BusinessObjectAttribute attribute = null;

    private Screen screen = null;

    private Screen referencedScreen = null;

    public ScreenField(final Step step) {
        this.step = step;
        this.keyname = step.getKeyname();
        this.constraints = step.getStepConstraint();
    }

    public String getName() {
        return step.getName();
    }

    public String getNameFirstCapitalized() {
        return StringHelper.getFirstCapitalizedAndLoweredTheRest(getName());
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(final String keyname) {
        this.keyname = keyname;
    }

    public String getKeynameFirstCapitalized() {
        return StringHelper.getFirstCapitalizedAndLoweredTheRest(getKeyname());
    }

    public String getType() {
        return step.getStepConstraint().getType();
    }

    public String getTypeFirstCapitalized() {
        return StringHelper.getFirstCapitalized(getType());
    }

    public String getBusinessObjectName() {
        return attribute.getBusinessObjectName();
    }

    //TODO push that to a JavaScreenField class !
    public String getJavaName() {
        return StringHelper.getFirstCapitalized(step.getName());
    }

    public boolean isScreenReference() {
        return step.getStepConstraint().isScenarioReference();
    }

    public boolean isData() {
        return step.getStepConstraint().isData();
    }

    public String getDataTargetBusinessObject() {
        return getAttribute().getTypeFirstCapitalized();
    }

    public String getStepTypeForDebug() {
        return step.getStepConstraint().toString();
    }

    private Screen findReferencedScreen() {
        Screen result = null;
        if (isScreenReference()) {
            final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
            if (referencedScenario != null) {
                result = referencedScenario.getAssociatedScreen();
            } else {
                compileError("Unable to find scenario referenced by step " + step, true);
            }
        }
        return result;
    }

    public ScreenField disentanglingClone(final Screen screen) {
        final ScreenField result = new ScreenField(step);
        result.setScreen(screen);
        result.setAttribute(attribute);
        if (result.isScreenReference()) {
            beginBlock("Found a screen reference in screen " + screen.getKeyname() + " for field " + getKeyname() + ". Proceeding to disentanglement.");
            result.cloneReferencedScreen();
            endBlock("Screen reference to " + result.referencedScreen.getKeyname() + "by field " + getKeyname() + " in screen " + screen.getKeyname() + " succesfully disentangled.");
        }
        return result;
    }

    public void cloneReferencedScreen() {
        final Screen referencedScreen = findReferencedScreen();
        if (referencedScreen != null) {
            setReferencedScreen(referencedScreen.disentanglingClone());
        }
    }

    public boolean isCollection() {
        return step.isCollection();
    }

    public boolean isCalculated() {
        return step.isCalculated();
    }

    public boolean isCondition() {
        return step.isCondition();
    }

    public boolean isConstantRule() {
        return step.isConstantRule();
    }

    public boolean isDataset() {
        return step.getStepConstraint().isDataset();
    }

    public boolean isBuiltIn() {
        return attribute.isBuiltIn();
    }

    public boolean isMandatory() {
        return step.isMandatory();
    }

    public DataSet getDataset() {
        return attribute.getDataset();
    }

    public int getSize() {
        int result = -1;
        Object rule = null;
        rule = step.getRule("size");

        if (rule instanceof Integer) {
            result = (Integer) rule;
            info(getName() + " has a size of " + result);
        } else {
            info(getName() + " has no size");
        }
        return result;
    }

    public String getTooltip() {
        String doc = step.getDocumentation();
        if (doc != null) {
            doc = doc.replace("\"", "\\\"");
            doc = doc.replace("'", "\'");
            if (doc.contains("\n")) {
                final String[] lines = doc.split("\n");
                doc = "<html><body>";
                for (final String line : lines) {
                    doc += line;
                    doc += "<br/>";
                }
                doc += "</body></html>";
            }
        }
        return doc;
    }

    public boolean hasRichTooltip() {
        return hasTooltip() && getTooltip().startsWith("<html>");
    }

    public boolean hasTooltip() {
        return step.getDocumentation() != null && step.getDocumentation().length() > 1;
    }

    public Set<Rule> getRulesForDependencies() {
        return step.getRulesForDependencies();
    }

    public void addDependency(ScreenField sf) {
        dependencies.add(sf);
    }

    public void addSumDescripton(final SumDescriptor descriptor) {
        sums.add(descriptor);
    }

    public Collection<SumDescriptor> getSums() {
        return sums;
    }

    public boolean hasSum() {
        return sums.size() > 0;
    }

    public Collection<ScreenField> getDependencies() {
        return dependencies;
    }

    public boolean hasDependency() {
        return dependencies != null && dependencies.size() > 0;
    }

    public String getMax() {
        return step.getMax();
    }

    public BusinessObjectAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(BusinessObjectAttribute attribute) {
        if (this.attribute == null) {
            this.attribute = attribute;
        } else {
            //TODO refactor when we'll have money
            throw new RuntimeException("Illegal attempt to set BusinessObjectAttribute (" + attribute.getNameFirstCapitalized() + ") : value already set to " + attribute.getNameFirstCapitalized() + " in ScreenField " + getKeyname());
        }
    }

    public String getCondition() {
        return step.getConditionRule().getValue();
    }

    @Override
    public String toString() {
        return "ScreenField{"
                + "keyname =" + getKeyname()
                + "step =" + step
                + ", dependencies =" + dependencies
                + ", isDate =" + isDate()
                + ", attribute =" + attribute
                + ", isCollection = " + isCollection()
                + ", isBuild-in = " + isBuiltIn()
                + '}';
    }

    public boolean isText() {
        return constraints.isText();
    }

    public boolean isPassword() {
        return constraints.isPassword();
    }

    public boolean isBoolean() {
        return constraints.isBoolean();
    }

    public boolean isNumber() {
        return constraints.isNumber();
    }

    public boolean isMultitext() {
        return constraints.isMultitext();
    }

    public boolean isFormatedtext() {
        return constraints.isFormatedtext();
    }

    public boolean isInteger() {
        return constraints.isInteger();
    }

    public boolean isReal() {
        return constraints.isReal();
    }

    public boolean isAmount() {
        return constraints.isAmount();
    }

    public boolean isPercentile() {
        return constraints.isPercentile();
    }

    public boolean isDate() {
        return constraints.isDate();
    }

    public boolean isTime() {
        return constraints.isTime();
    }

    public boolean isDateTime() {
        return constraints.isDateTime();
    }

    public boolean isFile() {
        return constraints.isFile();
    }

    public boolean isImage() {
        return constraints.isImage();
    }

    public boolean isNumberKind() {
        return isNumber() || isReal() || isInteger();
    }

    public boolean isTextual() {
        return isText() || isFormatedtext() || isMultitext();
    }

    public boolean isTemporal() {
        return isDate() || isDateTime() || isTime();
    }

    public String getDebugInfo() {
        return getKeyname() + " (I'm a ScreenField) " + constraints.toString();
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public boolean hasJumps() {
        boolean result = false;
        final Set<Jump> jumps = step.getJumps();
        if (jumps != null) {
            result = jumps.size() > 0;
        }
        return result;
    }

    public boolean hasDefaultDeletor() {
        boolean result = false;
        if (hasJumps()) {
            for (final Jump jump : step.getJumps()) {
                if (jump.isCreate()) {
                    result = true;
                } else if (jump.isDelete()) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    public Set<Jump> getJumps() {
        return step.getJumps();
    }

    public boolean hasMaxiJumps() {
        return step.hasMaxiJumps();
    }

    public Jump getSmallSelector() {
        Jump result = null;
        if (hasJumps()) {
            for (final Jump jump : getJumps()) {
                if (jump.isSearch() && (jump.isSmall() || jump.isXSmall())) {
                    result = jump;
                    break;
                }
            }
        }
        return result;
    }

    public boolean hasSmallSelector() {
        return getSmallSelector() != null;
    }

    private void setReferencedScreen(final Screen screen) {
        referencedScreen = screen;
    }

    public Screen getReferencedScreen() {
        return referencedScreen;
    }

    public String getDataKeyname() {
        return step.getKeyname();
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Object getConstraint(final String name) {
        return step.getConstraint(name);
    }

    public boolean hasConstraint(final String name) {
        return step.hasConstraint(name);
    }

    public Object getEscapedConstraint(final String name) {
        Object returnVal = step.getConstraint(name);
        if (returnVal instanceof String) {
            returnVal = StringHelper.getWithEscapedDoubleQuotes((String) returnVal);
        }

        return returnVal;
    }

    public boolean hasDefaultValue() {
        return hasConstraint("defaultValue");
    }

    public void free() {
        step = null;

        constraints = null;

        dependencies.clear();
        dependencies = null;

        attribute = null;

        screen = null;

        if (referencedScreen != null) {
            referencedScreen.free();
            referencedScreen = null;
        }
    }
}
