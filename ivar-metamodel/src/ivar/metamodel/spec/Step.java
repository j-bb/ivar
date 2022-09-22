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

import ivar.metamodel.SpecElement;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import ivar.helper.CollectionFactory;

public class Step extends SpecElement {

    private String techkeyname = null;

    private String min = null;

    private String max = null;

    private Set<Step> steps = CollectionFactory.newSetWithInsertionOrderPreserved();

    private StepConstraint stepConstraint;

    private Set<Rule> rules = CollectionFactory.newSetWithInsertionOrderPreserved();

    // if true, the collection of roles is the list of authorized roles. Else it is the list of excluded roles
    // TODO ROLESALLOWED
    private boolean rolesAllowed = true;

    private Set<String> roles = CollectionFactory.newSet();

    private Set<Jump> jumps = CollectionFactory.newSetWithInsertionOrderPreserved();

    private transient Map<String, Rule> rulesMap = CollectionFactory.newMapWithInsertionOrderPreserved();

    private transient boolean synthetic = false;

    public Step() {
    }

    public Step(String type, String keyname, String name) {
        setName(name);
        setKeyname(keyname);
        setType(type);
    }

    public Scenario findMyScenario(final Application application) {
        Scenario result = null;
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                if (step == this) {
                    result = scenario;
                }
            }
        }
        return result;
    }

    @Override
    public Step getJPAClone() {
        return getJPAClone(new Step());
    }

    protected Step getJPAClone(final Step dst) {
        super.getJPAClone(dst);

        if (techkeyname != null) {
            dst.techkeyname = new String(techkeyname);
        } else {
            dst.techkeyname = null;
        }

        if (max != null) {
            dst.max = new String(max);
        } else {
            dst.max = null;
        }
        if (min != null) {
            dst.min = new String(min);
        } else {
            dst.min = null;
        }

//        if (pos != null) {
//            dst.pos = new Integer(pos);
//        }
        if (stepConstraint != null) {
            dst.stepConstraint = stepConstraint.getJPAClone();
        } else {
            dst.stepConstraint = null;
        }

        if (rules == null) {
            dst.rules = null;
        } else {
            dst.rules.clear();
            for (final Rule rule : rules) {
                dst.rules.add(rule.getJPAClone());
            }
        }

        if (roles != null) {
            dst.roles.clear();
            for (final String role : roles) {
                dst.roles.add(new String(role));
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

        if (steps == null) {
            dst.steps = null;
        } else {
            dst.steps.clear();
            if (steps != null) {
                for (final Step step : steps) {
                    dst.steps.add(step.getJPAClone());
                }
            }
        }

        return dst;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
        //this.minInt = Integer.decode(min);
    }

    public void setMinInt(Integer minInt) {
        this.min = minInt.toString();
    }

    public void setMaxInt(Integer maxInt) {
        if (maxInt == Integer.MAX_VALUE) {
            max = "*";
        } else {
            this.max = maxInt.toString();
        }
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
//        if ("*".equals(max)) {
//            this.maxInt = Integer.MAX_VALUE;
//        } else {
//            this.maxInt = Integer.decode(max);
//        }
    }

    public Integer getMaxInt() {
        Integer i = Integer.MAX_VALUE;
        if (!"*".equals(getMax()) && max != null) {
            i = Integer.decode(max);
        }
        if (max == null) {
            i = Integer.MIN_VALUE;
        }
        return i;
    }

    public Integer getMinInt() {
        Integer i = Integer.MIN_VALUE;
        if (min != null) {
            i = Integer.decode(min);
        }
        return i;
    }

    public boolean isCollection() {
        return getMaxInt() > 1;
    }

    public boolean isMandatory() {
        return getMinInt() > 0;
    }

    public Rule getCalculatedRule() {
        Rule result = null;
        for (Rule rule : rules) {
            if (rule.getKind().equals(RuleKinds.calculated) && rule.getHook().equals(RuleHooks.stepbefore)) {
                result = rule;
                break;
            }
        }
        return result;
    }

    public Rule getConditionRule() {
        Rule result = null;
        for (Rule rule : rules) {
            if (rule.getKind().equals(RuleKinds.check) && rule.getHook().equals(RuleHooks.stepbefore)) {
                result = rule;
                break;
            }
        }
        return result;
    }

    public Set<Rule> getRules() {
        return CollectionFactory.newSetWithInsertionOrderPreserved(rules);
    }

    public void addRule(final Rule rule) throws RuleException {
        final Rule duplicate = rulesMap.get(rule.getKeyname());
        boolean isDuplicate = duplicate != null;
        if (isDuplicate) {
            if ((!rule.getKind().equals(duplicate.getKind())) || (!rule.getHook().equals(duplicate.getHook()))) {
                isDuplicate = false;
            }
        }
        if (!isDuplicate) {
            if (rule.getKind().equals(RuleKinds.calculated) && rule.getHook().equals(RuleHooks.stepbefore) && isCalculated()) {
                throw new RuleException("Step " + getName() + " only one calculated rule per Step is possible. Cannot add rule " + rule.getKeyname());
            }
            rules.add(rule);
            rulesMap.put(rule.getKeyname(), rule);
        } else {
            throw new RuleException("Step " + getName() + " duplicate rule. A rule already exist with keyname= " + rule.getKeyname() + ", hook= " + rule.getHook() + ", kind= " + rule.getKind());
        }
    }

    public StepConstraint getStepConstraint() {
        return stepConstraint;
    }

    public void setType(final String type) {
        if (this.stepConstraint == null) {
            stepConstraint = new StepConstraint(type);
        }
        stepConstraint.setType(type);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getMin());
        sb.append("..");
        sb.append(getMax());
        sb.append(") ");
        if (isCalculated()) {
            sb.append("calc@");
            sb.append(getCalculatedRule().getName());
            sb.append(" ");
        }
        if (isCondition()) {
            sb.append("cond@");
            sb.append(getConditionRule().getName());
            sb.append(" ");
        }
        sb.append(getStepConstraint().getType());
        sb.append(" [");
        sb.append(getKeyname());
        sb.append("] ");
        sb.append(getName());
        return sb.toString();
    }

    public void addEmbededStep(final Step step) throws DuplicateStepException {
        for (final Step existingStep : steps) {
            if (step.getKeyname().equals(existingStep.getKeyname())) {
                throw new DuplicateStepException(existingStep, step);
            }
        }
        steps.add(step);
    }

    public Set<Step> getEmbededSteps() {
        return steps;
    }

    public boolean isUpToStar() {
        boolean result = false;
        if (max != null) {
            result = getMax().equals("*");
        }
        return result;
    }

    public String getRule(final String key) {
        for (Rule rule : rules) {
            if (key.equals(rule.getKeyname())) {
                return rule.getValue();
            }
        }
        return null;
    }

    public Set<Rule> getRulesForDependencies() {
        final Set<Rule> result = CollectionFactory.newSetWithInsertionOrderPreserved(1);
        for (final Rule rule : rules) {
            if ((rule.getHook().equals(RuleHooks.stepbefore) || rule.getHook().equals(RuleHooks.stepafter))
                    && (rule.getKind().equals(RuleKinds.calculated) || rule.getKind().equals(RuleKinds.check))) {
                result.add(rule);
            }
        }
        return result;
    }

    public boolean isCondition() {
        return getConditionRule() != null;
    }

    public boolean isCalculated() {
        return getCalculatedRule() != null;
    }

    public void setRules(final Map<String, Rule> rules) throws RuleException {
        this.rulesMap.clear();
        this.rules.clear();
        final Set<String> keys = rules.keySet();
        for (final String key : keys) {
            addRule(rules.get(key));
        }
    }

    public String getTechkeyname() {
        return techkeyname;
    }

    public void setTechkeyname(final String techkeyname) {
        this.techkeyname = techkeyname;
    }

    public boolean hasRoles() {
        return getRoles() != null && getRoles().size() > 0;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean isRolesAllowed() {
        return rolesAllowed;
    }

    public Set<Jump> getJumps() {
        return jumps;
    }

    /**
     * Jump as a String must be : scenarioKeyname([[several optional parameter]])
     *
     * Used in the ODT reader
     *
     * @param jump
     */
    public void addJump(final Jump jump) {
        jumps.add(jump);
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic() {
        this.synthetic = true;
    }

    public void computeMiniJumps() {
        if (jumps != null && jumps.size() > 0) {
            beginBlock("Computing minijumps info for step " + getKeyname());
            final Map<CRUD, Jump> cruds = CollectionFactory.newMap();
            for (final Jump jump : jumps) {
                CRUD jumpCrud = jump.getCrudOrTargetCrud();
                if (jump.isSmallSelector() || jumpCrud == null) {
                    continue;
                }

                info("Analyzing jump " + jump.getId());
                jumpCrud = (jumpCrud == CRUD.dread) ? CRUD.read : (jumpCrud == CRUD.dsearch) ? CRUD.search : jumpCrud;
                if (cruds.containsKey(jumpCrud)) {
                    if (jump.getPos() < cruds.get(jumpCrud).getPos()) {
                        cruds.put(jumpCrud, jump);
                    }
                } else {
                    cruds.put(jumpCrud, jump);
                }
            }
            for (final Jump jump : cruds.values()) {
                info("Jump " + jump.getId() + " is mini");
                jump.setMiniJump(true);
            }
            endBlock("Computing minijumps info for step " + getKeyname());
        }
    }

    public boolean hasMaxiJumps() {
        boolean result = false;
        for (final Jump jump : jumps) {
            if (!jump.isMiniJump()) {
                result = true;
                break;
            }
        }

        return result;
    }

    public Object getConstraint(final String name) {
        Object result = null;
        if (stepConstraint != null) {
            try {
                final Field f = stepConstraint.getClass().getDeclaredField(name);
                final boolean access = f.isAccessible();
                if (!access) {
                    f.setAccessible(true);
                }
                result = f.get(stepConstraint);
                if (!access) {
                    f.setAccessible(false);
                }
            } catch (IllegalAccessException e) {
                // Should not happen...
                error(e, true);
            } catch (NoSuchFieldException e) {
                final Field[] declaredFields = stepConstraint.getClass().getDeclaredFields();
                final StringBuilder sb = new StringBuilder(" Possible values based on ");
                sb.append(stepConstraint.getClass().getSimpleName());
                sb.append(" {");
                boolean first = true;
                for (final Field field : declaredFields) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(field.getName());
                }
                sb.append("}");
                final String message = "Invalid stepConstraint " + name + "!!" + sb.toString();
                error(message, e, true);
                compileError("There is an error, probably in a template : " + message, true);
            }
        }
        return result;
    }

    public boolean hasConstraint(final String name) {
        return getConstraint(name) != null;
    }

    public void free() {
        for (final Step step : steps) {
            step.free();
        }
        steps.clear();
        steps = null;

        if (stepConstraint != null) {
            stepConstraint.free();
        }

        rulesMap.clear();
        rulesMap = null;
        for (final Rule rule : rules) {
            rule.free();
        }
        rules.clear();
        rules = null;

        roles.clear();
        roles = null;

        for (final Jump jump : jumps) {
            jump.free();
        }
        jumps.clear();
        jumps = null;
    }

    public boolean isConstantRule() {
        final Rule calculatedRule = getCalculatedRule();
        return calculatedRule != null && calculatedRule.isConstantRule();
    }
}
