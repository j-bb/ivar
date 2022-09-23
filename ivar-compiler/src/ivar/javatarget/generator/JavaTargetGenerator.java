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

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import ivar.metamodel.target.java.JavaBusinessObject;
import java.util.Map;

// TODO Ajouter une methode check avec des exceptions pour la validite de chaque objet.
public class JavaTargetGenerator extends AbstractGenerator {

    public JavaTargetGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public JavaTargetGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    protected void doGenerate() {

        //public void generate(Application application, IvarcParameters ivarcParameters, String optionalSubFolder) {
        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();

        //public void generate(final CompilerVelocityEngine templateEngine, final String outputFolderPath, final Application application, final CompileParameters compileParams, final String uniqueDate, final String timeTrace, final String appTrace, final TargappConfig targappConfig) throws GeneratorException {
        beginBlock("Generating java files for " + application.getName());

        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();

//        final CompilerGenerationContext jboGenerationContext = new CompilerGenerationContext("java_business-object.java.vm", uniqueDate, timeTrace, appTrace);
//        final CompilerGenerationContext controllerGenerationContext = new CompilerGenerationContext("jpa_controller-java.vm", uniqueDate, timeTrace, appTrace);
        final Map<String, Object> contextsNoPool = newContexts("this", application);
        contextsNoPool.put("pool", false);

        final Map<String, Object> contextsWithPool = newContexts("this", application);
        contextsWithPool.put("pool", true);

        for (final JavaBusinessObject javaBusinessObject : application.getJavaBusinessObjects()) {
            String packageName = javaBusinessObject.getPackageName();
            packageName = packageName.replace('.', '/');

//            String outputAndPackageFoldername = outputFolderPath + "/" + packageName;
//            outputAndPackageFoldername = outputAndPackageFoldername.replace('\\', '/');
//            final File outputAndPackageFolder = new File(outputAndPackageFoldername);
//            if (!outputAndPackageFolder.exists()) {
//                try {
//                    if (!IOHelper.mkdirs(outputAndPackageFoldername)) {
//                        error("Unable to create `" + outputAndPackageFoldername + "`");
//                    } else {
//                        debug("Folder created " + outputAndPackageFolder);
//                    }
//                } catch (IOHelperException e) {
//                    error("Unable to create `" + outputAndPackageFoldername + "`", e);
//                }
//            }
            final String businessobjectFilename = javaBusinessObject.getName() + ".java";// + jboGenerationContext.getFileSuffix();
            final String controllerFilename = javaBusinessObject.getName() + "Controller.java";// + controllerGenerationContext.getFileSuffix();
//jboGenerationContext.setOutputFolder(outputAndPackageFoldername);
//controllerGenerationContext.setOutputFolder(outputAndPackageFoldername);
//jboGenerationContext.setOutputFileName(businessobjectFilename);
//controllerGenerationContext.setOutputFileName(controllerFilename);
//templateEngine.apply(, jboGenerationContext, FileCategories.server, application.getFileCounter(), compileParams);
            compilerVelocityEngine.generate("java_business-object.java.vm", packageName, businessobjectFilename, FileCategories.server, newContexts("this", javaBusinessObject), ivarcParameters);

//templateEngine.apply(, controllerGenerationContext, FileCategories.server, application.getFileCounter(), compileParams);
            compilerVelocityEngine.generate("jpa_controller-java.vm", packageName, controllerFilename, FileCategories.server, newContexts("javaBusinessObject", javaBusinessObject), ivarcParameters);
        }

//final String metainfFolder = outputFolderPath + "/META-INF";
//try {
//IOHelper.mkdirs(metainfFolder);
//} catch (IOHelperException e) {
//error("Unable to create directory " + metainfFolder, e);
//}
//contextsWithPool.put("targappConfig", targappConfig);
//contextsNoPool.put("targappConfig", targappConfig);
//CompilerGenerationContext generationContext = new CompilerGenerationContext(metainfFolder, "persistence.xml", "jpa_persistence-xml.vm", uniqueDate, timeTrace, appTrace);
//templateEngine.apply(contextsWithPool, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("jpa_persistence-xml.vm", "META-INF", "persistence.xml", FileCategories.server, contextsWithPool, ivarcParameters);

//generationContext = new CompilerGenerationContext(outputFolderPath, "persistence-no-pool.xml", "jpa_persistence-xml.vm", uniqueDate, timeTrace, appTrace);
//templateEngine.apply(contextsNoPool, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("jpa_persistence-xml.vm", "META-INF", "persistence-no-pool.xml", FileCategories.server, contextsNoPool, ivarcParameters);

        endBlock("Generating java files for " + application.getName() + ". Done.");
    }
}
