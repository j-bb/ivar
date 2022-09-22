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
package ivar.target.j2ee;

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;

public class JEEGenerator extends AbstractGenerator {

    public JEEGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public JEEGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {
        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();

        //public void generate(final CompilerVelocityEngine templateengine, final String outputFolderPath, final Application application, final CompileParameters compileParams, final String uniqueDate, final String timeTrace, final String appTrace) throws GeneratorException {
        beginBlock("Generating JEE files for " + application.getName());
//        debug("Current folder is " + new File(outputFolderPath).getAbsolutePath());
        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();
//CompilerGenerationContext routerServletContext = new CompilerGenerationContext(outputFolderPath, "web.xml", "/common/jee_web.xml.vm", uniqueDate, timeTrace, appTrace);
//templateengine.apply(newContexts("this", application), routerServletContext, FileCategories.client, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("/common/jee_web.xml.vm", "web.xml", FileCategories.server, newContexts("this", application), ivarcParameters);
        endBlock("Generating JEE files for " + application.getName() + " done.");
    }
}
