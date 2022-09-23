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
package ivar.target.qx;

import ivar.generator.AbstractGenerator;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.helper.filecounter.FileCategories;
import ivar.ivarc.IvarcParameters;
import ivar.metamodel.spec.Application;
import ivar.metamodel.target.generic.DataSet;
import ivar.metamodel.target.generic.ui.Screen;
import ivar.metamodel.target.generic.ui.ScreenField;
import java.io.File;
import java.util.Map;

public class QooxdooGenerator extends AbstractGenerator {

    public QooxdooGenerator(String name, Application application, IvarcParameters ivarcParameters) {
        super(name, application, ivarcParameters);
    }

    public QooxdooGenerator(String name, String generatorSubFolder, Application application, IvarcParameters ivarcParameters) {
        super(name, generatorSubFolder, application, ivarcParameters);
    }

    @Override
    public void doGenerate() {
        //public void generate(final Application application, final IvarcParameters ivarcParameters, final String optionalSubFolder) {
//    public void generate(final CompilerVelocityEngine compilerVelocityEngine, final String outputFolderPath, final Application application, final CompileParameters compileParams, final String uniqueDate, final String timeTrace, final String appTrace, final TargappConfig targappConfig) throws GeneratorException {

        final Application application = getApplication();
        final IvarcParameters ivarcParameters = getIvarcParameters();

        beginBlock("Generating qooxdoo files for " + application.getName());
        final CompilerVelocityEngine compilerVelocityEngine = ivarcParameters.getCompilerVelocityEngine();
        debug("Current folder is " + new File(ivarcParameters.getCompileRootFolder()).getAbsolutePath());
        //final QxApplication qxapp = new QxApplication(application);
        final String packageName = StringHelper.getStringForJavaPackage(application.getKeyname());
        final String rootQxSrc = "/qx-src/" + packageName;
        final String rootQxjson = "/qx-json";

        compilerVelocityEngine.generate("qx_config-json.vm", rootQxjson, "config.json", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_config-json.vm", rootQxjson, "qxfwk-config.json", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_index-html.vm", "index.html", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_application-js.vm", rootQxSrc, "Application.js", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_main-js.vm", rootQxSrc, application.getKeynameFirstCapitalized() + ".js", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_about-js.vm", rootQxSrc, "About.js", FileCategories.client, null, ivarcParameters);

        compilerVelocityEngine.generate("qx_preference-manager-js.vm", rootQxSrc, "PreferenceManager.js", FileCategories.client, null, ivarcParameters);

        //final Map<String, Object> webXmlContexts = newContexts("application", new J2EEApplication(application, ivarcParameters.getCompileRootFolder()));
        compilerVelocityEngine.generate("web-xml.vm", "web", "web.xml", FileCategories.client, null, ivarcParameters);

        final Map<String, DataSet> datasets = CollectionFactory.newMap();
        for (final Screen screen : application.getScreens()) {
            for (final ScreenField field : screen.getFields()) {
                if (field.isDataset()) {
                    final DataSet dataset = field.getDataset();
                    datasets.put(dataset.getKeyname(), dataset);
                }
            }

            final Map<String, Object> contexts = newContexts("screen", screen);
            //contexts.put("application", qxapp);

            if (screen.isPureComposition()) {
                compilerVelocityEngine.generate("qx_pure-composite-view.vm", rootQxSrc + "/ui/composite", screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isCreate()) {
                compilerVelocityEngine.generate("qx_create-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isDelete()) {
            } else if (screen.isDSearch()) {
                compilerVelocityEngine.generate("qx_search-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isSearch()) {
                compilerVelocityEngine.generate("qx_search-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isUpdate()) {
                compilerVelocityEngine.generate("qx_update-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isDRead()) {
                compilerVelocityEngine.generate("qx_read-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            } else if (screen.isRead()) {
                compilerVelocityEngine.generate("qx_read-view.vm", rootQxSrc + "/ui/" + screen.getBusinessObjectName(), screen.getFsUniqueKey() + ".js", FileCategories.client, contexts, ivarcParameters);
            }
        }
        beginBlock("Qooxdoo datasets ...");
        for (final String key : datasets.keySet()) {
            final DataSet dataSet = datasets.get(key);
            compilerVelocityEngine.generate("qx_dataset-model.vm", rootQxSrc + File.separator + "datasets", dataSet.getFSName() + ".js", FileCategories.client, newContexts("dataSet", dataSet), ivarcParameters);
        }
        endBlock("Qooxdoo datasets. Done. Generated " + datasets.size());
        endBlock("Generating qooxdoo files for " + application.getName() + " done.");
    }
}
