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
package ivar.generictarget.generator;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.Rule;
import ivar.metamodel.spec.RuleKinds;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.BusinessObjectType;
import ivar.metamodel.target.generic.WorkflowRule;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Take a Requirement Repository and build business object and all needed stuff but for generic target only. TODO Better error Handling when generate file. For
 * ex an exception with a boolean continue ? TODO create a second pass that will browe object repos for inheritance, factorising, cleaning and reordering
 * purpose. CHECK that this never generate concrete material again. this should build in memory repository only.
 */
public class BusinessObjectGenerator extends AbstractObject {

    private Set<Scenario> scenarios;
    private Application application;
    private Map<String, String> tableNamesForData = CollectionFactory.newMapWithInsertionOrderPreserved();

    /**
     * A collection of BusinessObject
     */
    public BusinessObjectGenerator(final Application application) {
        this.application = application;
        this.scenarios = application.getScenarios();
    }

    public void buildBusinessObject() {
        beginBlock("BusinessObjectGenerator.buildBusinessObject. Reading scenarios ...");
        databaseNamePass();
        // TODO refactor names with candidate postfix.
        // A Map of BusinessObject but only candidates. Key is the bo name.
        final Map<String, BusinessObject> businessObjectsCandidates = CollectionFactory.newMapWithInsertionOrderPreserved();
        BusinessObject businessObjectCandidate = null;
        int scenarioNumber = 1;
        for (final Scenario scenario : scenarios) {

            emptyLine();
            debug(scenarioNumber + ". reading [" + scenario.getKeyname() + "] " + scenario.getName() + " " + scenario.getCrud() + " " + scenario.getData());
            scenarioNumber++;
            // case of scenario that contain only referenceScenario step : do not build add to business object computation.
            if (scenario.isSynthetic()) {
                info("Synthetic scenario are excluded from business object computation : " + scenario.getKeyname());
            } else if (scenario.isComposite()) {
                info("Scenario that contains only referenceScenario steps are excluded from business object computation : " + scenario.getKeyname());
            } else {
                if (!businessObjectsCandidates.containsKey(scenario.getFullEntityName())) {
                    final String tableName = tableNamesForData.get(scenario.getData());
                    if (tableName == null || tableName.length() == 0) {
                        error("table name not found for data " + scenario.getData() + " while computing " + scenario.getKeyname(), true);
                    }
                    final BusinessObject bo = new BusinessObject(application, scenario, tableName);
                    businessObjectsCandidates.put(scenario.getFullEntityName(), bo);
                    debug("detecting a new business object : " + scenario.getFullEntityName());
                } else {
                    final BusinessObject businessObject = businessObjectsCandidates.get(scenario.getFullEntityName());
                    businessObject.addScenario(scenario);
                    debug("using already found business object : " + scenario.getFullEntityName());
                }

                businessObjectCandidate = businessObjectsCandidates.get(scenario.getFullEntityName());

                if (businessObjectCandidate == null) {
                    error("businessObjectCandidate is null for scenario " + scenario.getKeyname());
                }

                if (scenario.getRootBusinesObject() != null && scenario.getRootBusinesObject() != businessObjectCandidate) {
                    error("Scenario " + scenario.getKeyname() + " has wrong root business object. Was " + scenario.getRootBusinesObject() + " waiting for " + businessObjectCandidate);
                }
                scenario.setRootBusinesObject(businessObjectCandidate);

                businessObjectCandidate.addDoc(scenario.getComment());
                for (final Step step : scenario.getSteps()) {
                    if (step.getKeyname() == null) {
                        compileError("null keyname in a step");
                        beginCompileHelpBlock("For helping purpose here are details on the faulty step");
                        compileHelp("Step id = " + step.getId());
                        compileHelp(step.toString());
                        endCompileHelpBlock();
                    } else if (step.getStepConstraint() == null) {
                        compileError("null type in a step");
                        beginCompileHelpBlock("For helping purpose here are details on the faulty step");
                        compileHelp("Step id = " + step.getId());
                        compileHelp(step.toString());
                        endCompileHelpBlock();
                    } else {
                        onStep(businessObjectCandidate, step, scenario);
                    }
                }
            }
        }
        checkAttributes(businessObjectsCandidates);

        /*
        * Fill businessObject collection attribute from a Map of
        * BusinessObjectsCandidates as defined by buildBusinessObjects method. This
        * method instanciate real businessObject since there are no real
        * BusinessObjects in BusinessObjectsCandidates but only names and attributes.
        *
         */
        for (final BusinessObject businessObject : businessObjectsCandidates.values()) {
            application.addbusinessObject(businessObject);
        }

        // This step is usefull for search scenario that do not have full step (aka business item).
        // This is the case of default search where there is no step at all
        // 1. Provide the list of business items for a business object
        // TODO !!! Does not apears to work with a FastMap
        beginBlock("Fill empty or incomplete scenario");
        final Map<BusinessObject, Map<String, Step>> stepsForBusinessObject = CollectionFactory.newMapWithInsertionOrderPreserved();

        for (final Scenario scenario : scenarios) {
            final BusinessObject bo = scenario.getRootBusinesObject();
            Map<String, Step> businesselements = stepsForBusinessObject.get(bo);
            if (businesselements == null) {
                businesselements = CollectionFactory.newMap();
            }
            for (Step step : scenario.getSteps()) {
                businesselements.put(step.getKeyname(), step);
            }
            stepsForBusinessObject.put(bo, businesselements);
        }

        beginBlock("Synthetic scenarios ...");
        for (final Scenario scenario : scenarios) {
            if (scenario.isSynthetic()) {
                info("Found a synthetic scenario " + scenario.getKeyname());
                final String originalScenarioKeyname = scenario.getSynthetic();
                final Scenario originalScenario = application.getScenarioForKeyname(originalScenarioKeyname);
                if (originalScenario != null) {
                    scenario.setRootBusinesObject(originalScenario.getRootBusinesObject());
                    info("Found original scenario " + originalScenario.getKeyname() + ", Set RootBusinessObject " + scenario.getRootBusinesObject());
                } else {
                    error("Unable to find the original scenario and thus to set the RootBusinessObject");
                }
//                boolean found = false;
//                for (final Scenario originalScenario : scenarios) {
//                    if (originalScenario.getKeyname().equals(originalScenarioKeyname)) {
//                        scenario.setRootBusinesObject(originalScenario.getRootBusinesObject());
//                        info("Found original scenario " + originalScenario.getKeyname() + ", Set RootBusinessObject " + scenario.getRootBusinesObject());
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    error("Unable to find the original scenario and thus to set the RootBusinessObject");
//                }
                emptyLine();
            }
        }
        endBlock("Synthetic scenarios ...");
        emptyLine();
        beginBlock("Empty scenarios ...");
        for (final Scenario scenario : scenarios) {
            if (scenario.getRootBusinesObject() != null || scenario.isComposite()) {
                continue;
            }
            debug("Empty scenario found (no business object associated) " + scenario.getKeyname() + " " + scenario.getCrud().toString().toUpperCase() + " " + scenario.getData());
            final String businessObjectKey = scenario.getFullEntityName();
            final BusinessObject businessObject = businessObjectsCandidates.get(businessObjectKey);
            if (businessObject != null) {
                scenario.setRootBusinesObject(businessObject);
                // Now, fill the computed businessElement instead of the empty one.
                Set<Step> oldSteps = scenario.getSteps();
                Map<String, Step> referenceSteps = stepsForBusinessObject.get(businessObject);
                Set<Step> newSteps = CollectionFactory.newSetWithInsertionOrderPreserved(oldSteps.size());
                for (final Step oldStep : oldSteps) {
                    Step newStep = referenceSteps.get(oldStep.getKeyname());
                    newSteps.add(newStep);
                }
                scenario.setSteps(newSteps);
            } else {
                String message = "Unknown business object " + scenario.getData() + " in scenario " + scenario.getCrud().toString().toUpperCase() + " " + scenario.getKeyname();
                error(message);
            }
        }
        endBlock("Empty scenarios ...");

        endBlock("Fill empty or incomplete scenario. Done.");

        populatebusinessObjectType(businessObjectsCandidates);

        mandatoryCheckForStepThatAreNotPresentInAllScenario();

        workflowStep();

        endBlock("BusinessObjectGenerator.buildBusinessObject. Done. " + scenarioNumber + " scenarios read");
    }

    private void mandatoryCheckForStepThatAreNotPresentInAllScenario() {
        beginBlock("BusinessObjectGenerator : check that Step that are not present in all scenario really are mandatory.");
        int count = 0;
        for (final Scenario scenario : scenarios) {
            if (!scenario.getCrud().isCreate()) {
                continue;
            }
            final BusinessObject bo = scenario.getRootBusinesObject();
            for (final BusinessObjectAttribute boa : bo.getAttributes()) {
                if (!boa.isMandatory()) {
                    continue;
                }
                for (final Step step : boa.getSteps()) {
                    boolean found = false;
                    for (final Step scenarioStep : scenario.getSteps()) {
                        if (step.getKeyname().equals(scenarioStep.getKeyname())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        info("BOA " + bo.getFullName() + "." + boa.getKeyname() + " is marked as mandatory but corresponding step"
                                + " is not present in create scenario " + scenario.getKeyname() + ". Changing it to non-mandatory.");
                        boa.setMandatory(false);
                        count++;
                        break;
                    }
                }
            }
        }
        endBlock("BusinessObjectGenerator : check that Step that are not present in all scenario really are mandatory. Found " + count + ". Done.");
    }

    private void databaseNamePass() {
        beginBlock("BusinessObjectGenerator.databaseNamePass");
        final Map<String, Set<Scenario>> sortedScenario = CollectionFactory.newMapWithInsertionOrderPreserved();
        for (final Scenario scenario : scenarios) {
            final String key = scenario.getData();
            if (!sortedScenario.containsKey(key)) {
                final Set<Scenario> col = CollectionFactory.newSetWithInsertionOrderPreserved();
                sortedScenario.put(key, col);
            }
            final Set<Scenario> scenariosForData = sortedScenario.get(key);
            scenariosForData.add(scenario);
        }
        for (final String data : sortedScenario.keySet()) {
            final Set<Scenario> scenariosForData = sortedScenario.get(data);
            String tableNameCandidate = null;
            for (final Scenario scenario : scenariosForData) {
                if (scenario.getTechdata() != null && scenario.getTechdata().length() > 0) {
                    if (tableNameCandidate == null) {
                        tableNameCandidate = scenario.getTechdata();
                    } else {
                        if (!tableNameCandidate.equals(scenario.getTechdata())) {
                            compileError("Tech data differs for scenario " + scenario.getKeyname() + " data is " + scenario.getData() + " tech data is " + scenario.getTechdata() + " expected " + tableNameCandidate, true);
                            beginCompileHelpBlock("For helping purpose, here is a dump of all scenario that has " + scenario.getData() + " as Data :");
                            compileHelp("Make sure all potential TechData are consistent.");
                            for (final Scenario dbgScenario : scenariosForData) {
                                compileHelp("\t* " + dbgScenario.getKeyname() + " data is " + dbgScenario.getData() + " techdata is " + dbgScenario.getTechdata());
                            }
                            endCompileHelpBlock();
                        }
                    }
                }
            }
            if (tableNameCandidate == null) {
                tableNameCandidate = data;
            }
            tableNamesForData.put(data, tableNameCandidate);
        }
        beginBlock("Check techname are consistent");
        final Map<String, Set<Scenario>> scenariosForTechData = CollectionFactory.newMapWithInsertionOrderPreserved();
        for (final Scenario scenario : scenarios) {
            final String key = scenario.getTechdata();
            if (key != null && key.length() > 0) {
                if (!scenariosForTechData.containsKey(key)) {
                    final Set<Scenario> scenarios = CollectionFactory.newSetWithInsertionOrderPreserved();
                    scenariosForTechData.put(key, scenarios);
                }
                final Set<Scenario> scenariosForData = scenariosForTechData.get(key);
                scenariosForData.add(scenario);
            }
        }
        for (final String techdata : scenariosForTechData.keySet()) {
            final Set<Scenario> scenarios = scenariosForTechData.get(techdata);
            String data = null;
            for (final Scenario scenario : scenarios) {
                if (scenario.getData() != null && scenario.getData().length() > 0) {
                    if (data == null) {
                        data = scenario.getData();
                    } else {
                        if (!data.equals(scenario.getData())) {
                            compileError("Data or Tech data inconsistency for scenario " + scenario.getKeyname() + " data is " + scenario.getData() + " tech data is " + scenario.getTechdata() + " expected " + data, true);
                            beginCompileHelpBlock("For helping purpose, here is a dump of all scenario that has " + scenario.getTechdata() + " as Techdata :");
                            compileHelp("Make sure all potential Data or TechData are consistent.");
                            for (final Scenario dbgScenario : scenarios) {
                                compileHelp("\t* " + dbgScenario.getKeyname() + " data is " + dbgScenario.getData() + " techdata is " + dbgScenario.getTechdata());
                            }
                            endCompileHelpBlock();
                        }
                    }
                }
            }
        }
        endBlock("Check techname are consistent");

        endBlock("BusinessObjectGenerator.databaseNamePass");
    }

    private void checkStates(final Map<String, Collection<WorkflowRule>> ref, final Map<String, Collection<WorkflowRule>> test, final boolean refSet) {
        final Set<String> testKeySet = test.keySet();
        for (final String testBusinessObjectName : testKeySet) {
            final Collection<WorkflowRule> testRules = test.get(testBusinessObjectName);
            final Collection<WorkflowRule> refRules = ref.get(testBusinessObjectName);
            if (refRules == null) {
                if (refSet) {
                    error("[Workflow] inconsistency : bo " + testBusinessObjectName + " has state(s) that are checked but never set", true);
                } else {
                    warning("[Workflow] inconsistency : bo " + testBusinessObjectName + " has state(s) that are set but never checked");
                }
            }
            // This is Map to make sure each state is here only once
            final Map<String, String> refStates = CollectionFactory.newMap();
            final Map<String, String> testStates = CollectionFactory.newMap();

            for (WorkflowRule workflowRule : testRules) {
                final Collection<String> states = workflowRule.getStates();
                for (String state : states) {
                    testStates.put(state, state);
                }
            }

            for (WorkflowRule workflowRule : refRules) {
                final Collection<String> states = workflowRule.getStates();
                for (String state : states) {
                    refStates.put(state, state);
                }
            }

            final Set<String> testStatesKeySet = testStates.keySet();
            for (final String testState : testStatesKeySet) {
                final String refState = refStates.get(testState);
                if (refState == null) {
                    if (refSet) {
                        error("[Workflow] inconsistency : state " + testBusinessObjectName + "." + testState + " is checked but never set", true);
                    } else {
                        warning("[Workflow] inconsistency : state " + testBusinessObjectName + "." + testState + " is set but never checked");
                    }
                }
            }
            refStates.putAll(testStates);
            info("[Workflow] " + testBusinessObjectName + " found " + refStates.size() + " states");

            for (final BusinessObject businessObject : application.getBusinessObjects()) {
                if (businessObject.getFullName().equals(testBusinessObjectName)) {
                    final Set<String> bostates = refStates.keySet();
                    businessObject.setSates(bostates);
                    for (final String bostate : bostates) {
                        debug("[State] " + testBusinessObjectName + ".state = " + bostate);
                    }
                    break;
                }
            }
            refStates.clear();
            testStates.clear();
        }
    }

    private void workflowStep() {
        beginBlock("Workflow step");
        Map<String, Collection<WorkflowRule>> rulesSetMap = CollectionFactory.newMap();
        Map<String, Collection<WorkflowRule>> rulesCheckMap = CollectionFactory.newMap();
        for (Scenario scenario : scenarios) {
            for (Step step : scenario.getSteps()) {
                for (Rule rule : step.getRules()) {
                    if (RuleKinds.stateset.equals(rule.getKind())) {
                        final BusinessObject rootBusinesObject = scenario.getRootBusinesObject();
                        if (rootBusinesObject == null) {
                            error("[Workflow] step, scenario " + scenario.getKeyname() + " doesn't have rootbusinessobject", true);
                        }
                        final String rootBusinessObjectName = rootBusinesObject.getFullName();
                        Collection<WorkflowRule> rulesSet;
                        rulesSet = rulesSetMap.get(rootBusinessObjectName);
                        if (rulesSet == null) {
                            rulesSet = CollectionFactory.newList();
                            rulesSetMap.put(rootBusinessObjectName, rulesSet);
                        }
                        WorkflowRule workflowRule = new WorkflowRule(rule);
                        rulesSet.add(workflowRule);
                    }
                    if (RuleKinds.statecheckset.equals(rule.getKind()) || RuleKinds.statechecknotset.equals(rule.getKind())) {
                        final BusinessObject rootBusinesObject = scenario.getRootBusinesObject();
                        if (rootBusinesObject == null) {
                            error("[Workflow] step, scenario " + scenario.getKeyname() + " doesn't have rootbusinessobject", true);
                        }
                        final String rootBusinessObjectName = rootBusinesObject.getFullName();
                        Collection<WorkflowRule> rulesChecked;
                        rulesChecked = rulesCheckMap.get(rootBusinessObjectName);
                        if (rulesChecked == null) {
                            rulesChecked = CollectionFactory.newList();
                            rulesCheckMap.put(rootBusinessObjectName, rulesChecked);
                        }
                        WorkflowRule workflowRule = new WorkflowRule(rule);
                        rulesChecked.add(workflowRule);
                    }
                }

            }
        }
        checkStates(rulesSetMap, rulesCheckMap, true);
        checkStates(rulesCheckMap, rulesSetMap, false);
        endBlock("Workflow step");
    }

    private void onStep(final BusinessObject businessObjectCandidate, final Step step, final Scenario scenario) {
        if (step.getStepConstraint().isScenarioReference()) {
            info("Reference scenario step for business object computation (" + businessObjectCandidate.getFullName() + ") : " + step.toString());
        }
        final boolean noSubStep = step.getEmbededSteps().size() == 0;
        if (!noSubStep) {
            info("Not done yet : case of substep of " + scenario.getKeyname() + "." + step.getKeyname());
        }
        BusinessObjectAttribute businessAttributeCandidate = businessObjectCandidate.getBusinessAttributeByName(step.getKeyname());
        if (businessAttributeCandidate == null) {
            businessAttributeCandidate = onStepNewAttribute(step, scenario, businessObjectCandidate);
            businessObjectCandidate.addAttributes(businessAttributeCandidate);
        } else {
            // Merge section
            onStepMergeAttribute(businessAttributeCandidate, step, scenario);
        }
        businessAttributeCandidate.addDoc(step.getDocumentation());
        // Check section
        // TODO check what ? explain a good and a wrong test case.
        if (!businessAttributeCandidate.getName().equals(step.getKeyname())) {
            compileError("There is an consistency error in business element's name : " + businessAttributeCandidate.getName() + " versus " + businessAttributeCandidate.getName());
        }
    }

    private void onStepMergeAttribute(final BusinessObjectAttribute businessAttributeCandidate, final Step step, final Scenario scenario) {
        businessAttributeCandidate.addStep(step);
        // TODO a simple merge
        debug("check for a merge for " + scenario.getData() + "." + step.getKeyname());
        // Mandatory merge
        if (!step.isMandatory()) {
            businessAttributeCandidate.setMandatory(false);
        }
        // Cardinality merge :
        // Prendre la plus large. Attention toutefois aux collections

        if (!(businessAttributeCandidate.isCollection() == step.isCollection())) {
            warning("Collection property is not consistent across scenarii for the " + step.getKeyname() + " for " + scenario.getData() + "");
            warning("\t so requirements are amended following this rule : resulting " + step.getKeyname() + " cardinality will have");
            final int newMin = Math.min(businessAttributeCandidate.getMinInt(), step.getMinInt());
            warning("\t the minimum from " + businessAttributeCandidate.getMin() + " and " + step.getMin() + " = " + newMin);
            businessAttributeCandidate.setMinInt(newMin);
            step.setMinInt(newMin);
            // See (1)
            if (businessAttributeCandidate.isUpToStar() || step.isUpToStar()) {
                businessAttributeCandidate.setMax("*");
                step.setMax("*");
                warning("\t the maximum from " + businessAttributeCandidate.getMax() + " and " + step.getMax() + " = *");
            } else {
                final int newMax = Math.max(businessAttributeCandidate.getMaxInt(), step.getMaxInt());
                businessAttributeCandidate.setMaxInt(newMax);
                step.setMaxInt(newMax);
                warning("\t the maximum from " + businessAttributeCandidate.getMax() + " and " + step.getMax() + " = " + newMax);
            }
            warning("It is strongly suggested that you amend the requirement to be consistent for the cardinality of " + scenario.getData() + " across scenarii.");
            // See (1)
            // (1) : we have to change the businesselement because of the UI.
            // Say one step is String as one name and another one is also String for one or more name.
            // Then, in the form, wich name should be shown ?
            // Has we don't know and it is not possible to guess, we change the user requirement to be consistent.
            //
            // Now in case, we have to replace the new cardinality in all other similar Step
            // NOTE : THIS WILL BE THE CASE FOR ALL MERGING
            for (final Scenario theScenario : scenarios) {
                if (theScenario.getData().equals(scenario.getData())) {
                    for (Step theStep : theScenario.getSteps()) {
                        if (theStep.getKeyname().equals(step.getKeyname())) {
                            theStep.setMin(step.getMin());
                            theStep.setMax(step.getMax());
                        }
                    }
                }
            }
        }

        // Type merge
        BusinessObjectType bolTypeCandidate = new BusinessObjectType(step, businessAttributeCandidate);
        if (!businessAttributeCandidate.getType().equals(bolTypeCandidate)) {
            warning("type are different for " + businessAttributeCandidate.getName() + " : " + businessAttributeCandidate.getType()
                    + " | " + bolTypeCandidate);
            // TODO Classer les types par ordre croissant et prendre le moins contraignant ?
        }

        // Calculated merge
        if (businessAttributeCandidate.isCalculated() != step.isCalculated()) {
            warning("calculated status inconstistency for " + businessAttributeCandidate.getName());
            // How to deal with that ?
            // What if rules are different depending on scenario?
            // Does that mean a non persistent attribute ?
            // Rules are started on client side then ...
        }
    }

    private BusinessObjectAttribute onStepNewAttribute(final Step step, final Scenario scenario, final BusinessObject businessObject) {
        BusinessObjectAttribute businessAttributeCandidate;
        businessAttributeCandidate = new BusinessObjectAttribute(scenario, step, businessObject);

        // Warning : relation management.
        // How to handle non bolis primitive type ?
        // Reflexion du 9 aout : les connections sur les type
        // complexe (non primitif au sens bolis)
        // ne peuvent se faire que sur une deuxieme passe car pour
        // ne pas imposer d'ordre dans la declaration
        // on peut faire reference a des BO non encore deduit lors
        // de la premiere passe.
        // Voila pourquoi il faut prendre ici, le type tel quel dans
        // verif particuliere.
        // Ce n'est qu'ensuite que l'on verifiera que la reference
        // est bien un BO existant.
        // Attention au cas particulier suivant : Client -> adresse.
        // par ex, il se peut qu'aucun scenatio ne mentionne
        // l'adresse.
        // Tout reste pourtant valide. Un client possede une
        // adresse, qui est un type complexe
        // mais qui n'est pas editable en tant que tel, donc pas de
        // scenario d�di� a l'adresse.
        // Dans ce cas, comment distinguer une faute de frappe sur
        // un attribut complexe du cas precedant ?
        //
        // Possibilite d'emettre des WRN en cas d'entite
        // innaccesible ?
        // Genre seulement un ou des scenar de lecture de Truc ?
        // Du coup, si je trouve aucun scenar principal pour Adresse
        // ET QUE
        // l'Adresse n'est referenc�e que sur un scenar en lecture
        // pour Client,
        // ALORS Adresse n'est jamais saisie.
        // Attention a l'integration de systeme, ce n'est pas
        // forcement un pb redibitoire
        // donc seulement un WRN.
        //
        // La deuxieme passe peut se faire sur le repos de BO et non
        // pas sur les scenars.
        //
        // Reflexion du 13 Aout.
        // En fait il n'est peut etre pas necessaire de faire cette
        // passe a ce niveau
        // c'est a dire au niveau de la donctruction du referentiel
        // de requirements.
        // Cette passe peut se faire au niveau des divers
        // generations exploitant le refernetiel de requirement.
        return businessAttributeCandidate;
    }

    protected void checkAttributes(final Map<String, BusinessObject> businessObjectsCandidates) {
        final Collection<BusinessObject> businessObjects = businessObjectsCandidates.values();
        for (final BusinessObject businessObject : businessObjects) {
            for (final BusinessObjectAttribute businessAttribute : businessObject.getAttributes()) {
                if (!businessAttribute.getType().isBuiltIn()) {
                    // TODO really handle package name including for BolType
                    // that can by anything from any businessobject even in
                    // another package.
                    final String type = businessAttribute.getType().getName();
                    final String key = businessObject.getDomainName() + "." + type;
                    if (null == businessObjectsCandidates.get(key)) {
                        error(businessObject.getName() + "." + businessAttribute.getName() + " has an unrecognized type : " + type, true);
//                        beginCompileHelpBlock("For compile help, here is the faulty type");
//                        compileHelp(businessAttribute.get);
//                        endCompileHelpBlock();
                    }
                }
            }
        }
    }

    protected void populatebusinessObjectType(final Map<String, BusinessObject> businessObjectsMap) {
        final Collection<BusinessObject> businessObjects = businessObjectsMap.values();
        for (final BusinessObject businessObject : businessObjects) {
            for (final BusinessObjectAttribute businessAttribute : businessObject.getAttributes()) {
                final BusinessObjectType businessAttributeType = businessAttribute.getType();
                if (!businessAttributeType.isBuiltIn()) {
                    final String type = businessAttribute.getType().getName();
                    final String key = businessObject.getDomainName() + "." + type;
                    if (null == businessObjectsMap.get(key)) {
                        error(businessObject.getName() + "." + businessAttribute.getName() + " has an unrecognized type : " + type, true);
                    }
                    final String name = businessAttributeType.getName();
                    //TODO beware : this won't retreive BO if a domain is set at scenario level !!! Improve that when we'll have money.
                    final BusinessObject businessObjectForType = businessObjectsMap.get(key);
                    if (businessObjectForType == null) {
                        error(businessAttributeType.getName() + " " + businessObject.getName() + "." + businessAttribute.getName() + " : can't find BusinessObject " + key, true);
                    }
                    businessAttributeType.setBusinessObject(businessObjectForType);
                }
            }
        }
    }
}
