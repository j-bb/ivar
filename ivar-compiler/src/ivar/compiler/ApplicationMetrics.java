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
package ivar.compiler;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.helper.filecounter.FileCategories;
import ivar.helper.io.IOHelper;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.Filter;
import ivar.metamodel.spec.FilterElement;
import ivar.metamodel.spec.Jump;
import ivar.metamodel.spec.Rule;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ApplicationMetrics extends AbstractObject {

    private Collection<Scenario> scenarios;
    private int stepNumber;
    private int ruleNumber;
    private int filterNumber;
    private int specLineNumber;
    private int jumpNumber;
    private Application application;
    private IvarcParameters ivarcParameters;
    private int lineNumber;
    private int fileNumber;

    public ApplicationMetrics(final Application application, final IvarcParameters ivarcParameters) {
        this.application = application;
        this.ivarcParameters = ivarcParameters;
        scenarios = CollectionFactory.newList(application.getScenarios());
        computeMetrics();
    }

    public int getScenarioNumber() {
        return scenarios.size();
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public int getRuleNumber() {
        return ruleNumber;
    }

    public int getFilterNumber() {
        return filterNumber;
    }

    public int getSpecLineNumber() {
        return specLineNumber;
    }

    public int getJumpNumber() {
        return jumpNumber;
    }

    private void computeMetrics() {
        // TODO add the SQL generated for the DB schema
        stepNumber = 0;
        ruleNumber = 0;
        filterNumber = 0;
        specLineNumber = 0;
        for (final Scenario scenario : scenarios) {
            if (!scenario.isSynthetic()) {
                specLineNumber++; // for one scenario.

                if (scenario.hasRoles()) {
                    specLineNumber++;
                }

                if (scenario.getDocumentation() != null) {
                    specLineNumber += StringHelper.count(scenario.getDocumentation(), (char) Character.LINE_SEPARATOR);
                }

                if (scenario.getJumps() != null) {
                    for (final Jump jump : scenario.getJumps()) {
                        jumpNumber++;
                        specLineNumber++;
                    }
                }

                final Filter dynamicFilter = scenario.getStaticFilter();
                if (dynamicFilter != null) {
                    specLineNumber++;
                    filterNumber++;
                    for (final FilterElement filterElement : dynamicFilter.getFilterElements()) {
                        if (!filterElement.isBooleanOperator()) {
                            specLineNumber++;
                        }
                    }
                }

                final Filter staticFilter = scenario.getStaticFilter();
                if (staticFilter != null) {
                    specLineNumber++;
                    filterNumber++;
                    for (final FilterElement filterElement : staticFilter.getFilterElements()) {
                        if (!filterElement.isBooleanOperator()) {
                            specLineNumber++;
                        }
                    }
                }

                final Collection<Step> steps = scenario.getSteps();

                Set<Rule> rules = scenario.getRules();
                ruleNumber += rules.size();
                for (final Rule rule : rules) {
                    specLineNumber += StringHelper.count(rule.getValue(), (char) Character.LINE_SEPARATOR);
                }
                for (final Step step : steps) {
                    computeStep(step);
                }
            }
        }
    }

    private void computeStep(final Step step) {
        if (!step.isSynthetic()) {
            specLineNumber++; // for one step.
            stepNumber++;

            if (step.hasRoles()) {
                specLineNumber++;
            }

            if (step.getJumps() != null) {
                for (final Jump jump : step.getJumps()) {
                    jumpNumber++;
                    specLineNumber++;
                }
            }

            final Set<Rule> rules = step.getRules();
            ruleNumber += rules.size();
            for (final Rule rule : rules) {
                specLineNumber += StringHelper.count(rule.getValue(), (char) Character.LINE_SEPARATOR);
            }
            if (step.getEmbededSteps() != null) {
                for (final Step embededStep : step.getEmbededSteps()) {
                    computeStep(embededStep);
                }
            }
        }
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> result = CollectionFactory.newMap();
        result.put("scenarioNumber", getScenarioNumber());
        result.put("jumpNumber", jumpNumber);
        result.put("ruleNumber", ruleNumber);
        result.put("specLineNumber", specLineNumber);
        result.put("filterNumber", filterNumber);
        result.put("stepNumber", stepNumber);
        return result;
    }

    public int getFileNumber() {
        final Map<FileCategories, Set<String>> files = ivarcParameters.getFileCounter().getFiles();
        fileNumber = 0;
        for (final FileCategories fileType : files.keySet()) {
            final Set<String> filesOftheGivenType = files.get(fileType);
            fileNumber += filesOftheGivenType.size();
        }
        return fileNumber;
    }

    public float getRatio() {
        return lineNumber / specLineNumber;
    }

    public int getLineNumber() {
        final Map<FileCategories, Set<String>> files = ivarcParameters.getFileCounter().getFiles();
        lineNumber = 0;
        for (final FileCategories fileCategory : files.keySet()) {
            for (final String file : files.get(fileCategory)) {
                final File fileToInclude = new File(file);

                final BufferedReader fileReader;
                try {
                    fileReader = new BufferedReader(new FileReader(fileToInclude), IOHelper.BUFFER_SIZE);
                    String line;
                    String previousLine = null;
                    if (fileReader != null) {
                        try {
                            while ((line = fileReader.readLine()) != null) {
                                if (!line.equals(previousLine)) {
                                    lineNumber++;
                                }
                                previousLine = line;
                            }
                        } catch (IOException e) {
                            error("Error while computing line number in ApplicationMetrics (reading stream). File was " + file, e, true);
                        } finally {
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e) {
                                    // Nothing we can do.
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    error("Error (File not found) while computing line number in ApplicationMetrics (instanciating FileReader). File was " + file, e, true);
                }
            }
        }
        return lineNumber;
    }

    public IvarcParameters getIvarcParameters() {
        return ivarcParameters;
    }
}
