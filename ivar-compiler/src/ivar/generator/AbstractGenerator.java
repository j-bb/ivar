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
package ivar.generator;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.io.FileHelper;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractGenerator extends AbstractObject {

    private final String name;
    private final String generatorSubFolder;
    private final Application application;
    private final IvarcParameters ivarcParameters;

    public AbstractGenerator(final String name, final Application application, IvarcParameters ivarcParameters) {
        this(name, null, application, ivarcParameters);
    }

    public AbstractGenerator(final String name, final String generatorSubFolder, final Application application, IvarcParameters ivarcParameters) {
        this.name = name;
        this.generatorSubFolder = generatorSubFolder;
        this.application = application;
        this.ivarcParameters = ivarcParameters;
    }

    protected Map<String, Object> newContexts(final String key, final Object context) {
        final Map<String, Object> result = CollectionFactory.newMap();
        return addContext(result, key, context);
    }

    protected Map<String, Object> addContext(final Map<String, Object> contexts, final String key, final Object context) {
        contexts.put(key, context);
        return contexts;
    }

    public String getName() {
        return name;
    }

    public String getGeneratorSubFolder() {
        return generatorSubFolder;
    }

    public String getGeneratorFolder() {
        String result = ivarcParameters.getCompileRootFolder();
        final String generatorSubFolder = getGeneratorSubFolder();
        if (generatorSubFolder != null && generatorSubFolder.length() > 1) {
            result += File.separator + generatorSubFolder;
        }
        return result;
    }

    public Application getApplication() {
        return application;
    }

    public IvarcParameters getIvarcParameters() {
        return ivarcParameters;
    }

    public final void generate() {
        final String generatorRootFolder = getGeneratorFolder();
        beginBlock(getName() + " generator in folder " + generatorRootFolder);
        try {
            FileHelper.ensurePath(generatorRootFolder);
        } catch (IOException ex) {
            error("Generator folder not usable " + generatorRootFolder, ex, true);
        }

        doGenerate();
        endBlock();
    }

    abstract protected void doGenerate();
}
