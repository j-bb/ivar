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
package ivar.metamodel.spec;

import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.MetaObject;
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.ui.Menu;
import ivar.metamodel.target.generic.ui.Screen;
import ivar.metamodel.target.java.JavaBusinessAttribute;
import ivar.metamodel.target.java.JavaBusinessObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Application extends MetaObject {

    private Date lastCompile;
    private String lastCompileType;
    private String version = "not set";
    private String about;
    private ApplicationStates state = ApplicationStates.empty;
    private Languages lang = Languages.en;

    private transient Set<BusinessObject> businessObjects = CollectionFactory.newSetWithInsertionOrderPreserved();
    private transient Set<Screen> screens = CollectionFactory.newSetWithInsertionOrderPreserved();
    private transient Set<JavaBusinessObject> javabusinessObjects = CollectionFactory.newSetWithInsertionOrderPreserved();
    private List<CompileLog> compileLogs = CollectionFactory.newList();
    private Set<Scenario> scenarios = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Set<Dataset> dataSets = CollectionFactory.newSetWithInsertionOrderPreserved();
    private transient List<String> errors;
    private transient String folder;
    private transient String url;
    private transient Menu rootMenu = null;

    public Application() {
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLang() {
        return lang.toString();
    }

    public void setLang(String lang) {
        lang = lang.replace("-", "_");
        this.lang = Languages.valueOf(lang);
    }

    public ApplicationStates getState() {
        return state;
    }

    public void setState(final ApplicationStates state) {
        this.state = state;
    }

    public Set<BusinessObject> getBusinessObjects() {
        return businessObjects;
    }

    public Set<JavaBusinessObject> getJavaBusinessObjects() {
        return javabusinessObjects;
    }

    public void addbusinessObject(final BusinessObject businessObject) {
        businessObjects.add(businessObject);
    }

    public Set<Screen> getScreens() {
        return screens;
    }

    public void addScreen(final Screen screen) {
        screens.add(screen);
    }

    public int getScreensSize() {
        return screens.size();
    }

    public Set<Scenario> getScenarios() {
        return scenarios;
    }

    public void addScenario(final Scenario scenario) throws DuplicateScenarioException {
        if (scenario == null) {
            throw new NullPointerException("Scenario is null !");
        }
        for (final Scenario existingScenario : scenarios) {
            //if (scenario.getKeyname().equals(existingScenario.getKeyname())) {
            if (scenario.equals(existingScenario)) {
                throw new DuplicateScenarioException(existingScenario, scenario);
            }
        }
        scenarios.add(scenario);
    }

    public void addOrReplaceScenario(final Scenario scenario) throws DuplicateScenarioException {
        final List<Scenario> toDelete = CollectionFactory.newList();

        for (Scenario existingScenario : scenarios) {
            if (scenario.getKeyname().equals(existingScenario.getKeyname())) {
                debug("addOrReplaceScenario() : Scenario already exists, remove it fo replacement : " + existingScenario.getKeyname());
                toDelete.add(existingScenario);
            }
        }
        scenarios.removeAll(toDelete);
        addScenario(scenario);
    }

    public int getScenariosSize() {
        final Set<Scenario> ses = getScenarios();
        return ses != null ? ses.size() : 0;
    }

    public Scenario getScenarioForKeyname(final String keyname) {
        Scenario result = null;
        for (final Scenario scenario : scenarios) {
            if (keyname.equals(scenario.getKeyname())) {
                result = scenario;
                break;
            }
        }
        return result;
    }

    public boolean isOk() {
        return errors != null && errors.size() == 0;
    }

    public Set<JavaBusinessObject> getJavabusinessObjects() {
        return javabusinessObjects;
    }

    public JavaBusinessObject getJavaBusinessObject(final String typename) {
        JavaBusinessObject result = null;
//        debug("@@@@@@@ searching JBO with typename: " + typename);
        for (final JavaBusinessObject javaBusinessObject : javabusinessObjects) {
//            debug("@@@@@@@ JBO typename: " + javaBusinessObject.getName());
            if (javaBusinessObject.getName().equals(typename)) {
                if (result == null) {
                    result = javaBusinessObject;
                } else {
                    // TODO: remove following line ASAP
                    result = javaBusinessObject;

                    compileError("There is more then one JavaBusinessObject with the type " + typename + " in application " + getKeyname());
                    beginCompileHelpBlock("For helping purpose, here are all the java business object available.");

                    final List<JavaBusinessObject> sortedJBO = CollectionFactory.newList(javabusinessObjects.size());
                    sortedJBO.addAll(javabusinessObjects);
                    Collections.sort(sortedJBO);
                    for (final JavaBusinessObject jbo : sortedJBO) {
                        final Collection<JavaBusinessAttribute> attributes = jbo.getAttributes().values();
                        final List<JavaBusinessAttribute> sortedJBA = CollectionFactory.newList(attributes.size());
                        sortedJBA.addAll(attributes);
                        Collections.sort(sortedJBA);
                        emptyLine();
                        compileHelp("public class " + jbo.getName() + " {");
                        for (final JavaBusinessAttribute attribute : sortedJBA) {
                            compileHelp("     " + attribute.getType() + " " + attribute.getKeyname());
                        }
                        compileHelp("}");
                    }

                    endCompileHelpBlock();

                    //throw new IllegalStateException("There is more then one JavaBusinessObject with the type " + typename + " in application " + getKeyname());
                }
            }
        }
        return result;
    }

    public void addDataSet(final Dataset dataSet) throws DuplicateDatasetException {
        for (Dataset existingDataSet : dataSets) {
            if (dataSet.getKeyname().equals(existingDataSet.getKeyname())) {
                throw new DuplicateDatasetException(existingDataSet, dataSet);
            }
        }
        dataSets.add(dataSet);
    }

    public void addOrReplaceDataSet(final Dataset dataSet) throws DuplicateDatasetException {
        final List<Dataset> toDelete = CollectionFactory.newList();
        for (Dataset existingDataSet : dataSets) {
            if (dataSet.getKeyname().equals(existingDataSet.getKeyname())) {
                debug("addOrReplaceDataSet() : Dataset already exists, remove it fo replacement : " + existingDataSet.getKeyname());
                toDelete.add(existingDataSet);
            }
        }
        dataSets.removeAll(toDelete);
        addDataSet(dataSet);
    }

    public Set<Dataset> getDataSet() {
        return dataSets;
    }

    public String getDBUser() {
        return getKeyname().substring(0, Math.min(15, getKeyname().length()));
    }

    public String getDBPass() {
        return getKeyname().substring(0, Math.min(15, getKeyname().length()));
    }

//    public FileCounter getFileCounter() {
//        return fileCounter;
//    }
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastCompile() {
        return lastCompile;
    }

    public void setLastCompileNow(final Date uniqueCompileDate) {
        this.lastCompile = uniqueCompileDate;
    }

    public Menu getRootMenu() {
        return rootMenu;
    }

    public void setRootMenu(final Menu menu) {
        rootMenu = menu;
    }

    public Application getJPAClone() {
        return getJPAClone(new Application());
    }

    protected Application getJPAClone(Application dst) {
        super.getJPAClone(dst);

        if (lastCompile != null) {
            dst.lastCompile = (Date) lastCompile.clone();
        } else {
            lastCompile = null;
        }

        if (version != null) {
            dst.version = new String(version);
        } else {
            dst.version = null;
        }

        dst.state = state;
        dst.lang = lang;

        if (scenarios == null) {
            dst.scenarios = null;
        } else {
            dst.getScenarios().clear();
            for (final Scenario scenario : scenarios) {
                dst.scenarios.add(scenario.getJPAClone());
            }
        }

        if (dataSets == null) {
            dst.dataSets = null;
        } else {
            dst.getDataSet().clear();
            for (final Dataset dataSet : dataSets) {
                dst.dataSets.add(dataSet.getJPAClone());
            }
        }

        return dst;
    }

    public String getLastCompileType() {
        return lastCompileType;
    }

    public void setLastCompileType(String lastCompileType) {
        this.lastCompileType = lastCompileType;
    }

//        beginBlock("Calls collection");
//        Map<String, Object> jumps = CollectionFactory.newMap();
//        if (scenarios != null) {
//            for (final Scenario scenario : scenarios) {
//                if (scenario.getJumps() != null) {
//                    for (final String jump : scenario.getJumps()) {
//                        jumps.put(jump, null);
////                        debug("Scenario " + scenario.getKeyname() + " " + scenario.isRolesAllowed() + " " + jump);
//                    }
//                }
//                for (final Step step : scenario.getSteps()) {
//                    if (step.getJumps() != null) {
//                        for (final String jump : step.getJumps()) {
//                            jumps.put(jump, null);
////                            debug("Step " + step.getKeyname() + " " + step.isRolesAllowed() + " " + jump);
//                        }
//                    }
//                }
//            }
//        }
////        endBlock("Calls collection");
//        return jumps.keySet();
//    }
    @Override
    public String toString() {
        String result = "Application{"
                + " lastCompile=" + lastCompile
                + ", version=" + version
                + ", state=" + state;
        if (getScenarios() != null) {
            result += ", scenarios=" + scenarios.size() + "\n";

            for (final Scenario scenario : scenarios) {
                result += "          " + scenario.toString();
            }
        } else {
            result += ", scenarios= 0\n";
        }

        result
                += /*", parentApplicaitons=" + parentApplicaitons +*/ //                ", folder='" + folder + '\''
                +'}';
        result = super.toString() + " " + result;
        return result;
    }

    public void buildJavaBusinessObjects() {
        beginBlock("Building Java Business Object from Business Object for application " + getName());
        info("BO number " + businessObjects.size());
        emptyLine();
        JavaBusinessObject jbo = null;
        for (final BusinessObject businessObject : businessObjects) {
            jbo = new JavaBusinessObject(businessObject);
            javabusinessObjects.add(jbo);
        }
        endBlock("Building Java Business Object from Business Object. Done.");

        // Step 2
        beginBlock("Computing types of Java Business Attributes for each Java Business Object of application " + getName());

        // TODO
        for (final JavaBusinessObject javaBusinessObject : javabusinessObjects) {
            for (final JavaBusinessAttribute attribute : javaBusinessObject.getAttributes().values()) {
                if (!attribute.isJavaBuiltIn()) {
                    final JavaBusinessObject typeAsJavaBusinessObject = getJavaBusinessObject(attribute.getType());
                    if (typeAsJavaBusinessObject != null) {
                        attribute.setTypeAsJavaBusinessObject(typeAsJavaBusinessObject);
                    } else {
                        compileError("Unable to find a type for the non built-in attribute " + attribute.getKeyname() + " in java business object " + javaBusinessObject.getName());
                        beginCompileHelpBlock("For helping purpose, here are all the java business object types available.");
                        for (JavaBusinessObject javaObject : javabusinessObjects) {
                            compileHelp("    " + javaObject.getName());
                        }
                        endCompileHelpBlock();
                    }
                }
            }
        }
        endBlock("Computing types of Java Business Attributes for each Java Business Object. Done.");
    }

    public void computeURL(String clientURL, final String port) {
        URL url = null;
        try {
            url = new URL(clientURL);
        } catch (MalformedURLException e) {
            error("Application.computeURL() : bad URL format for " + clientURL, e);
        }
        /* What could be a better way to do this?
           /(\s*)https?://[a-zA-Z0-9.]+(:[0-9]+)?(/[^/]*)*(\s*)/ ? */
        if (url != null) {
            final boolean secure = clientURL.startsWith("https://");
            String newClientURL = url.getHost();
            newClientURL.replaceAll("https?://", "");
            if ("80".equals(port)) {
                newClientURL = "http://" + newClientURL;
            } else if ("443".equals(port)) {
                newClientURL = "https://" + newClientURL;
            } else {
                newClientURL = StringHelper.concat("http", (secure ? "s://" : "://"), newClientURL, ":", port);
            }
            newClientURL += "/" + keyname;

            if ("debug".equals(getLastCompileType())) {
                newClientURL += "/" + keyname + "/source";
            }
            this.url = newClientURL;
        }
    }

    public boolean hasPublicScreen() {
        boolean result = false;
        for (final Screen screen : screens) {
            if (screen.isPublic()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasScenario(String keyname) {
        boolean result = false;
        if (keyname != null) {
            for (final Scenario scenario : scenarios) {
                if (scenario.getKeyname().equals(keyname)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public boolean hasFileUploads() {
        boolean result = false;
        for (final Scenario scenario : scenarios) {
            for (final Step step : scenario.getSteps()) {
                if (step.getStepConstraint().isFile()) {
                    result = true;
                    break;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }

    public void addCompileLog(final CompileLog log) {
        compileLogs.add(log);
    }

    public void free() {
        beginBlock("Freeing application " + getKeyname());
        for (final Scenario scenario : scenarios) {
            scenario.free();
        }
        scenarios.clear();
        scenarios = null;

//        fileCounter.free();
        for (final BusinessObject businessObject : businessObjects) {
            businessObject.free();
        }
        businessObjects.clear();
        businessObjects = null;

        for (final Screen screen : screens) {
            screen.free();
        }
        screens.clear();
        screens = null;

        for (JavaBusinessObject javaBusinessObject : javabusinessObjects) {
            javaBusinessObject.free();
        }
        javabusinessObjects.clear();
        javabusinessObjects = null;
        endBlock("Freeing application " + getKeyname() + ". Done.");
    }

    public String getAbout() {
        return about;
    }

    public boolean isAbout() {
        return about != null && about.length() > 1;
    }
}
