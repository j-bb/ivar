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
package ivar.target.ant;

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.CollectionFactory;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import java.io.File;
import java.util.Map;

public class AntGenerator extends AbstractGenerator {

    public AntGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public AntGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {
        final IvarcParameters ivarcParameters = getIvarcParameters();
        final Application application = getApplication();
        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();
        final String outputFolder = getGeneratorFolder();

        beginBlock("Generating ant build files for " + application.getName());
        debug("Current folder is " + new File(outputFolder).getAbsolutePath());

        final Map<String, Object> optionalContext = CollectionFactory.newMap();
        optionalContext.put("application", application);

        // Main ant file
        compilerVelocityEngine.generate("ant_build-xml.vm", "", "build.xml", FileCategories.tooling, optionalContext, ivarcParameters);

        // Properties ant file
        compilerVelocityEngine.generate("ant_build-properties-xml.vm", "", application.getKeyname() + "-build-properties.xml", FileCategories.tooling, optionalContext, ivarcParameters);

        // Database ant file
        compilerVelocityEngine.generate("ant_build-database-xml.vm", "", application.getKeyname() + "-build-database.xml", FileCategories.tooling, optionalContext, ivarcParameters);

        // Database ant file
        compilerVelocityEngine.generate("ant_build-package-xml.vm", "", application.getKeyname() + "-build-package.xml", FileCategories.tooling, optionalContext, ivarcParameters);

        // Help ant file
        compilerVelocityEngine.generate("ant_build-help-xml.vm", "", "build-help.xml", FileCategories.tooling, optionalContext, ivarcParameters);
        compilerVelocityEngine.copyFile(compilerVelocityEngine.getTemplateFolder() + File.separator + "java" + File.separator + "ant-build-master.xml", ivarcParameters.getCompileRootFolder() + File.separator + "build-master.xml", FileCategories.tooling, ivarcParameters);
        compilerVelocityEngine.generate("ant_wrapper-sh.vm", "", "ant-launcher.sh", FileCategories.tooling, optionalContext, ivarcParameters);

//        if (application.hasInjectedFiles()) {
//            compilerVelocityEngine.copyFile(compilerVelocityEngine.getTemplateFolder() + File.separator + "java" + File.separator + "data-injector.xml", ivarcParameters.getCompileRootFolder() + File.separator + "data-injector.xml", FileCategories.tooling, ivarcParameters);
//        }
        endBlock("Generating ant build files for " + application.getName() + ". Done.");

        beginBlock("Generating unix shell script for " + application.getName());
        compilerVelocityEngine.generate("unix_novbox-zero-local.sh.vm", "", "gotocloud.sh", FileCategories.tooling, optionalContext, ivarcParameters);
        compilerVelocityEngine.generate("unix_novbox-zero-remote.sh.vm", "", "remote-setup.sh", FileCategories.tooling, optionalContext, ivarcParameters);
        compilerVelocityEngine.generate("unix_novbox-zero-remote-iptables.sh.vm", "", "remote-iptables.sh", FileCategories.tooling, optionalContext, ivarcParameters);
        endBlock("Generating unix shell script for " + application.getName() + ". Done.");
    }
}
