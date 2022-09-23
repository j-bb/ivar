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
package ivar.generator.velocity;

import ivar.common.velocity.AbstractVelocityEngine;
import ivar.generator.GeneratorConstants;
import ivar.helper.filecounter.FileCategories;
import ivar.helper.io.FileHelper;
import ivar.helper.io.IOHelper;
import ivar.helper.io.IOHelperException;
import ivar.ivarc.IvarcParameters;
import java.io.*;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class CompilerVelocityEngine extends AbstractVelocityEngine {

    private static final GeneratorConstants generatorConstants = new GeneratorConstants();

    public CompilerVelocityEngine(final String velocityTemplateFolderPath) {
        super(velocityTemplateFolderPath);
    }

    public void generate(final String templateName, final String outputFilename, final FileCategories fileCategory, final Map<String, Object> optionalContext, final IvarcParameters ivarcParameters) {
        generate(templateName, null, outputFilename, fileCategory, optionalContext, ivarcParameters);
    }

    public void generate(final String templateName, final String optionalSubfolder, final String outputFilename, final FileCategories fileCategory, final Map<String, Object> optionalContext, final IvarcParameters ivarcParameters) {
        if (templateName.startsWith(File.separator) || templateName.startsWith("/") || templateName.startsWith("\\")) {
            throw new RuntimeException("CompiletVelocityEngine.generate: template " + templateName + "name has bad leading slash, remove it ! this is a DEV time bug to correct right now. Follow the white rabbit ...");
        }
        //final String templateName = generationContext.getTemplateName();
        String outputFolderName = ivarcParameters.getCompileRootFolder();
        if (optionalSubfolder != null && optionalSubfolder.length() > 0) {
            if (optionalSubfolder.startsWith(File.separator) || optionalSubfolder.startsWith("/") || optionalSubfolder.startsWith("\\")) {
                throw new RuntimeException("CompiletVelocityEngine.generate: template " + optionalSubfolder + "name has bad leading slash, remove it ! this is a DEV time bug to correct right now. Follow the white rabbit ...");
            }
            outputFolderName += File.separator + optionalSubfolder;
        }
        try {
            FileHelper.ensurePath(outputFolderName);
        } catch (IOException ex) {

        }
        final String cartridge = ivarcParameters.getCartridge();

        emptyLine();
        beginBlock("Generating " + outputFilename + " with " + cartridge + "/" + templateName + " debug = " + ivarcParameters.isDebug());
        info("Output folder is " + outputFolderName);

        // 2020, why is outputFolderName would be null ? Not good with encureFolderCreated().
        // TODO investigate.
        String completeOutputFileName = null;
        if (outputFolderName != null && outputFolderName.length() > 0) {
            completeOutputFileName = outputFolderName + File.separator + outputFilename;
        } else {
            completeOutputFileName = outputFilename;
        }

        String status = "OK";
        status = initEngine(status);
        if (velocityEngine != null) {
            org.apache.velocity.Template template = getTemplate(velocityEngine, cartridge, templateName);
            if (template == null) {
                error("Unable to get the template " + templateName, true);
                status = "ERROR";
            } else {
                debug("Velocity generation for " + completeOutputFileName);
                BufferedWriter writer = null;

                try {
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(completeOutputFileName), "UTF-8"), BUFFER_SIZE);
                    } catch (IOException e2) {
                        error("Error while trying to instanciate a new velocity writer for " + completeOutputFileName, e2, true);
                        status = "ERROR";
                    }

                    final VelocityContext velocityContext = new VelocityContext();
                    // Add better error handling message.
                    velocityContext.attachEventCartridge(getEventCartridge());
                    velocityContext.put("log", getLogger());
                    velocityContext.put("ivarc", ivarcParameters);
                    velocityContext.put("genetator", generatorConstants);

                    if (optionalContext != null && !optionalContext.isEmpty()) {
                        for (final String key : optionalContext.keySet()) {
                            if (velocityContext.get(key) != null) {
                                error("Error while adding optionalContext to the final context, key already exists. " + key + ". Remove this key from optionalContext.", true);
                                status = "ERROR";
                            } else {
                                velocityContext.put(key, optionalContext.get(key));
                            }
                        }
                    }

                    final Object[] keys = velocityContext.getKeys();
                    for (final Object key : keys) {
                        debug("Velocity context key $" + key + " (" + velocityContext.get(key.toString()).getClass().getName() + ")");
                    }

                    try {
                        template.touch();
                        template.merge(velocityContext, writer);
                        writer.flush();

                        ivarcParameters.addFileForMetric(completeOutputFileName, fileCategory);
                    } catch (ResourceNotFoundException e3) {
                        error("TemplateEngine not found", e3, true);
                        status = "ERROR";
                    } catch (ParseErrorException e3) {
                        error("TemplateEngine parsing error", e3, true);
                        status = "ERROR";
                    } catch (MethodInvocationException e3) {
                        error("TemplateEngine method invocation error", e3, true);
                        status = "ERROR";
                    } catch (Throwable t) {
                        error(t, true);
                        status = "ERROR";
                    }
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e4) {
                        error("Error while trying to close Velocity writer", e4, false);
                        status = "ERROR";
                    } finally {
                        writer = null;
                        template = null;
                    }
                }
            }
        }
        endBlock("Generation from [" + templateName + "] done " + status + " for " + completeOutputFileName);
    }

    public void copyFile(final String source, final String destination, final FileCategories fileCategories, IvarcParameters ivarcParameters) {
        try {
            IOHelper.writeFileToLocation(new File(source), destination);
            ivarcParameters.addFileForMetric(destination, FileCategories.tooling);
        } catch (IOHelperException e) {
            error("Could not copy " + destination + " from " + source, e, true);
        }
    }
}
