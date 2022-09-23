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
package ivar.common.velocity;

import ivar.common.AbstractObject;
import ivar.helper.oo.ClassHelper;
import java.io.File;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.util.introspection.Info;

public abstract class AbstractVelocityEngine extends AbstractObject {

    // Can be null -> classpath loader.
    // Cannot be removed because of FusionTemplate.
    protected final String velocityTemplateFolder;
    public static final int BUFFER_SIZE = 10000;
    protected VelocityEngine velocityEngine;
    //protected EventCartridge eventCartidge;

    public AbstractVelocityEngine(final String velocityTemplateFolderPath) {
        this.velocityTemplateFolder = velocityTemplateFolderPath;
        if (null == velocityTemplateFolderPath || velocityTemplateFolderPath.length() == 0) {
            final String message = "[AbstractTemplateEngine] try to instanciate with a null velocityTemplateFolderPath";
            error(message, true);
            throw new RuntimeException(message);
        }
        final File templateFolder = new File(velocityTemplateFolderPath);
        if (!templateFolder.exists()) {
            final String message = "[AbstractTemplateEngine] velocity folder doesn't exists : " + velocityTemplateFolderPath;
            error(message, true);
            throw new RuntimeException(message);
        }
        if (!templateFolder.canRead()) {
            final String message = "[AbstractTemplateEngine] can't read in velocity folder, check permissions : " + velocityTemplateFolderPath;
            error(message, true);
            throw new RuntimeException(message);
        }
        if (!templateFolder.isDirectory()) {
            final String message = "[AbstractTemplateEngine] velocity folder is not a folder : " + velocityTemplateFolderPath;
            error(message, true);
            throw new RuntimeException(message);
        }
        info("[AbstractTemplateEngine] velocityTemplateFolderPath " + velocityTemplateFolderPath);
        // 2020 StructuredGlobbingResourceLoader.FOLDER = velocityTemplateFolderPath;
    }

    protected EventCartridge getEventCartridge() {
        final EventCartridge result = new EventCartridge();

        result.addInvalidReferenceEventHandler(new InvalidReferenceEventHandler() {

            @Override
            public Object invalidGetMethod(Context cntxt, String reference, Object object, String property, Info info) {
                error("[velocity] in " + info.getTemplateName() + " LC[" + info.getLine() + ", " + info.getColumn() + "] " + (object == null ? "object is null" : "there is no getter") + " for " + property + " property in " + reference);
                return null;
            }

            @Override
            public boolean invalidSetMethod(Context cntxt, String leftReference, String rightReference, Info info) {
                error("[velocity] in " + info.getTemplateName() + " LC[" + info.getLine() + ", " + info.getColumn() + "] object is null or there is no setter for the given property in left='" + leftReference + "', right='" + rightReference + "'");
                return false;
            }

            @Override
            public Object invalidMethod(Context cntxt, String reference, Object object, String method, Info info) {
                error("[velocity] in " + info.getTemplateName() + " LC[" + info.getLine() + ", " + info.getColumn() + "] " + (object == null ? "object is null" : "there is no method") + " for " + method + " property in " + reference);
                return null;
            }
        });

        result.addMethodExceptionHandler(new MethodExceptionEventHandler() {
            @Override
            public Object methodException(Context cntxt, Class aClass, String method, Exception e, Info info) {
                error("[velocity] in " + info.getTemplateName() + " LC[" + info.getLine() + ", " + info.getColumn() + "] exception when calling " + ClassHelper.getShortName(aClass) + "." + method + " : " + e.getMessage(), e, true);
                return null;
            }
        });

        return result;
    }

    protected String initEngine(String status) {
        if (velocityEngine == null) {
            beginBlock("Instanciating Velocity engine ...");
            try {
                velocityEngine = getNewEngine();
            } catch (TemplateException e) {
                error("TemplateEngine not found", e, true);
                status = "ERROR";
            }
            endBlock("Instanciating Velocity engine. Done.");
        }
        return status;
    }

    private VelocityEngine getNewEngine() throws TemplateException {
        final VelocityEngine result = new VelocityEngine();
        debug("Velocity engine instanciated");
        /*
        *  configure the engine.  In this case, we are using
        *  ourselves as a logger (see logging examples..)
         */
        final Properties properties = new Properties();

        // 2020 to have error stricly like a programming language
        // https://velocity.apache.org/engine/2.2/user-guide.html#strict-rendering-mode
        properties.put("runtime.strict_mode.enable", true);

        // 2020 activate more debug
        // https://velocity.apache.org/engine/2.2/configuration.html#logging
        properties.put("runtime.log.track_location", true);
        properties.put("runtime.log.log_invalid_references", true);
        properties.put("runtime.log.track_locations", true);
        properties.put("runtime.log.log_invalid_method_calls", true);
        // Add log when a template or more generally a ressource is found the firts time
        properties.put("resource.manager.log_when_found", true);

        // 2020 velocimacro
        // https://velocity.apache.org/engine/2.2/configuration.html#macro-display-foo-foo-end
        properties.put("velocimacro.enable_bc_mode", true);
        properties.put("velocimacro.arguments.strict", true);

        // Resource loading.
        // From 2020: save you few DAYS (yeah) of investiguation and keep use this File resource loader.
        // DO NOT try again to switch to a Classloader.
        // Used by Fusion templates where File is mandatory and the only one option.
        // Failed to use this parsing for npn .vm file. This looked like a good trick but it doesn't work.
        // => must use IO to produce file. This need also File loader, not class loader.
        info("[AbstractTemplateEngine] Template loading file from this root " + velocityTemplateFolder);
        properties.put("resource.loaders", "file");
        properties.put("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        properties.put("resource.loader.class.description", "Velocity File Resource Loader from local root " + velocityTemplateFolder);
        //properties.put("file.resource.loader.cache", "true");
        //properties.put("file.resource.loader.modificationCheckInterval", "2");
        properties.put("resource.loader.file.cache", false);
        properties.put("resource.loader.file.modification_check_interval", 1);
        properties.put("resource.loader.file.path", velocityTemplateFolder);

        doAddEngineProperties(properties);

        try {
            result.init(properties);
        } catch (Throwable e) {
            error("[AbstractTemplateEngine] error during initialization : init(properties)" + e.getMessage(), e, true);
            throw new TemplateException(e);
        }

        return result;
    }

    protected void doAddEngineProperties(final Properties properties) {
    }

    protected org.apache.velocity.Template getTemplate(final VelocityEngine velocityEngine, final String folder, final String templateName) {
        org.apache.velocity.Template template = null;
        String fullTemplatePath = templateName;
        if (folder != null) {
            fullTemplatePath = folder + File.separator + templateName;
        }
        debug("AbstractTemplateEngine.getTemplate() trying to get " + fullTemplatePath);
        try {
            template = velocityEngine.getTemplate(fullTemplatePath, "UTF-8");
            debug("AbstractTemplateEngine.getTemplate() OK.");
            debug("AbstractTemplateEngine.getTemplate() template full path " + template.getResourceLoader().toString());
        } catch (ResourceNotFoundException rnfe) {
            error("AbstractTemplateEngine.getTemplate() : couldn't find the template " + fullTemplatePath, rnfe, true);
        } catch (ParseErrorException pee) {
            error("AbstractTemplateEngine.getTemplate() : syntax error : problem parsing the template " + fullTemplatePath, pee, true);
        } catch (MethodInvocationException mie) {
            error("AbstractTemplateEngine.getTemplate() : something invoked in the template " + fullTemplatePath + " threw an exception", mie, true);
        } catch (Throwable t) {
            error("AbstractTemplateEngine.getTemplate() : Exception raised while trying to get template " + fullTemplatePath, t, true);
        }
        return template;
    }

    public String getTemplateFolder() {
        return velocityTemplateFolder;
    }
}
