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
package ivar.metamodel.target.java;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.CRUD;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.target.generic.FetchPathType;
import ivar.metamodel.target.generic.ServerCall;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;

public class JavaServerCall extends AbstractObject {

    private ServerCall serverCall;
    private JavaFilter staticFilter;
    private JavaFilter dynamicFilter;
    private String scenarioKeyname;
    private CRUD crud;

    public JavaServerCall(final ServerCall sc) {
        serverCall = sc;
        scenarioKeyname = sc.getScenario().keyname;
        crud = sc.getCrud();
    }

    public String getParamName() {
        return serverCall.getParamName();
    }

    public boolean isFusion() {
        return serverCall.isFusion();
    }

    public String getTemplateName() {
        return serverCall.getTemplateName();
    }

    public String getMethodReturnType() {
        String result = null;

        if (isCreate() || isDelete()) {
            result = "void";
        } else if (isStrictlyRead() || isUpdate()) {
            result = serverCall.getBusinessObject().getName();
        } else if (isRead() && !isStrictlyRead()) {
            result = serverCall.getBusinessObject().getName() + "[]";
        }

        return result;
    }

    public String getParamsForMethodDeclaration() {
        final Map<String, String> serverCallParams = computeParameters();
        String result = "";
        boolean first = true;
        for (final Entry<String, String> param : serverCallParams.entrySet()) {
            if (!first) {
                result += ", ";
            }

            result += "final " + param.getValue() + " " + param.getKey();
            first = false;
        }

        return result;
    }

    public boolean isScenarioEqual(final Scenario scenario) {
        return (scenario != null) && serverCall.isScenarioEqual(scenario);
    }

    public FetchPathType getPathType() {
        return serverCall.getPathType();
    }

    public String getServerMethodName() {
        String result = null;
        final String crudString = crud.getAsString();
        if (hasFilters()) {
            result = "get" + StringHelper.getFirstCapitalized(scenarioKeyname);
        } else {
            if (isPrepare()) {
                result = "prepare";
            }

            result = ((result == null) ? crudString.toLowerCase() : result + crudString) + "In" + getPrettyScenarioKey();
        }

        if (serverCall.isContextual()) {
            result += "For" + serverCall.getContextKeyname();
        }

        return result;
    }

    public List<FetchPlanElement> getFetchPlan() {
        return serverCall.getFetchPlan();
    }

    public boolean hasFetchPlan() {
        final List<FetchPlanElement> fp = serverCall.getFetchPlan();
        return fp != null && fp.size() != 0;
    }

    public boolean isCreate() {
        return crud.isCreate();
    }

    public boolean isUpdate() {
        return crud.isUpdate();
    }

    public boolean isDelete() {
        return crud.isDelete();
    }

    public boolean isRead() {
        return crud.isRead();
    }

    public boolean isStrictlyRead() {
        return crud.isStrictlyRead();
    }

    public boolean isSearch() {
        return crud.isSearch();
    }

    public boolean isPrepare() {
        return serverCall.isPrepare();
    }

    public boolean isExecute() {
        return serverCall.isExecute();
    }

    public void setDynamicFilter(final JavaFilter filter) {
        dynamicFilter = filter;
    }

    public JavaFilter getDynamicFilter() {
        return dynamicFilter;
    }

    public boolean hasDynamicFilter() {
        return dynamicFilter != null;
    }

    public void setStaticFilter(final JavaFilter filter) {
        staticFilter = filter;
    }

    public JavaFilter getStaticFilter() {
        return staticFilter;
    }

    public boolean hasStaticFilter() {
        return staticFilter != null;
    }

    public boolean hasFilters() {
        return hasDynamicFilter() || hasStaticFilter();
    }

    public void setCrud(final CRUD crud) {
        this.crud = crud;
    }

    private String getPrettyScenarioKey() {
        return StringHelper.getCamel(scenarioKeyname, "_");
    }

    public String getScenarioKeyname() {
        return scenarioKeyname;
    }

    private Map<String, String> computeParameters() {
        Map<String, String> serverCallParams = CollectionFactory.newMapWithInsertionOrderPreserved();
        serverCallParams.put("cc", "ControllerContext");
        if (isCreate() || (isUpdate() && isExecute())) {
            serverCallParams.put(getParamName(), serverCall.getBusinessObject().getName());
        } else if (isDelete() || isStrictlyRead() || (isUpdate() && isPrepare())) {
            serverCallParams.put(getParamName() + "Id", "long");
        }

        if (hasDynamicFilter()) {
            for (final JavaFilterElement jfe : dynamicFilter.getNonBooleanOperatorFilterElements()) {
                serverCallParams.put(jfe.getParamName(), jfe.getJavabusinessAttribute().getType());
                serverCallParams.put(jfe.getParamName() + "NullValue", "boolean");
            }
        }
        if (hasStaticFilter() && hasRuntimeVariable()) {
            final List<String> runtimeVariables = staticFilter.getRuntimeVariables();
            for (final JavaFilterElement jfe : staticFilter.getNonBooleanOperatorFilterElements()) {
                if (jfe.isRuntimeVariable()) {
                    for (final String rt : runtimeVariables) {
                        if (jfe.getValue().contains(rt)) {
                            debug("JavaServerCall : case of a static filter with Dynamic Variable : " + rt);
                            debug("JavaServerCall : case of a static filter with Dynamic Variable, filter element : " + jfe);
                            serverCallParams.put(jfe.getParamName(), jfe.getJavabusinessAttribute().getType());
//                            serverCallParams.put(jfe.getParamName() + "NullValue", "boolean");
                            break;
                        }
                    }
                }
            }
        }

        if (isSearch()) {
            serverCallParams.put("limit", "Integer");
        }
        return serverCallParams;
    }

    public boolean hasRuntimeVariable() {
        return staticFilter.hasRuntimeVariable();
    }
}
