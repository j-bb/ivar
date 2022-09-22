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
 */package ivar.metamodel.target.java;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.target.FilterShell;
import ivar.metamodel.target.generic.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;

public class JavaBusinessObject extends AbstractObject implements Comparable<JavaBusinessObject> {

    private String name;

    private JavaBusinessObject parent;

    private BusinessObject businessObject = null;

    private Map<String, JavaServerCall> serverCalls = CollectionFactory.newMap();

    /**
     * A Collection of JavaBusinessAttribute.
     */
    private Map<String, JavaBusinessAttribute> attributes = CollectionFactory.newMapWithInsertionOrderPreserved();

    public JavaBusinessObject(final BusinessObject bo) {
        businessObject = bo;
        name = StringHelper.getFirstCapitalized(bo.getName());
        info("JBO " + name + " {");
        for (BusinessObjectAttribute businessAttribute : bo.getAttributes()) {
            JavaBusinessAttribute javaBusinessAttribute = new JavaBusinessAttribute(businessAttribute);
            info("   " + javaBusinessAttribute.getType() + " " + javaBusinessAttribute.getName() + (javaBusinessAttribute.isJavaBuiltIn() ? " is JAVA BUILT-IN" : ""));

            addAttributes(javaBusinessAttribute);
        }
        info("}");
        emptyLine();
    }

    public String getVariableName() {
        return StringHelper.getFirstLowerCase(getName());
    }

    public String getIdColumnName(final JavaBusinessAttribute attribute) {
        final String typeName = getColumnName(attribute);
        final String attributeKeyname = StringHelper.unCamel(attribute.getName(), "_");
        return (attributeKeyname + "_" + typeName + "_id").toLowerCase();
    }

    public String getColumnName(final JavaBusinessAttribute attribute) {
        return attribute.getTypeAsJavaBusinessObject().getTableName();
    }

    public String getTableName(final JavaBusinessAttribute attribute) {
        return getTableName() + "_" + attribute.getColumnName() + "_" + attribute.getTypeAsJavaBusinessObject().getTableName();
    }

    public String getTableName() {
        return businessObject.getTableName();
    }

    public String getIdTableName() {
        final String tableName = businessObject.getTableName();
        final String id;
        if (StringHelper.isAllLowerCase(tableName)) {
            id = "id";
        } else if (StringHelper.isAllUpperCase(tableName)) {
            id = "ID";
        } else {
            id = "ID";
        }
        return tableName + "_" + id;
    }

    public String getDoc() {
        return businessObject.getDoc() == null ? "" : businessObject.getDoc();
    }

    public String getDomainName() {
        return businessObject.getDomainName();
    }

    public String getPackageName() {
        return StringHelper.getStringForJavaPackage(getDomainName());
    }

    public JavaBusinessAttribute getAttribute(final String attributeName) {
        return attributes.get(attributeName);
    }

    public Map<String, JavaBusinessAttribute> getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public JavaBusinessObject getParent() {
        return parent;
    }

    public void addAttributes(final JavaBusinessAttribute a) {
        attributes.put(a.getKeyname(), a);
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @param object
     */
    public void setParent(JavaBusinessObject object) {
        parent = object;
    }

    public String toString() {
        int attributeNumber = attributes != null ? attributes.size() : 0;
        if (attributeNumber == 0) {
            warning("The BusinessObject " + getName() + " has no attribute");
        }
        String result = "JBO " + getName() + " " + attributeNumber + " attributes";
        return result;
    }

    public boolean hasState() {
        return businessObject.hasState();
    }

    public List<String> getStates() {
        return businessObject.getStates();
    }

    public int compareTo(JavaBusinessObject anotherJBO) {
        return getName().compareTo(anotherJBO.getName());
    }

    public boolean hasServerCalls() {
        return !serverCalls.isEmpty();
    }

    public boolean hasFileAttributes() {
        boolean result = false;
        for (JavaBusinessAttribute jba : attributes.values()) {
            if (jba.isFile()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasFusionServerCalls() {
        return businessObject.hasFusionServerCalls();
    }

    public Collection<JavaServerCall> getServerCalls() {
        return serverCalls.values();
    }

    public void computeTargetServerCalls() {
        final Application application = businessObject.getApplication();

        for (final ServerCall sc : businessObject.getServerCalls()) {
            putServerCall(sc.getScenario().getKeyname(), new JavaServerCall(sc));
        }

        int filterCount = 0;
        for (final FilterShell<BlankFilter> blankFilterFilterShell : businessObject.getFilters().values()) {
            ServerCall rootServerCall = null;
            final String scenarioKeyname = blankFilterFilterShell.getScenarioKeyname();

            for (final ServerCall sc : businessObject.getServerCalls()) {
                if (sc.isPrepare() && sc.getScenario().getKeyname().equals(scenarioKeyname)) {
                    rootServerCall = sc;
                    break;
                }
            }

            if (rootServerCall == null) {
                rootServerCall = new ServerCall(businessObject, application.getScenarioForKeyname(scenarioKeyname), FetchPathType.PREPARATION);
            }

            final JavaServerCall newJavaCall = new JavaServerCall(rootServerCall);

            if (blankFilterFilterShell.hasDynamic()) {
                final JavaFilter filter = new JavaFilter(blankFilterFilterShell.getDynamicFilter(), this);
                newJavaCall.setDynamicFilter(filter);
                filterCount++;
            }
            if (blankFilterFilterShell.hasStatic()) {
                final JavaFilter filter = new JavaFilter(blankFilterFilterShell.getStaticFilter(), this);
                newJavaCall.setStaticFilter(filter);
                filterCount++;
            }
            putServerCall(scenarioKeyname, newJavaCall);
            info("Java server calls : found " + serverCalls.size() + " and " + filterCount + " filters for " + getName());
        }
    }

    private void putServerCall(final String scenarioKeyname, final JavaServerCall call) {
        putServerCall(scenarioKeyname, call, null);
    }

    private void putServerCall(final String scenarioKeyname, final JavaServerCall call, final String context) {
        final String typeStr = (call.getPathType() == FetchPathType.PREPARATION) ? "prepare" : "exec";
        serverCalls.put("scenario=" + scenarioKeyname + "&type=" + typeStr + "&ctx=" + (context == null ? "" : context), call);
    }

    public void gatherContextualServerCalls() {
        for (final ServerCall sc : businessObject.getServerCalls()) {
            if (sc.isContextual()) {
                putServerCall(sc.getScenario().keyname, new JavaServerCall(sc), sc.getContextKeyname());
            }
        }
    }

    public void getFetchPlansForDebug() {
        final String controllerContextVarName = "cc";
        debug("======== FETCH PLANS FOR JBO " + getName() + " ========");
        for (Scenario scenario : businessObject.getScenarios()) {
            debug("    ==== Fetch plans for scenario " + scenario.getKeyname() + " ====");
            debug("      == Path type : preparation ==");
            businessObject.getFetchPlanDebug(controllerContextVarName, scenario, FetchPathType.PREPARATION);
            debug("      == Path type : execution ==");
            businessObject.getFetchPlanDebug(controllerContextVarName, scenario, FetchPathType.EXECUTION);
        }
    }

    public int getFetchPathNumber() {
        return businessObject.getFetchPathNumber();
    }

    public void free() {
        parent = null;

        businessObject = null;

        serverCalls.clear();
        serverCalls = null;

        for (final JavaBusinessAttribute jba : attributes.values()) {
            jba.free();
        }
        attributes.clear();
        attributes = null;
    }
}
