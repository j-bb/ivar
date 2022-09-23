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
package ivar.target.doc;

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;

public class AdminSummaryGenerator extends AbstractGenerator {

    public AdminSummaryGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public AdminSummaryGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {
        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();

        beginBlock("Generating admin summary file for " + application.getName());
        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();
        if (application.hasPublicScreen()) {
            compilerVelocityEngine.generate("doc_summary-txt.vm", "", "public-screen-summary.txt", FileCategories.documentation, newContexts("this", application), ivarcParameters);
        } else {
            info("no summary to generate since there is no public screen.");
        }
        endBlock();
    }
}
