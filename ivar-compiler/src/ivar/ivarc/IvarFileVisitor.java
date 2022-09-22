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
package ivar.ivarc;

import ivar.common.logger.FullLogger;
import ivar.grammar.analysis.DepthFirstAdapter;
import ivar.grammar.node.AApplicationDeclaration;
import ivar.grammar.node.ACompleteScenarioDeclaration;
import ivar.grammar.node.ADataDeclaration;
import ivar.grammar.node.ADatasetDeclaration;
import ivar.grammar.node.ANumberedCardinality;
import ivar.grammar.node.ARole;
import ivar.grammar.node.ASimpleScenarioDeclaration;
import ivar.grammar.node.ASimpleStepDeclaration;
import ivar.grammar.node.ASingleTypeImportDeclaration;
import ivar.grammar.node.AStarCardinality;
import ivar.grammar.node.ATypeImportStarDeclaration;
import ivar.grammar.node.Node;
import ivar.helper.StringHelper;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.Dataset;
import ivar.metamodel.spec.DatasetData;
import ivar.metamodel.spec.DuplicateDatasetDataException;
import ivar.metamodel.spec.DuplicateDatasetException;
import ivar.metamodel.spec.DuplicateRoleException;
import ivar.metamodel.spec.DuplicateScenarioException;
import ivar.metamodel.spec.DuplicateStepException;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;

public class IvarFileVisitor extends DepthFirstAdapter {

    private final FullLogger log;
    private final IvarFilePositionsVisitor positionVisitor;

    private final Application currentApplication;
    private Scenario currentScenario;
    private Step currentStep;
    private String currentDocumentation;
    private String currentComment;
    private Dataset currentDataset;

    IvarFileVisitor(final FullLogger logger, final Application application, final IvarFilePositionsVisitor positionVisitor) {

        if (application == null) {
            this.currentApplication = new Application();
        } else {
            this.currentApplication = application;
        }
        this.log = logger;
        this.positionVisitor = positionVisitor;
    }

    public Application getApplication() {
        return currentApplication;
    }

    public String getPositionFromNode(final Node node) {
        return positionVisitor.getPositionFromNode(node);
    }

//    private String getTokenValue(final Token token) {
//        String result = null;
//        if (token != null) {
//            result = token.getText();
//            if (result != null) {
//                result = result.trim();
//            }
//        }
//        return result;
//    }
    private String NameFromFQDN(final String fqdn) {
        return StringHelper.getLastRight(fqdn, ".");
    }

    private String packageFromFQDN(final String fqdn) {
        return StringHelper.getLastLeft(fqdn, ".");
    }

    private String removeSpace(final String s) {
        return s.replaceAll("\\s", "");
    }

    private String removeQuote(final String s) {
        return s.trim().replaceAll("\"", "");
    }

    @Override
    public void inASingleTypeImportDeclaration(ASingleTypeImportDeclaration node) {
        final String importName = removeSpace(node.getName().toString());
        log.info("[visitor] Encounter import " + importName);
        log.compileInfo("Import feature is not ready. Import " + importName + " is ignored for now.");
    }

    @Override
    public void inATypeImportStarDeclaration(ATypeImportStarDeclaration node) {
        final String importName = removeSpace(node.getName().toString());
        log.info("[visitor] Encounter import * " + importName);
        log.compileInfo("Import feature is not ready. Import " + importName + " is ignored for now.");
    }

    @Override
    public void inAApplicationDeclaration(AApplicationDeclaration node) {
        final String applicationName = removeQuote(node.getApplicationName().getText());
        final String applicationKeyname = node.getApplicationKeyname().toString().trim();
        final String applicationLang = node.getLang().getText().trim();
        log.info("[visitor] Encounter application " + applicationKeyname + " \"" + applicationName + "\" " + applicationLang);
        currentApplication.setLang(applicationLang);
        currentApplication.setKeyname(applicationKeyname);
        currentApplication.setName(applicationName);
    }

    @Override
    public void inASimpleScenarioDeclaration(ASimpleScenarioDeclaration node) {
        //super.caseASimpleScenarioDeclaration(node);
        final String data = removeSpace(node.getScenarioData().toString());
        final String keyname = removeSpace(node.getScenarioKeyname().toString());
        final String name = removeQuote(node.getScenarioName().getText());
        final String CRUD = removeSpace(node.getCrudModifier().toString());
        log.info("[visitor] Encounter simple Scenario " + keyname + " \"" + name + "\" " + CRUD + " " + data);
        currentScenario = new Scenario(keyname, name, CRUD, data);
    }

    @Override
    public void inACompleteScenarioDeclaration(ACompleteScenarioDeclaration node) {
        //super.caseACompleteScenarioDeclaration(node);
        final String data = removeSpace(node.getScenarioData().toString());
        final String keyname = removeSpace(node.getScenarioKeyname().toString());
        final String name = removeQuote(node.getScenarioName().getText());
        final String CRUD = removeSpace(node.getCrudModifier().toString());
        log.info("[visitor] Encounter complete Scenario " + keyname + " \"" + name + "\" " + CRUD + " " + data);
        currentScenario = new Scenario(keyname, name, CRUD, data);
    }

    @Override
    public void outASimpleScenarioDeclaration(ASimpleScenarioDeclaration node) {
        addCurrentScenarioToApplication(node);
    }

    @Override
    public void outACompleteScenarioDeclaration(ACompleteScenarioDeclaration node) {
        addCurrentScenarioToApplication(node);
    }

    private void addCurrentScenarioToApplication(final Node node) {
        log.beginBlock("[visitor] adding scenario " + currentScenario.getKeyname() + " " + currentScenario.getCrud() + " " + currentScenario.getData() + " ...");
        try {
            currentApplication.addScenario(currentScenario);
            currentScenario.setApplication(currentApplication);
        } catch (DuplicateScenarioException ex) {
            final String message = "[visitor] " + getPositionFromNode(node) + ". Duplicate scenario " + currentScenario.getKeyname();
            log.compileError(message, true);
            throw new IvarcException(message, ex);
        }
        log.endBlock("[visitor] adding scenario " + currentScenario.getKeyname() + " " + currentScenario.getCrud() + " " + currentScenario.getData() + ". Done.");
        currentScenario = null;
    }

    @Override
    public void inARole(ARole node) {
        final String keyname = node.getRoleKeyname().toString().trim();
        final String modifier = node.getRoleModifier().toString().trim();
        final String name = (node.getRoleName() == null) ? null : removeQuote(node.getRoleName().getText().trim());

        log.info("[visitor] Encounter a role " + modifier + " " + keyname + ((name == null) ? "" : " \"" + name + "\""));
        if (name != null) {
            log.compileInfo("Role name feature is not ready. Role will be used but name " + " \"" + name + "\"" + " is ignored for now.");
        }
        if (!"+".equals(modifier)) {
            log.compileInfo("Disallowed role feature is not ready. Role " + keyname + " is ignored for now.");
        } else {
            if (currentStep == null) {
                log.beginBlock("Adding Role " + modifier + " " + keyname + " to scenario " + currentScenario.getKeyname());
                try {
                    currentScenario.addRole(keyname);
                } catch (DuplicateRoleException e) {
                    final String message = "[visitor] " + getPositionFromNode(node) + ". Duplicate role " + keyname + " in scenario " + currentScenario.getKeyname();
                    log.compileError(message, true);
                    throw new IvarcException(message, e);
                }
                log.endBlock("Adding Role " + modifier + " " + keyname + ". Done.");
            } else {
                //log.beginBlock("Adding Role " + modifier + " " + keyname + " to step " + currentScenario.getKeyname() + "." + currentStep.getKeyname());
                log.compileInfo("Step Role feature is not ready. The following step role is ignored: " + modifier + " " + keyname + " to step " + currentScenario.getKeyname() + "." + currentStep.getKeyname());
                //currentStep.addRole(keyname);
                //log.endBlock("Adding Role " + modifier + " " + keyname + ". Done.");
            }
        }
    }

    @Override
    public void inASimpleStepDeclaration(ASimpleStepDeclaration node) {
        final String type = node.getStepType().toString().trim();
        final String keyname = node.getStepKeyname().toString().trim();
        final String name = (node.getStepName() != null) ? removeQuote(node.getStepName().getText().trim()) : null;
        log.info("[visitor] Encounter a step " + type + " " + keyname + " " + ((name == null) ? "" : " \"" + name + "\""));
        currentStep = new Step(type, keyname, name);
    }

    @Override
    public void outASimpleStepDeclaration(ASimpleStepDeclaration node) {
        log.beginBlock("Adding step " + currentStep.getStepConstraint().getType() + " " + currentStep.getMin() + " .. " + currentStep.getMax() + " " + currentStep.getKeyname() + " ...");
        try {
            currentScenario.addStep(currentStep);
        } catch (DuplicateStepException ex) {
            final String message = "[visitor] " + getPositionFromNode(node) + ". Duplicate step " + currentStep.getStepConstraint().getType() + " " + currentStep.getMin() + " .. " + currentStep.getMax() + " " + currentStep.getKeyname();
            log.compileError(message, true);
            throw new IvarcException(message, ex);
        }
        log.endBlock("Adding step " + currentStep.getStepConstraint().getType() + " " + currentStep.getMin() + " .. " + currentStep.getMax() + " " + currentStep.getKeyname() + ". Done.");
        currentStep = null;
    }
// TODO take into account optional techkeyname for both scenario and step.

    @Override
    public void inANumberedCardinality(ANumberedCardinality node) {
        final String min = node.getMin().getText().trim();
        final String max = node.getMax().getText().trim();
        log.info("[visitor] Encounter a cardinality " + min + " .. " + max);
        currentStep.setMin(min);
        currentStep.setMax(max);
    }

    @Override
    public void inAStarCardinality(AStarCardinality node) {
        final String min = node.getMin().getText().trim();
        final String max = "*";
        log.info("[visitor] Encounter a cardinality " + min + " .. " + max);
        currentStep.setMin(min);
        currentStep.setMax(max);
    }

    @Override
    public void inADatasetDeclaration(ADatasetDeclaration node) {
        final String keyname = node.getDatasetKeyname().toString().trim();
        final String name = (node.getDatasetName() != null) ? removeQuote(node.getDatasetName().getText().trim()) : null;
        log.info("[visitor] Encounter a dataset " + keyname + " " + ((name == null) ? "" : " \"" + name + "\""));
        currentDataset = new Dataset(keyname, name);
    }

    @Override
    public void inADataDeclaration(ADataDeclaration node) {
        final String keyname = node.getDataKeyname().toString().trim();
        final String modifier = (node.getDataModifier() != null) ? node.getDataModifier().toString() : null;
        final String valueInLang = (node.getDataValueInLang() != null) ? removeQuote(node.getDataValueInLang().getText().trim()) : null;
        if (valueInLang != null) {
            log.compileInfo("Value in lang (name) for DatasetData is not yet ready. It will be ignored.");
        }
        log.info("[visitor] Encounter a data " + currentDataset.getKeyname() + " : " + (modifier != null ? "*" : "") + " " + keyname + " " + ((valueInLang == null) ? "" : " \"" + valueInLang + "\""));
        final DatasetData dataSetData = new DatasetData(keyname, modifier != null);

        log.beginBlock("[visitor] Adding to Dataset " + currentDataset.getKeyname() + " data " + (modifier != null ? "*" : "") + " " + keyname + " " + ((valueInLang == null) ? "" : " \"" + valueInLang + "\" ..."));
        try {
            currentDataset.addData(dataSetData);
        } catch (DuplicateDatasetDataException ex) {
            final String message = "[visitor] " + getPositionFromNode(node) + ". Duplicate datasetData " + currentDataset.getKeyname() + " : " + dataSetData.getValue();
            log.compileError(message, true);
            throw new IvarcException(message, ex);
        }
        log.endBlock("[visitor] Adding to Dataset " + currentDataset.getKeyname() + " data " + (modifier != null ? "*" : "") + " " + keyname + " " + ((valueInLang == null) ? "" : " \"" + valueInLang + "\". Done."));
    }

    @Override
    public void outADatasetDeclaration(ADatasetDeclaration node) {
        log.beginBlock("Adding Dataset " + currentDataset.getKeyname() + " " + ((currentDataset.getName() == null) ? "" : " \"" + currentDataset.getName() + "\" ..."));
        try {
            currentApplication.addDataSet(currentDataset);
        } catch (DuplicateDatasetException ex) {
            final String message = "[visitor] " + getPositionFromNode(node) + ". Duplicate dataset " + currentDataset.getKeyname();
            log.compileError(message, true);
            throw new IvarcException(message, ex);
        }
        log.endBlock("Adding Dataset " + currentDataset.getKeyname() + " " + ((currentDataset.getName() == null) ? "" : " \"" + currentDataset.getName() + "\". Done."));
        currentDataset = null;
    }

    void freeBeforeCompile() {
        positionVisitor.freeBeforeCompile();
        currentScenario = null;
        currentStep = null;
        currentDocumentation = null;
        currentComment = null;
        currentDataset = null;
    }
}
