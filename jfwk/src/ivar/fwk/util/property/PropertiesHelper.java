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
package ivar.fwk.util.property;

import ivar.common.AbstractObject;
import ivar.common.logger.Logger;
import ivar.common.logger.LoggerDefaultImplementation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import ivar.helper.CollectionFactory;

// TODO Keys should be dot-separated, like.this, and not dash-separated like-this !
// TODO 2020 improve so we can really fail on non existing folder.
public class PropertiesHelper extends AbstractObject {

    private static final String ENGINE_PROPERTIES = "engine.properties";
    private static final Map<String, PropertyDesc> propertiesDesc = CollectionFactory.newMap();
    private static final Properties properties = new Properties();

    // List of possible oroperties
    //template-dir=/Users/jbb/Dev/Projects/orbiter-engine-netbeans/compiler/template
    //qxsdk-dir=
    //compiler-output-dir=/Users/jbb/Dev/Projects/orbiter-engine-root
    //composer-version=DEV-JBB
    //composer-database-port=3306
    //composer-ant-dir=/Applications/apache-ant-1.9.14
    //composer-icons-dir=/Users/jbb/BUCL-files/icons
    //targapp-icons-dir=/Users/jbb/BUCL-files/icons
    //targapp-database-port=3306
    //targapp-tomcat-port=8181
    //targapp-tomcat-manager-user=admin
    //targapp-tomcat-manager-password=admin
    static {
        // 2020 TODO refactor mustr exists parameter which is stupid as it make sense only for file.
        propertiesDesc.put("template-dir", new PropertyDesc("template-dir", true, PropertyType.folderAbsolute, true, "Full path to the generation template folder"));
        propertiesDesc.put("qxsdk-dir", new PropertyDesc("qxsdk-dir", true, PropertyType.folderAbsolute, true, "Full path to the Qooxdoo SDK"));
        propertiesDesc.put("compiler-output-dir", new PropertyDesc("compiler-output-dir", true, PropertyType.folderAbsolute, true, "Full path to the folder where compiler will vreate artifact for targapps."));
        propertiesDesc.put("composer-version", new PropertyDesc("composer-version", false, PropertyType.string, false, "String used into the composer UI to differenciate DEV or production version"));
        propertiesDesc.put("composer-database-port", new PropertyDesc("composer-database-port", true, PropertyType.number, false, "Database port for the composer"));
        propertiesDesc.put("composer-ant-dir", new PropertyDesc("composer-ant-dir", true, PropertyType.folderAbsolute, true, "Full path to the ant installation for the targapp compile and deploy"));
//        propertiesDesc.put("composer-icons-dir", new PropertyDesc("composer-icons-dir", true, PropertyType.folderAbsolute, true, "Full path to the icons folder librairy for the composer"));
//        propertiesDesc.put("targapp-icons-dir", new PropertyDesc("targapp-icons-dir", true, PropertyType.folderAbsolute, true, "Full path to the icons folder librairy for the targapp"));
        propertiesDesc.put("targapp-database-port", new PropertyDesc("targapp-database-port", true, PropertyType.number, false, "Database port for the generated targapp"));
        propertiesDesc.put("targapp-database-password", new PropertyDesc("targapp-database-password", true, PropertyType.string, false, "Database password for the generated targapp"));
        propertiesDesc.put("targapp-tomcat-port", new PropertyDesc("targapp-tomcat-port", true, PropertyType.number, false, "Tomcat port for the generated targapp"));
        propertiesDesc.put("targapp-tomcat-manager-user", new PropertyDesc("targapp-tomcat-manager-user", true, PropertyType.string, false, "Tomcat user to deploy the targapp"));
        propertiesDesc.put("targapp-tomcat-manager-password", new PropertyDesc("targapp-tomcat-manager-password", true, PropertyType.string, false, "Tomcat password to deploy the targapp"));
        propertiesDesc.put("targapp-filestorage-dir", new PropertyDesc("targapp-filestorage-dir", true, PropertyType.folderAbsolute, true, "Full path to the targapp file storage root"));
        propertiesDesc.put("targapp-lib-dir", new PropertyDesc("targapp-lib-dir", true, PropertyType.folderAbsolute, true, "Full path to the targapp folder that contains all jar files for build and runtime"));
        propertiesDesc.put("targapp-dep-dir", new PropertyDesc("targapp-dep-dir", true, PropertyType.folderAbsolute, true, "Full path to the IDE project folder that must contains json, jfwk, ... that must in turn contains src with Java sources"));
    }

    private static PropertiesHelper instance = null;
    private String resourceName;

    private PropertiesHelper() {
        load();
        check();
    }

    private void check() {
        boolean allgood = true;
        beginBlock("[PropertiesHelper] Checking properties ...");
        beginBlock("[PropertiesHelper] Checking for mandatory properties ...");
        for (final PropertyDesc propertyDesc : propertiesDesc.values()) {
            if (!propertyDesc.isMandatory()) {
                continue;
            }
            debug("[PropertiesHelper] Checking for " + propertyDesc.getName());
            if (!properties.containsKey(propertyDesc.getName())) {
                allgood = false;
                error("[PropertiesHelper] A mandatory property is nissing name=" + propertyDesc.getName());
                error("[PropertiesHelper] A mandatory property is nissing type=" + propertyDesc.getType());
                error("[PropertiesHelper] A mandatory property is nissing desc=" + propertyDesc.getDesc());
                error("[PropertiesHelper] A mandatory property is nissing " + propertyDesc);
                error("[PropertiesHelper] A mandatory property " + propertyDesc.getName() + "is nissing in " + ENGINE_PROPERTIES, true);
            } else {
                debug("[PropertiesHelper] OK. Found " + propertyDesc.getName());
            }
        }
        endBlock("[PropertiesHelper] Checking for mandatory properties. Done.");

        beginBlock("[PropertiesHelper] Checking for consistency ...");
        for (final String key : properties.stringPropertyNames()) {
            final String value = properties.getProperty(key);
            debug("[PropertiesHelper] Checking for " + key + "=" + value);
            final PropertyDesc propertyDesc = propertiesDesc.get(key);
            if (null == propertyDesc) {
                debug("[PropertiesHelper] Found an unknown key (probably unused) " + key);
                info("   - checking " + key + " for path " + value);
                if (value.startsWith("/") || value.startsWith("\\")) {
                    final File file = new File(value);
                    if (!file.exists()) {
                        warning("[PropertiesHelper] path doesn't exist [" + key + "] " + value);
                    } else {
                        info("      OK " + key + " for path " + value);
                    }
                } else {
                    info("      Bypassed, not a folder " + key + " : " + value + ".");
                }
            } else {
                debug("[PropertiesHelper] Found a valid key " + key);
                final boolean valid = propertyDesc.isValid(value);
                if (!valid) {
                    allgood = false;
                    error("[PropertiesHelper] Invalid property value name=" + propertyDesc.getName());
                    error("[PropertiesHelper] Invalid property value type=" + propertyDesc.getType());
                    error("[PropertiesHelper] Invalid property value desc=" + propertyDesc.getDesc());
                    error("[PropertiesHelper] Invalid property value " + propertyDesc);
                    error("[PropertiesHelper] Invalid property value " + propertyDesc.getName() + "=" + value + " in " + ENGINE_PROPERTIES, true);
                }
            }
        }
        endBlock("[PropertiesHelper] Checking for consistency ...");
        endBlock("[PropertiesHelper] Checking properties. Done. " + (allgood ? "No errors. Green light Houston for the launch." : " KO. Sorry Houston, no way to launch."));
        if (!allgood) {
            System.out.println("[PropertiesHelper] Error in properties, it will not work. Check previous line on log. Houston, launch canceled, pressing the big red button.");
            System.exit(-1);
        }
    }

    private void load() throws RuntimeException {
        final String enginePropertiesFileName = System.getProperty(ENGINE_PROPERTIES);
        info("[PropertiesHelper] " + ENGINE_PROPERTIES + "= " + (enginePropertiesFileName == null ? "null" : enginePropertiesFileName));
        if (enginePropertiesFileName == null) {
            final String username = System.getProperty("user.name");
            resourceName = "/" + username + "-" + ENGINE_PROPERTIES;
            info("[PropertiesHelper] " + ENGINE_PROPERTIES + " is null. Resource is based on user name (" + username + ") : " + resourceName);
        } else {
            resourceName = "/" + enginePropertiesFileName + ".properties";
            info("[PropertiesHelper] " + ENGINE_PROPERTIES + " is set (" + enginePropertiesFileName + "). Resource is " + resourceName);
        }

        beginBlock("[PropertiesHelper] Loading " + resourceName + " ...");
        final InputStream asStream = this.getClass().getResourceAsStream(resourceName);
        if (asStream == null) {
            final String message = "[PropertiesHelper] Loading " + resourceName + " : failed " + resourceName + " file was not there. For info, ./ is " + getCurrentPath();
            error(message, true);
            debug("------ begin FOR DEBUGGING, here is the dump of all properties ");
            final Properties sysProperties = System.getProperties();
            for (final String key : sysProperties.stringPropertyNames()) {
                debug("  - " + key + " : " + sysProperties.getProperty(key));
            }
            debug("------ end FOR DEBUGGING, here is the dump of all properties ");
            throw new RuntimeException(message);
        }
        try {
            properties.load(asStream);
        } catch (IOException e) {
            final String message = "[PropertiesHelper] Loading " + resourceName + " : failed ! for information, ./ is " + getCurrentPath();
            error(message, e, true);
            throw new RuntimeException(message, e);
        }
        endBlock("[PropertiesHelper] Loading " + resourceName + ". Done.");
    }

    private String getCurrentPath() {
        String currentPath = null;
        try {
            currentPath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            // Nothing we can do
        }
        return currentPath;
    }

    public static PropertiesHelper getInstance() {
        if (instance == null) {
            final Logger log = LoggerDefaultImplementation.getInstance();
            log.beginBlock("[PropertiesHelper] Reading engine.properties ...");
            instance = new PropertiesHelper();
            log.endBlock("[PropertiesHelper] Reading engine.properties. Done.");
        }
        return instance;
    }

    private String getProperty(final String key) {
        return getProperty(key, null);
    }

    private String getProperty(final String key, final String defaultValue) {
        final Logger log = LoggerDefaultImplementation.getInstance();
        String value;
        if (defaultValue != null) {
            value = properties.getProperty(key, defaultValue);
        } else {
            value = properties.getProperty(key);
        }
        if (value == null) {
            String message = "[PropertiesHelper] " + key + " not found. Resource name is " + resourceName;
            log.error(message, true);
            throw new RuntimeException(message);
        }
        log.debug("[PropertiesHelper] getProperty(" + key + ") : " + value);
        return value;
    }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
////////        param value section    //////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    public String getTemplateDir() {
        return properties.getProperty("template-dir");
    }

    public String getQxsdkDir() {
        return properties.getProperty("qxsdk-dir");
    }

    public String getCompilerOutputDir() {
        return properties.getProperty("compiler-output-dir");
    }

    public String getComposerVersion() {
        return properties.getProperty("composer-version");
    }

    public String getComposerDatabasePort() {
        return properties.getProperty("composer-database-port");
    }

    public String getComposerAntDir() {
        return properties.getProperty("composer-ant-dir");
    }

//    public String getComposerIconsDir() {
//        return properties.getProperty("composer-icons-dir");
//    }
//
//    public String getTargappIconsDir() {
//        return properties.getProperty("targapp-icons-dir");
//    }
    public String getTargappDatabasePort() {
        return properties.getProperty("targapp-database-port");
    }

    public String getTargappDatabasePassword() {
        return properties.getProperty("targapp-database-password");
    }

    public String getTargappFilestorageDir() {
        return properties.getProperty("targapp-filestorage-dir");
    }

    public String getTargappTomcatPort() {
        return properties.getProperty("targapp-tomcat-port");
    }

    public String getTargappTomcatManagerUser() {
        return properties.getProperty("targapp-tomcat-manager-user");
    }

    public String getTargappTomcatManagerPassword() {
        return properties.getProperty("targapp-tomcat-manager-password");
    }

    public String getTargappLibDir() {
        return properties.getProperty("targapp-lib-dir");
    }

    public String getTargappDepDir() {
        return properties.getProperty("targapp-dep-dir");
    }
}
