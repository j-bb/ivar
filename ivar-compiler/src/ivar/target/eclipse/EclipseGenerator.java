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
package ivar.target.eclipse;

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import java.io.File;
import java.util.Map;

public class EclipseGenerator extends AbstractGenerator {

    public EclipseGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public EclipseGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {

        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();

        beginBlock("Generating Eclipse config files for " + application.getName());
        debug("Current folder is " + new File(ivarcParameters.getCompileRootFolder()).getAbsolutePath());

        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();

        Map<String, Object> contexts = CollectionFactory.newMap();
        contexts.put("application", application);
        contexts.put("projectFolderName", StringHelper.getLastRight(ivarcParameters.getCompileRootFolder(), '/'));

        compilerVelocityEngine.generate("eclipse_classpath.vm", ".classpath", FileCategories.tooling, contexts, ivarcParameters);

        compilerVelocityEngine.generate("eclipse_project.vm", ".project", FileCategories.tooling, contexts, ivarcParameters);

        compilerVelocityEngine.generate("eclipse_facet.vm", ".settings", "org.eclipse.wst.common.project.facet.core.xml", FileCategories.tooling, contexts, ivarcParameters);

        compilerVelocityEngine.generate("eclipse_component.vm", ".settings", "org.eclipse.wst.common.component", FileCategories.tooling, contexts, ivarcParameters);

        compilerVelocityEngine.generate("eclipse_pref.vm", ".settings", "org.eclipse.jdt.core.prefs", FileCategories.tooling, contexts, ivarcParameters);

        compilerVelocityEngine.generate("eclipse_scope.vm", ".settings", ".jsdtscope", FileCategories.tooling, contexts, ivarcParameters);

        endBlock("Generating Eclipse config files for " + application.getName() + " done.");
    }
}
