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
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.FetchPathType;
import ivar.metamodel.target.java.JavaBusinessObject;
import ivar.metamodel.target.java.JavaServerCall;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Screen extends AbstractObject {

    private BusinessObject businessObject;

    private Set<ScreenField> fields = CollectionFactory.newSetWithInsertionOrderPreserved();

    private String fullBusinessEntityName;
    private CRUD crud;
    private String title;
    private String xmlkey;
    private String javakey;
    protected String jsUniqueKey;
    private String javaScriptKey;
    private String applicationName;
    private Scenario scenario;
    private String fsUniqueKey;
    private Application application;

    /**
     * pureComposition screen are screen that only have reference to other screen but no data in them. It could be a bit assembly of other screen that should
     * open, close, commit ... live together.
     */
    private boolean pureComposition = false;
    private String section;
    private boolean isPublic;
    private String uniqueID;

    private transient Map<String, Set<String>> publicServerCall = CollectionFactory.newMap();

    private transient Map<String, JavaServerCall> serverCalls = CollectionFactory.newMap();

    public Screen(final Application application, final Scenario scenario) {
        this.isPublic = false;
        for (final String role : scenario.getRoles()) {
            if ("public".equals(role)) {
                this.isPublic = true;
                this.uniqueID = StringHelper.getRandomString(100);
                break;
            }
        }
        scenario.addScreen(this);
        this.application = application;
        this.pureComposition = scenario.isComposite();
        if (scenario.getRootBusinesObject() == null && scenario.getCrud() != CRUD.search && !pureComposition) {
            error("The screen " + scenario.getKeyname() + " does not have businessObject !", true);
        }

        this.scenario = scenario;

        this.businessObject = scenario.getRootBusinesObject();
        this.fullBusinessEntityName = scenario.getFullEntityName();
        this.crud = scenario.getCrud();
        this.title = scenario.getName();
        javaScriptKey = StringHelper.getJavaString(scenario.getKeyname()).toLowerCase();
        javakey = StringHelper.getJavaString(scenario.getKeyname());
        xmlkey = StringHelper.getXMLString(scenario.getKeyname());
        jsUniqueKey = javaScriptKey; //StringHelper.concat(scenario.getKeyname(), "_", javaScriptKey);
        // This works because screen files are for qooxdoo located in <bo>/views/fsUniqueKey.js
//        fsUniqueKey = scenario.getKeyname().toLowerCase() + "_" + scenario.getData().toLowerCase();
        fsUniqueKey = StringHelper.getFirstCapitalized(scenario.getKeyname());
        section = scenario.getSection();

        linkServerCalls();
    }

    private void linkServerCalls() {
        // it's most unlikely that these are going to change during the compilation so we only need to make this once.

        final JavaBusinessObject jbo = application.getJavaBusinessObject(businessObject.getName());
        for (JavaServerCall call : jbo.getServerCalls()) {
            if (call.isScenarioEqual(scenario)) {
                serverCalls.put((call.getPathType().equals(FetchPathType.EXECUTION) ? "exec" : "prepare"), call);
            }
        }
    }

    public void addPublicServerCall(final String service, String method) {
        Set<String> methods = publicServerCall.get(service);
        if (methods == null) {
            methods = CollectionFactory.newSet();
            publicServerCall.put(service, methods);
        }
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    public Map<String, Set<String>> getPublicServerCall() {
        return publicServerCall;
    }

    public boolean hasPrepareCall() {
        return (serverCalls.containsKey("prepare"));
    }

    public boolean hasExecuteCall() {
        return (serverCalls.containsKey("exec"));
    }

    public JavaServerCall getPrepareCall() {
        return serverCalls.get("prepare");
    }

    public JavaServerCall getExecuteCall() {
        return serverCalls.get("exec");
    }

    public Map<String, JavaServerCall> getServerCalls() {
        return serverCalls;
    }

    // Called from template
    public boolean hasDynamicFilter() {
        final Filter dynamicFilter = scenario.getDynamicFilter();
        return dynamicFilter != null;
    }

    public boolean hasStaticFilter() {
        final Filter staticFilter = scenario.getStaticFilter();
        return staticFilter != null;
    }

    public boolean hasDynamicRuntimeVariable() {
        final Filter dynamicFilter = scenario.getDynamicFilter();
        return dynamicFilter != null && dynamicFilter.hasRuntimeVariable();
    }

    public boolean hasStaticRuntimeVariable() {
        final Filter staticFilter = scenario.getStaticFilter();
        return staticFilter != null && staticFilter.hasRuntimeVariable();
    }

    public List<Map<String, String>> getStaticRuntimeVariables() {
        final Filter staticFilter = scenario.getStaticFilter();
        final List<String> variables = staticFilter.getRuntimeVariables();
        final List<Map<String, String>> result = CollectionFactory.newList(variables.size());
        for (final String variable : variables) {
            final Map<String, String> map = CollectionFactory.newMap(2);
            map.put("key", StringHelper.getLeft(variable, '.').toLowerCase());
            map.put("value", StringHelper.getRight(variable, '.'));
            result.add(map);
        }
        return result;
    }

    public List<Map<String, String>> getDynamicRuntimeVariables() {
        final Filter dynamicFilter = scenario.getDynamicFilter();
        final List<String> variables = dynamicFilter.getRuntimeVariables();
        final List<Map<String, String>> result = CollectionFactory.newList(variables.size());
        for (final String variable : variables) {
            final Map<String, String> map = CollectionFactory.newMap(2);
            map.put("key", StringHelper.getLeft(variable, '.').toLowerCase());
            map.put("value", StringHelper.getRight(variable, '.'));
            result.add(map);
        }
        return result;
    }
    // Called from template

    public Set<FilterElement> getDynamicFilterElement() {
        final Set<FilterElement> result = CollectionFactory.newSetWithInsertionOrderPreserved();

        // This is done to remove boolean operator from the return list. The list is used on template for parameters, so no boolean operator.
        final Filter filter = scenario.getDynamicFilter();
        for (final FilterElement filterElement : filter.getFilterElements()) {
            if (!filterElement.isBooleanOperator()) {
                result.add(filterElement);
            }
        }
        return result;
    }

    // Called from template
    public String getDynamicFilterDocumentation() {
        String doc = scenario.getDynamicFilter().getDocumentation();
        if (doc == null || doc.length() == 0) {
            doc = "";
        }
        return doc;
    }

    // Called from template
    public String getDynamicFilterName() {
        return scenario.getDynamicFilter().getName();
    }

    public boolean isPureComposition() {
        return pureComposition;
    }

    public String getKeyname() {
        return scenario.getKeyname();
    }

    public String getQxClassName() {
        String result = StringHelper.getStringForJavaPackage(application.getKeyname());
        if (isPureComposition()) {
            result += ".ui.composite." + StringHelper.getFirstCapitalized(scenario.getKeyname());
        } else {
            result += ".ui." + getBusinessObjectName() + "." + StringHelper.getFirstCapitalized(scenario.getKeyname());
        }
        return result;
    }

    public BusinessObject getBusinessObject() {
        return businessObject;
    }

    public String getBusinessObjectClass() {
        String fullName = businessObject.getFullName();
        final int i = fullName.lastIndexOf(".");
        String result = fullName.substring(0, i + 1) + fullName.substring(i + 1, i + 2).toUpperCase();
        result += fullName.substring(i + 2, fullName.length());
        return result;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getBusinessObjectName() {
        return businessObject.getName().toLowerCase();
    }

    public String getBusinessObjectNameFirstCapitalized() {
        return StringHelper.getFirstCapitalized(businessObject.getName());
    }

    public void addField(final ScreenField screenField) {
        String screenFieldName = screenField.getKeyname();
        if (isPureComposition()) {
            fields.add(screenField);
        } else {
            final BusinessObjectAttribute objectAttribute = businessObject.getBusinessAttributeByName(screenFieldName);
            if (objectAttribute == null) {
                compileError("Null attribute for a ScreenField. Screen field name is " + screenFieldName);
                beginCompileHelpBlock("For Helping purpose, here is a dump of the ScreenField");
                compileHelp(screenField.toString());
                endCompileHelpBlock();
            } else {
                screenField.setAttribute(objectAttribute);
                fields.add(screenField);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return fullBusinessEntityName;
    }

    public Collection<ScreenField> getFields() {
        return fields;
    }

    public ScreenField getScreenFieldByKeyName(String keyname) {
        ScreenField result = null;
        for (ScreenField screenField : fields) {
            if (screenField.getKeyname().equals(keyname)) {
                result = screenField;
                break;
            }
        }
        return result;
    }

    public void disentangleScreen() {
        for (final ScreenField field : getFields()) {
            if (field.isScreenReference()) {
                beginBlock("Found a screen reference in screen " + getKeyname() + " for field " + field.getKeyname() + ". Proceeding to disentanglement.");
                field.cloneReferencedScreen();
                endBlock("Screen reference to " + field.getReferencedScreen().getKeyname() + "by field " + field.getKeyname() + " in screen " + getKeyname() + " succesfully disentangled.");
            }
        }
    }

    public Screen disentanglingClone() {
        final Screen result = new Screen(application, scenario);
        info("[disentanglingClone] Cloning fields in screen " + getKeyname());
        for (final ScreenField field : fields) {
            result.fields.add(field.disentanglingClone(result));
        }
        result.setApplicationName(applicationName);
        return result;
    }

    public CRUD getCrud() {
        return crud;
    }

    public String getJavaCrud() {
        return crud.toString();
    }

    public String getKeyForJava() {
        return javakey;
    }

    public String getJavaScriptKey() {
        return javaScriptKey;
    }

    public String getKeyForXML() {
        return xmlkey;
    }

    public String getJsUniqueKey() {
        return jsUniqueKey;
    }

    public String getFsUniqueKey() {
        return fsUniqueKey;
    }

    /**
     * Return a file name valid in XML world representing this screen at generation time.
     *
     * @return
     */
    public String getXMLFile() {
        return getKeyForXML();
    }

    public boolean isCreate() {
        return getCrud() == CRUD.create;
    }

    public boolean isDRead() {
        return getCrud() == CRUD.dread;
    }

    public boolean isRead() {
        return getCrud() == CRUD.read;
    }

    public boolean isUpdate() {
        return getCrud() == CRUD.update;
    }

    public boolean isDelete() {
        return getCrud() == CRUD.delete;
    }

    public boolean isSearch() {
        return getCrud() == CRUD.search;
    }

    public boolean isDSearch() {
        return getCrud() == CRUD.dsearch;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationKeyname() {
        return application.getKeyname();
    }

    public String debugIdentifier() {
        return "[" + getJsUniqueKey() + "] '" + getTitle() + "'";
    }

    public Set<String> getRoles() {
        return scenario.getRoles();
    }

    public boolean isRolesAllowed() {
        return scenario.isRolesAllowed();
    }

    public boolean hasRoles() {
        return scenario.hasRoles();
    }

    public boolean hasJumps() {
        boolean result = false;
        final Set<Jump> jumps = scenario.getJumps();
        if (jumps != null) {
            result = jumps.size() > 0;
        }
        return result;
    }

    public boolean canHasSelectableSteps() {
        return crud.isCreate() || crud.isUpdate();
    }

    public Set<Jump> getJumps() {
        return scenario.getJumps();
    }

    public Scenario getScenario() {
        return scenario;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void free() {
        businessObject = null;

        for (final ScreenField sf : fields) {
            sf.free();
        }
        fields.clear();
        fields = null;

        scenario = null;

        application = null;

        publicServerCall.clear();
        publicServerCall = null;

        serverCalls.clear();
        serverCalls = null;
    }

    public void disambiguateKeynames(final String differentiator) {
        for (final ScreenField field : fields) {
            if (differentiator != null) {
                field.setKeyname(differentiator + field.getKeynameFirstCapitalized());
            }
            if (field.isScreenReference()) {
                field.getReferencedScreen().disambiguateKeynames(field.getKeyname());
            }
        }
    }

    public Set<ScreenField> getFileFields() {
        final Set<ScreenField> result = CollectionFactory.newSet();
        for (ScreenField field : fields) {
            if (field.isFile()) {
                result.add(field);
            }
        }
        return result;
    }

    public boolean hasFileFields() {
        boolean result = false;
        for (final ScreenField field : fields) {
            if (field.isFile()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasImageFields() {
        boolean result = false;
        for (final ScreenField field : fields) {
            if (field.isImage()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasBooleanFields() {
        boolean result = false;
        for (final ScreenField field : fields) {
            if (field.isBoolean()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void addContextualServerCall(final String rootScenarioKey, final JavaServerCall serverCall) {
        serverCalls.put(rootScenarioKey, serverCall);
    }
}
