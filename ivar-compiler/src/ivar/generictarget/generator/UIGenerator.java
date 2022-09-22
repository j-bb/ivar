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
import ivar.generictarget.generator.rule.RuleDependencyManager;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.CRUD;
import ivar.metamodel.spec.Rule;
import ivar.metamodel.spec.RuleKinds;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import ivar.metamodel.target.generic.rule.DependencyDesc;
import ivar.metamodel.target.generic.rule.FunctionDesc;
import ivar.metamodel.target.generic.rule.IdentifierDesc;
import ivar.metamodel.target.generic.rule.ParamDesc;
import ivar.metamodel.target.generic.rule.SumFunctionDesc;
import ivar.metamodel.target.generic.ui.Screen;
import ivar.metamodel.target.generic.ui.ScreenField;
import ivar.metamodel.target.generic.ui.SumDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.scriptonite.lexer.LexerException;
import org.scriptonite.parser.ParserException;

public class UIGenerator extends AbstractObject {

    public void buildUI(final Application application) {
        final List<Scenario> notDoneScenarios = CollectionFactory.newList();
        for (final Scenario scenario : application.getScenarios()) {
            if (scenario.isComposite()) {
                final Screen screen = new Screen(application, scenario);
                for (final Step step : scenario.getSteps()) {
                    onStep(step, screen);
                }
                addScreen(application, screen);
            } else if (scenario.getCrud() == CRUD.create) {
                final Screen screen = new Screen(application, scenario);
                for (final Step step : scenario.getSteps()) {
                    onStep(step, screen);
                }
                addScreen(application, screen);
            } else if (scenario.getCrud().isRead()) {
                final Screen searchScreen = new Screen(application, scenario);
                for (final Step step : scenario.getSteps()) {
                    onStep(step, searchScreen);
                }
                addScreen(application, searchScreen);
            } else {
                notDoneScenarios.add(scenario);
            }

        }

        // Phase II
        for (final Scenario scenario : notDoneScenarios) {
            debug("Scenario computed in phase II (from notdoneScenarios) : " + scenario.getKeyname());
            final Screen screen = new Screen(application, scenario);
            final boolean hasStep = scenario.getSteps().size() > 0;
            if (hasStep) {
                for (final Step step : scenario.getSteps()) {
                    onStep(step, screen);
                }
            }
            addScreen(application, screen);
        }
        generateDependencies(application);
    }

    private void addScreen(final Application application, final Screen screen) {
        info("[SCREEN] new screen detected [" + screen.getCrud() + "] " + screen.getQxClassName());
        application.addScreen(screen);
    }

    //
    // Special case for an empty search scenario : find the business object and take all attribute.
    //
    // Forcing empty search scenario :

    /*private void computeDefaultSearchScreen(final Application application, final Scenario scenario) {
        if (scenario == null) {
            throw new NullPointerException("Scenario is null");
        }
        final Screen searchScreen = new Screen(application, scenario);
        final BusinessObject bo = scenario.getRootBusinesObject();

        if (bo != null) {
            final Collection<BusinessObjectAttribute> attributes = bo.getAttributes();
            for (final BusinessObjectAttribute attribute : attributes) {
                Step step = null;
                try {
                    step = attribute.getArtificialStepForDefaultSearchScreen();
                    onStep(application, step, searchScreen);
                } catch (SpecException e) {
                    error("Error while compiling default search screen " + searchScreen.getName() + " on attribute " + attribute.getName() + ". ", e, true);
                }
            }
        } else {
            if (!scenario.isComposite()) {
                compileError("Scenario " + scenario.getKeyname() + " do not have business object associeted with and this scenario is not only composed of reference scenario steps. " + scenario.toString(), true);
            } else {
                info("Scenario " + scenario.getKeyname() + " bypassed because composed only with reference scenario steps.");
            }
        }
        addScreen(application, searchScreen);
    }*/
    private void generateDependencies(final Application application) {
        emptyLine();
        beginBlock("Generating UI phase III");
        for (final Screen screen : application.getScreens()) {
            generateFieldDependencies(screen);
        }
        endBlock("Generating UI phase III");
        emptyLine();
    }

    /**
     * This is a phase III compilation
     */
    public void generateFieldDependencies(final Screen screen) {
        beginBlock("Search for field dependency in screen " + screen.getKeyForXML());
        for (final ScreenField screenField : screen.getFields()) {
            final Set<Rule> rules = screenField.getRulesForDependencies();
            for (final Rule rule : rules) {
                String ruleCode = rule.getValue();
                if (ruleCode != null && ruleCode.length() > 0) {
                    final RuleDependencyManager cdm = new RuleDependencyManager();
                    if (rule.getKind().equals(RuleKinds.calculated) || rule.getKind().equals(RuleKinds.check)) {
                        if (ruleCode.startsWith("=")) {
                            compileError("Syntax error in calculated rule, it must not start with equal sign");
                            beginCompileHelpBlock("Here is the rule text :");
                            compileHelp(ruleCode);
                            endCompileHelpBlock();
                        }
                        ruleCode = "return " + ruleCode + ";";
                    }
                    DependencyDesc dependencyDesc = null;
                    try {
                        dependencyDesc = cdm.getDependencyDesc(ruleCode, rule.getKeyname());
                    } catch (LexerException e) {
                        compileError("There is an error (LexerException) for the rule " + rule.getKeyname(), e);
                    } catch (IOException e) {
                        compileError("IOError when compiling rule " + rule.getKeyname(), e);
                    } catch (ParserException e) {
                        compileError("There is an error (ParserException) for the rule " + rule.getKeyname(), e);
                    }

                    if (dependencyDesc != null) {

                        screenField.setRule(StringHelper.getRight(dependencyDesc.getRule(), "return "));

                        final Set<ParamDesc> variables = dependencyDesc.getVariables();
                        int zeroVariables = 0;

                        beginBlock("Exploring dependency found for rule " + rule.getKeyname() + " on field " + screenField.getKeyname() + ". Found " + variables.size());
                        for (final ParamDesc dependency : variables) {
                            String dependencyName = dependency.getName();
                            if (dependencyName.contains(".")) {
                                dependencyName = StringHelper.getLeft(dependencyName, ".");
                            }
                            System.out.println("dependencyName = " + dependencyName);
                            final ScreenField screenDep = screen.getScreenFieldByKeyName(dependencyName);
                            if (screenDep != null) {
                                if (screenDep.isConstantRule()) {
                                    info(dependency + " : dependency on " + screen.getKeyForJava() + "." + screenField.getKeyname() + " to " + screen.getKeyForJava() + "." + screenDep.getKeyname() + " is bypassed because it is a constant rule, so that dependency can't change its value.");
                                } else {
                                    screenDep.addDependency(screenField);
                                    zeroVariables++;
                                    info(dependency + " : dependency on " + screen.getKeyForJava() + "." + screenField.getKeyname() + " is added to " + screen.getKeyForJava() + "." + screenDep.getKeyname());
                                }
                            } else {
                                error(dependency + " : a dependency was found in a rule but the field doesn't exist. Can't found '" + dependency + "' in screen " + screen.getName());
                            }
                        }
                        endBlock("Exploring dependency found for rule " + rule.getKeyname() + " on field " + screenField.getKeyname());

                        final Map<String, FunctionDesc> functions = dependencyDesc.getFunctions();
                        int zeroFunctions = 0;

                        beginBlock("Exploring function dependency for rule " + rule.getKeyname() + " on field " + screenField.getKeyname() + ". Found " + functions.size());
                        for (final FunctionDesc dependency : functions.values()) {
                            if (dependency instanceof SumFunctionDesc) {
                                final SumFunctionDesc sum = (SumFunctionDesc) dependency;
                                final IdentifierDesc firstParam = sum.getFirstParam();
                                final String left = StringHelper.getLeft(firstParam.getName(), '.');
                                final String right = StringHelper.getLastRight(firstParam.getName(), '.');
                                final SumDescriptor sumDescriptor = new SumDescriptor(left, right);
                                final ScreenField screenDep = screen.getScreenFieldByKeyName(left);
                                if (screenDep != null) {
                                    if (!screenDep.isBuiltIn() && screenDep.isCollection()) {
                                        screenDep.addSumDescripton(sumDescriptor);
                                        zeroFunctions++;
                                        info(dependency + " : sum() function dependency on " + screen.getKeyForJava() + "." + screenField.getKeyname() + " is added to " + screen.getKeyForJava() + "." + screenDep.getKeyname());
                                    } else {
                                        compileError("Sum() apply only on collection of not built-in step. This is not the case of " + left);
                                        beginCompileHelpBlock("For helpping purpose, here is the characteristic of the step " + left);
                                        compileHelp(screenDep.toString());
                                        endCompileHelpBlock();
                                    }
                                } else {
                                    compileError(dependency + " : a sum() function dependency was found in a rule but the field doesn't exist. Can't found '" + dependency + "' in screen " + screen.getName());
                                    beginCompileHelpBlock("For helping purpose, here are the field of the Screen " + screen.getName());
                                    for (final ScreenField field : screen.getFields()) {
                                        compileHelp("  * " + field.getName());
                                    }
                                    endCompileHelpBlock();
                                }
                            }
                        }
                        endBlock("Exploring function dependency for rule " + rule.getKeyname() + " on field " + screenField.getKeyname());

                        if (zeroVariables == 0 && zeroFunctions == 0) {
                            info("Case of a constant rule " + rule.getKeyname() + " on field " + screenField.getKeyname());
                            rule.setConstantRule();
                        }
                    }
                } else {
                    error("Empty rule (no code) : id= " + rule.getId() + " ,name= " + rule.getKeyname() + " on screen " + screen.getName(), true);
                }
            }
        }
        // //////// Beware, detection of circular dependency !!! if a field is
        // modified and then update a field that in turn update that one.
        endBlock("Search for field dependency in screen " + screen.getKeyForXML());
    }

    private void onStep(final Step step, final Screen screen) {
        final ScreenField screenField = new ScreenField(step);
        screen.addField(screenField);
        screenField.setScreen(screen);
    }
}
