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

import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.SpecElement;
import ivar.metamodel.target.ContextualizedScenarios;
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.ui.Screen;
import java.util.Objects;
import java.util.Set;

public class Scenario extends SpecElement {

    private String techdata = null;
    private String section = null;
    private CRUD crud = null;
    private String data = null;
    public String parentKeyname = null;
    public boolean audit = false;

    /*
   A synthetic scenario is a scenario created at runtime by the compiler.
   This is done because a step can reference a scenario.
   That referenced scenario may not have the correct CRUD
   so a synthetic scenario is created with the right CRUD.
   Example :
   In a scenario READ A, a step reference not just B but a scenario CREATE B with bbb keyname.
   This is done so only step defined in bbb will be kept in A instead of all B's attributes.
   Unfortunatly, bbb is a CREATE scenario.
   A synthetic scenario bbb_READ will be created.

   This atribute would contain bbb so we know bbb_create come from bbb.
   This is used in the BusinessObjectGenerator to find bbb (bbb_READ is excluded from BO computation)
   and set the RootBusinessObject of bbb to bbb_READ.
     */
    private transient String synthetic = null;

    private transient BusinessObject rootBusinesObject;

    private Set<Step> steps = CollectionFactory.newSetWithInsertionOrderPreserved();

    private Set<Rule> rules = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Set<Jump> jumps = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Filter staticFilter;
    private Filter dynamicFilter;

    // if true, the collection of roles is the list of authorized roles. Else it is the list of excluded roles
    private boolean rolesAllowed = true;

    private Set<String> roles = CollectionFactory.newSet();

    private transient Application application;

    private transient Screen associatedScreen = null;

    private transient boolean inheritenceComputed = false;

    private transient ContextualizedScenarios subsequentScenarios = null;

    public Scenario() {
    }

    public Scenario(final String keyname, final String name, final String CRUD, final String data) {
        setKeyname(keyname);
        setName(name);
        setCrud(CRUD);
        setData(data);
    }

    public void addStep(final Step step) throws DuplicateStepException {
        for (final Step existingStep : steps) {
            if (step.getKeyname().equals(existingStep.getKeyname())) {
                throw new DuplicateStepException(existingStep, step);
            }
        }
        steps.add(step);
    }

    public CRUD getCrud() {
        return crud;
    }

    public void setCrud(CRUD crud) {
        this.crud = crud;
    }

    public void setCrud(final String crud) {
        this.crud = CRUD.valueOf(crud);
        if (this.crud == null) {
            throw new IllegalArgumentException("CRUD must be Create, Read, Update, Delete. " + crud + " is unknown");
        }
    }

    public boolean isSynthetic() {
        return synthetic != null;
    }

    public void setSynthetic(final String synthetic) {
        this.synthetic = synthetic;
    }

    public String getSynthetic() {
        return synthetic;
    }

    public Scenario getJPAClone() {
        return getJPAClone(new Scenario());
    }

    protected Scenario getJPAClone(final Scenario dst) {
        super.getJPAClone(dst);

        if (techdata != null) {
            dst.techdata = new String(techdata);
        } else {
            dst.techdata = null;
        }

        if (parentKeyname != null) {
            dst.parentKeyname = new String(parentKeyname);
        } else {
            dst.parentKeyname = null;
        }

        if (section != null) {
            dst.section = new String(section);
        } else {
            dst.section = null;
        }

        dst.crud = crud;

        if (data != null) {
            dst.data = new String(data);
        } else {
            dst.data = null;
        }

        dst.steps.clear();
        for (final Step step : steps) {
            dst.steps.add(step.getJPAClone());
        }

        dst.rules.clear();
        for (final Rule rule : rules) {
            dst.rules.add(rule.getJPAClone());
        }

        if (staticFilter != null) {
            dst.staticFilter = staticFilter.getJPAClone();
        } else {
            dst.staticFilter = null;
        }

        if (dynamicFilter != null) {
            dst.dynamicFilter = dynamicFilter.getJPAClone();
        } else {
            dst.dynamicFilter = null;
        }

        if (roles != null) {
            dst.roles.clear();
            for (final String role : roles) {
                if (!"public".equals(role)) {
                    dst.roles.add(new String(role));
                }
            }
        } else {
            dst.roles = null;
        }

        if (jumps != null) {
            dst.jumps.clear();
            for (final Jump jump : jumps) {
                dst.jumps.add(jump.getJPAClone());
            }
        } else {
            dst.jumps = null;
        }

        if (application != null) {
            dst.setApplication(application);
        }

        return dst;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("scenario ");
        sb.append(getSection() == null ? "" : getSection());
        sb.append(" [");
        sb.append(getKeyname());
        sb.append("] ");
        sb.append(getName());
        sb.append(" ");
        sb.append(getCrud().toString().toUpperCase());
        sb.append(" ");
        sb.append(getData());
        // TODO Check if that if is needed with the new loop stuff.
        if (steps == null || steps.size() == 0) {
            sb.append(" {}\n");
        } else {
            sb.append(" {");
            for (final Step step : steps) {
                sb.append("\n");
                sb.append("               ");
                sb.append(step.toString());
            }
            sb.append("\n}");
        }
        return sb.toString();
    }

    public boolean isComposite() {
        boolean result = true;
        if (data != null) {
            result = false;
        } else if (steps.size() == 0) {
            result = false;
        } else if (crud.isDefault() && steps.size() == 0) {
            result = false;
        } else {
            for (final Step step : steps) {
                if (!step.getStepConstraint().isScenarioReference()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public Set<Step> getSteps() {
        return steps;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setSteps(Set<Step> steps) {
        this.steps = steps;
    }

    public String getFullEntityName() {
        return application.getKeyname() + "." + StringHelper.getFirstCapitalized(getData());
    }

    public boolean isInheritenceComputed() {
        return inheritenceComputed;
    }

    public void setInheritenceComputed() {
        this.inheritenceComputed = true;
    }

    /**
     * @return Returns the businessEntityName.
     */
    public String getData() {
        return data;
    }

    public String getTechdata() {
        return techdata;
    }

    public void setTechdata(String techdata) {
        this.techdata = techdata;
    }

    /**
     * @param businessEntityName The businessEntityName to set.
     */
    public void setData(String businessEntityName) {
        this.data = businessEntityName;
    }

    public BusinessObject getRootBusinesObject() {
        return rootBusinesObject;
    }

    public void setRootBusinesObject(BusinessObject rootBusinesObject) {
        this.rootBusinesObject = rootBusinesObject;
        rootBusinesObject.addScenario(this);
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void addRule(final Rule rule) throws DuplicateRuleException {
        for (final Rule existingRule : rules) {
            if (rule.getKeyname().equals(existingRule.getKeyname())) {
                throw new DuplicateRuleException(existingRule, rule);
            }
        }
        rules.add(rule);
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(final Application application) {
        this.application = application;
    }

    public void addScreen(final Screen screen) {
        if (associatedScreen != null) {
            //throw new NullPointerException("associatedScreen must be null. Conflict between actuel associatedScreen " + associatedScreen.getQxClassName() + " and passed screen " + screen.getQxClassName());
        } else {
            associatedScreen = screen;
        }
    }

    public Screen getAssociatedScreen() {
        return associatedScreen;
    }

    public void setStaticFilter(Filter staticFilter) {
        this.staticFilter = staticFilter;
    }

    public void setDynamicFilter(Filter dynamicFilter) {
        this.dynamicFilter = dynamicFilter;
    }

    public Filter getStaticFilter() {
        return staticFilter;
    }

    public Filter getDynamicFilter() {
        return dynamicFilter;
    }

    public String getParentKeyname() {
        return parentKeyname;
    }

    public boolean hasParent() {
        return parentKeyname != null;
    }

    public void setParentKeyname(String parentKeyname) {
        this.parentKeyname = parentKeyname;
    }

    public void addRole(final String role) throws DuplicateRoleException {
        if (!roles.contains(role)) {
            roles.add(role);
        } else {
            throw new DuplicateRoleException(this, role);
        }
    }

    public boolean hasRoles() {
        return getRoles() != null && getRoles().size() > 0;
    }

    public boolean hasStep(final Step step) {
        boolean result = false;
        for (final Step scenarioStep : steps) {
            if (step.getKeyname().equals(scenarioStep.getKeyname())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void setRolesAllowed(final boolean allowed) {
        rolesAllowed = allowed;
    }

    public boolean isRolesAllowed() {
        return rolesAllowed;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<Jump> getJumps() {
        return jumps;
    }

    public Set<String> getTemplateContextKeys() {
        final Set<String> res = CollectionFactory.newSet();
        if (isComposite()) {
            for (final Step step : steps) {
                if (step.getStepConstraint().isScenarioReference()) {
                    res.add(StringHelper.getFirstLowerCase(step.getStepConstraint().getReferencedscenario().keyname));
                }
            }
        } else {
            res.add(StringHelper.getFirstLowerCase(data));
        }
        return res;
    }

    /**
     * Jump as a String must be : scenarioKeyname([[several optional parameter]])
     * <p/>
     * Used in the ODT reader
     *
     * @param jump
     */
    public void addJump(final Jump jump) {
        jumps.add(jump);
    }

    public void populateDefaultSteps(final int depth) {
        if (depth >= 0) {
            beginBlock("[populateDefaultSteps] depth = " + depth + " .Found a default scenario: " + keyname + ". Let's fill it with synthetic steps!");
            info("Business object for our scenario will be " + rootBusinesObject.getName());
            for (final BusinessObjectAttribute attr : rootBusinesObject.getAttributes()) {
                // int pos = 1;
                beginBlock("Try to create a Step for attribute " + attr.getKeyname());
                final Step step = new Step();
                step.setSynthetic();
                try {
                    step.setName(attr.getName());
                    step.setKeyname(attr.getKeyname());
                    // step.setPos(pos++);

                    if (!attr.isBuiltIn()) {
                        info("Attribute " + attr.getKeyname() + " is not built-in. Creating or re-using synthetic scenario");
                        step.setMinInt(attr.isMandatory() ? 1 : 0);
                        step.setMax(attr.isCollection() ? "*" : "1");
                        final BusinessObject bObject = attr.getType().getBusinessObject();
                        // This keyname must be unique. We're not trying to reuse scenario.
                        final String scenarioKeyname = keyname + "_" + step.getKeyname() + "_dread";

                        // Create synthetic scenario
                        info("Creating synthetic scenario to link to our step.");
                        final Scenario linkedScenario = new Scenario();
                        info("Populating steps of created scenario");
                        linkedScenario.setRootBusinesObject(bObject);
                        linkedScenario.setKeyname(scenarioKeyname);
                        info("Keyname of new scenario is " + linkedScenario.getKeyname());
                        linkedScenario.setSynthetic("");
                        linkedScenario.setData(bObject.getName());
                        linkedScenario.setCrud(CRUD.dread);
                        linkedScenario.setApplication(application);
                        linkedScenario.setName("Default read " + bObject.getName());
                        linkedScenario.populateDefaultSteps(depth - 1);
                        try {
                            application.addScenario(linkedScenario);
                        } catch (DuplicateScenarioException e) {
                            error(e.getMessage(), e);
                            // TODO check that the endblock is called in the finally
                            continue;
                        }
                        info("Setting step type to linked scenario since it's a scenario reference now");
                        step.setType(linkedScenario.getKeyname());
                        step.getStepConstraint().compute(application);
                    } else {
                        info("Setting step " + step.getKeyname() + " type to null, that way it will become a shadow step.");
                        step.setType(null);
                    }
                    try {
                        addStep(step);
                        info("Step " + step.getKeyname() + " successfully added to scenario " + keyname);
                    } catch (DuplicateStepException e) {
                        error(e.getMessage(), e);
                        // TODO check that the endblock is called in the finally
                        continue;
                    }
                } finally {
                    endBlock("Try to create a Step for attribute " + attr.getKeyname() + ". Done.");
                }
            }
            endBlock("[populateDefaultSteps] depth = " + depth + " .Found a default scenario: " + keyname + ". Let's fill it with synthetic steps. Done.");
        }
    }

    public void addSubsequentScenario(final Scenario scenario, final boolean influencing, final boolean influenced) {
        if (subsequentScenarios == null) {
            subsequentScenarios = new ContextualizedScenarios();
        }

        subsequentScenarios.addSubsequentScenario(scenario, influencing, influenced);
    }

    public void computeContextualFetchPlans() {
        if (subsequentScenarios != null) {
            subsequentScenarios.computeContextualFetchPlans(this);
        }
    }

    public void free() {
        application = null;
        rootBusinesObject = null;
        for (final Step step : steps) {
            step.free();
        }
        steps.clear();
        steps = null;

        for (final Rule rule : rules) {
            rule.free();
        }
        rules.clear();
        rules = null;

        for (final Jump jump : jumps) {
            jump.free();
        }
        jumps.clear();
        jumps = null;

        associatedScreen = null;

        roles.clear();
        roles = null;
        if (staticFilter != null) {
            staticFilter.free();
            staticFilter = null;
        }

        if (dynamicFilter != null) {
            dynamicFilter.free();
            dynamicFilter = null;
        }

        if (subsequentScenarios != null) {
            subsequentScenarios.free();
            subsequentScenarios = null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.crud);
        hash = 59 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Scenario other = (Scenario) obj;
        if (!Objects.equals(this.keyname, other.keyname)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (this.crud != other.crud) {
            return false;
        }
        return true;
    }

}
