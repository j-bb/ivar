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
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.generator.AbstractGenerator;
import ivar.generictarget.generator.BusinessObjectGenerator;
import ivar.generictarget.generator.UIGenerator;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.helper.filecounter.FileCategories;
import ivar.helper.io.IOHelper;
import ivar.helper.io.IOHelperException;
import ivar.ivarc.IvarcParameters;
import ivar.javatarget.generator.JavaAuthenticationGenerator;
import ivar.javatarget.generator.JavaTargetGenerator;
import ivar.metamodel.ReservedKeywords;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.CRUD;
import ivar.metamodel.spec.Dataset;
import ivar.metamodel.spec.DuplicateRoleException;
import ivar.metamodel.spec.DuplicateRuleException;
import ivar.metamodel.spec.DuplicateScenarioException;
import ivar.metamodel.spec.DuplicateStepException;
import ivar.metamodel.spec.Filter;
import ivar.metamodel.spec.FilterElement;
import ivar.metamodel.spec.FilterOperators;
import ivar.metamodel.spec.Jump;
import ivar.metamodel.spec.Rule;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import ivar.metamodel.spec.StepConstraint;
import ivar.metamodel.spec.StepTypes;
import ivar.metamodel.spec.TemporalFilterElementParameterCompilation;
import ivar.metamodel.spec.TimePeriodUnit;
import ivar.metamodel.target.ConstraintChecker;
import ivar.metamodel.target.generic.*;
import ivar.metamodel.target.generic.ui.Menu;
import ivar.metamodel.target.generic.ui.Screen;
import ivar.metamodel.target.generic.ui.ScreenField;
import ivar.metamodel.target.java.JavaBusinessObject;
import ivar.target.ant.AntGenerator;
import ivar.target.doc.AdminSummaryGenerator;
import ivar.target.eclipse.EclipseGenerator;
import ivar.target.j2ee.JEEGenerator;
import ivar.target.metric.MetricGenerator;
import ivar.target.qx.QooxdooGenerator;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class ApplicationCompiler extends AbstractObject {

    private static final String[] SYSTEM_PROPS = new String[]{"ANT_OPTS=-Xmn128M -Xms256M -Xmx400M"};
    private static final String ANT_ROOT = System.getenv("ANT_HOME");// PropertiesHelper.getInstance().getComposerAntDir();

    public ApplicationCompilerResult compile(final Application application, IvarcParameters ivarcParameters) {
        LoggerDefaultImplementation.getInstance().setApplicationKeyname("[compiler] " + application.getKeyname());
        final boolean debug = ivarcParameters.isDebug();
        final boolean dbdebug = ivarcParameters.isDBDebug();
        final String cartridge = ivarcParameters.getCartridge();
        final ApplicationMetrics applicationMetrics = new ApplicationMetrics(application, ivarcParameters);

        info("compiler, cartridge is " + cartridge);
        info("compiler, debug is " + debug);
        info("compiler, dbdebug is " + dbdebug);

        addApplicationToScenarioLink(application);

        unShadowSteps(application);

        computeScenariosInheritance(application);

        // TODO check method
        // 1. default scenario must not have step. This is possible due to bad inheritance so check is needed.
        // Must be after inheritance since inheritance might add some jump to the child scenario from its parent.
        compileJumps(application);

        typeResolution(application, "Type resolution I");

        reservedKeywordChecking(application);

        scenarioReferenceSubstitution(application);

        //// TODO Passer ce check plus tard pour bien englober tous les synthetic !!
        checkType(application);

        checkConstraints(application);

        computeBusinessObject(application, dbdebug);

        computeBusinessObjectRelation(application);

        checkExclusion(application);

        emptyLine();
        beginBlock("Recheck the exclusion, none should found now ...");
        int exclusionfound = checkExclusion(application);
        endBlock("Recheck the exclusion, none should found now. Found " + exclusionfound);
        if (exclusionfound != 0) {
            compileError("!! Spec violation !! The application will not work as described in the spec.", true);
        }

        // Need BOA, so must be after computeBO
        compileDataSteps(application);

        compileDefaultScenarios(application);

        // Filters are currently stored on Scenarios. => no scenario clone after this step !!!
        compileFilter(application);

//        compileFusionScenarios(application);
        computeServerCall(application);

        computeUI(application);

        disentangleScreens(application);

        disambiguateScreenFieldKeynames(application);

        linkScreenToJumps(application);

        computeMenu(application);

        // Must be after BO and filter
        compileJumpsContext(application);

        computeFetchPaths(application);

        // This will control all the method called only in templates in order to have proper messages
        control(application);

        emptyLine();
        info("-----------------------------------------------");
        info("-----------------------------------------------");
        info("--------- GENERATION PHASE --------------------");
        info("-----------------------------------------------");
        info("-----------------------------------------------");
        emptyLine();

        final String compileRootFolder = ivarcParameters.getCompileRootFolder();

        info("The root Folder is " + compileRootFolder);
        // Force velocity instnciation now: trace more readable and more human logic.
        ivarcParameters.getCompilerVelocityEngine();

        // Register generators
        final Set<AbstractGenerator> generators = CollectionFactory.newSetWithInsertionOrderPreserved();
        generators.add(new JavaTargetGenerator("Java Business Object", "/src", application, ivarcParameters));
        generators.add(new AntGenerator("Ant build file", application, ivarcParameters));
        generators.add(new EclipseGenerator("Eclipse project file", application, ivarcParameters));
        generators.add(new JavaAuthenticationGenerator("Java Authentication files", application, ivarcParameters));
        if ("J2EE".equalsIgnoreCase(cartridge)) {
            generators.add(new JEEGenerator("JEE build file", application, ivarcParameters));
        }
        if ("java".equalsIgnoreCase(cartridge)) {
            generators.add(new QooxdooGenerator("Qooxdoo files", application, ivarcParameters));
        }
        generators.add(new AdminSummaryGenerator("Doc for targapp sysadmin", application, ivarcParameters));
        generators.add(new MetricGenerator("Metric", application, ivarcParameters));

        // Generate
        for (final AbstractGenerator generator : generators) {
            emptyLine();
            beginBlock("Producing " + generator.getName() + " ...");
            generator.generate();
            endBlock("Producing " + generator.getName() + ". Ok.");
            emptyLine();
        }

//        if (application.hasInjectedFiles()) {
//            exportFusionTemplateFiles(application, compileRootFolder + "/files/technical/templates");
//        }
        generateCompilationReport(application, "engine-power", applicationMetrics, true, true);

        // and now launch ant and compile the generated Java files !
        launchAnt(compileRootFolder, ivarcParameters);

        try {
            application.free();
        } catch (Throwable t) {
            error("[Post Compile] Error while freeing the application " + application.getKeyname() + ". Houston, this is quite annoying, but the GC will finish the job, no need to cancel the mission.", t, true);
        }

        return new ApplicationCompilerResult(applicationMetrics);
    }

    private void checkConstraints(final Application application) {
        emptyLine();
        beginBlock("Checking constraints validity for each step in the application...");
        final ConstraintChecker checker = new ConstraintChecker(StepConstraint.class);
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                checker.validate(step);
                if (step.hasConstraint("hash") && step.isCollection()) {
                    compileError("A collection can't be hashed : " + scenario.getKeyname() + "." + step.getKeyname());
                }
            }
        }
        endBlock("Checking constraints validity for each step in the application... Done.");
        emptyLine();
    }

//    private void exportFusionTemplateFiles(final Application application, final String rootFolder) {
//        beginBlock("Exporting fusion template files to: " + rootFolder);
//        try {
//            IOHelper.mkdirs(rootFolder);
//        } catch (IOHelperException e) {
//            error("Access denied while trying to create templates directory " + rootFolder, e);
//        }
//        for (final Scenario scenario : application.getScenarios()) {
//            if (!scenario.hasFusionTemplate()) {
//                continue;
//            }
//
//            final ApplicationFileIndex template = scenario.getCompileTimeTemplateFileIndex();
//            final File actualFile = template.getFile();
//            if (actualFile != null) {
//                try {
//                    final File outputFile = new File(rootFolder, scenario.getKeyname() + template.getExtension());
//                    IOHelper.mkdirs(outputFile.getParent());
//                    IOHelper.writeFileToLocation(actualFile, outputFile.getAbsolutePath());
//                    ApplicationFileIndex.createDebugFile(template, rootFolder, scenario.getKeyname());
//                } catch (IOHelperException e) {
//                    error("An I/O error occurred while trying to export fusion template " + template.getFileName(), e, true);
//                } catch (SecurityException e) {
//                    error("Access denied while trying to export fusion template " + template.getFileName(), e);
//                }
//            }
//        }
//        endBlock("Exporting fusion template files to: " + rootFolder + " ... Done.");
//    }
//    private void compileFusionScenarios(final Application application) {
//        emptyLine();
//        beginBlock("Compiling fusion scenarios...");
//
//        int errors = 0;
//        int num = 0;
//
//        beginBlock("Checking context consistency in templates...");
//        for (final Scenario scenario : application.getScenarios()) {
//            if (!scenario.hasFusionTemplate()) {
//                continue;
//            }
//
//            beginBlock("Checking scenario " + scenario.getKeyname());
//            final ApplicationFileIndex template = scenario.getCompileTimeTemplateFileIndex();
//            num++;
//            if (template == null) {
//                final String message = "Unable to find fusion template file for scenario " + scenario.getKeyname();
//                error(message);
//                compileError(message);
//                errors++;
//                continue;
//            }
//            info("Fusion template File " + template.getFileName() + " is used in " + scenario.getKeyname());
//            if (template.getFile() == null) {
//                error("Unable to find fusion template file : " + template.getFileName() + ". File was supposed to be in " + template.getTmpDir() + ". File name was supposed to be " + template.getId() + ". File extension was supposed to be " + template.getExtension());
//                compileError("File not found, see previous error. " + template.getFileName());
//                errors++;
//                continue;
//            }
//
//            if (!new TemplatePreParser(template.getFile()).checkAgainstContext(scenario)) {
//                errors++;
//            }
//            endBlock("Checking scenario " + scenario.getKeyname() + ". Done.");
//            emptyLine();
//        }
//        endBlock("Checking context consistency in templates... Done.");
//        endBlock("Compiling fusion scenarios... Done. Found " + num + " fusion scenarios, with " + errors + " errors.");
//        emptyLine();
//    }
    private void disentangleScreens(final Application application) {
        beginBlock("Disentangling screen references in the application.");
        for (final Screen screen : application.getScreens()) {
            screen.disentangleScreen();
        }
        endBlock("Disentangling screen references in the application. Done.");
    }

    private void disambiguateScreenFieldKeynames(final Application application) {
        beginBlock("Disambiguating keynames for screenfields.");
        for (final Screen screen : application.getScreens()) {
            screen.disambiguateKeynames(null);
        }
        endBlock("Disambiguating keynames for screenfields. Done.");
    }

    private void addApplicationToScenarioLink(final Application application) {
        for (final Scenario scenario : application.getScenarios()) {
            scenario.setApplication(application);
        }
    }

    private void unShadowSteps(final Application application) {
        beginBlock("Find shadow steps in application " + application.getKeyname() + " and replace them with real steps.");
        for (final Scenario scenario : application.getScenarios()) {
            beginBlock("Look for shadow steps in scenario " + scenario.getKeyname());
            final Set<Step> toRemove = CollectionFactory.newSetWithInsertionOrderPreserved();
            final Set<Step> toAdd = CollectionFactory.newSetWithInsertionOrderPreserved();
            for (Step step : scenario.getSteps()) {
                if (step.getMin() == null && step.getMax() == null && step.getStepConstraint().getType() == null) {
                    info("Houston, step " + step.getKeyname() + " seems to be a shadow step. We'll handle that.");
                    final Step unshadowed = unShadowStep(application, step);
                    toRemove.add(step);
                    toAdd.add(unshadowed);
                    debug("Let's see what our new step looks like...");
                    debug(unshadowed.toString());
                }
            }
            scenario.getSteps().removeAll(toRemove);
            scenario.getSteps().addAll(toAdd);
            endBlock("Look for shadow steps in scenario " + scenario.getKeyname() + ". Done.");
        }
        endBlock("Find shadow steps in application " + application.getKeyname() + " and replace them with real steps. Done.");
    }

    private Step unShadowStep(final Application application, final Step step) {
        Step result = null;
        Step nonShadowStep = null;

        info("Trying to find corresponding non-shadow step for step " + step.getKeyname());
        final Scenario parent = step.findMyScenario(application);
        final String data = parent.getData();
        for (final Scenario scenario : application.getScenarios()) {
            if (data.equals(scenario.getData())) {
                for (final Step scenarioStep : scenario.getSteps()) {
                    if (step.getKeyname().equals(scenarioStep.getKeyname())) {
                        if (scenarioStep.getMin() != null || scenarioStep.getMax() != null || scenarioStep.getStepConstraint().getType() != null) {
                            info("Found non-shadow step in scenario " + scenario.getKeyname() + " for step " + step.getKeyname());
                            nonShadowStep = scenarioStep;
                            break;
                        }
                    }
                }
                if (nonShadowStep != null) {
                    break;
                }
            }
        }
        if (nonShadowStep == null) {
            compileError("Step " + step.getKeyname() + " (id= " + step.getId() + ") is a shadow step but the real step doesn't exist");
            beginCompileHelpBlock("For helping purpose, here is a dump of all Steps that has a keyname = " + step.getKeyname());
            compileHelp("Make sure there is no shadow without real Step.");
            compileHelp("Important note : the real Step and all its shadow *must* share the same data from their scenario");
            for (final Scenario scenario : application.getScenarios()) {
                for (final Step scenarioStep : scenario.getSteps()) {
                    if (scenarioStep.getKeyname().equals(step.getKeyname())) {
                        compileHelp("\t* Data = " + scenario.getData() + " Step keyname = " + scenarioStep.getKeyname() + " (" + scenarioStep.getId() + ")");
                    }
                }
            }
            endCompileHelpBlock();
            result = step;
        } else {
            result = nonShadowStep.getJPAClone();
            result.getJumps().clear();
            result.getJumps().addAll(step.getJumps());
            result.getRoles().clear();
            result.getRoles().addAll(step.getRoles());
        }

        return result;
    }

    /**
     * Insert shadow steps in default scenarios recursively until a certain depth is reached.
     *
     * @param application
     */
    private void compileDefaultScenarios(final Application application) {
        emptyLine();
        beginBlock("Compiling default scenarios...");
        final Set<Scenario> scenarios = CollectionFactory.newSet(application.getScenarios());
        int defaultScenarioCompiled = 0;
        int defaultScenarioCompiledError = 0;
        for (final Scenario scenario : scenarios) {
            if (scenario.getCrud().isDefault()) {
                if (scenario.getSteps() != null && scenario.getSteps().size() > 0) {
                    warning("Found a default scenario with step, Houston, this is really bad, please check that (maybe a bad inheritance ?). Scenario is " + scenario.getKeyname());
                    defaultScenarioCompiledError++;
                } else {
                    scenario.populateDefaultSteps(1);
                    defaultScenarioCompiled++;
                }
            }
        }

        beginBlock("Ok Houston, now we will do another pass of steps unshadowing to handle those we just created. Yes, we're a little schizophrenic.");
        unShadowSteps(application);
        endBlock();

        endBlock("Compiling default scenarios. Found " + defaultScenarioCompiled + " OK and " + defaultScenarioCompiledError + " with error. Done.");
        emptyLine();
    }

    /**
     * Modify steps where step.getType().isData() == true and change their type to a default scenario reference which will be populated afterwards
     *
     * @param application
     */
    private void compileDataSteps(final Application application) {
        emptyLine();
        beginBlock("Compiling steps where type is data...");
        final Set<Scenario> scenarios = CollectionFactory.newSet(application.getScenarios());
        for (final Scenario scenario : scenarios) {
            for (final Step step : scenario.getSteps()) {
                if (!step.getStepConstraint().isData()) {
                    continue;
                }

                info("Found step " + step.getKeyname() + " in scenario " + scenario.getKeyname() + " with type " + step.getStepConstraint().getType());
                final BusinessObject bo = scenario.getRootBusinesObject();
                final BusinessObjectAttribute boa = bo.getBusinessAttributeByName(step.getKeyname());

                final BusinessObject bObject = boa.getType().getBusinessObject();

                final String newScenarioKeyname = scenario.getKeyname() + "_" + step.getKeyname() + "_dread"; //bObject.getName() + "_dread";

                info("Ok Houston, creating a new scenario.");
                final Scenario newScenario = new Scenario();
                newScenario.setKeyname(newScenarioKeyname);
                newScenario.setCrud(CRUD.dread);
                newScenario.setApplication(application);
                newScenario.setRootBusinesObject(bObject);
                newScenario.setName("Synthetic default read" + bObject.getName());
                newScenario.setData(bObject.getName());
                // JBB scenario.step ?
                newScenario.setSynthetic("Doesn't come from an existing scenario. This one is for the default read of " + bObject.getFullName() + " for scenario " + scenario.getKeyname());
                try {
                    application.addScenario(newScenario);
                    info("New synthetic scenario " + newScenario.getKeyname() + " has been added to the application");
                } catch (DuplicateScenarioException e) {
                    error("Fail to add default scenario " + newScenarioKeyname + " for the compile of data step " + step.getKeyname(), e);
                    continue;
                }
                step.setType(newScenarioKeyname);
                step.getStepConstraint().compute(application);
                step.getStepConstraint().check("compileDataSteps()");
                info("Check : is step " + step.getKeyname() + " a scenario reference? " + step.getStepConstraint().isScenarioReference());
            }
        }
        endBlock("Compiling steps where type is data... done");
        emptyLine();
    }

    private int checkExclusion(final Application application) {

        beginBlock("Checking exclusion ...");
        int i = 0;
        final Set<String> alreadyThereScenario = CollectionFactory.newSetWithInsertionOrderPreserved();
        for (final Scenario scenario : application.getScenarios()) {
            alreadyThereScenario.clear();
            if (scenario.getCrud().isCreate() || scenario.getCrud().isUpdate()) {
                for (final Scenario sameScenario : application.getScenarios()) {
                    if (sameScenario.getData() != null && sameScenario.getData().equals(scenario.getData())) {
                        for (final Step step : sameScenario.getSteps()) {
                            if (step.isMandatory()) {
                                final String stepUniqueString = sameScenario.getKeyname() + "." + step.getKeyname();
                                if (!alreadyThereScenario.contains(stepUniqueString)) {
                                    alreadyThereScenario.add(stepUniqueString);
                                    compileError("Spec violation : mandatory excluded step in create or update", true);
                                    beginCompileHelpBlock("For helping purpose, here are the info :");
                                    if (scenario.isSynthetic()) {
                                        compileHelp("The faulty scenario is due to the following jump(s) :");
                                        for (final Scenario sc : application.getScenarios()) {
                                            for (final Jump jump : sc.getJumps()) {
                                                if (jump.getTargetScenario().getKeyname().equals(scenario.getKeyname())) {
                                                    compileHelp("   * " + jump);
                                                }
                                            }
                                        }
                                        compileHelp("");
                                    }

                                    compileHelp("Scenario [" + scenario.getKeyname() + "] " + scenario.getName() + " want to do a " + scenario.getCrud() + " on " + scenario.getData());
                                    compileHelp("Unfortunately, on the scenario [" + sameScenario.getKeyname() + "] " + sameScenario.getName() + " define the following step :");
                                    compileHelp(step.getStepConstraint().getType() + " " + step.getMin() + ".." + step.getMax() + " " + step.getKeyname() + " : a mandatory excluded step.");
                                    compileHelp("=> as a result the column will be not nullable but the field will not either be provided because it is excluded.");
                                    compileHelp(" Correct the spec and put the step not mandatory or remove that " + scenario.getCrud() + " scenarios that works on " + scenario.getData());
                                    compileHelp("");
                                    compileHelp("I'll switch the step to not mandatory for you on that compile because I'm a cool compiler, but you'll have to do in the spec.");
                                    step.setMinInt(0);
                                    endCompileHelpBlock();
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
        }
        endBlock("Checking exclusion. Done. Found " + i + " problems.");
        return i;
    }

    private void control(final Application application) {
        emptyLine();
        beginBlock("----------- General control -------------");
        beginBlock("----------- Scenarios and Steps");
        for (final Scenario scenario : application.getScenarios()) {
            jumpDataControl(scenario.getJumps());
            for (final Step step : scenario.getSteps()) {
                jumpDataControl(step.getJumps());
            }
        }
        endBlock("----------- Scenarios and Steps");

        beginBlock("----------- Screen");
        for (final Screen screen : application.getScreens()) {
            if (screen.getKeyname() == null) {
                compileError("Keyname is null");
            }

            if (screen.hasJumps()) {
                info("Screen " + screen.getKeyname() + " has " + screen.getJumps().size() + " jumps");
                jumpDataControl(screen.getJumps());
            } else {
                info("Screen " + screen.getKeyname() + " has no jumps");
            }
            for (final ScreenField screenField : screen.getFields()) {
                screenField.isScreenReference();
            }
        }
        endBlock("----------- Screen");

        endBlock("----------- General control -------------");
        emptyLine();
    }

    private void jumpDataControl(Set<Jump> jumps) {
        for (final Jump jump : jumps) {
            beginBlock("Jump control '" + jump + "'");
            if (!jump.isCompiled()) {
                compileError("Jump not compiled !!!", true);
            }
            if (jump.getScreen() == null) {
                compileError("Screen is null");
            }
            if (jump.getTargetScenario() == null) {
                compileError("TargetScenario is null");
            }
            if (jump != null && jump.getTargetScenario() != null && jump.getScreen() != null) {
                info("'" + jump + "' CRUD= " + jump.getCrud() + ", targetScenario= " + jump.getTargetScenario().getKeyname() + ", Screen= " + jump.getScreen().getKeyname() + " (screen QxClassName= " + jump.getScreen().getQxClassName() + "), isCreate= " + jump.isCreate() + ", isDelete= " + jump.isDelete() + ", isRead= " + jump.isRead() + ", isUpdate" + jump.isUpdate());
            }
            endBlock("Jump control");
            //jumpData.getScreen().getQxClassName()
        }
    }

    private Object genericCallForControl(final Object instance, final String methodName, final boolean notNull, final Object... args) {
        Object returnObject = null;
        final Method[] methods = instance.getClass().getMethods();
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                try {
                    returnObject = method.invoke(instance, args);
                    if (notNull && returnObject == null) {
                        compileError(methodName + " returned null value");
                    }
                } catch (IllegalAccessException e) {
                    error(e, true);
                } catch (InvocationTargetException e) {
                    error(e, true);
                }
                break;
            }
        }
        return returnObject;
    }

    private void launchAnt(final String rootFolder, final IvarcParameters ivarcParameters) {
        emptyLine();
        beginBlock("Packaging the application");

        final boolean debug = ivarcParameters.isDebug();
        final boolean onlySQL = ivarcParameters.isOnlySQL();
        final boolean deploy = ivarcParameters.isDeploy();

        if (onlySQL) {
            new ProcessLauncher().run(ANT_ROOT + "/bin/ant -f " + rootFolder + "/build.xml only-sql", rootFolder, SYSTEM_PROPS);
        } else {
            if (debug) {
                new ProcessLauncher().run(ANT_ROOT + "/bin/ant -diagnostics", rootFolder, SYSTEM_PROPS);
                new ProcessLauncher().run(ANT_ROOT + "/bin/ant -f " + rootFolder + "/build.xml all-source", rootFolder, SYSTEM_PROPS);
            } else {
                new ProcessLauncher().run(ANT_ROOT + "/bin/ant -f " + rootFolder + "/build.xml all", rootFolder, SYSTEM_PROPS);
            }

            if (deploy) {
                beginBlock("Deploying application...");
                new ProcessLauncher().run(ANT_ROOT + "/bin/ant -f " + rootFolder + "/build.xml all-deploy" + (debug ? "-source" : ""), rootFolder, SYSTEM_PROPS);
                endBlock("Deploying application... Done.");
            }
        }

        endBlock("Packaging the application : done.");
        emptyLine();
    }

    public Scenario findScenarioFromStep(final Application application, final Step stepToReach) {
        Scenario result = null;
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                if (step == stepToReach) {
                    result = scenario;
                    break;
                }
            }
        }
        return result;
    }

    private void calculatingDBColumnName(final Application application, final boolean DBDebug) {
        emptyLine();
        beginBlock("Calculating DB column name");
        for (final BusinessObject bo : application.getBusinessObjects()) {
            if (DBDebug) {
                bo.setTableName(StringHelper.getFirstCapitalized(bo.getName()));
            }
            for (final BusinessObjectAttribute boa : bo.getAttributes()) {
                boolean techName = false;
                for (final Step step : boa.getSteps()) {
                    if (step.getTechkeyname() != null && step.getKeyname().length() > 0) {
                        if (!techName) {
                            boa.setColumnName(step.getTechkeyname());
                            techName = true;
                        } else {
                            if (!step.getTechkeyname().equals(boa.getColumnName())) {
                                compileError("TechKeyname must be consistent across step that represent the same attribute or column");
                                beginCompileHelpBlock("For helping purpose, here are all the step concerning attribute " + bo.getFullName() + "." + boa.getName());
                                for (final Step debugStep : boa.getSteps()) {
                                    compileHelp("step id: " + step.getId() + " " + debugStep.getName() + " [" + debugStep.getKeyname() + "] techkeyname=" + debugStep.getTechkeyname());
                                }
                                endCompileHelpBlock();
                            }
                        }
                    }
                }
                if (!techName || DBDebug) {
                    boa.setColumnName(boa.getKeyname());
                }
            }
        }

        beginBlock("Checking for the same TECHKEYNAME on different attribute ...");
        Map<String, Step> techKeynames = CollectionFactory.newMap();
        List<Step> steps = CollectionFactory.newList();
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                final String techKeyname = step.getTechkeyname();
                if (techKeyname != null) {
                    if (techKeynames.containsKey(techKeyname)) {
                        final Step associatedStep = techKeynames.get(techKeyname);
                        final Scenario associatedScenario = findScenarioFromStep(application, associatedStep);
                        if (!step.getKeyname().equals(associatedStep.getKeyname()) && scenario.getData().equals(associatedScenario.getData()) && !steps.contains(step) && !steps.contains(associatedStep)) {
                            compileError("TechKeyname unicity broken ! " + techKeyname + " is used on step " + scenario.getKeyname() + "." + step.getKeyname() + " and " + (associatedScenario != null ? associatedScenario.getKeyname() : "?") + "." + associatedStep.getKeyname());
                            beginCompileHelpBlock("For helping purpose, here are all the step using this techKeyname " + techKeyname);
                            for (final Scenario scenarioHelp : application.getScenarios()) {
                                for (final Step stepHelp : scenario.getSteps()) {
                                    final String techKeynameHelp = stepHelp.getTechkeyname();
                                    if (techKeynameHelp != null && techKeynameHelp.equals(techKeyname)) {
                                        compileHelp("   " + scenarioHelp.getKeyname() + "." + stepHelp.getKeyname());
                                    }
                                }
                            }
                            endCompileHelpBlock();
                            steps.add(step);
                            steps.add(associatedStep);
                        }
                    } else {
                        techKeynames.put(techKeyname, step);
                    }
                }
            }
        }
        techKeynames = null;
        steps = null;

        endBlock("Checking for the same TECHKEYNAME on different attribute ...");
        endBlock("Calculating DB column name");
        emptyLine();
    }

    private void scenarioReferenceSubstitution(Application application) {
        emptyLine();
        beginBlock("Scenario reference substitution ...");
        boolean newScenarioCreated = false;
        final Set<Scenario> scenarios = CollectionFactory.newSet(application.getScenarios());
        for (final Scenario scenario : scenarios) {
            if (!scenario.isComposite()) {
                for (final Step originalStep : scenario.getSteps()) {
                    if (originalStep.getStepConstraint().isScenarioReference()) {
                        final Scenario referencedScenario = originalStep.getStepConstraint().getReferencedscenario();
                        info("Scenario referenced found [scenario.step]= " + scenario.getKeyname() + "." + originalStep.getKeyname() + " reference scenario " + referencedScenario.getKeyname());
                        final String referencedScenarioKey = referencedScenario.getKeyname() + "_" + CRUD.dread;
                        info("Seeking for synthetic scenario " + referencedScenarioKey + " in application scenarios");
                        /*
                        THEORY
                        This might be an issue to create a synthetic scenario all times in that situation. Why not search in the classical one, just in case it exists
                        instead os searching only using a synthetic key and create a new one each time.
                        See what has been done for jump.
                        Also, this scenario will give birth to Screen and ScreenField, witch is usefull but will the Qooxdoo screen class be used ? Not sure ...
                         */
                        Scenario scenarioClone = null;
                        for (final Scenario existingScenario : application.getScenarios()) {
                            if (existingScenario.getKeyname().equals(referencedScenarioKey)) {
                                scenarioClone = existingScenario;
                                break;
                            }
                        }

                        if (scenarioClone == null) {
                            info("Scenario not found in existing scenarios, need to create a new one");
                            scenarioClone = getNewSyntheticScenario(referencedScenario, CRUD.dread);

                            //TODO Begin : could be factorized
                            debug("Adding the new synthetic scenario " + scenarioClone.getKeyname() + " ...");
                            try {
                                application.addScenario(scenarioClone);
                                newScenarioCreated = true;
                            } catch (DuplicateScenarioException e) {
                                error("Error during new scenario addition for jump compilation " + scenarioClone.getKeyname(), e);
                            }
                            debug("Adding the new synthetic scenario. Done.");
                            // End : could be factorized

                            info("New synthetic scenario created as a replacement : " + scenarioClone.getKeyname());
                        } else {
                            info("Synthetic scenario already exists : " + scenarioClone.getKeyname());
                        }

                        originalStep.setType(scenarioClone.getKeyname());
                        originalStep.getStepConstraint().compute(application);
                        info("New step constraint created " + originalStep.getStepConstraint().toString());
                        emptyLine();
                    }
                } // end for steps
            } else {
                info("Scenario " + scenario.getKeyname() + " is a pure composite scenario and thus, excluded from scenario reference analysis.");
            }
        }
        endBlock("Scenario reference substitution. Done.");
        emptyLine();

        if (newScenarioCreated) {
            typeResolution(application, "Type resolution re-launched because new Scenarios had been created in Scenario reference substitution phase");
        } else {
            emptyLine();
            info("No need for a type resolution II : no new scenarios created in Scenario reference substitution previous phase");
        }
    }

    private void checkType(Application application) {
        emptyLine();
        beginBlock("Type checking ...");
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                checkType(scenario, step, false);
            }
        }
        endBlock("Type checking. Done");
        emptyLine();
    }

    private void checkType(final Scenario scenario, final Step step, final boolean substep) {
        if (step != null) {
            step.getStepConstraint().check(scenario.getKeyname() + "." + step.getKeyname() + ", substep=" + substep);
            if (step.getEmbededSteps() != null) {
                for (final Step subStep : step.getEmbededSteps()) {
                    checkType(scenario, subStep, true);
                }
            }
        }
    }

    private void computeServerCall(final Application application) {
        emptyLine();
        beginBlock("Compute server calls ...");

        final Map<String, ServerCall> serverCalls = CollectionFactory.newMap();
        int serverCallNumber = 0;

        for (final Scenario scenario : application.getScenarios()) {
            beginBlock("Server calls for scenario " + scenario.keyname + "...");
            final BusinessObject businessObject = scenario.getRootBusinesObject();
            if (scenario.getCrud().isUpdate() || scenario.getCrud().isRead()) {
                final ServerCall sc = new ServerCall(businessObject, scenario, FetchPathType.PREPARATION);
                info("Trying to add server call " + sc.getKey());
                if (!serverCalls.containsKey(sc.getKey())) {
                    businessObject.addServerCall(sc);
                    serverCalls.put(sc.getKey(), sc);
                    serverCallNumber++;
                    info("Trying to add server call " + sc.getKey() + ": success");
                } else {
                    info("Server call " + sc.getKey() + " already exists, it has not been added again");
                }

//                if (scenario.hasFusionTemplate() && scenario.getCrud().isRead()) {
//                    sc.setFusionTemplate(scenario.getFusionTemplate());
//                    sc.setTemplateName(scenario.getKeyname() + scenario.getCompileTimeTemplateFileIndex().getExtension());
//                }
            }
            if (scenario.getCrud().isDelete() || scenario.getCrud().isUpdate() || scenario.getCrud().isCreate()) {
                final ServerCall sc = new ServerCall(businessObject, scenario, FetchPathType.EXECUTION);
                info("Trying to add server call " + sc.getKey());
                if (!serverCalls.containsKey(sc.getKey())) {
                    businessObject.addServerCall(sc);
                    serverCalls.put(sc.getKey(), sc);
                    serverCallNumber++;
                    info("Trying to add server call " + sc.getKey() + ": success");
                } else {
                    info("Server call " + sc.getKey() + " already exists, it has not been added again");
                }
            }
            beginBlock("Exploring steps for scenario " + scenario.getKeyname() + "...");
            for (final Step step : scenario.getSteps()) {
                beginBlock("Evaluating jumps for step " + step.getKeyname());
                for (final Jump jump : step.getJumps()) {
                    info("Found jump " + jump.getName() + " in step " + step.getKeyname() + " with target scenario " + jump.getTargetScenario().getKeyname());
                    final Scenario targetScenario = jump.getTargetScenario();
                    final BusinessObject tBusinessObject = targetScenario.getRootBusinesObject();
                    if (jump.getCrudOrTargetCrud().isUpdate() || jump.getCrudOrTargetCrud().isStrictlyRead()) {
                        final ServerCall sc = new ServerCall(tBusinessObject, targetScenario, FetchPathType.PREPARATION);
                        info("Trying to add server call " + sc.getKey());
                        if (!serverCalls.containsKey(sc.getKey())) {
                            tBusinessObject.addServerCall(sc);
                            serverCalls.put(sc.getKey(), sc);
                            serverCallNumber++;
                            info("Trying to add server call " + sc.getKey() + ": success");
                        } else {
                            info("Server call " + sc.getKey() + " already exists, it has not been added again");
                        }
                    }
                    if (jump.getCrudOrTargetCrud().isDelete() || jump.getCrudOrTargetCrud().isUpdate() || jump.getCrudOrTargetCrud().isCreate()) {
                        final ServerCall sc = new ServerCall(tBusinessObject, targetScenario, FetchPathType.EXECUTION);
                        info("Trying to add server call " + sc.getKey());
                        if (!serverCalls.containsKey(sc.getKey())) {
                            tBusinessObject.addServerCall(sc);
                            serverCalls.put(sc.getKey(), sc);
                            serverCallNumber++;
                            info("Trying to add server call " + sc.getKey() + ": success");
                        } else {
                            info("Server call " + sc.getKey() + " already exists, it has not been added again");
                        }
                    }
                }
                endBlock("Evaluating jumps for step " + step.getKeyname() + ". Done.");
            }
            endBlock("Exploring steps jumps for scenario " + scenario.getKeyname() + "... Done.");
            endBlock("Server calls for scenario " + scenario.keyname + "... Done.");
        }
        endBlock("Compute server calls. Found " + serverCallNumber + " server calls.");

        beginBlock("Compute server calls, part 2: link with java business objects and manage filtered calls.");
        for (JavaBusinessObject jbo : application.getJavaBusinessObjects()) {
            jbo.computeTargetServerCalls();
        }
        endBlock("Compute server calls, part 2: link with java business objects and manage filtered calls. Done.");
        emptyLine();
    }

    private void linkScreenToJumps(final Application application) {
        emptyLine();
        beginBlock("Compiling jumps phase II : link screen ...");
        long jumpNumber = 0;
        for (final Scenario scenario : application.getScenarios()) {
            for (final Jump jump : scenario.getJumps()) {
                jumpNumber++;
                beginBlock("Linking scenario jump " + scenario.getKeyname() + " # jump " + jump);
                linkScreenJump(application, jump);
                endBlock();
            }
            for (final Step step : scenario.getSteps()) {
                for (final Jump jump : step.getJumps()) {
                    jumpNumber++;
                    beginBlock("Linking step jump " + scenario.getKeyname() + "." + step.getKeyname() + " # jump " + jump);
                    linkScreenJump(application, jump);
                    endBlock();
                }
            }
        }
        endBlock("Compiling jumps phase II : link screen. Done " + jumpNumber + " jumps");
        emptyLine();
    }

    private void linkScreenJump(Application application, Jump jump) {
        if (jump.hasTargetScenario()) {
            final Scenario targetScenario = jump.getTargetScenario();
            Screen screen = null;
            for (final Screen screenPointer : application.getScreens()) {
                if (screenPointer.getScenario().getKeyname().equals(targetScenario.getKeyname())) {
                    screen = screenPointer;
                    break;
                }
            }
            if (screen != null) {
                jump.setScreen(screen);
                info("Linked ! Found screen " + screen.getTitle());
            } else {
                compileError("Unable to find the corresponding screen for scenario " + targetScenario.getKeyname());
                beginCompileHelpBlock("For helping purpose, here are the screens we looked for");
                for (final Screen screenPointer : application.getScreens()) {
                    compileHelp("    screen : " + screenPointer.getTitle() + " -scenario-> " + screenPointer.getScenario().getKeyname());
                }
                endCompileHelpBlock();
            }
        } else {
            compileError("jump has no target scenario, check phase I.");
        }
    }

    private boolean isThereAScenarioWhereJumpWouldNotBeenCompiled(final Application application) {
        final Set<Scenario> allScenariosCollection = CollectionFactory.newSet(application.getScenarios());
        boolean result = false;
        for (final Scenario scenario : allScenariosCollection) {
            for (final Jump jump : scenario.getJumps()) {
                if (!jump.isCompiled()) {
                    result = true;
                    break;
                }
            }
            for (final Step step : scenario.getSteps()) {
                for (final Jump jump : step.getJumps()) {
                    if (!jump.isCompiled()) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void compileJumps(final Application application) {
        emptyLine();
        beginBlock("Compiling jumps ...");
        long scenarioJumpNumber = 0;
        long stepJumpNumber = 0;
        int loop = 1;
        do {
            emptyLine();
            beginBlock("There are uncompiled jump in some scenarios. >> Loop " + loop + " <<");
            final Set<Scenario> allScenariosCollection = CollectionFactory.newSet(application.getScenarios());
            for (final Scenario scenario : allScenariosCollection) {
                for (final Jump jump : scenario.getJumps()) {
                    if (!jump.isCompiled()) {
                        scenarioJumpNumber++;
                        emptyLine();
                        beginBlock("Compiling scenario jump " + scenario.getKeyname() + " # jump " + jump);
                        final Scenario targetScenario = getOrCreateTargetScenario(application, jump, "Scenario jump " + scenario.getKeyname() + " # jump " + jump);
                        if (targetScenario != null) {
                            jump.setSourceScenario(scenario);
                            jump.setTargetScenario(targetScenario);
                            info("Jump compiled : from scenario " + scenario.getKeyname() + " jump to " + (jump.getTargetScenario() != null ? jump.getTargetScenario().getKeyname() : "!! NOT FOUND (" + jump.getTargetKeyname() + ")!!"));
                            jump.setCompiled();
                        }
                        endBlock();
                    }
                }
                for (final Step step : scenario.getSteps()) {
                    final Set<Jump> smallSelectors = CollectionFactory.newSet();
                    for (final Jump jump : step.getJumps()) {
                        if (!jump.isCompiled()) {
                            stepJumpNumber++;
                            emptyLine();
                            beginBlock("Compiling step jump " + scenario.getKeyname() + "." + step.getKeyname() + " # jump " + jump);
                            final Scenario targetScenario = getOrCreateTargetScenario(application, jump, "Step jump " + scenario.getKeyname() + "." + step.getKeyname() + " # jump " + jump);
                            if (targetScenario != null) {
                                if (scenario.getCrud().isRead() && (targetScenario.getCrud().isCreate() || targetScenario.getCrud().isDelete())) {
                                    compileError("A step from a read or search scenario can not have jumps on create or delete scenarios");
                                    beginCompileHelpBlock("For helping purpose, here is a little detail of the jump that causes the problem...");
                                    compileHelp("Source scenario is: " + scenario.getKeyname() + " and has CRUD " + scenario.getCrud());
                                    compileHelp("Target scenario is: " + targetScenario.getKeyname() + " and has CRUD " + targetScenario.getCrud());
                                    endCompileHelpBlock();
                                } else {
                                    jump.setSourceScenario(scenario);
                                    jump.setTargetScenario(targetScenario);
                                    info("Jump compiled : from step " + scenario.getKeyname() + "." + step.getKeyname() + " jump to " + (jump.getTargetScenario() != null ? jump.getTargetScenario().getKeyname() : "!! NOT FOUND (" + jump.getTargetKeyname() + ")!!"));
                                    jump.setCompiled();
                                }
                            }
                            if (jump.isSmallSelector()) {
                                smallSelectors.add(jump);
                            }
                            endBlock();
                        }
                    }
                    if (smallSelectors.size() > 1) {
                        compileError("Too many small selectors on step " + step.getKeyname());
                        compileHelp("A step can have a maximum of one selector jump (CRUD search or default search) of size SMALL or XSMALL");
                        beginCompileHelpBlock("For helping purpose, here are the small selector jumps we detected on step " + step.getKeyname());
                        for (final Jump smallSelect : smallSelectors) {
                            compileHelp(smallSelect.toString());
                        }
                        endCompileHelpBlock();
                    }
                }
            }

            loop++;
        } while (isThereAScenarioWhereJumpWouldNotBeenCompiled(application));
        loop--;
        for (; loop > 0; loop--) {
            endBlock(" >> Loop " + loop + " <<");
        }
        emptyLine();

        endBlock("Compiled " + scenarioJumpNumber + " scenario-jumps and " + stepJumpNumber + " step-jumps.");
        emptyLine();
    }

    private void compileJumpsContext(final Application application) {
        emptyLine();
        beginBlock("Compiling jumps phase II : the context ...");
        long scenarioJumpNumber = 0;
        long stepJumpNumber = 0;
        final Set<Scenario> allScenariosCollection = CollectionFactory.newSet(application.getScenarios());
        for (final Scenario scenario : allScenariosCollection) {
            for (final Jump jump : scenario.getJumps()) {
                scenarioJumpNumber++;
                info("Compiling scenario [" + scenario.getKeyname() + "] JumpData context #jump " + jump + " ...");
                jump.computeJumpContext();
                info("   - Compiling scenario [" + scenario.getKeyname() + "] JumpData context #jump " + jump + ". Done. Context= " + jump.getContextAsString());
            }
            for (final Step step : scenario.getSteps()) {
                for (final Jump jump : step.getJumps()) {

                    stepJumpNumber++;
                    info("Compiling step [" + scenario.getKeyname() + "." + step.getKeyname() + "] JumpData context #jump " + jump + " ...");
                    jump.computeJumpContext();
                    info("   - Compiling step [" + scenario.getKeyname() + "." + step.getKeyname() + "] JumpData context #jump " + jump + ". Done. Context= " + jump.getContextAsString());
                }
            }
        }
        endBlock("Compiled " + scenarioJumpNumber + " scenario jumps and " + stepJumpNumber + " step jumps. Phase II the context");
        checkJumpsInconsistencies(application);
        emptyLine();
    }

    private void checkJumpsInconsistencies(final Application application) {
        beginBlock("Checking jump inconsistencies...");
        int errors = 0;
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                boolean missingJump = scenario.getCrud().isCreate() && step.isMandatory();
                if (!step.getStepConstraint().isScenarioReference()) {
                    continue;
                }
                for (final Jump jump : step.getJumps()) {

                    final CRUD jumpCrud = jump.getCrudOrTargetCrud();
                    missingJump = missingJump && !jumpCrud.isCreate() && !jumpCrud.isSearch();

                    if (!jumpCrud.isCreate() && !jumpCrud.isSearch()) {
                        continue;
                    }

                    if ((jumpCrud.isSearch()) && !(scenario.getCrud().isCreate() || scenario.getCrud().isUpdate())) {
                        continue;
                    }
                    final Scenario referencedScenario = step.getStepConstraint().getReferencedscenario();
                    final Scenario targetScenario = jump.getTargetScenario();
                    for (final Step referencedStep : referencedScenario.getSteps()) {
                        if (!targetScenario.hasStep(referencedStep)) {
                            compileError("MISSING STEP: Jump on step " + step.getKeyname() + " in scenario " + scenario.getKeyname() + " has jump with CRUD " + jumpCrud + ","
                                    + " referenced step " + referencedStep.getKeyname() + " in referenced scenario " + referencedScenario.getKeyname() + " is not present in jump target " + targetScenario.getKeyname());
                            beginCompileHelpBlock("For helping purpose, here are the steps present in this jump's target :");
                            for (final Step tStep : targetScenario.getSteps()) {
                                compileHelp(tStep.getKeyname() + " ( " + tStep.getName() + " )");
                            }
                            emptyLine();
                            compileHelp("And here are the steps present in the source -- all of these should be present in the target scenario!");
                            for (final Step rStep : referencedScenario.getSteps()) {
                                compileHelp(rStep.getKeyname() + " ( " + rStep.getName() + " )");
                            }
                            endCompileHelpBlock();
                            errors++;
                            break;
                        }
                    }
                }

                if (missingJump) {
                    compileError("MISSING JUMP: Step " + step.getKeyname() + " is mandatory in CREATE scenario " + scenario.getKeyname() + " but has no jump allowing it to be provided.");
                    beginCompileHelpBlock("For helping purpose, here is a list of the jumps on that step");
                    for (final Jump jump : step.getJumps()) {
                        // TODO The following line cause NPË in some case apparently when the target scenario is not found ...
                        compileHelp(jump.getName() + " pointing to scenario " + (jump.getTargetScenario() != null ? jump.getTargetScenario().getKeyname() : "'no target scenario !'") + " with CRUD " + jump.getCrudOrTargetCrud());
                    }
                    endCompileHelpBlock();
                    errors++;
                }
            }
        }
        endBlock("Checking jump inconsistencies : done. Found " + errors + " errors.");
    }

    private void computeFetchPaths(final Application application) {
        // TODO Ensure that there is no graph of scenario references/jumps before computing these.
        // Otherwise, we might recurse indefinitely!!
        // Examples :   scenarioA --jump--> scenarioA
        //              scenarioA --jump--> scenarioB --jump--> scenarioA
        emptyLine();
        beginBlock("Computing fetch paths...");
        for (final Scenario scenario : application.getScenarios()) {
            beginBlock("Computing fetch path for scenario: " + scenario.getKeyname());
            final BusinessObject businessObject = scenario.getRootBusinesObject();
            if (!scenario.getCrud().isDelete() && !scenario.getCrud().isCreate()) {
                info("Scenario " + scenario.getKeyname() + " is not delete or create, computing a preparation path.");
                businessObject.computeFetchPath(scenario, scenario, FetchPathType.PREPARATION, "");
            }

            if (!scenario.getCrud().isRead()) {
                info("Scenario " + scenario.getKeyname() + " is not read or search, computing an execution path.");
                businessObject.computeFetchPath(scenario, scenario, FetchPathType.EXECUTION, "");
            }

            endBlock("Computing fetch path for scenario: " + scenario.getKeyname() + "... done.");
        }
        beginBlock("--- Fetch Paths : SUMMARY ---");
        int total = 0;
        for (final JavaBusinessObject bo : application.getJavaBusinessObjects()) {
            final int num = bo.getFetchPathNumber();
            emptyLine();
            info("Business object " + bo.getName() + " has " + num + " fetch paths");
            bo.getFetchPlansForDebug();
            emptyLine();
            total += num;
        }
        info("A total of " + total + " fetch paths instances have been found.");
        info("(Several fetch paths instance can correspond to the same scenario !!!)");
        endBlock("--- Fetch Paths : SUMMARY, done. ---");
        beginBlock("Computing contextual fetch plans...");
        for (final Scenario scenario : application.getScenarios()) {
            scenario.computeContextualFetchPlans();
        }
        for (final JavaBusinessObject jbo : application.getJavabusinessObjects()) {
            jbo.gatherContextualServerCalls();
        }
        endBlock("Computing contextual fetch plans... Done.");
        endBlock("Computing fetch paths... done.");
        emptyLine();
    }

    private Scenario getOrCreateTargetScenario(final Application application, final Jump jump, final String contextInCaseOfError) {
        final String scenarioKeyname = jump.getTargetKeyname();
        final CRUD crud = jump.getCrud();
        Scenario result = null;
        final Scenario targetScenario = application.getScenarioForKeyname(scenarioKeyname);
        if (targetScenario != null && crud != targetScenario.getCrud()) {
            jump.setShowNoToolTip(true);
        }
        if (crud == null) {
            result = getTargetScenarioWithoutCRUD(application, scenarioKeyname);
        } else {
            result = getTargetScenarioWithCRUD(application, scenarioKeyname, crud, contextInCaseOfError);
        }
        if (result == null) {
            compileError("Unable to find the target scenario of a jump (" + contextInCaseOfError + ")");
            beginCompileHelpBlock("For helping purpose, here is the list of scenario I used to search for");
            compileHelp("Need to find " + scenarioKeyname + (crud == null ? ", no crud" : ", CRUD= " + crud) + " from ");
            emptyLine();
            for (final Scenario scenario : application.getScenarios()) {
                compileHelp("    Scenario " + scenario.getKeyname() + ", CRUD= " + scenario.getCrud());
            }
            endCompileHelpBlock();
        }

        return result;
    }

    private Scenario getTargetScenarioWithCRUD(final Application application, final String scenarioKeyname, final CRUD crud, final String contextInCaseOfError) {
        // We can assume crud is not null here.
        beginBlock("Seeking for scenario (case with CRUD) " + scenarioKeyname + " for CRUD= " + crud.toString());
        Scenario result = null;

        // Try to find by keyname and CRUD
        info("Seeking for that keyname and CRUD in existing scenarios : " + scenarioKeyname + " [CRUD= " + crud + "]");
        for (final Scenario scenario : application.getScenarios()) {
            if (scenario.getKeyname().equals(scenarioKeyname) && scenario.getCrud().equals(crud)) {
                result = scenario;
                break;
            }
        }
        if (result != null) {
            info("OK, Houston, classical case, we just got it. Yeah, I know its boring, sorry, I'm just applying the procedure ... " + result.getKeyname() + " [CRUD= " + result.getCrud() + "]");
        } else {
            final String syntheticScenarioKey = scenarioKeyname + "_" + crud;
            info("Houston, we didn't find it, we're now seeking with the synthetic scenario key : " + syntheticScenarioKey);
            for (final Scenario scenario : application.getScenarios()) {
                if (scenario.getKeyname().equals(syntheticScenarioKey)) {
                    result = scenario;
                    break;
                }
            }
            if (result != null) {
                info("OK, Houston, we managed to find it from the existing synthetic scenarios. " + result.getKeyname() + " [CRUD= " + result.getCrud() + "]");
            } else {
                info("Houston, we'll have to try to create a synthetic scenario, no other way to get it.");
                info("Seeking for that keyname in existing scenarios : " + scenarioKeyname);
                for (final Scenario scenario : application.getScenarios()) {
                    if (scenario.getKeyname().equals(scenarioKeyname)) {
                        result = scenario;
                        break;
                    }
                }
                if (result == null) {
                    compileError("Unable to get the target scenario for a jump based on that scenario keyname : " + scenarioKeyname + " and CRUD " + crud + " (" + contextInCaseOfError + ")");
                    beginCompileHelpBlock("For helping purpose, here are the scenarios we used for the search :");
                    for (final Scenario scenario : application.getScenarios()) {
                        compileHelp("    Scenario " + scenario.getKeyname() + ", CRUD= " + scenario.getCrud());
                    }
                    endCompileHelpBlock();
                } else {
                    info("OK, Houston, we got the scenario but with the wrong CRUD, let's create a synthetic one from it : " + result.getKeyname() + "[CRUD= " + result.getCrud() + "] -> " + syntheticScenarioKey + "[CRUD= " + crud + "]");
                    result = getNewSyntheticScenario(result, crud);
                    debug("Adding the new synthetic scenario " + result.getKeyname() + " ...");
                    try {
                        application.addScenario(result);
                    } catch (DuplicateScenarioException e) {
                        error("Error during new scenario addition for jump compilation " + result.getKeyname(), e);
                    }
                    debug("Adding the new synthetic scenario. Done.");
                }
            }
        }

        endBlock("Seeking for scenario (case with CRUD) " + scenarioKeyname + " => found " + (result == null ? " nothing !" : " 1 Scenario " + result.getKeyname() + " [CRUD= " + result.getCrud() + "]"));
        return result;
    }

    private Scenario getTargetScenarioWithoutCRUD(final Application application, final String scenarioKeyname) {
        beginBlock("Seeking for scenario (case without CRUD) " + scenarioKeyname);
        Scenario result = null;
        for (final Scenario scenario : application.getScenarios()) {
            if (scenario.getKeyname().equals(scenarioKeyname)) {
                result = scenario;
                break;
            }
        }
        endBlock("Seeking for scenario (case without CRUD) " + scenarioKeyname + " => found " + (result == null ? " nothing !" : " 1 Scenario " + result.getKeyname() + " [CRUD= " + result.getCrud() + "]"));
        return result;
    }

    private Scenario getNewSyntheticScenario(final Scenario scenario, final CRUD newCRUD) {
        if (newCRUD == null) {
            throw new NullPointerException("getNewSyntheticScenario : newCRUD is null");
        }
        final Scenario result = scenario.getJPAClone();
        result.setKeyname(scenario.getKeyname() + "_" + newCRUD.toString());
        result.setSection(null);
        result.setCrud(newCRUD);
        if (!newCRUD.isSearch()) {
            result.setStaticFilter(null);
            result.setDynamicFilter(null);
        }
        result.setSynthetic(scenario.getKeyname());

        if (result.getRootBusinesObject() != null) {
            result.getRootBusinesObject().addScenario(result);
        }

        return result;
    }

    /*
    // TODO case of childScenario that has steps.
    // must add another scenario attribute : insertionPosition where the parent steps will be inserted in the child.
     */
    private void computeScenariosInheritance(Application application) {
        emptyLine();
        beginBlock("Scenario inheritance resolution ...");
        for (final Scenario scenario : application.getScenarios()) {
            computeScenarioInheritance(application, scenario);
        }
        endBlock("Scenario inheritance resolution ...");
        emptyLine();
    }

    private void computeScenarioInheritance(final Application application, final Scenario scenario) {

        if (scenario.hasParent() && !scenario.isInheritenceComputed()) {
            beginBlock("Found inheritance : " + scenario.getKeyname() + " inherit from " + scenario.getParentKeyname());
            final Scenario parentScenario = application.getScenarioForKeyname(scenario.getParentKeyname());
            if (parentScenario == null) {
                error("Scenario " + scenario.getKeyname() + " inherit from " + scenario.getParentKeyname() + " but " + scenario.getParentKeyname() + " can't be found !", true);
            } else {
                info("Found the parent scenario : [" + parentScenario.getName() + "] " + parentScenario.getKeyname() + " " + parentScenario.getCrud() + " " + parentScenario.getData());
                if (scenario.getData() != null && !scenario.getData().equals(parentScenario.getData())) {
                    error("Scenario " + scenario.getKeyname() + " (data= " + scenario.getData() + ") and its parent " + parentScenario.getKeyname() + " (data= " + parentScenario.getData() + ") doesn't work on the same data, this is forbidden.", true);
                }
                if (scenario.getTechdata() != null && !scenario.getTechdata().equals(parentScenario.getTechdata())) {
                    error("Scenario " + scenario.getKeyname() + " (techdata= " + scenario.getTechdata() + ") and its parent " + parentScenario.getKeyname() + " (techdata= " + parentScenario.getTechdata() + ") doesn't work on the same techdata, this is forbidden.", true);
                }

                if (parentScenario.hasParent()) {
                    computeScenarioInheritance(application, parentScenario);
                }

                if (scenario.getComment() == null || scenario.getComment().length() == 0) {
                    scenario.setComment(parentScenario.getComment());
                }
                if (scenario.getDocumentation() == null || scenario.getDocumentation().length() == 0) {
                    scenario.setDocumentation(parentScenario.getDocumentation());
                }
                if (scenario.getName() == null || scenario.getName().length() == 0) {
                    scenario.setName(parentScenario.getName());
                }

                if (parentScenario.getData() == null) {
                    compileError("Error during inheritance compilation : parent scenario has no data !");
                    beginCompileHelpBlock("for helping purpose, here is some info on the parent scenario :");
                    compileHelp("   parent scenario keyname : " + parentScenario.getKeyname());
                    endCompileHelpBlock();
                }

                if (scenario.getData() != null && !parentScenario.getData().equals(scenario.getData())) {
                    compileError("Error during inheritance compilation. Data is different in child and parent.");
                    beginCompileHelpBlock("For helping purpose, here are the Data :");
                    compileHelp("   parent scenario Data : " + parentScenario.getData());
                    compileHelp("   chide scenario Data : " + scenario.getData());
                    endCompileHelpBlock();
                }
                scenario.setData(parentScenario.getData());

                if (scenario.getTechdata() != null && parentScenario.getTechdata() != null && !parentScenario.getTechdata().equals(scenario.getTechdata())) {
                    compileError("Error during inheritance compilation. TechData is different in child and parent.");
                    beginCompileHelpBlock("For helping purpose, here are the TechData :");
                    compileHelp("   parent scenario TechData : " + parentScenario.getTechdata());
                    compileHelp("   chide scenario TechData : " + scenario.getTechdata());
                    endCompileHelpBlock();
                }
                scenario.setTechdata(parentScenario.getTechdata());

                if (scenario.getStaticFilter() == null && parentScenario.getStaticFilter() != null) {
                    scenario.setStaticFilter(parentScenario.getStaticFilter().getJPAClone());
                } else if (scenario.getStaticFilter() != null && parentScenario.getStaticFilter() != null) {
                    //TODO for filter : proper merge rather than just ass from parent
                    compileError("Not done yet ! static filter should be merged. This is not done yet !");
                    beginCompileHelpBlock("For helping purpose :");
                    compileHelp("   parent scenario : " + parentScenario.getKeyname() + ", static filter : " + parentScenario.getStaticFilter().getKeyname());
                    compileHelp("   child scenario : " + scenario.getKeyname() + ", static filter : " + scenario.getStaticFilter().getKeyname());
                    endCompileHelpBlock();
                }

                if (scenario.getDynamicFilter() == null && parentScenario.getDynamicFilter() != null) {
                    scenario.setDynamicFilter(parentScenario.getDynamicFilter().getJPAClone());
                } else if (scenario.getDynamicFilter() == null && parentScenario.getDynamicFilter() != null) {
                    //TODO for filter : proper merge rather than just ass from parent
                    compileError("Not done yet ! dynamic filter should be merged. This is not done yet !");
                    beginCompileHelpBlock("For helping purpose :");
                    compileHelp("   parent scenario : " + parentScenario.getKeyname() + ", dynamic filter : " + parentScenario.getDynamicFilter().getKeyname());
                    compileHelp("   child scenario : " + scenario.getKeyname() + ", dynamic filter : " + scenario.getDynamicFilter().getKeyname());
                    endCompileHelpBlock();
                }

                if (scenario.getCrud() == null) {
                    scenario.setCrud(parentScenario.getCrud());
                } else {
                    info("Inheritance override : parent CRUD is " + parentScenario.getCrud() + " but child scenario will keep CRUD=" + scenario.getCrud());
                }

                for (final Step step : parentScenario.getSteps()) {
                    try {
                        final Step stepClone = step.getJPAClone();
                        stepClone.setSynthetic();
                        scenario.addStep(stepClone);
                    } catch (DuplicateStepException e) {
                        info("Inheritance override : parent Scenario already have a step " + step.getKeyname() + " . Child scenario step will be kept ");
                    }
                }

                for (final Rule rule : scenario.getRules()) {
                    try {
                        scenario.addRule(rule.getJPAClone());
                    } catch (DuplicateRuleException e) {
                        info("Inheritance override : parent Scenario already have a rule " + rule.getKeyname() + " . Child rule step will be kept ");
                    }
                }

                for (final String parentRole : parentScenario.getRoles()) {
                    if (!"public".equals(parentRole)) {
                        try {
                            scenario.addRole(new String(parentRole));
                        } catch (DuplicateRoleException ex) {
                            compileError("Duplicate role " + parentRole + " from parent scenario " + parentScenario.getKeyname() + " in scenario " + scenario.getKeyname());
                            beginCompileHelpBlock("For helping purpose :");
                            compileHelp("   parent scenario : " + parentScenario.getKeyname() + ", dynamic filter : " + parentScenario.getDynamicFilter().getKeyname());
                            compileHelp("   child scenario : " + scenario.getKeyname() + ", dynamic filter : " + scenario.getDynamicFilter().getKeyname());
                            endCompileHelpBlock();
                        }
                    }
                }
                for (final Jump jump : parentScenario.getJumps()) {
                    scenario.addJump(jump);
                }
            }
            scenario.setInheritenceComputed();
            endBlock("Inheritance computed for : " + scenario.getKeyname() + " inherit from " + scenario.getParentKeyname());
        }
    }

    private void compileFilter(Application application) {
        emptyLine();
        beginBlock("Filter phase ...");
        for (final Scenario scenario : application.getScenarios()) {
            if (!scenario.isComposite()) {
                compileFilter(scenario, scenario.getStaticFilter(), application);
                compileFilter(scenario, scenario.getDynamicFilter(), application);
            }
        }
        emptyLine();
        beginBlock("Building FilterElement parameter name ...");
        for (final Scenario scenario : application.getScenarios()) {
            if (!scenario.isComposite()) {
                final Filter staticFilter = scenario.getStaticFilter();
                if (staticFilter != null) {
                    staticFilter.setFilterElementParamName();
                }

                final Filter dynamicFilter = scenario.getDynamicFilter();
                if (dynamicFilter != null) {
                    dynamicFilter.setFilterElementParamName();
                }
            }
        }
        endBlock("Building FilterElement parameter name. Done.");
        endBlock("Filter phase.Done.");
    }

    private void compileFilter(final Scenario scenario, final Filter filter, final Application application) {
        if (filter != null) {
            if (!scenario.getCrud().isSearch()) {
                compileError("A filter was found in a scenario that is not a search scenario, filter " + filter.getKeyname() + " in scenario " + scenario.getKeyname());
                beginCompileHelpBlock("For helping purpose, here is the list of the filter elements you have used.");
                if (filter.getFilterElements().size() != 0) {
                    for (FilterElement fe : filter.getFilterElements()) {
                        compileHelp("=== " + fe.getKeyname() + " === " + fe.getParamName() + " " + fe.getOperatorForUI() + " " + fe.getValue() + " ===");
                    }
                }

                endCompileHelpBlock();
            } else {
                emptyLine();
                beginBlock("Compiling filter (<scenario keyname>.<filter keyname>) " + scenario.getKeyname() + "." + filter.getKeyname());

                final BusinessObject businesObject = scenario.getRootBusinesObject();
                if (businesObject == null) {
                    compileError("???????????? BusinessObject is null for scenario " + scenario.getKeyname() + " during compilation of filter " + filter.getKeyname());
                }
                final Set<FilterElement> filterElements = filter.getFilterElements();
                if (filterElements == null || filterElements.size() == 0) {
                    compileError("A filter must contains at least one filter element. Filter " + filter.getKeyname() + " has no filter elements", true);
                } else {
                    info("Found " + filterElements.size() + " filter elements for filter " + filter.getKeyname());
                    for (final FilterElement filterElement : filterElements) {
                        final String keyname = filterElement.getKeyname() == null ? null : filterElement.getKeyname().trim();
                        final FilterOperators operator = filterElement.getOperator();
                        final String value = filterElement.getValue() == null ? null : filterElement.getValue().trim();
                        info("\t\t '" + keyname + "' '" + operator + "' '" + value + "' (bool op=" + filterElement.isBooleanOperator() + ")");
                    }
                    beginBlock("Compiling filter elements ...");
                    for (final FilterElement filterElement : filterElements) {
                        final String keyname = filterElement.getKeyname() == null ? null : filterElement.getKeyname().trim();
                        final FilterOperators operator = filterElement.getOperator();
                        final String value = filterElement.getValue() == null ? null : filterElement.getValue().trim();
                        beginBlock("Compiling filter element '" + keyname + "' '" + operator + "' '" + value + "'");
                        if (operator == null) {
                            compileError("A filter element must contains an operator", true);
                        }

                        // Check for acces to the keyname in the metamodel
                        if (keyname == null) {
                            if (!operator.isBoolean()) {
                                compileError("A filter element must have a keyname");
                            }
                        } else {
                            if (!filterElement.isBooleanOperator()) {
                                beginBlock("Seeking for attribute which this filter element apply on '" + keyname + "' '" + operator + "' '" + value + "'");
                                boolean foundAttribute = false;
                                final String[] keynamesPath = keyname.split("\\."); // matches the dot character "."
                                int foundKeynames = 0;
                                BusinessObject currentBO = businesObject;
                                final List<BusinessObjectAttribute> associatedBusinessObjectAttributesPath = CollectionFactory.newList();

                                boolean checkNextKeyname;
                                for (final String key : keynamesPath) {
                                    checkNextKeyname = false;
                                    for (final BusinessObjectAttribute businessObjectAttribute : currentBO.getAttributes()) {
                                        if (key.equals(businessObjectAttribute.getKeyname())) {
                                            debug("keyname " + key + " found in BO " + currentBO.getName());
                                            foundKeynames++;
                                            debug("type of the attribute is " + businessObjectAttribute.getTypeName());
                                            // WARNING this can return null in case of previous errors
                                            // Check that currentBO for null and exclude filter from compile.
                                            currentBO = businessObjectAttribute.getType().getBusinessObject();
                                            checkNextKeyname = true;
                                            associatedBusinessObjectAttributesPath.add(businessObjectAttribute);
                                            break;
                                        }
                                    }
                                    if (!checkNextKeyname) {
                                        break;
                                    }
                                }
                                int deepness = keynamesPath.length;
                                foundAttribute = (deepness != 0) && (foundKeynames == deepness) && (associatedBusinessObjectAttributesPath.size() == deepness);

                                if (!foundAttribute) {
                                    compileError("Unable to link target and an object's attribute. " + keyname);
                                    beginCompileHelpBlock("For helping purpose, here are all the business object's attribute.");
                                    compileHelp("We are in the scenario [" + scenario.getKeyname() + "] " + scenario.getName());
                                    compileHelp("and in the filter element '" + keyname + "' '" + operator + "' '" + value + "'");
                                    compileHelp("The BusinessObject is " + businesObject.getFullName() + " : ");
                                    for (final BusinessObjectAttribute businessObjectAttribute : businesObject.getAttributes()) {
                                        compileHelp("\t\t " + businessObjectAttribute.getTypeFirstCapitalized() + " [" + businessObjectAttribute.getKeyname() + "] " + businessObjectAttribute.getName());
                                    }
                                    if (currentBO != businesObject) {
                                        compileHelp("");
                                        compileHelp("The associated BusinessObject is " + currentBO.getFullName() + " : ");
                                        for (final BusinessObjectAttribute businessObjectAttribute : currentBO.getAttributes()) {
                                            compileHelp("\t\t " + businessObjectAttribute.getTypeFirstCapitalized() + " [" + businessObjectAttribute.getKeyname() + "] " + businessObjectAttribute.getName());
                                        }
                                    }
                                    endCompileHelpBlock();
                                } else {
                                    final BusinessObjectAttribute associatedBusinessObjectAttribute = associatedBusinessObjectAttributesPath.get(deepness - 1);

                                    final Step associatedStep = findStepFromBusinessObjectAttribute(application, associatedBusinessObjectAttribute);
                                    final String UIName = associatedStep.getName();
                                    if (associatedStep.getStepConstraint().isDataset()) {
                                        filterElement.setDataset(associatedStep.getStepConstraint().getReferencedDataset());
                                    } else if (associatedBusinessObjectAttribute.isFile()) {
                                        compileError("Search can not be performed on file type step !");
                                        beginCompileHelpBlock("For helping purpose, here are some details on the scenario that contain that step.");
                                        compileHelp("Scenario is \"" + scenario.getKeyname() + "\" and filter \"" + filter.getKeyname() + "\" has a filter element on an attribute of type file.");
                                        compileHelp("The business object attribute that is causing the problem is \"" + associatedBusinessObjectAttribute.getKeyname() + "\"");
                                        compileHelp("Here are the different attributes on which you could perform the search instead.");
                                        for (final BusinessObjectAttribute businessObjectAttribute : associatedBusinessObjectAttribute.getBusinessObject().getAttributes()) {
                                            if (!businessObjectAttribute.isFile()) {
                                                compileHelp("\t\t " + businessObjectAttribute.getTypeFirstCapitalized() + " [" + businessObjectAttribute.getKeyname() + "] " + businessObjectAttribute.getName());
                                            }
                                        }
                                        endCompileHelpBlock();
                                    }

                                    filterElement.setUIName(UIName);
                                    final String attributesPathAsString = filterElement.getAssociatedBusinessObjectAttributesPathAsString(associatedBusinessObjectAttributesPath);

                                    info("Found a correspondance for the filter element '" + keyname + "' '" + operator + "' '" + value + "' => " + businesObject.getFullName() + "." + attributesPathAsString + " (" + associatedBusinessObjectAttribute.getTypeFirstCapitalized() + " [" + associatedBusinessObjectAttribute.getKeyname() + "] " + associatedBusinessObjectAttribute.getName() + ")");
                                    filterElement.checked(associatedBusinessObjectAttributesPath);
                                }
                                endBlock("Done seeking for attribute which this filter element apply on '" + keyname + "' '" + operator + "' '" + value + "'");
                            }
                        }
                        if (value != null) {
                            beginBlock("Compiling filterElement's value " + value);
                            compileFilterElementValue(filterElement, application);
                            endBlock("Compiling filterElement's value " + value);
                        } else {
                            if (value == null && filter.isStatic() && !filterElement.isBooleanOperator()) {
                                compileError("In a static filter value can't be null ! Filter is " + filter.getKeyname() + " in filter element " + filterElement.getKeyname() + " " + filterElement.getOperator() + " " + filterElement.getValue());
                            }
                        }
                        endBlock("Compiling filter element '" + keyname + "' '" + operator + "' '" + value + "'");
                    }
                    endBlock("Compiling filter elements. Done.");
                    if (filter.isChecked()) {
                        businesObject.addFilter(scenario.getKeyname(), new BlankFilter(filter));
                    } else {
                        info("Filter " + filter.getKeyname() + " not compiled due to previous errors. Will ignore starting from now. Roger that Houston.");
                    }
                }
                checkForBooleanOperator(filter);
                endBlock("Compiling filter " + scenario.getKeyname() + "." + filter.getKeyname() + ". Done.");
            }
        }
    }

    private void checkForBooleanOperator(final Filter filter) {
        beginBlock("Check for boolean operator in filter " + filter.getKeyname());
        boolean shouldBeBoolean = false;
        for (final FilterElement filterElement : filter.getFilterElements()) {
            if (shouldBeBoolean && !filterElement.isBooleanOperator()) {
                compileError("Error in filter " + filter.getKeyname() + " There must be a boolean operator (AND or OR) between filter element");
                beginCompileHelpBlock("For helping purpose, here are the filter elements for the filter " + filter.getKeyname());
                for (final FilterElement filterElementError : filter.getFilterElements()) {
                    compileHelp("   " + filterElementError.getPos() + "- " + (filterElementError.isBooleanOperator() ? filterElementError.getOperator() : filterElementError.getKeyname() + " " + filterElementError.getOperator() + " :" + filterElementError.getParamName()));
                }
                endCompileHelpBlock();
            }
            shouldBeBoolean = !shouldBeBoolean;
        }
        endBlock("Check for boolean operator in filter " + filter.getKeyname());
    }

    private Step findStepFromBusinessObjectAttribute(Application application, BusinessObjectAttribute businessObjectAttribute) {
        Step result = null;
        final BusinessObject businessObject = businessObjectAttribute.getBusinessObject();
        for (final Scenario scenario : application.getScenarios()) {
            if (!scenario.isSynthetic() && scenario.getRootBusinesObject() == businessObject) {
                for (final Step step : scenario.getSteps()) {
                    final Set<Step> steps = businessObjectAttribute.getSteps();
                    if (steps.contains(step)) {
                        result = step;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void compileFilterElementValue(final FilterElement filterElement, final Application application) {

        if (filterElement.isRuntimeVariable()) {
            if (filterElement.isDotRuntimeVariable()) {
                final String dotRuntimeVariableLeftPart = filterElement.getDotRuntimeVariableLeftPart();
                final String[] validRuntimeVariables = new String[]{"currentUser"};
                boolean found = false;
                for (final String valieRuntimeVariable : validRuntimeVariables) {
                    if (valieRuntimeVariable.equalsIgnoreCase(dotRuntimeVariableLeftPart)) {
                        info("Found a filter element against runtime variable : " + dotRuntimeVariableLeftPart);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    compileError("This filter element (" + filterElement.toStringForTrace() + ") is an unknown runtime variable : " + dotRuntimeVariableLeftPart);
                    beginCompileHelpBlock("For helping purpose, here are the valid runtime variable :");
                    for (final String valieRuntimeVariable : validRuntimeVariables) {
                        compileHelp("   - " + valieRuntimeVariable);
                    }
                    endCompileHelpBlock();
                } else {
                    // TODO use BusinessObject attribute to check the right part : for example, currentuser.id is OK but currentuser.blabla may not ... depending on spec.
                }
            } else {
                compileError("This filter element value (" + filterElement.toStringForTrace() + ") is a runtime variable but has no dot.");
                // TODO exclude filter element
            }
        } else {
            String value = filterElement.getValue();
            if (value.startsWith("=")) {
                // Remove the leading =
                value = value.substring(1, value.length());
                compileFilterElementValueTemporalCase(value, "CURRENT_DATE", filterElement);
                compileFilterElementValueTemporalCase(value, "CURRENT_TIME", filterElement);
                compileFilterElementValueTemporalCase(value, "CURRENT_DATETIME", filterElement);
                compileFilterElementValueDatasetCase(value, "DATASET", filterElement, application);
            } else if ("null".equals(value.toLowerCase())) {
                info("Found a filter element against NULL");

                if (!FilterOperators.is.equals(filterElement.getOperator()) && !FilterOperators.isnot.equals(filterElement.getOperator())) {
                    compileError("Invalid operator on filter element against NULL. In that case, operator must be one of IS or IS NOT.");
                    beginCompileHelpBlock("For helping purpose, here is the faulty filter element's");
                    compileHelp("'" + filterElement.getKeyname() + "' '" + filterElement.getOperator() + "' '" + filterElement.getValue() + "'");
                    endCompileHelpBlock();
                } else {
                    filterElement.setAgainstNull(true);
                    if (filterElement.getAssociatedBusinessObjectAttribute().isCollection()) {
                        filterElement.setAgainstNullCollection(true);
                        filterElement.setValue("EMPTY");
                    } else {
                        filterElement.setAgainstNullCollection(false);
                        filterElement.setValue("NULL");
                    }
                }
            }
        }
    }

    private void compileFilterElementValueDatasetCase(final String value, final String keyword, final FilterElement filterElement, final Application application) {
        if (value.toUpperCase().startsWith(keyword)) {
            info("Found a dataset value " + value + " [" + keyword + "]");
            if (value.length() > keyword.length()) {
                final String parameters = StringHelper.getRight(value, keyword);
                if (parameters != null && parameters.length() > 0) {
                    info("Found dataset parameters " + parameters);
                    String datasetKeyname = StringHelper.getRight(parameters, "(");
                    datasetKeyname = StringHelper.getLeft(datasetKeyname, ")");
                    info("Found a datast keyname " + datasetKeyname);
                    boolean found = false;
                    for (final Dataset dataset : application.getDataSet()) {
                        if (datasetKeyname.equals(dataset.getKeyname())) {
                            filterElement.setDataset(new ivar.metamodel.target.generic.DataSet(application, dataset));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        compileError("Unable to find dataset targeted by filter element dataset (" + keyword + ").");
                        beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                        compileHelp(parameters);
                        compileHelp("Entire value string is " + value);
                        endCompileHelpBlock();
                    }
                } else {
                    compileError("Filter element dataset (" + keyword + ") parameter must respect the syntax DATASET(<dataset keyname>)");
                    beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                    compileHelp(parameters);
                    compileHelp("Entire value string is " + value);
                    endCompileHelpBlock();
                }
            } else {
                compileError("Filter element dataset (" + keyword + ") parameter must respect the syntax DATASET(<dataset keyname>). Parameter is madadatory to provide dataset's keyname");
                beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                compileHelp("Entire value string is " + value);
                endCompileHelpBlock();
            }
        }
    }

    private void compileFilterElementValueTemporalCase(final String value, final String temporalKeyword, final FilterElement filterElement) {
        if (value.toUpperCase().startsWith(temporalKeyword)) {
            info("Found a temporal value " + value + " [" + temporalKeyword + "]");
            if (value.length() > temporalKeyword.length()) {
                final String parameters = StringHelper.getRight(value, temporalKeyword).trim();
                if (parameters != null && parameters.length() > 0) {
                    info("Found temporal parameters " + parameters);
                    final StringTokenizer st = new StringTokenizer(parameters, " ", false);

                    while (st.hasMoreTokens()) {
                        final String parameter = st.nextToken();
                        info("Compiling temporal parameter : " + parameter);
                        if (parameter.length() < 3) {
                            compileError("Filter element " + filterElement.getKeyname() + " is temporal (" + temporalKeyword + ") parameter must be at least 3 char length like in +2Y or -1Y : " + filterElement.getValue());
                            beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                            compileHelp(parameter);
                            compileHelp("Entire value string is " + value);
                            endCompileHelpBlock();
                        } else {
                            boolean temporalOperationOK = false;
                            final TemporalFilterElementParameterCompilation temporalFilterElementParameterCompilation = new TemporalFilterElementParameterCompilation();
                            if ('+' == parameter.charAt(0)) {
                                temporalFilterElementParameterCompilation.setAddition(true);
                                info("parameter is an adition " + parameter);
                                temporalOperationOK = true;
                            } else if ('-' == parameter.charAt(0)) {
                                temporalFilterElementParameterCompilation.setAddition(false);
                                info("parameter is a substraction " + parameter);
                                temporalOperationOK = true;
                            } else {
                                compileError("Filter element temporal (" + temporalKeyword + ") parameter must start with + or -");
                                beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                                compileHelp(parameter);
                                compileHelp("Entire value string is " + value);
                                endCompileHelpBlock();
                            }
                            if (temporalOperationOK) {
                                final char timeUnitString = parameter.charAt(parameter.length() - 1);
                                debug("timeUnitString = " + timeUnitString);

                                switch (timeUnitString) {
                                    // y (Year), M (Month), w (Week), d (Day), h (Hour), m (Minute), s (Second), S (Millisecond)
                                    case 'y':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.year);
                                        break;
                                    case 'Y':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.year);
                                        break;
                                    case 'M':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.month);
                                        break;
                                    case 'w':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.week);
                                        break;
                                    case 'W':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.week);
                                        break;
                                    case 'd':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.day);
                                        break;
                                    case 'D':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.day);
                                        break;
                                    case 'h':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.hour);
                                        break;
                                    case 'H':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.hour);
                                        break;
                                    case 'm':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.minute);
                                        break;
                                    case 's':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.second);
                                        break;
                                    case 'S':
                                        temporalFilterElementParameterCompilation.setTimeUnit(TimePeriodUnit.milisecond);
                                        break;
                                    default:
                                        compileError("Filter element temporal (" + temporalKeyword + ") parameter must ends with a char identifying the time period unit like in a SimpleDateFormat y (Year), M (Month), w (Week), d (Day), h (Hour), m (Minute), s (Second), S (Millisecond)");
                                        beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                                        compileHelp(parameter);
                                        compileHelp("Entire value string is " + value);
                                        endCompileHelpBlock();
                                }
                                final String durationString = parameter.substring(1, parameter.length() - 1);
                                debug("duration string is " + durationString);
                                try {
                                    final int duration = Integer.parseInt(durationString);
                                    temporalFilterElementParameterCompilation.setQuantity(duration);
                                } catch (NumberFormatException e) {
                                    compileError("Filter element temporal (" + temporalKeyword + ") parameter must respect +2M or -3Y form. The problem here is the central part doesn't look like a number " + durationString);
                                    beginCompileHelpBlock("For helping purpose, here is the faulty filter element's parameter");
                                    compileHelp(parameter);
                                    compileHelp("Entire value string is " + value);
                                    endCompileHelpBlock();
                                }
                                filterElement.addTemporalFilterElementParameterCompilation(temporalFilterElementParameterCompilation);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean typeResolution(final Application application, final String message) {
        emptyLine();
        beginBlock(message + " ...");
        boolean result = true;
        for (final Scenario scenario : application.getScenarios()) {
            beginBlock("Type computation for Scenario " + scenario.getKeyname());
            if (scenario.getSteps().size() == 0) {
                info("Scenario " + scenario.getKeyname() + " has no steps");
            } else {
                for (final Step step : scenario.getSteps()) {
                    typeResolution(application, step, false);
                }
            }
            emptyLine();
            endBlock();
        }
        endBlock(message + ". Done.");
        emptyLine();
        return result;
    }

    private void typeResolution(Application application, Step step, final boolean isForsubStep) {
        final String message = " " + (isForsubStep ? " -subStep- " : "") + "(" + step.getMin() + ".." + step.getMax() + ") " + step.getStepConstraint().getType() + " " + step.getKeyname();
        debug(" -- computing type for step " + message);
        step.getStepConstraint().compute(application);
        info(message + " : " + step.getStepConstraint());
        if (step.getEmbededSteps() != null) {
            for (final Step subStep : step.getEmbededSteps()) {
                typeResolution(application, subStep, true);
            }
        }
    }

    private void reservedKeywordChecking(final Application application) {
        emptyLine();
        beginBlock("Checking for reserved keyword ...");
        for (final Scenario scenario : application.getScenarios()) {
            checkReservedKeywords(scenario.getKeyname(), "The scenario's keyname (id= " + scenario.getId() + "). " + scenario, true, true);
            checkReservedKeywords(scenario.getData(), "The scenario's data (id= " + scenario.getId() + "). " + scenario, true, true);

            for (final Step step : scenario.getSteps()) {
                checkStepForReservedKeyword(step);
            }
            for (final Rule rule : scenario.getRules()) {
                checkRuleForReservedKeyword(rule);
            }
        }
        for (final Dataset dataSet : application.getDataSet()) {
            checkReservedKeywords(dataSet.getKeyname(), "The dataset's keyname (id= " + dataSet.getId() + "). " + dataSet, true, true);
        }
        endBlock("Checking for reserved keyword. Done.");
        emptyLine();
    }

    private void checkStepForReservedKeyword(final Step step) {
        checkReservedKeywords(step.getKeyname(), "The step's keyname (id= " + step.getId() + "). " + step, true, true);

        for (final Rule rule : step.getRules()) {
            checkRuleForReservedKeyword(rule);
        }
        Set<Step> embededSteps = step.getEmbededSteps();
        if (embededSteps != null && embededSteps.size() > 0) {
            for (final Step embedeStep : embededSteps) {
                checkStepForReservedKeyword(embedeStep);
            }
        }
    }

    private boolean checkRuleForReservedKeyword(final Rule rule) {
        return checkReservedKeywords(rule.getKeyname(), "The rule's keyname (id= " + rule.getId() + "). " + rule, true, true);
    }

    private boolean checkReservedKeywords(final String keyword, final String desc, final boolean checkAgainstType, final boolean checkAgainstSQL) {
        boolean result = false;
        for (final String reservedKeyword : ReservedKeywords.specReservedKeywords) {
            if (reservedKeyword.equalsIgnoreCase(keyword)) {
                error("Use of a reserved keyword " + reservedKeyword + " (It is not case sensitive) in " + desc, true);
                result = true;
            }
        }

        for (final String[] reservedKeywords : ReservedKeywords.LANGUAGES_RESERVED_KEYWORDS) {
            for (final String reservedKeyword : reservedKeywords) {
                if (reservedKeyword.equalsIgnoreCase(keyword)) {
                    error("Use of a reserved keyword " + reservedKeyword + " (It is not case sensitive) in " + desc, true);
                    result = true;
                }
            }
        }

        if (checkAgainstType) {
            for (StepTypes reservedKeyword : StepTypes.values()) {
                if (reservedKeyword.toString().equalsIgnoreCase(keyword)) {
                    error("Use of a reserved keyword " + reservedKeyword + " (It is not case sensitive) in " + desc, true);
                    result = true;
                }
            }
        }

        if (checkAgainstSQL) {
            for (final String[] reservedKeywords : ReservedKeywords.SQL_RESERVED_KEYWORDS) {
                for (final String reservedKeyword : reservedKeywords) {
                    if (reservedKeyword.equalsIgnoreCase(keyword)) {
                        error("Use of a reserved keyword " + reservedKeyword + " (It is not case sensitive) in " + desc, true);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private void computeMenu(final Application application) {
        // Check the case of a menu that have a screen (so a leaf) but would have child.
        // This is not possible since clicking on the menu launch the screen but should also open the menu the presenting the child to the end-user.
        //
        // Check the cases of screen that would have the same path.
        emptyLine();
        beginBlock("Building menu ...");
        beginBlock("Checking section ...");
        int menuScreen = 0;
        int nomenuScreen = 0;
        for (final Screen screen : application.getScreens()) {
            final String section = screen.getSection();
            if (section != null && section.startsWith("/")) {
                warning("Section must *not* start with a leading / but I'm a cool compiler so I'll handle it before you correct it");
                screen.setSection(section.substring(1, section.length()));
            }
            if (section != null && section.length() > 0) {
                menuScreen++;
            } else {
                nomenuScreen++;
            }
        }
        info("Found " + menuScreen + " screens for the main menu and " + nomenuScreen + " screens that are not directly accessible via the main menu.");
        endBlock("Checking section. Done.");

        final Map<Menu, Screen> screenForMenu = CollectionFactory.newMap(application.getScenariosSize());
        final Menu rootMenu = new Menu();
        int menuNumber = 0;
        for (final Screen screen : application.getScreens()) {
            if (screen.isCreate() || screen.isDSearch() || screen.isSearch() || screen.isPureComposition()) {
                final String section = screen.getSection();
                if (section != null && section.length() > 0) {
                    String element = "";
                    Menu lastMenu = rootMenu;
                    String path = section;
                    boolean doWeContinue = true;
                    while (doWeContinue) {
                        element = StringHelper.getLeft(path, "/");
                        path = StringHelper.getRight(path, "/");
                        Menu menu = lastMenu.getMenu(element);
                        if (menu == null) {
                            if (element != null) {
                                menu = new Menu(element);
                            } else {
                                if (path != null) {
                                    menu = new Menu(path);
                                } else {
                                    menu = new Menu(screen);
                                    screenForMenu.put(menu, screen);
                                    doWeContinue = false;
                                }
                            }
                            lastMenu.addMenu(menu);
                        }
                        lastMenu = menu;
                    }
                    info("Action included to the menu : " + section + " : [" + screen.getKeyForJava() + "] " + screen.getName());
                    menuNumber++;
                } else {
                    info("Action excluded from menu because no section is defined : [" + screen.getKeyForJava() + "] " + screen.getName());
                }
            } else {
                compileWarning("Screen " + screen.getKeyname() + " (CRUD=" + screen.getJavaCrud() + ") has a section but will not be in the menu");
            }
        }
        application.setRootMenu(rootMenu);

        beginBlock("Check for menu consistency ...");
        checkMenu(screenForMenu, rootMenu);
        endBlock("Check for menu consistency. Done.");

        endBlock("Building menu Done. Found " + menuNumber + " menus.");
        emptyLine();
    }

    private void checkMenu(final Map<Menu, Screen> screenForMenu, final Menu currentMenu) {
        if (currentMenu.isLeaf() && currentMenu.getMenus().size() > 0) {
            compileError("Menu consistency error : a menu link with an action must be a leaf. This one has child " + currentMenu.toString());
            beginCompileHelpBlock("for helping purpose, here is the screen that menu come from and the child :");
            compileHelp(screenForMenu.get(currentMenu).debugIdentifier());
            compileHelp("-- Now the child :");
            for (final Menu subMenu : currentMenu.getMenus()) {
                compileHelp(subMenu.toString());
            }
            endCompileHelpBlock();
        }
        for (final Menu subMenu : currentMenu.getMenus()) {
            checkMenu(screenForMenu, subMenu);
        }
    }

    private void generateCompilationReport(final Application application, final String subFolder, final ApplicationMetrics applicationMetrics, final boolean writeFile, final boolean echo) {
        emptyLine();
        beginBlock("Generating Compilation report write file = " + writeFile + ", echo = " + echo + "...");

        final String outputFolder = applicationMetrics.getIvarcParameters().getCompileRootFolder() + File.separator + subFolder;
        final String applicationName = application.getName();
        final int scenarioNumber = applicationMetrics.getScenarioNumber();
        final int stepNumber = applicationMetrics.getStepNumber();
        final int jumpNumber = applicationMetrics.getJumpNumber();
        final int ruleNumber = applicationMetrics.getRuleNumber();
        final int specLineNumber = applicationMetrics.getSpecLineNumber();
        final int filterNumber = applicationMetrics.getFilterNumber();
        int fileNumber = applicationMetrics.getFileNumber();

        if (echo) {
            info("-------------------------------------------");
            info("----- Compilation report for application " + applicationName);
            info("-------------------------------------------");
            info(scenarioNumber + " scenarios, " + stepNumber + " steps, " + ruleNumber + " rules, " + filterNumber + " filters");
            info("This is the equivalent of " + specLineNumber + " engine line number");
        }

        try {
            OutputStreamWriter writer = null;
            if (writeFile) {
                final File enginepowerFile = new File(outputFolder + "/engine-power.html");
                IOHelper.mkdirs(outputFolder);
                writer = new OutputStreamWriter(new FileOutputStream(enginepowerFile));
            }
            int lineNumber = applicationMetrics.getLineNumber();

            final double ratio = applicationMetrics.getRatio();

            if (echo) {
                info("Number of produced line " + lineNumber);
                info("ORBITER ENGINE POWER RATIO is  " + ratio);
                info("Number of produced file " + fileNumber);
            }
            if (writeFile) {
                beginBlock("Writing orbiter engine power ...");
                writer.write("<html><head><title>Orbiter engine power ! -- " + applicationName + "</title>");
                writer.write("<META HTTP-EQUIV=\"CACHE-CONTROL\" CONTENT=\"NO-CACHE\"/>");
                writer.write("<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\"/>");
                writer.write("<META HTTP-EQUIV=\"Expires\" CONTENT=\"0\"/>");

                writer.write("</head>");
                writer.write("<body scroll=\"no\" style=\"background-color:#F3F3F3; font-family: Arial;\" background=\"/icons/do/background.png\"><embed src=\"/icons/sounds/zic-generic.wav\" height=\"0\" width=\"0\"><h1 style=\"text-align: center;\"><img src=\"/icons/do/logo_white.png\">&nbsp; <big>POWER !</big><br/></h1><center><table width=\"1024\" style=\"text-align: center;\" border=\"1\" cellpadding=\"2\" cellspacing=\"0\"><tbody><tr><i><td width=\"512\"><h1 style=\"text-align: center;\">With Orbiter Engine</h1></td><td width=\"512\"><h1>By hand</h1></td></i></tr>");

                writer.write("<tr><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write(" 1 Word document for the specification");
                writer.write("</big></td><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write(fileNumber + " files");
                writer.write("<br/></big></td></tr>");

                writer.write("<tr><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write("  " + scenarioNumber + " scenarios, " + stepNumber + " steps, " + ruleNumber + " rules, " + filterNumber + " filters, " + jumpNumber + " jumps");
                writer.write("</big></td><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write(" specification Word document + SQL and Ant files, JavaScript and Java Classes.");
                writer.write("<br/></big></td></tr>");

                writer.write("<tr><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write(lineNumber + " lines");

                writer.write("<br/></big></td><td width=\"512\" style=\"text-align: center; font-weight: bold;\"><big>");
                writer.write(lineNumber + " lines");

                writer.write("<br/></big></td></tr>");
                writer.write("</tbody></table><br/>");
                writer.write("<div style=\"text-align: center;\"><big><big><span style=\"font-weight: bold;\">");

                writer.write("1 line in Orbiter Engine produce <font color='green'>" + ratio + "</font> lines");
                writer.write("</span></big></big><br/></div>");

                writer.write("</center><MARQUEE DIRECTION=\"up\" width=\"1024\" height=\"700\" SCROLLAMOUNT=\"10\" loop=\"1\" SCROLLDELAY=\"3\" background=\"/icons/do/background.png\">");
                final Map<FileCategories, Set<String>> files = applicationMetrics.getIvarcParameters().getFileCounter().getFiles();
                for (final FileCategories fileCategory : files.keySet()) {
                    for (final String file : files.get(fileCategory)) {
                        final File tmpFile = new File(file);
                        writer.write("<br/><b>" + StringHelper.getLastRight(tmpFile.getParentFile().getPath(), '/') + "/" + tmpFile.getName() + "</b><br/>");
                        writer.write("\n<pre><code>\n");
                        final File fileToInclude = new File(file);
                        final BufferedReader fileReader = new BufferedReader(new FileReader(fileToInclude), IOHelper.BUFFER_SIZE);
                        String line;
                        String previousLine = null;

                        while ((line = fileReader.readLine()) != null) {
                            if (!line.equals(previousLine)) {
                                line = line.replaceAll("&", "&amp;");
                                line = line.replaceAll("<", "&lt;");
                                line = line.replaceAll(">", "&gt;");
                                writer.write(line + "\n");
                            }
                            previousLine = line;
                        }
                        fileReader.close();
                        writer.write("\n</code></pre>\n");
                    }
                }
                writer.write("<br/> <center>END</center></MARQUEE>\n");
                writer.write("</BODY></HTML>");
                writer.flush();
                writer.close();
                endBlock("Writing Orbiter Engine power. Done.");
            }
        } catch (FileNotFoundException e) {
            error(e, false);
        } catch (IOException e) {
            error(e, false);
        } catch (IOHelperException e) {
            error(e, false);
        }
        info("-------------------------------------------");
        if (echo) {
            getLogger().errorReport();
        }

        endBlock("Generating Compilation report write file = " + writeFile + ", echo = " + echo + " : done.");
        emptyLine();
    }

    /**
     * Build business object from requirements
     */
    private void computeBusinessObject(final Application application, final boolean DBDebug) {
        emptyLine();
        beginBlock("Calculating BusinessObject ...");
        info("Will proceed with " + application.getScenariosSize() + " scenarios for building BusinessObject");

        BusinessObjectGenerator genericTargetGenerator = new BusinessObjectGenerator(application);

        genericTargetGenerator.buildBusinessObject();
        calculatingDBColumnName(application, DBDebug);

        application.buildJavaBusinessObjects();

        endBlock("Calculated BusinessObject : OK, " + application.getBusinessObjects().size() + " done.");
        emptyLine();
    }

    private void computeBusinessObjectRelation(final Application application) {
        emptyLine();
        beginBlock("Calculating BusinessObject relations ...");

        beginBlock("Choosing ManyToOne or ManyToMany for n-n relation to non atomic ...");
        int n = 0;
        for (final BusinessObject bo : application.getBusinessObjects()) {
            for (final BusinessObjectAttribute boa : bo.getAttributes()) {
                if (boa.isCollection() && !boa.isBuiltIn()) {
                    final BusinessObject targetObject = boa.getType().getBusinessObject();
                    int relationCountToTargetObject = getRelationCount(application, targetObject);
                    info("Relation count from " + bo.getName() + " to " + targetObject.getName() + " : " + relationCountToTargetObject);
                    n++;
                    if (relationCountToTargetObject > 1) {
                        boa.setToMany(true);
                    } else {
                        boa.setToMany(false);
                    }

                }
            }
        }
        endBlock("Choosing ManyToOne or ManyToMany for n-n relation to non atomic. Done for " + n + " relations");

        emptyLine();
        beginBlock("Computing bidirectional relation ...");
        final Set<BidirectionalRelation> bidirectionalRelations = CollectionFactory.newSetWithInsertionOrderPreserved();
        final Set<BidirectionalRelation> reflexiveRelations = CollectionFactory.newSetWithInsertionOrderPreserved();

        beginBlock("Step 1 : find bidirectional and reflexive relations ...");

        for (final BusinessObject bo : application.getBusinessObjects()) {
            for (final BusinessObjectAttribute boa : bo.getAttributes()) {
                if (!boa.isBuiltIn()) {
                    final BusinessObject targetObject = boa.getType().getBusinessObject();
                    for (final BusinessObjectAttribute targetBoa : targetObject.getAttributes()) {
                        if (!targetBoa.isBuiltIn() && targetBoa.getType().getBusinessObject().equals(bo)) {
                            final BidirectionalRelation bidirectionalRelation = new BidirectionalRelation(bo, boa, targetObject, targetBoa);
                            final BidirectionalRelation alreadyThereRelation = BidirectionalRelation.bidirectionalRelationAlreadyThere(bidirectionalRelations, bidirectionalRelation);
                            if (alreadyThereRelation == null) {
                                if (bo.getFullName().equals(targetObject.getFullName())) {
                                    debug("Found a new reflexive relation on " + bo.getName() + " " + boa.getKeyname());
                                    reflexiveRelations.add(bidirectionalRelation);
                                } else {
                                    debug("Found a new bidirectional relation from " + bo.getName() + " " + boa.getKeyname() + " to " + targetObject.getName());
                                    bidirectionalRelations.add(bidirectionalRelation);
                                }
                            } else {
                                if (bo.getFullName().equals(targetObject.getFullName())) {
                                    debug("Already found reflexive relation on " + bo.getName() + " " + boa.getKeyname());
                                } else {
                                    debug("Already found bidirectional relation from " + bo.getName() + " " + boa.getKeyname() + " to " + targetObject.getName());
                                    alreadyThereRelation.addBidirectionalRelation(bidirectionalRelation);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        endBlock("Step 1 : find bidirectional and reflexive relations. Done. Found " + bidirectionalRelations.size() + " bidirectional relations and " + reflexiveRelations.size() + " reflexive relations");

        if (reflexiveRelations.size() > 0) {
            compileWarning("Reflexive relation not done yet ! Found " + reflexiveRelations.size());
            beginBlock("Reflexive relation summary :", false);
            int i = 1;
            for (final BidirectionalRelation bidirectionalRelation : reflexiveRelations) {
                debug("   " + i + ". on " + bidirectionalRelation.getLeftObject().getName() + " " + bidirectionalRelation.getLeftAttribute().getKeyname());
                i++;
            }
            endBlock("Reflexive relation summary.", false);
        }

        if (bidirectionalRelations.size() > 0) {
            compileWarning("Bidirectional relation not done yet ! Found " + bidirectionalRelations.size());
            beginBlock("Bidirectional relation summary :", false);
            int i = 1;
            for (final BidirectionalRelation bidirectionalRelation : bidirectionalRelations) {
                debug("   " + i + ". from " + bidirectionalRelation.getLeftObject().getName() + " " + bidirectionalRelation.getLeftAttribute().getKeyname() + " -> " + bidirectionalRelation.getRightObject().getName() + " " + bidirectionalRelation.getRightAttribute().getKeyname());
                int j = 1;
                for (final BidirectionalRelation bidirectionalRelation2 : bidirectionalRelation.getBidirectionalRelations()) {
                    debug("      " + i + "." + j + ".  from " + bidirectionalRelation2.getLeftObject().getName() + " " + bidirectionalRelation2.getLeftAttribute().getKeyname() + " -> " + bidirectionalRelation2.getRightObject().getName() + " " + bidirectionalRelation2.getRightAttribute().getKeyname());
                    j++;
                }
                i++;
            }
            endBlock("Bidirectional relation summary.", false);
            emptyLine();
            beginBlock("Step 2 : bidirectional relations, find the most used direction ...", false);
//            for (final Scenario scenario : application.getScenarios()) {
//                final BusinessObject bo = scenario.getRootBusinesObject();
//                for (final Step step : scenario.getSteps()) {
//                    if (!step.getStepType().isAtomic()) {
//                        for (final BidirectionalRelation relation : bidirectionalRelations) {
//                            if (relation.getLeftObject().getFullName().equals(bo.getFullName())) {
//
//                            }
//                        }
//                    }
//                }
//            }

            endBlock("Step 2 : bidirectional relations, find the most used direction ...", false);
        }

        endBlock("Computing bidirectional relation. Done.");

        endBlock("Calculating BusinessObject relations. Done.");
        emptyLine();
    }

    private int getRelationCount(final Application application, final BusinessObject businessObject) {
        int result = 0;
        for (final BusinessObject bo : application.getBusinessObjects()) {
            for (final BusinessObjectAttribute boa : bo.getAttributes()) {
                if (boa.isCollection() && !boa.isBuiltIn()) {
                    final BusinessObject targetObject = boa.getType().getBusinessObject();
                    if (businessObject.equals(targetObject)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Build GUI generic definition from requirement and business objects.
     */
    private void computeUI(final Application application) {
        emptyLine();
        beginBlock("Calculating UI ...");

        UIGenerator generator = new UIGenerator();
        generator.buildUI(application);
        beginBlock("Computing minijumps...");
        for (final Scenario scenario : application.getScenarios()) {
            for (final Step step : scenario.getSteps()) {
                step.computeMiniJumps();
            }
        }
        endBlock("Computing minijumps... Done.");
        beginBlock("Field Check ...");
        for (final Screen screen : application.getScreens()) {
            for (final ScreenField field : screen.getFields()) {
                if (field.getName() == null || field.getName().length() == 0) {
                    compileError("Screen field doesn't have a name : [" + screen.getQxClassName() + "] " + screen.getName() + "." + field.getKeyname() + " This screen is related to " + screen.getBusinessObjectClass() + "." + (field.getAttribute() == null ? "null" : field.getAttribute().getKeyname()));
                }
            }
        }
        endBlock("Field Check ...");

        endBlock("Calculating UI : OK, " + application.getScreensSize() + " screens found.");
        emptyLine();
    }
}
