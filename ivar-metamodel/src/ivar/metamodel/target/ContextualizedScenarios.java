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
package ivar.metamodel.target;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.FetchPathType;
import ivar.metamodel.target.generic.ServerCall;
import ivar.metamodel.target.java.FetchPlanElement;
import ivar.metamodel.target.java.JavaServerCall;
import java.util.Map;
import java.util.Set;
import ivar.helper.CollectionFactory;

public class ContextualizedScenarios extends AbstractObject {

    private Map<BusinessObject, Set<Scenario>> influencingScenarios = CollectionFactory.newMap();
    private Map<BusinessObject, Set<Scenario>> influencedScenarios = CollectionFactory.newMap();

    public ContextualizedScenarios() {
    }

    public void addSubsequentScenario(final Scenario scenario, final boolean influencing, final boolean influenced) {
        if (influenced) {
            addInfluencedScenario(scenario.getRootBusinesObject(), scenario);
        }
        if (influencing) {
            addInfluencingScenario(scenario.getRootBusinesObject(), scenario);
        }
    }

    public void computeContextualFetchPlans(final Scenario rootScenario) {
        beginBlock("Computing contextual fetch plans for root scenario " + rootScenario.keyname + "...");
        for (final BusinessObject businessObject : influencingScenarios.keySet()) {
            debug("Focus on business object " + businessObject.getName());
            final Set<FetchPlanElement> elems = CollectionFactory.newSet();
            for (final Scenario scenario : influencingScenarios.get(businessObject)) {
                debug("Scenario " + scenario.keyname + " influences fetch path, getting attributes list...");
                elems.addAll(businessObject.getFetchPlanList(scenario, FetchPathType.EXECUTION, ""));
                elems.addAll(businessObject.getFetchPlanList(scenario, FetchPathType.PREPARATION, ""));
            }
            setContextualServerCalls(businessObject, elems, rootScenario);
        }
        endBlock("Computing contextual fetch plans for root scenario " + rootScenario.keyname + "... Done.");
    }

    private void setContextualServerCalls(final BusinessObject rootBO, final Set<FetchPlanElement> elems, final Scenario rootScenario) {
        debug("Adding contextual server calls for all scenarios subsequent to " + rootScenario.keyname + " on root business object " + rootBO.getName());
        if (influencedScenarios.get(rootBO) != null) {
            for (final Scenario scenario : influencedScenarios.get(rootBO)) {
                debug("Scenario " + scenario.keyname + " is marked as influenced.");
                if (!scenario.getCrud().isCreate()) {
                    final ServerCall serverCall = new ServerCall(rootBO, scenario, FetchPathType.PREPARATION);
                    serverCall.setContextual(rootScenario, elems);
                    rootBO.addServerCall(serverCall);
                    scenario.getAssociatedScreen().addContextualServerCall(rootScenario.keyname + "prepare", new JavaServerCall(serverCall));
                }
                if (!scenario.getCrud().isSearch()) {
                    final ServerCall serverCall = new ServerCall(rootBO, scenario, FetchPathType.EXECUTION);
                    serverCall.setContextual(rootScenario, elems);
                    rootBO.addServerCall(serverCall);
                    scenario.getAssociatedScreen().addContextualServerCall(rootScenario.keyname + "exec", new JavaServerCall(serverCall));
                }
            }
        }
    }

    private void addInfluencedScenario(final BusinessObject rootBO, final Scenario scenario) {
        addTo(rootBO, scenario, influencedScenarios);
    }

    private void addInfluencingScenario(final BusinessObject rootBO, final Scenario scenario) {
        addTo(rootBO, scenario, influencingScenarios);
    }

    private void addTo(final BusinessObject rootBO, final Scenario scenario, final Map<BusinessObject, Set<Scenario>> target) {
        if (!target.containsKey(rootBO)) {
            final Set<Scenario> scenarios = CollectionFactory.newSet();
            target.put(rootBO, scenarios);
        }
        target.get(rootBO).add(scenario);
    }

    public void free() {
        influencedScenarios.clear();
        influencedScenarios = null;
        influencingScenarios.clear();
        influencingScenarios = null;
    }
}
