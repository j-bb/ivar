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
import ivar.helper.StringHelper;
import ivar.metamodel.spec.*;
import ivar.metamodel.target.FilterShell;
import ivar.metamodel.target.java.FetchPlanElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BusinessObject extends AbstractObject {

    private BusinessObject parent;
    private String name;
    private String doc;
    private String domain;
    private Set<Scenario> scenarios = CollectionFactory.newSet();
    private List<String> states = CollectionFactory.newList();
    private Map<String, FilterShell<BlankFilter>> filters = CollectionFactory.newMap();
    private List<ServerCall> serverCalls = CollectionFactory.newList();
    private FetchPaths fetchPaths = new FetchPaths();

    /**
     * A Collection of BusinessObjectAttribute.
     */
    private Map<String, BusinessObjectAttribute> attributes = CollectionFactory.newMap();
    private Application application;
    private String tableName;

    public BusinessObject(final Application application, final Scenario scenario, final String tableName) {
        this.application = application;
        this.tableName = tableName;
        name = scenario.getData();
        domain = application.getKeyname();
    }

    public String getColumnName(final BusinessObjectAttribute attribute) {
        String result = attribute.getTypeName();
        if (!attribute.getName().equalsIgnoreCase(attribute.getTypeName())) {
            result = attribute.getTypeName() + "_" + attribute.getName();
        }
        return result.toLowerCase();
    }

    public String getDomainName() {
        return domain;
    }

    public String getFullName() {
        return getDomainName() + "." + getName();
    }

    public Collection<BusinessObjectAttribute> getAttributes() {
        return attributes.values();
    }

    public BusinessObjectAttribute getBusinessAttributeByName(String name) {
        return attributes.get(name);
    }

    public String getName() {
        return name;
    }

    public String getNameFirstCapitalized() {
        return StringHelper.getFirstCapitalized(getName());
    }

    public BusinessObject getParent() {
        return parent;
    }

    public void addAttributes(BusinessObjectAttribute a) {
        attributes.put(a.getName(), a);
    }

    public void setParent(BusinessObject object) {
        parent = object;
    }

    public String getDoc() {
        return doc;
    }

    public boolean hasFusionServerCalls() {
        boolean result = false;
        for (ServerCall call : serverCalls) {
            if (call.isFusion()) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void addDoc(String doc) {
        if (doc != null) {
            if (this.doc == null) {
                this.doc = doc;
            } else {
                this.doc += "\n" + doc;
            }
        }
    }

    public void addScenario(final Scenario scenario) {
        scenarios.add(scenario);
    }

    public void addFilter(final String filterGroupKey, final BlankFilter filter) {
        FilterShell<BlankFilter> filterShell = filters.get(filterGroupKey);
        if (filterShell == null) {
            filterShell = new FilterShell<BlankFilter>(filterGroupKey);
        }
        if (filter.isDynamic()) {
            filterShell.setDynamicFilter(filter);
        } else {
            filterShell.setStaticFilter(filter);
        }
        filters.put(filterGroupKey, filterShell);
    }

    public Map<String, FilterShell<BlankFilter>> getFilters() {
        return filters;
    }

    public Application getApplication() {
        return application;
    }

    public List<String> getRelations() {
        final Map<String, BusinessObjectAttribute> relations = CollectionFactory.newMap();
        for (BusinessObjectAttribute attribute : attributes.values()) {
            if (!attribute.isBuiltIn()) {
                relations.put(attribute.getKeyname(), attribute);
            }
        }

        final List<String> result = CollectionFactory.newList(relations.size());
        for (BusinessObjectAttribute attribute : relations.values()) {
            result.add(attribute.getType().getName());
        }
        return result;
    }

    public void setSates(Set<String> states) {
        this.states.addAll(states);
    }

    public List<String> getStates() {
        return states;
    }

    public boolean hasState() {
        return states.size() > 0;
    }

    @Override
    public String toString() {
        return (domain == null || domain.length() == 0 ? "" : domain) + "." + name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public void addServerCall(final ServerCall serverCall) {
        serverCalls.add(serverCall);
    }

    public boolean hasServerCalls() {
        return serverCalls.size() > 0;
    }

    public List<ServerCall> getServerCalls() {
        return serverCalls;
    }

    public FetchPaths getFetchPaths() {
        return fetchPaths;
    }

    /**
     * Compute the fetch path for a given root scenario recursively traversing every linked business object.
     *
     * @param originalScenario The root scenario
     * @param scenario The current scenario that has been accessed by a step jump or a scenario reference
     * @param type Whether path is computed for preparation time or execution time
     * @param currentPath Additional element to compute the key to get the correct fetch path at any moment in the recursive descent. Must be set an empty
     * string when first calling this method.
     */
    public void computeFetchPath(final Scenario originalScenario, final Scenario scenario, final FetchPathType type, final String currentPath) {
        beginBlock("Computing fetch path " + type + " of scenario " + originalScenario.getKeyname() + ", currently in " + scenario.getKeyname() + " in business object " + name + ". Path : " + currentPath);
        final FetchPath fetchPath = new FetchPath(type, originalScenario, currentPath);
        final CRUD crud = originalScenario.getCrud();

        for (final Step step : scenario.getSteps()) {
            final BusinessObjectAttribute bAttribute = getBusinessAttributeByName(step.getKeyname());

            info("Evaluating step " + step.keyname + ", Corresponding Object : " + bAttribute.getBusinessObject().getFullName() + "." + bAttribute.getKeyname());
            fetchPath.addAttribute(bAttribute);

            if (bAttribute.isFile()) {
                fetchPath.addFileAttribute(bAttribute);
            }
            final StepConstraint constraints = step.getStepConstraint();
            debug("step " + step.keyname + " is atomic ? " + constraints.isAtomic());
            if (!constraints.isAtomic()) {
                if (constraints.isScenarioReference() && ((type == FetchPathType.PREPARATION) || originalScenario.getCrud().isDelete())) {
                    info("Step " + step.keyname + " is scenario reference and path type is preparation, or we are in a delete scenario. Traversing referenced scenario " + constraints.getReferencedscenario().getKeyname());
                    constraints.getReferencedscenario().getRootBusinesObject().computeFetchPath(originalScenario, constraints.getReferencedscenario(), type, currentPath + "." + step.keyname);
                    if (crud.isCreate() || crud.isUpdate()) {
                        originalScenario.addSubsequentScenario(constraints.getReferencedscenario(), true, false);
                        debug("Adding subsequent scenario" + constraints.getReferencedscenario().keyname + " to " + originalScenario.keyname + " because of scenario reference on step " + step.keyname + "... influencing = true");
                    }
                }
                for (final Jump jump : step.getJumps()) {
                    final CRUD jumpCrudOrTargetCrud = jump.getCrudOrTargetCrud();
                    boolean traverse = false;

                    switch (crud) {
                        case create:
                            traverse = (jumpCrudOrTargetCrud.isCreate() || jumpCrudOrTargetCrud.isUpdate());
                            break;
                        case update:
                            traverse = (jumpCrudOrTargetCrud.isCreate() || jumpCrudOrTargetCrud.isUpdate());
                            break;
                        case search:
                            traverse = (jumpCrudOrTargetCrud == CRUD.search || jumpCrudOrTargetCrud == CRUD.dsearch);
                            break;
                        case dsearch:
                            traverse = (jumpCrudOrTargetCrud == CRUD.search || jumpCrudOrTargetCrud == CRUD.dsearch);
                            break;
                        default:
                            break;
                    }

                    if (traverse) {
                        info("Step " + step.keyname + " jumps to scenario " + jump.getTargetScenario().keyname + " and CRUD indicates that we must traverse it.");
                        jump.getTargetScenario().getRootBusinesObject().computeFetchPath(originalScenario, jump.getTargetScenario(), type, currentPath + "." + step.keyname);
                        if (crud.isCreate() || crud.isUpdate()) {
                            originalScenario.addSubsequentScenario(jump.getTargetScenario(), true, jumpCrudOrTargetCrud.isUpdate());
                            debug("Adding subsequent scenario" + jump.getTargetScenario().keyname + " to " + originalScenario.keyname + " because of jump on step " + step.keyname + "... influencing = true");
                        }
                    } else {
                        if (crud.isCreate() || crud.isUpdate() && jumpCrudOrTargetCrud.isSearch()) {
                            originalScenario.addSubsequentScenario(jump.getTargetScenario(), false, true);
                            debug("Adding subsequent scenario" + jump.getTargetScenario().keyname + " to " + originalScenario.keyname + " because of jump on step " + step.keyname + "... influencing = false");
                        }
                    }
                }
            }
        }
        fetchPaths.addFetchPath(fetchPath);
        endBlock("Computing fetch path " + type + " of scenario " + originalScenario.getKeyname() + ", currently in " + scenario.getKeyname() + " in business object " + name + ". Path : " + currentPath + ". Done.");
    }

    public String getFetchPlanDebug(final String controllerContextVarName, final Scenario scenario, final FetchPathType type) {
        return getFetchPlanDebug(controllerContextVarName, scenario, type, "");
    }

    /**
     * Using a recursive descent algorithm with the fetch path, prepare a string to be copied in the controller.java file of the business object to compute the
     * fetch plan dynamically for the given scenario
     *
     * @param controllerContextVarName The variable/attribute name of the ControllerContext instance
     * @param scenario The scenario for which we want to compute the fetch path
     * @param type Whether we are at preparation time or execution time
     * @param currentPath Additional element to compute the key to get the correct fetch path at any moment in the recursive descent. Must be set an empty
     * string when first calling this method.
     * @return A string that can be inserted in the corresponding controller method code.
     */
    public String getFetchPlanDebug(final String controllerContextVarName, final Scenario scenario, final FetchPathType type, final String currentPath) {
        String result = "";
        final FetchPath fetchPath = getFetchPaths().getFetchPath(scenario, type, currentPath);
        if (fetchPath != null) {
            debug("Fetchpath found in BO " + getName() + ", for scenario " + scenario.getKeyname() + " ----{");
            beginBlock();
            debug(controllerContextVarName + ".addField(" + getName() + ".class, \"id\");");
            for (BusinessObjectAttribute attr : fetchPath.getAttributes()) {
                debug(controllerContextVarName + ".addField(" + getName() + ".class, \"" + attr.getKeyname() + "\");");
                if (!attr.isBuiltIn() && !attr.isDataset()) {
                    Step step = null;
                    for (Scenario tmpScenario : this.getScenarios()) {
                        for (Step tmpStep : attr.getSteps()) {
                            if (tmpScenario.getSteps().contains(tmpStep)) {
                                step = tmpStep;
                                break;
                            }
                        }
                        if (step != null) {
                            break;
                        }
                    }
                    final BusinessObject otherBO = attr.getType().getBusinessObject();
                    otherBO.getFetchPlanDebug(controllerContextVarName, scenario, type, currentPath + "." + step.keyname);
                }
            }
            endBlock();
            debug("----}");
        }
        return result;
    }

    public List<FetchPlanElement> getFetchPlanList(final Scenario scenario, final FetchPathType type, final String currentPath) {
        final List<FetchPlanElement> result = CollectionFactory.newList();
        final FetchPath fetchPath = getFetchPaths().getFetchPath(scenario, type, currentPath);
        if (fetchPath != null) {
            result.add(new FetchPlanElement(getName() + ".class", "id"));
//            if (fetchPath.hasFileAttributes() && type.equals(FetchPathType.PREPARATION)) {
//                for (final Field classfield : SerializableFileIndex.class.getDeclaredFields()) {
//                    result.add(new FetchPlanElement(SerializableFileIndex.class.getSimpleName() + ".class", classfield.getName(), true));
//                }
//        }
            for (final BusinessObjectAttribute attr : fetchPath.getAttributes()) {
                result.add(new FetchPlanElement(getName() + ".class", attr.getKeyname()));
                if (attr.isFile() && type.equals(FetchPathType.PREPARATION)) {
                    result.add(new FetchPlanElement(getName() + ".class", attr.getKeyname() + "FileIndex", true));
                }
                if (!attr.isBuiltIn() && !attr.isDataset()) {
                    Step step = null;
                    for (final Scenario tmpScenario : this.getScenarios()) {
                        for (final Step tmpStep : attr.getSteps()) {
                            if (tmpScenario.getSteps().contains(tmpStep)) {
                                step = tmpStep;
                                break;
                            }
                        }
                        if (step != null) {
                            break;
                        }
                    }
                    final BusinessObject otherBO = attr.getType().getBusinessObject();
                    result.addAll(otherBO.getFetchPlanList(scenario, type, currentPath + "." + step.keyname));
                }
            }
        }
        return result;
    }

    public Set<Scenario> getScenarios() {
        return scenarios;
    }

    public int getFetchPathNumber() {
        return getFetchPaths().getNumber();
    }

    public void free() {
        parent = null;
        scenarios.clear();
        scenarios = null;
        states.clear();
        states = null;

        for (final FilterShell<?> filterShell : filters.values()) {
            filterShell.free();
        }
        filters.clear();
        filters = null;

        for (final ServerCall serverCall : serverCalls) {
            serverCall.free();
        }
        serverCalls.clear();
        serverCalls = null;

        fetchPaths.free();
        fetchPaths = null;

        for (final BusinessObjectAttribute attribute : attributes.values()) {
            attribute.free();
        }
        attributes.clear();
        attributes = null;

        application = null;
    }
}
