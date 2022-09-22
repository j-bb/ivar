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
package ivar.ivarc;

import ivar.common.AbstractObject;
import ivar.generator.velocity.CompilerVelocityEngine;
import ivar.helper.CollectionFactory;
import ivar.helper.DateHelper;
import ivar.helper.filecounter.FileCategories;
import ivar.helper.filecounter.FileCounter;
import java.util.Date;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

public class IvarcParameters extends AbstractObject {

    @Option(required = true, name = "-file", usage = "Ivar file to compile")
    private String ivarfile = null;

    @Option(multiValued = true, required = false, name = "-pipeline", usage = "Give the pipeline step to run. By default, all steps are run. Ex with everything: -pipeline lex parse visit compile. Only visit and compile are meaningful.")
    private String pipeline = null;

    @Option(required = false, name = "-destFolder", usage = "Destination folder where to generate files. By default, it will be .")
    private String compilerootfolder = "./targapp";

    @Option(required = true, name = "-templateFolder", usage = "Velocity template folder.")
    private String velocitytemplatefolder;

    @Option(required = false, name = "-app.dbrootuser", usage = "Root username to connect the database with. Default is 'SteveDB'")
    private String app_dbrootuser = "SteveRootDB";

    @Option(required = false, name = "-app.dbrootpass", usage = "Root password to connect the database with. Default is 'AustinDB'")
    private String app_dbrootpassword = "AustinRootDB";

    @Option(required = false, name = "-app.dbuser", usage = "App username to connect the database with. Default is 'SteveDB'")
    private String app_dbuser = "SteveDB";

    @Option(required = false, name = "-app.dbpass", usage = "App password to connect the database with. Default is 'AustinDB'")
    private String app_dbpassword = "AustinDB";

    @Option(required = false, name = "-app.dbport", usage = "Database port. Default is 3306")
    private String dbport = "3306";

    @Option(required = false, name = "-dbarch", usage = "Database architecture. Possible arch are 'mysql', 'sqlserver'. Default is 'mysql'.")
    private String dbarch = "mysql";

    @Option(required = false, name = "-newdb", usage = "If set, database will be erased (drop table, create table), schema created and existing data lost, otherwise, by default, database schema will be upgraded (update table) to fit new spec while preserving data, may leave unused columns with data (column rename case).")
    private boolean newdb = false;

    @Option(required = false, name = "-onlysql", usage = "If set, database will not be touched, only an SQL file will be produce, otherwise, by default, database schema will be touch.")
    private boolean onlysql = true;

    @Option(required = false, name = "-dbdebug", usage = "If set, generated code will embed more trace on persistence and SQL, otherwise, by default, generated code will embed less trace on persistence and SQL.")
    private boolean dbdebug = false;

    @Option(required = false, name = "-app.user", usage = "username to connect the database with. Default is 'Steve'")
    private String app_user = "Steve";

    @Option(required = false, name = "-app.pass", usage = "password to connect the database with. Default is 'Austin'")
    private String app_password = "Austin";

    @Option(required = false, name = "-tomcatmanageruser", usage = "username to connect to the Tomcat Manager. Default is 'SteveTomcat'")
    private String tomcatmanageruser = "SteveTomcat";

    @Option(required = false, name = "-tomcatmanagerpass", usage = "password to connect to the Tomcat Manager. Default is 'AustinTomcat'")
    private String tomcatmanagerpassword = "AustinTomcat";

    @Option(required = false, name = "-tomcatport", usage = "Tomcat port. Default is 8080")
    private String tomcatport = "8080";

    @Option(required = false, name = "-deploy", usage = "If set, deploy will be tempted to Tomcat, otherwise, if set to false, no deployment will be tempted. By default, generted application will be deploy to TOmcat.")
    private boolean deploy = true;

    @Option(required = false, name = "-debug", usage = "If set, generated code will embed more trace, otherwise, by default, generated code will be optimized with less trace.")
    private boolean debug = false;

    @Option(required = false, name = "-production", usage = "If set, all will be optimize for production (javascript minified, less trace, ...), otherwise, by default, generated code will be optimized for test and dev with more trace, not minigied javascript, ....")
    private boolean production = false;

    @Option(required = false, name = "-horodate", usage = "If set, compilation output log file will be horodated (name will contains date), otherwise, by default, same filename than Ivar file.log")
    private boolean horodateLog = false;

    @Option(required = false, name = "-cartridge", usage = "Specify the cartridge to use for generation. A cartridge is a set of template. Default cartridge is 'default'")
    private String cartridge = "default";

// several folder to add :
    // 1. the template folder, link to cartridge
    // 2. the root folder from file to inject into targapp
    // 3. the root folder for file in the targapp. Prefix all by targapp !!!!! for targapp properties
    // 4. root folder for generated files.
    private String filesDir = null;

//
//    @Option(required = false, name = "-jdbcdriver", usage = "Fully qualified JDBC class name to use")
//    protected String jdbc = "com.mysql.jdbc.Driver";
    // receives other command line parameters than options
    @Argument
    private final List<String> arguments = CollectionFactory.newList();

    private final Date uniqueCompileDate = new Date();

    /**
     * The velocity engine used to produce all files. Is reused from file to file, but can be reinstanciated during compiler lifetime depending on "genetator"
     */
    private CompilerVelocityEngine compilerVelocityEngine;

    /**
     * This handle a global counter of generated files for compute metric at the end.
     */
    private final FileCounter fileCounter = new FileCounter();

    public IvarcParameters() {
    }

    /**
     * Constructor used by test.
     *
     * @param ivarFIle
     * @param isParse
     * @param isVisit
     * @param isCompile
     */
    public IvarcParameters(final String ivarFIle, final boolean isParse, final boolean isVisit, final boolean isCompile) {
        pipeline = "";
        if (isParse) {
            pipeline += " -parse";
        }
        if (isVisit) {
            pipeline += " -visit";
        }
        if (isCompile) {
            pipeline += " -compile";
        }
        this.ivarfile = ivarFIle;
    }

//From ApplicationController :
//        final String applicationFolder = "orbiter-engine-" + application.getKeyname() + "-" + params.getDbArch() + "-" + (params.isProd() ? "prod" : "dev") + "-" + uniqueCompileDateString;
//        final String rootFolder = COMPILE_FOLDER + "/" + applicationFolder;
//
// AppTrace is application.getComments from a call to compiler from ApplicationController.
//
    public void parseArgs(final String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);

        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");
            // after parsing arguments, you should check
            // if enough arguments are given.
//            System.out.println("Arguments :");
//            for (String s : arguments) {
//                System.out.println(s);
//            }
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            error(e, true);
            info("java " + getClass().getName() + " [options...]");
            // print the list of available options
            parser.printUsage(getLogger().getOut());
            emptyLine();

            // print option sample. This is useful some time
            info("  Example: java " + getClass().getName() + " " + parser.printExample(ExampleMode.ALL));
            emptyLine();
            throw new IvarcException("Invalid arguments", e);
        }
        check();
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getIvarfile() {
        return ivarfile;
    }

    private void check() {
        // TODO 2020
        // check compilerootfolder if not null + readable, ...

        beginBlock("Checking compilation param ...");
        if (!"mysql".equalsIgnoreCase(getDBArch()) && !"sqlserver".equalsIgnoreCase(getDBArch())) {
            final String message = "dbarch error. Must be 'mysql' or 'sqlserver' was '" + getDBArch() + "'";
            throw new IvarcException(message);
        }

//        TargappConfig result;
//        if (targappConfig != null) {
//            result = targappConfig;
//            info("Validating custom targapp configuration.");
//            if (application.hasFileUploads() || application.hasFusionTemplates()) {
//                if (targappConfig.getFilesDir() == null || targappConfig.getFilesDir().equals("")) {
//                    compileError("Application" + application.keyname + " has file uploads and/or fusion templates, you MUST provide a files directory.");
//                }
//            }
//            if (!targappConfig.checkValidity()) {
//                compileError("Some targapp configurations were missing, please specify them all.");
//                beginCompileHelpBlock("For helping purpose, here are all the mandatory configs with their respective value.");
//                compileHelp("tomcatManagerUser : '" + targappConfig.getTomcatManagerUser() + "'");
//                compileHelp("tomcatManagerPassword : '" + targappConfig.getTomcatManagerPassword() + "'");
//                compileHelp("tomcatPort : '" + targappConfig.getTomcatPort() + "'");
//                compileHelp("dbRootPassword : '" + targappConfig.getDbRootPassword() + "'");
//                compileHelp("dbPort : '" + targappConfig.getDbPort() + "'");
//                endCompileHelpBlock();
//            }
//        } else {
//            debug("No custom targapp config. Creating default targapp config.");
//            final PropertiesHelper properties = PropertiesHelper.getInstance();
//            result = new TargappConfig(properties.getTargappTomcatManagerUser(), properties.getTargappTomcatManagerPassword(), properties.getTargappTomcatPort(), properties.getTargappDatabasePassword(), properties.getTargappDatabasePort(), properties.getTargappFilestorageDir());
//        }
        endBlock("Checking compilation param. Done.");
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public boolean isPipeline() {
        return null != pipeline && pipeline.length() != 0;
    }

    public boolean isParse() {
        return !isPipeline() || pipeline.toLowerCase().contains("parse");
    }

    public boolean isVisit() {
        return !isPipeline() || pipeline.toLowerCase().contains("visit");
    }

    public boolean isCompile() {
        return !isPipeline() || pipeline.toLowerCase().contains("compile");
    }

    public String getCompileRootFolder() {
        return compilerootfolder;
    }

    public String getLogFilename() {
        return horodateLog ? ivarfile + "-compile-" + uniqueCompileDate + ".log'" : ivarfile + "-compile.log'";
    }

    public Date getUniqueCompileDate() {
        return uniqueCompileDate;
    }

    public String getUniqueCompileTimeTrace() {
        return DateHelper.getLogDate(uniqueCompileDate);
    }

    public String getCartridge() {
        return cartridge;
    }

    public boolean isDBDebug() {
        return dbdebug;
    }

    public boolean isNewDB() {
        return newdb;
    }

    public boolean isProd() {
        return production;
    }

    public String getDBArch() {
        return dbarch.toLowerCase();
    }

    public boolean isOnlySQL() {
        return onlysql;
    }

    public CompilerVelocityEngine getCompilerVelocityEngine() {
        return getCompilerVelocityEngine(false);
    }

    public CompilerVelocityEngine getCompilerVelocityEngine(boolean newOne) {
        if (compilerVelocityEngine == null || newOne) {
            emptyLine();
            beginBlock("Instanciating template engine ...");
            compilerVelocityEngine = new CompilerVelocityEngine(velocitytemplatefolder);
            endBlock("Instanciating template engine : done.");
            emptyLine();
        }
        return compilerVelocityEngine;
    }

    public FileCounter getFileCounter() {
        return fileCounter;
    }

    public void addFileForMetric(final String fileAbsolutePath, final FileCategories fileCategory) {
        if (fileAbsolutePath == null) {
            throw new NullPointerException("IvarcParameters.addFileForMetric: fileAbsolutePath is null");
        }
        getFileCounter().addFile(fileCategory, fileAbsolutePath);
    }
}
