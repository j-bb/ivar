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
import ivar.common.logger.FullLogger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.compiler.ApplicationCompiler;
import ivar.compiler.ApplicationCompilerResult;
import ivar.grammar.lexer.Lexer;
import ivar.grammar.lexer.LexerException;
import ivar.grammar.node.Start;
import ivar.grammar.node.Token;
import ivar.grammar.parser.Parser;
import ivar.grammar.parser.ParserException;
import ivar.helper.StopWatch;
import ivar.metamodel.spec.Application;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Date;

/*
    七転び八起き
    “Sept fois à terre, huit fois debout”
 */
public class Ivarc extends AbstractObject {

    public static final String VERSION = "0.9.0";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final FullLogger logger = LoggerDefaultImplementation.getInstance();
        logger.reset(System.out);
        logger.info("Ivarc launched with the following " + args.length + " args");
        for (String arg : args) {
            logger.info(arg);
        }
        final IvarcParameters params = new IvarcParameters();
        params.parseArgs(args);
        ivarc(logger, params);
    }

    public static void ivarc(final FullLogger logger, final IvarcParameters params) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String filename = params.getIvarfile();
        final File ivarFile = new File(filename);
        if (!ivarFile.exists()) {
            final String message = filename + " doesn't exists";
            logger.error(message, true);
            throw new IvarcException(message);
        }
        if (!ivarFile.canRead()) {
            final String message = filename + " : can't read.";
            logger.error(message, true);
            throw new IvarcException(message);
        }

        logger.info("ivarc " + filename + " Exists and can read");
        logger.info(ivarFile.getAbsolutePath());
        if (params.isPipeline()) {
            logger.info("Found pipeline directive");
            logger.info("  Parse :" + params.isParse());
            logger.info("  Visit :" + params.isVisit());
            logger.info("  Compile :" + params.isCompile());
        }
        if (ivarFile.isDirectory()) {
            final String message = filename + " is a folder. Only one ivar file can be compiled.";
            logger.error(message, true);
            throw new IvarcException(message);
        } else {
            logger.beginBlock("---------- Compiling Ivar file " + ivarFile.getName());
            final Ivarc ivarc = new Ivarc();
            ivarc.ivarcOneFIle(ivarFile, params, stopWatch);
            logger.endBlock("---------- Compile done for Ivar file " + ivarFile.getName());
        }
    }

    public void dumpFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            error(ex, true);
            throw new IvarcException(ex);
        }

        try {
            beginBlock("Dumping " + file.getAbsolutePath());
            int lines = 0;
            while (true) {
                String line = null;
                try {
                    line = reader.readLine();
                    lines++;
                } catch (IOException ex) {
                    error(ex);
                    throw new IvarcException(ex);
                }
                if (line == null) {
                    break;
                }
                compileInfo(line);
            }
            endBlock("Dump done. " + lines + " lines.");
        } finally {

            try {
                reader.close();
            } catch (IOException ex) {
                // Nothing to do.
            }
            reader = null;
        }
    }

    public void ivarcOneFIle(final File ivarFile, final IvarcParameters ivarcParameters, final StopWatch stopWatch) {
        ivarcOneFIle(null, ivarFile, ivarcParameters, stopWatch);
    }

    public void ivarcOneFIle(Application application, final File ivarFile, final IvarcParameters params, final StopWatch stopWatch) {
        dumpFile(ivarFile);
        emptyLine();

        // If application is null, this is the first turn.
        // Otherwise, there was imports and it was added.
        final File srcRootDir = ivarFile.getParentFile();
        BufferedReader reader = null;
        beginBlock("Compiling " + ivarFile);
        try {
            reader = new BufferedReader(new FileReader(ivarFile));
            ivarcOneStream(application, reader, params, stopWatch);
        } catch (FileNotFoundException ex) {
            error(ex, true);
            throw new IvarcException(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    error("Error while closing reader stream at the end of compilation for " + ivarFile, ex);
                }
                reader = null;
            }
        }

        endBlock("Compiling " + ivarFile + ". Done.");
    }

    private String getInfoFromToken(final Token token) {
        return "@" + token.getLine() + ":" + token.getPos();
    }

    public void ivarcOneStream(final Application application, final BufferedReader reader, final IvarcParameters params, final StopWatch stopWatch) {
        if (params.isParse()) {
            beginBlock("New Lexer");
            final Lexer lexer = new Lexer(new PushbackReader(reader, 2048)); // defaulted to 1024 originally.
            endBlock("New Lexer. Done.");
            // parser program
            beginBlock("New Parser");
            final Parser parser = new Parser(lexer);
            endBlock("New Parser. Done.");
            info("pipeline *lex, parse* -> visit -> compile. ");
            info(stopWatch.getIntermediateDeltaTimeTrace());
            parseStream(application, parser, params, stopWatch);
        } else {
            info("pipeline bypass activated lex, parse -> visit -> compile. No parse. Why did you launch Ivarc then?");
        }
    }

    private void parseStream(final Application application, final Parser parser, final IvarcParameters params, final StopWatch stopWatch) throws IvarcException {
        Start ast;
        try {
            beginBlock("Parsing ...");
            ast = parser.parse();
            endBlock("Parsing. Done.");
        } catch (ParserException ex) {
            final String message = "Parser error on '" + ex.getToken().getText() + "' " + getInfoFromToken(ex.getToken());
            compileError(message, true);
            throw new IvarcException(message);
        } catch (LexerException ex) {
            final String message = "Lexer error on '" + ex.getToken().getText() + "' " + getInfoFromToken(ex.getToken());
            compileError(message, true);
            throw new IvarcException(message);
        } catch (IOException ex) {
            final String message = "IO error";
            compileError(message, ex, true);
            throw new IvarcException(message);
        }
        if (!params.isVisit()) {
            info("pipeline bypass activated lex, parse -> visit -> compile. No visit.");
        } else {
            info("pipeline lex, parse -> *visit* -> compile.");
            info(stopWatch.getIntermediateDeltaTimeTrace());
            if (ast != null) {
                beginBlock("Visiting ...");

                beginBlock("Visiting Ivar file for token position ...");
                final IvarFilePositionsVisitor positionVisitor = new IvarFilePositionsVisitor();
                ast.apply(positionVisitor);
                endBlock("Visiting Ivar file for token position. Done. found " + positionVisitor.getNodesNumber() + " nodes.");

                beginBlock("Visiting Ivar file ...");
                final IvarFileVisitor visitor = new IvarFileVisitor(getLogger(), application, positionVisitor);
                // check program semantics
                ast.apply(visitor);
                endBlock("Visiting Ivar file. Done.");
                endBlock("Visiting. Done.");
                if (params.isCompile()) {
                    info("pipeline lex, parse -> visit -> *compile*.");
                    info(stopWatch.getIntermediateDeltaTimeTrace());
                    visitor.freeBeforeCompile();
                    compileApplication(visitor.getApplication(), params, stopWatch);
                } else {
                    info("pipeline bypass activated lex, parse -> visit -> compile. No compile.");
                }
            } else {
                throw new IvarcException("ast is null in ivarc visit pipeline step");
            }
        }
    }

    private void compileApplication(final Application application, final IvarcParameters ivarcParams, final StopWatch stopWatch) {
        final ApplicationCompiler compiler = new ApplicationCompiler();
        final ApplicationCompilerResult applicationCompilerResult;
        // 2020 : Not sure to understand back the difference between timeTrace, appTrace and uniqueCompileDate
        final String appTrace = "app-trace-" + ivarcParams.getIvarfile();
        final Date uniqueCompileDate = ivarcParams.getUniqueCompileDate();
        final String uniqueCompileTimeTrace = ivarcParams.getUniqueCompileTimeTrace();
        boolean compileOK;
        final FullLogger log = this.getLogger();
        log.flush();

        try {
            log.startBufferingToFile(ivarcParams.getLogFilename());
        } catch (IOException ex) {
            throw new IvarcException("Error while creating log file " + ivarcParams.getLogFilename());
        }

        log.beginBlock("ivarc.compileApplication(cartridge= " + ivarcParams.getCartridge() + ", debug= " + ivarcParams.isDebug() + ", dbdebug= " + ivarcParams.isDBDebug() + ", newdb= " + ivarcParams.isNewDB() + ", onlysql= " + ivarcParams.isOnlySQL() + ", dbarch= " + ivarcParams.getDBArch() + ", prod= " + ivarcParams.isProd() + ", version= " + VERSION + " )");
        log.info("This compile time trace is " + uniqueCompileTimeTrace);

        log.info("Compiling application " + application.getKeyname() + ", id=" + application.getId() + ", Application has " + application.getScenariosSize() + " scenarios");
        log.info("Root folder: " + ivarcParams.getCompileRootFolder());

        try {
            applicationCompilerResult = compiler.compile(application, ivarcParams);
            compileOK = log.isBuildOK();
        } catch (RuntimeException t) {
            compileOK = false;
            final String message = "Error (RuntimeException) during compilation";
            log.error(message, t, true);
            log.flush();
            t.printStackTrace(log.getOut());
            throw new IvarcException(message, t);
        } catch (Exception t) {
            compileOK = false;
            final String message = "Error (Exception) during compilation";
            log.error(message, t, false);
            log.flush();
            t.printStackTrace(log.getOut());
            throw new IvarcException(message, t);
        } catch (Throwable t) {
            compileOK = false;
            final String message = "Error (Throwable) during compilation.";
            log.error(message, t, true);
            log.flush();
            t.printStackTrace(log.getOut());
            throw new IvarcException(message, t);
        }

        log.emptyLine();
        log.emptyLine();
        log.info("----------------------------------------------");
        log.info("Global status for this build is " + compileOK);
        log.info("----------------------------------------------");
        log.emptyLine();
        log.emptyLine();

        stopWatch.stop();

        log.emptyLine();
        log.emptyLine();
        log.endBlock("Compilation of [" + application.getKeyname() + "] " + application.getName() + " finished.");
        log.info(stopWatch.getDeltaTimeTrace());
    }
}
