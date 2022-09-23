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
package ivar.javatarget.generator;

import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.Scenario;
import ivar.metamodel.spec.Step;
import ivar.metamodel.target.generic.ui.Screen;
import ivar.metamodel.target.generic.ui.ScreenField;
import java.util.Map;
import java.util.Set;

public class JavaAuthentication {

    protected String rootPackage = null;
    private Application application;
    private Set<String> securityStrings;

    public JavaAuthentication(final Application application) {
        this.application = application;
        rootPackage = application.getKeyname().toLowerCase();
        // TODO: add application name at the beginning of the servlet classname and do the same in web-xml.vm
        securityStrings = computeSecurityStrings();
    }

    public String getPackageName() {
        return StringHelper.getStringForJavaPackage(rootPackage);
    }

    public Set<String> getRoles() {
        Map<String, Object> roles = CollectionFactory.newMap();
        final Set<Scenario> scenarios = application.getScenarios();
        if (scenarios != null) {
            for (final Scenario scenario : scenarios) {
                if (scenario.getRoles() != null) {
                    for (final String role : scenario.getRoles()) {
                        roles.put(role, null);
//                        debug("Scenario " + scenario.getKeyname() + " " + scenario.isRolesAllowed() + " " + role);
                    }
                }
                for (final Step step : scenario.getSteps()) {
                    if (step.getRoles() != null) {
                        for (final String role : step.getRoles()) {
                            roles.put(role, null);
//                            debug("Step " + step.getKeyname() + " " + step.isRolesAllowed() + " " + role);
                        }
                    }
                }
            }
        }
        return roles.keySet();
    }

    public Set<String> computeSecurityStrings() {
        final Set<String> securityStrings = CollectionFactory.newSet();
        for (final Screen screen : application.getScreens()) {
            if (screen.isPublic()) {
                final String screenId = screen.getUniqueID();
                if (screen.isCreate()) {
                    // this.send("Saving new ${this.getBusinessObjectName()} ...", {svc:"${application.getKeyname().toLowerCase()}.${this.getBusinessObjectNameFirstCapitalized()}sController", m:"add", p:[_N_${this.getBusinessObjectName()}]},
                    String svc = application.getKeyname().toLowerCase() + "." + screen.getBusinessObjectNameFirstCapitalized() + "Controller";
                    String method = "add";
                    securityStrings.add(screenId + "." + svc + "." + method);

                    method = "getAll";
                    for (final ScreenField field : screen.getFields()) {
                        if (!field.isBuiltIn()) {
                            if (field.isScreenReference()) {
                                // this.send("Loading ${field.getAttribute().getNameFirstCapitalized()} ...", {svc:"${application.getKeyname().toLowerCase()}.${field.getAttribute().getNameFirstCapitalized()}sController", m:"getAll", p:null},
                                svc = application.getKeyname().toLowerCase() + "." + field.getAttribute().getNameFirstCapitalized() + "Controller";
                                securityStrings.add(screenId + "." + svc + "." + method);
                            } else {

                                // this.send("Loading ${field.getType().toLowerCase()} ...", {svc:"${application.getKeyname().toLowerCase()}.${field.getTypeFirstCapitalized()}sController", m:"getAll", p:null},                                // "${application.getKeyname().toLowerCase()}.${field.getTypeFirstCapitalized()}sController"
                                svc = application.getKeyname().toLowerCase() + "." + field.getTypeFirstCapitalized() + "Controller";
                                securityStrings.add(screenId + "." + svc + "." + method);
                            }
                        }
                    }
                } else if (screen.isRead()) {
                    String method = "getAll";
                    for (ScreenField field : screen.getFields()) {
                        if (!field.isBuiltIn()) {
                            if (field.isScreenReference()) {
                                // this.send("Loading ${field.getAttribute().getNameFirstCapitalized()} ...", {svc:"${application.getKeyname().toLowerCase()}.${field.getAttribute().getNameFirstCapitalized()}sController", m:"getAll", p:null},
                                final String svc = application.getKeyname().toLowerCase() + "." + field.getAttribute().getNameFirstCapitalized() + "Controller";
                                securityStrings.add(screenId + "." + svc + "." + method);
                            } else {
                                // this.send("Loading ${field.getType().toLowerCase()} ...", {svc:"${application.getKeyname().toLowerCase()}.${field.getTypeFirstCapitalized()}sController", m:"getAll", p:null},
                                final String svc = application.getKeyname().toLowerCase() + "." + field.getTypeFirstCapitalized() + "Controller";
                                securityStrings.add(screenId + "." + svc + "." + method);
                            }
                        }
                    }
                } else if (screen.isSearch()) {
                    // this.send("${this.getDynamicFilterName()} ...", {svc:"${application.getKeyname().toLowerCase()}.${this.getBusinessObjectNameFirstCapitalized()}sController", m:"${this.getServerMethodName()}", p:params},
                    final String method = screen.getPrepareCall().getServerMethodName();
                    final String svc = application.getKeyname().toLowerCase() + "." + screen.getBusinessObjectNameFirstCapitalized() + "Controller";
                    securityStrings.add(screenId + "." + svc + "." + method);
                }
            }
        }
        return securityStrings;
    }

    public boolean hasPublicScreen() {
        return application.hasPublicScreen();
    }

    public Set<String> getSecurityStrings() {
        return securityStrings;
    }
}
