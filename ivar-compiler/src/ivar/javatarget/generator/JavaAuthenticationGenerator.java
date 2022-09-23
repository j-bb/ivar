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
import java.util.Map;

public class JavaAuthenticationGenerator extends AbstractGenerator {

    public JavaAuthenticationGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public JavaAuthenticationGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {
        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();
        //public void generate(Application application, IvarcParameters ivarcParameters, String optionalSubFolder) {
        //public void generate(final CompilerVelocityEngine templateEngine, final Application application, final String outputFolder, final CompileParameters compileParams, final String uniqueDate, final String appTrace, final String timeTrace) throws GeneratorException {
        beginBlock("Generating authentication files for " + application.getName());
//        debug("Current folder is " + new File(ivarcParameters).getAbsolutePath());

        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();

        final JavaAuthentication javaAuthentication = new JavaAuthentication(application);
        Map<String, Object> context = newContexts("this", javaAuthentication);
        context.put("app", application);

//        final String baseFolderName = outputFolder + "/" + javaAuthentication.getPackageName();
//        final String authOutputAndPackageFoldername = baseFolderName + "/auth";
//        final String dataconfigOutputAndPackageFoldername = baseFolderName + "/dataconfig";
//        final String rpcOutputAndPackageFoldername = baseFolderName + "/rpc";
//CompilerGenerationContext generationContext = new CompilerGenerationContext(authOutputAndPackageFoldername, , , uniqueDate, timeTrace, appTrace);
//templateEngine.apply(context, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("java_authentication-filter.vm", "auth", "AuthenticationFilter.java", FileCategories.server, context, ivarcParameters);

//        generationContext = new CompilerGenerationContext(authOutputAndPackageFoldername, ,, uniqueDate, timeTrace, appTrace);
//        templateEngine.apply(context, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("java_authentication-servlet.vm", "auth", "AuthenticationServlet.java", FileCategories.server, context, ivarcParameters);

//generationContext = new CompilerGenerationContext(authOutputAndPackageFoldername, , , uniqueDate, timeTrace, appTrace);
//templateEngine.apply(context, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("java_authentication-result.vm", "auth", "AuthenticationResult.java", FileCategories.server, context, ivarcParameters);

//generationContext = new CompilerGenerationContext(dataconfigOutputAndPackageFoldername, , , uniqueDate, timeTrace, appTrace);
//templateEngine.apply(context, generationContext, FileCategories.tooling, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("java_data-injector.java.vm", "dataconfig", "DataInjector.java", FileCategories.tooling, context, ivarcParameters);

        beginBlock("Generating RPC files for " + application.getName() + " ...");
//generationContext = new CompilerGenerationContext(rpcOutputAndPackageFoldername, , , uniqueDate, timeTrace, appTrace);
//templateEngine.apply(context, generationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("java_rpc-servlet.java.vm", "rpc", "JavaRpcServlet.java", FileCategories.server, context, ivarcParameters);
        endBlock("Generating RPC files for " + application.getName() + ". Done.");

        // Generation of a special controller to update preferences
        // TODO: this should be temporary
        //final String outputFolder = outputFolderPath + "/" + application.getKeyname();
//final CompilerGenerationContext prefControllerGenerationContext = new CompilerGenerationContext(, uniqueDate, timeTrace, appTrace);
//prefControllerGenerationContext.setOutputFolder(outputFolder + "/" + javaAuthentication.getPackageName());
//prefControllerGenerationContext.setOutputFileName();
//templateEngine.apply(, prefControllerGenerationContext, FileCategories.server, application.getFileCounter(), compileParams);
        compilerVelocityEngine.generate("jpa_update-pref-controller-java.vm", javaAuthentication.getPackageName(), "UpdateAppUserPreferenceController.java", FileCategories.server, newContexts("this", javaAuthentication), ivarcParameters);
        // END special controller

        endBlock("Generating authentication files for " + application.getName() + " done.");
    }
}
