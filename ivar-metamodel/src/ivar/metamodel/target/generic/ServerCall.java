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
import ivar.metamodel.spec.CRUD;
import ivar.metamodel.spec.Jump;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.target.java.FetchPlanElement;
import java.util.List;
import java.util.Set;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;

public class ServerCall extends AbstractObject {

    private BusinessObject businessObject;
    private Scenario scenario;
    private CRUD crud;
    private FetchPathType pathType;
    private Long fusionTemplate;
    private String templateFileName;

    /* Contextual server calls only. */
    private Scenario context = null;
    private List<FetchPlanElement> fetchPlan = null;

    public ServerCall(final Jump jump, final BusinessObject businessObject, final Scenario scenario) {
        this.businessObject = businessObject;
        this.scenario = scenario;
        crud = (jump != null) ? jump.getCrudOrTargetCrud() : scenario.getCrud();
    }

    public ServerCall(final Jump jump, final BusinessObject businessObject, final Scenario scenario, final FetchPathType pathType) {
        this(jump, businessObject, scenario);
        this.pathType = pathType;
    }

    public ServerCall(final BusinessObject businessObject, final Scenario scenario, final FetchPathType pathType) {
        this(null, businessObject, scenario, pathType);
    }

    public String getKey() {
        String result = null;
        final String crudString = crud.getAsString();
        if (pathType == FetchPathType.PREPARATION) {
            result = "prepare";
        }

        result = (result == null ? crudString.toLowerCase() : (result + crudString));
        result += businessObject.getNameFirstCapitalized() + "In" + scenario.keyname;

        return result;
    }

    public String getParamName() {
        return StringHelper.getFirstLowerCase(businessObject.getName());
    }

    public FetchPath getFetchPath() {
        return businessObject.getFetchPaths().getFetchPath(scenario, pathType);
    }

    public List<FetchPlanElement> getFetchPlan() {
        final List<FetchPlanElement> result;
        if (!isContextual()) {
            result = businessObject.getFetchPlanList(scenario, pathType, "");
        } else {
            result = this.fetchPlan;
        }
        return result;
    }

    public boolean isScenarioEqual(final Scenario otherScenario) {
        return scenario.keyname.equals(otherScenario.keyname);
    }

    public FetchPathType getPathType() {
        return pathType;
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
        return pathType == FetchPathType.PREPARATION;
    }

    public boolean isExecute() {
        return pathType == FetchPathType.EXECUTION;
    }

    public boolean isFusion() {
        return fusionTemplate != null;
    }

    public String getTemplateName() {
        return templateFileName;
    }

    public void setTemplateName(final String name) {
        this.templateFileName = name;
    }

    public void setFusionTemplate(final long template) {
        this.fusionTemplate = template;
    }

    public BusinessObject getBusinessObject() {
        return businessObject;
    }

    public void setContextual(final Scenario context, final Set<FetchPlanElement> fetchPlan) {
        this.context = context;
        this.fetchPlan = CollectionFactory.newList();
        this.fetchPlan.addAll(fetchPlan);
    }

    public boolean isContextual() {
        return context != null;
    }

    public String getContextKeyname() {
        return context.getKeyname();
    }

    public Scenario getScenario() {
        return scenario;
    }

    public CRUD getCrud() {
        return crud;
    }

    public void free() {
        businessObject = null;
        scenario = null;
        crud = null;
        pathType = null;
        fusionTemplate = null;
        templateFileName = null;

        context = null;
        if (fetchPlan != null) {
            fetchPlan.clear();
            fetchPlan = null;
        }
    }
}
