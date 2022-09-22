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
package ivar.common.logger;

import ivar.common.error.Error;
import ivar.common.error.ErrorMgr;
import ivar.helper.StringHelper;
import ivar.helper.io.IOHelper;
import ivar.helper.oo.ClassHelper;
import java.io.*;
import java.lang.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggerDefaultImplementation implements FullLogger {

    private static final String GREEN_LIGHT = "------ ERROR DUMP : no error, no error, Houston, green light, I repeat, green light.";
    private static int count = 0;
    private int instanceCount = -1;

    private static final class ThreadLocalLogger extends ThreadLocal<LoggerDefaultImplementation> {

        private LoggerDefaultImplementation instance = null;

        @Override
        public LoggerDefaultImplementation initialValue() {
            instance = new LoggerDefaultImplementation(++count);
            return instance;
        }

        @Override
        public void remove() {
            if (instance != null) {
                instance.endOfThreadCleanup();
            }
            instance = null;
            super.remove();
        }
    }

    private static final ThreadLocalLogger threadLocalLogger = new ThreadLocalLogger();

    private static final String OPEN = "[";
    private static final String CLOSE = "] ";
    private static final String CLOSE_OPEN = "] [";
    private static final String OPENPAR = "(";
    private static final String EMPTY = "";
    private static final String ERR = "ERR";
    private static final String WRN = "WRN";
    private static final String INF = "INF";
    private static final String DBG = "DBG";
    private static final String CPLHLP = "CPL HLP";
    private static final String CPLINF = "CPL INF";
    private static final String CPLWRN = "CPL WRN";
    private static final String CPLERR = "CPL ERR";
    private static final String CPLFATAL = "CPL FATAL";
    private static final String FATAL = "FATAL";
    private static final String LINE_AND_TAB = "\n\t";
    private static final char BLANK_CHAR = ' ';
    private static final char PLUS = '+';
    private static final char MINUS = '-';
    private static final String LINE_HEADER = "(-------------------------- --- -- -  ..  .";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static FullLogger getInstance() {
        return LoggerDefaultImplementation.threadLocalLogger.get();
    }

    public static LoggerDefaultImplementation getLoggerInstance() {
        return LoggerDefaultImplementation.threadLocalLogger.get();
    }

    private int tab = 0;
    private String stringTab = null;
    private PrintStream out = null;
    private ErrorMgr errorMgr = null;
    private String applicationKeyname = null;
    // IP address for example
    private String clientTraceString = null;
    private boolean compileOK;

//    // Stick to StringBuffer because of thread.
//    private StringBuffer buffer = new StringBuffer();
//
//    private boolean activeBuffer = false;
    private Writer logfileWriter;
    private File logfile;

    @Override
    protected void finalize() throws Throwable {
        LoggerDefaultImplementation.threadLocalLogger.remove();
        super.finalize();
    }

    protected void endOfThreadCleanup() {
        print(DBG, "--- LoggerDefaultImplementation endOfThreadCleanup() will start. instanceCount is " + instanceCount + ", while count is " + count, EMPTY, true);
        System.out.println("--- " + now() + " LoggerDefaultImplementation endOfThreadCleanup() ...");
        if (out != null) {
            out.flush();
        }

        if (logfileWriter != null) {
            stopBufferingToFile();
        }
        logfileWriter = null;

        if (errorMgr != null) {
            errorMgr.clear();
        }

        logfile = null;
        applicationKeyname = null;
        clientTraceString = null;
        errorMgr = null;
        out = null;
        stringTab = null;
        System.out.println("--- " + now() + " LoggerDefaultImplementation endOfThreadCleanup() done.");
    }

    private LoggerDefaultImplementation(final int count) {
        this.instanceCount = count;
        errorMgr = new ErrorMgr();
    }

    public FullLogger reset(final PrintStream out) {
        if (logfileWriter != null) {
            stopBufferingToFile();
        }
        logfileWriter = null;
        this.compileOK = true;
        this.out = out;
        if (null != errorMgr) {
            errorMgr.clear();
        } else {
            errorMgr = new ErrorMgr();
        }
        tab = 0;
        stringTab = null;
        applicationKeyname = null;
        clientTraceString = null;
        return this;
    }

    public boolean isCompileOK() {
        return compileOK;
    }

    public boolean isBuildOK() {
        beginBlock("Checking build status for " + applicationKeyname + "...");
        boolean result = false;
        final String applicationKeynameForRegularExpression = applicationKeyname.replace("[", "\\[").replace("]", "\\]");
        final Pattern successfulCompilePattern = Pattern.compile("\\[[0-9_]*\\] \\[" + applicationKeynameForRegularExpression + "\\].*\\[INF\\] BUILD SUCCESSFUL");
        final Pattern successfulTargappCompilePattern = Pattern.compile("\\[[0-9_]*\\] \\[" + applicationKeynameForRegularExpression + "\\].*\\[INF\\] " + GREEN_LIGHT);

        System.out.flush();
        try {
            logfileWriter.flush();
        } catch (IOException e) {
            error(e);
        }

        boolean successfulCompile = false;
        boolean successfulTargappCompile = false;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logfile), IOHelper.BUFFER_SIZE);
            String line;
            while ((line = reader.readLine()) != null) {

                if (!successfulTargappCompile) {
                    final Matcher successfulTargappCompileMatcher = successfulTargappCompilePattern.matcher(line);
                    successfulTargappCompile = successfulTargappCompileMatcher.matches();
                }
                if (!successfulCompile) {
                    final Matcher successfulCompileMatcher = successfulCompilePattern.matcher(line);
                    successfulCompile = successfulCompileMatcher.matches();
                }
                if (successfulCompile && successfulTargappCompile) {
                    result = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            error(e);
        } catch (IOException e) {
            error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    error(e);
                } finally {
                    reader = null;
                }
            }
        }
        endBlock("Checking build status for " + applicationKeyname + ". Done : " + result);
        return result;
    }

    public void startBufferingToFile(final String filename) throws IOException {
        try {
            // Stick to non buffered writer because in case of error, the last line will be in the file. Must be sync for debug.
            // logfileWriter = new BufferedWriter(new FileWriter(new File(filename)), 1024);
            // logfileWriter = new FileWriter(new File(filename));
            logfile = new File(filename);
            if (!logfile.exists()) {
                final boolean created = logfile.createNewFile();
                if (created) {
                    info("Log file created. " + logfile.getCanonicalPath());
                }
            } else {
                info("Log file already exists. " + logfile.getCanonicalPath());
            }
            logfileWriter = new BufferedWriter(new FileWriter(logfile), IOHelper.BUFFER_SIZE);
        } catch (IOException e) {
            reportErrorOnLogFile(e);
            throw e;
        }
    }

    public boolean isBufferingToFile() {
        return logfileWriter != null;
    }

    public void stopBufferingToFile() {
        synchronized (LoggerDefaultImplementation.getInstance()) {
            if (isBufferingToFile()) {
                final Writer logfileWriter = this.logfileWriter;
                this.logfileWriter = null;
                try {
                    logfileWriter.flush();
                } catch (IOException e) {
                    reportErrorOnLogFile(e);
                }
                try {
                    logfileWriter.close();
                    // This must be done before the following trace
                    info("Log file successfully closed.");
                } catch (IOException e) {
                    reportErrorOnLogFile(e);
                }
            }
        }
    }

    public void setApplicationKeyname(final String applicationKeyname) {
        this.applicationKeyname = applicationKeyname;
    }

    // IP address for example
    public void setClientTraceString(final String clientTraceString) {
        this.clientTraceString = clientTraceString;
    }

    private String getTab() {
        return stringTab != null ? stringTab : EMPTY;
    }

    public int getTabValue() {
        return tab;
    }

    public void setTabValue(final int t) {
        this.tab = t;
    }

    public void beginBlock() {
        beginBlock(true);
    }

    public void beginBlock(final boolean ts) {
        beginBlock(null, ts);
    }

    public void beginBlock(final String message) {
        beginBlock(message, true);
    }

    public void beginBlock(final String message, final boolean ts) {
        if (message != null) {
            print(INF, PLUS + message, EMPTY, ts);
        }
        tab++;
        computeTabString();
    }

    public void beginCompileHelpBlock(final String message) {
        emptyLine();
        if (message != null) {
            print(CPLHLP, LINE_HEADER, EMPTY, false);
            print(CPLHLP, OPENPAR, EMPTY, false);
            print(CPLHLP, PLUS + message, EMPTY, false);
            print(CPLHLP, OPENPAR, EMPTY, false);
        }
        tab++;
        computeTabString();
    }

    private String now() {
        return dateFormat.format(new Date());
    }

    private void computeTabString() {
        char[] c = new char[2 * tab];
        Arrays.fill(c, BLANK_CHAR);
        stringTab = new String(c);
    }

    public void endBlock() {
        endBlock(true);
    }

    public void endBlock(final boolean ts) {
        endBlock(null, ts);
    }

    public void endBlock(final String message) {
        endBlock(message, true);
    }

    public void endBlock(final String message, final boolean ts) {
        tab--;
        if (tab <= 0) {
            tab = 0;
        }
        computeTabString();
        if (message != null) {
            print(INF, MINUS + message, EMPTY, ts);
        }
    }

    public void endCompileHelpBlock() {
        tab--;
        if (tab <= 0) {
            tab = 0;
        }
        computeTabString();

        print(CPLHLP, OPENPAR, EMPTY, false);
        print(CPLHLP, LINE_HEADER, EMPTY, false);
        emptyLine();
    }

    protected void print(final String pre, final String message, final String post) {
        print(pre, message, post, false);
    }

    public void emptyLine() {
        if (out == null) {
            newOut();
        }
        out.println();
        if (isBufferingToFile()) {
            try {
                logfileWriter.append("\n");
            } catch (IOException e) {
                reportErrorOnLogFile(e);
            }
        }
    }

    @Override
    public void flush() {
        out.flush();
    }

    private void reportErrorOnLogFile(final Throwable t) {
        IOException possibleIOException = null;

        if (logfileWriter != null) {
            // OK, the following look like duplicate of stopBuffering(), but believe me, its not.
            // This also look like simple, isn't it ? But its not, its back belt 9 dan code, believe me petit scarrabee.
            try {
                logfileWriter.flush();
            } catch (IOException e) {
                possibleIOException = e;
            }
            if (possibleIOException != null) {
                error("Houston, still the error during flush() in the error reporting (log file buffering) system. This might helps :", possibleIOException, true);
            }
            possibleIOException = null;

            try {
                logfileWriter.close();
                // This must be done before the following error.
                logfileWriter = null;
                error("Houston, Log file had been successfully closed before reporting an error on reporting ... yeah, I know ...");
            } catch (IOException e) {
                possibleIOException = e;
            } finally {
                // This is done so in any case (error or not) this is null. I know this duplicate that line ...
                logfileWriter = null;
            }
        }

        error("Houston, We've got a problem, Apollo XIII scenario ! An error occurs in the error reporting (log file buffering) system.", t, true);
        if (possibleIOException != null) {
            error("Houston, still the error during close() in the error reporting (log file buffering) system. This might helps :", possibleIOException, true);
        }
    }

    protected void print(final String pre, final String message, final String post, final boolean timeStamp) {
        String clientTraceStringToPrint = null;
        if (clientTraceString != null) {
            clientTraceStringToPrint = OPEN + clientTraceString + CLOSE;
        }
        if (out == null) {
            newOut();
        }
        String toPrint = null;
        if (timeStamp) {
            if (applicationKeyname == null) {
                toPrint = StringHelper.concat(OPEN, instanceCount, CLOSE, clientTraceStringToPrint, getTab(), OPEN, pre, CLOSE_OPEN, now(), CLOSE, message, post);
            } else {
                toPrint = StringHelper.concat(OPEN, instanceCount, CLOSE_OPEN, applicationKeyname, CLOSE, clientTraceStringToPrint, getTab(), OPEN, pre, CLOSE_OPEN, now(), CLOSE, message, post);
            }
        } else {
            if (applicationKeyname == null) {
                toPrint = StringHelper.concat(OPEN, instanceCount, CLOSE, clientTraceStringToPrint, getTab(), OPEN, pre, CLOSE, message, post);
            } else {
                toPrint = StringHelper.concat(OPEN, instanceCount, CLOSE_OPEN, applicationKeyname, CLOSE, clientTraceStringToPrint, getTab(), OPEN, pre, CLOSE, message, post);
            }
        }
        out.println(toPrint);
        if (isBufferingToFile()) {
            try {
                logfileWriter.append(toPrint);
                logfileWriter.append("\n");
            } catch (IOException e) {
                reportErrorOnLogFile(e);
            }
        }
//        if (out != System.out && out != System.err) System.out.println("(sout) " + toPrint);
    }

    private void newOut() {
        final String errormessage = "!!! out is null in " + ClassHelper.getShortName(this.getClass()) + ". Set System.out";
        reset(System.out);
        debug(errormessage);
    }

    public void error(final Throwable t) {
        error(t, false);
    }

    public void error(final String message) {
        error(message, null, false);
    }

    public void error(final Throwable t, final boolean fatal) {
        error(null, t, fatal);
    }

    public void error(final String message, final boolean fatal) {
        error(message, null, fatal);
    }

    public void error(final String message, final Throwable t) {
        error(message, t, false);
    }

    public void error(final String message, final Throwable t, final boolean fatal) {
        error(false, message, t, fatal);
    }

    public void compileError(final Throwable t) {
        compileError(t, false);
    }

    public void compileError(final String message) {
        compileError(message, null, false);
    }

    public void compileError(final Throwable t, final boolean fatal) {
        compileError(null, t, fatal);
    }

    public void compileError(final String message, final boolean fatal) {
        compileError(message, null, fatal);
    }

    public void compileError(final String message, final Throwable t) {
        compileError(message, t, false);
    }

    public void compileError(final String message, final Throwable t, final boolean fatal) {
        compileOK = false;
        emptyLine();
        error(true, message, t, fatal);
    }

    public void warning(final String message) {
        print(WRN, message, EMPTY);
    }

    public void compileWarning(final String message) {
        print(CPLWRN, message, EMPTY);
    }

    public void compileHelp(final String message) {
        print(CPLHLP, message, EMPTY);
    }

    public void compileInfo(final String message) {
        print(CPLINF, message, EMPTY);
    }

    public void info(final String message) {
        print(INF, message, EMPTY);
    }

    public void debug(final String message) {
        print(DBG, message, EMPTY);
    }

    private String getExceptionMessage(final Throwable t) {
        final String exceptionMessage = t.getMessage();
        return (t.getClass() != null ? ClassHelper.getShortName(t.getClass()) : "Throwable t => t is null, yes sir, null !") + " : " + ((exceptionMessage != null && exceptionMessage.length() > 0) ? exceptionMessage : "The exception has no message in it");
    }

    private void error(final boolean compile, final String message, final Throwable t, final boolean fatal) {
        final boolean ischild = message == null;
        String m = message;
        if (t != null) {
            if (m != null && m.length() > 0) {
                m += LINE_AND_TAB;
                m += getExceptionMessage(t);
            } else {
                m = getExceptionMessage(t);
            }
        }
        String prefix = ERR;
        if (compile) {
            if (fatal) {
                prefix = CPLERR;
            } else {
                prefix = CPLFATAL;
            }
        } else {
            if (fatal) {
                prefix = FATAL;
            }
        }
        print(prefix, m, EMPTY);

        if (fatal && t != null) {
            t.printStackTrace(out);
            if (out != System.err) {
                t.printStackTrace(System.err);
            }
        }
        if (t != null) {
            Throwable child = t.getCause();
            if (child != null) {
                beginBlock("Caused by " + getExceptionMessage(child));
                error(compile, null, child, fatal);
                endBlock();
            }
        }
        if (!ischild) {
            errorMgr.addError(m, fatal);
        }
    }

    public void dumpException(final Throwable t) {
        if (t != null) {
            beginBlock(ClassHelper.getShortName(t.getClass()) + " : " + t.getMessage());
            t.printStackTrace(out);
            if (out != System.out && out != System.err) {
                t.printStackTrace(System.err);
            }
            dumpException(t.getCause());
            endBlock();
        }
    }

    public PrintStream getOut() {
        return out;
    }

    public void errorReport() {
        emptyLine();
        emptyLine();
        final Iterator<Error> errorIterator = errorMgr.getErrors();
        if (errorIterator.hasNext()) {
            info("------ BEGIN ERROR DUMP : Houston, we've got a problem : -----");
            while (errorIterator.hasNext()) {
                final Error error = errorIterator.next();
                info("[ERR DUMP] fatal=" + error.isFatal() + ", " + error.getMessage());
            }
            info("------ END ERROR DUMP -----");
        } else {
            info(GREEN_LIGHT);
            info("OK Houston, application launched !");
        }
        emptyLine();
        emptyLine();
    }
}
