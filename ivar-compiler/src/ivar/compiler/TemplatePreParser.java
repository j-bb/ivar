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
package ivar.compiler;

import ivar.common.AbstractObject;
import ivar.helper.io.IOHelper;
import ivar.helper.io.ZipHelper;
import ivar.metamodel.spec.Scenario;
import java.io.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This pre-parser is not perfect. It only checks for the root elements in
 * velocity references (e.g. ${application} is checked but ${application.getCarrots()}
 * only checks the existence of "application", and not the one of "getCarrots()" ...
 * If "carrots" is not a step of the current scenario the parsing will still return true.
 *
 * However, Velocity is an interpreted language and there is no compiler for it.
 * We probably could write one with SableCC that would do a full check for consistency
 * but it would cost a lot of time and effort.
 *
 * Trying to capture all the "${.*}" patterns in the file is not a solution either; in
 * the following case :
 *      #set($a = ${application})
 *      ${a.getKeyname()}
 *
 * we would return false although it should be alright.
 *
 * The current pre-parser can produce false positives, but not false negatives; we keep it
 * as is for the moment until we can apply a real, full, thorough solution.
 */
public class TemplatePreParser extends AbstractObject {

    private File file = null;
    private static final Pattern velocityIdentifierPattern = Pattern.compile("\\$\\{?([a-zA-Z][a-zA-Z0-9_]*)(?:\\.[a-zA-Z][a-zA-Z0-9_]*(?:\\(\\))?)*\\}?");
    private static final Pattern setPattern;
    private static final Pattern foreachPattern;
    private static final Pattern arithPattern;
    private static final String unzipTargetDir = System.getProperty("java.io.tmpdir");

    static {
        final String velocityIdentifier = velocityIdentifierPattern.pattern();
        final String literal = "\"([^\"]|(?<=\\\\)\")*\"";
        final String arithOp = "(?:\\s)*(?:(\\*|\\+|-|/|%))(?:\\s)*";
        final String number = "[0-9][0-9]*(\\.[0-9]+)?";
        final String range = "\\[-?[0-9]+(?:\\s)*\\.\\.(?:\\s)*-?[0-9]+\\]";
        final String arith = "((" + number + ")|(" + velocityIdentifier + "))" + "(" + arithOp + "((" + number + ")|(" + velocityIdentifier + "))" + ")*";
        final String set = "\\#set\\((?:\\s)*\\$([a-zA-Z][A-Za-z_0-9]*)(?:\\s)*=(?:\\s)*" + "((" + arith + ")|(" + literal + "))(?:\\s)*\\)";
        final String foreach = "\\#foreach\\((?:\\s)*\\$([a-zA-Z][a-zA-Z0-9_]*)(?:\\s)*in(?:\\s)*((?:" + velocityIdentifier + ")|(?:" + range + "))(?:\\s)*\\)";
        arithPattern = Pattern.compile(arith);
        setPattern = Pattern.compile(set);
        foreachPattern = Pattern.compile(foreach);
    }

    public TemplatePreParser(final String inputFile) {
        this(new File(inputFile));
    }

    public TemplatePreParser(final File inputFile) {
        file = inputFile;
    }

    public boolean checkAgainstContext(final Scenario scenario) {
        boolean result = true;
        if (file.exists()) {
            Set<File> matches = null;
            try {
                matches = ZipHelper.unzipMatching(file.getAbsolutePath(), unzipTargetDir, "content\\.xml");
            } catch (IOException e) {
                error("Unzipping of " + file.getAbsolutePath() + " has failed.", e, true);
                result = false;
            }
            if (matches == null) {
                result = false;
                error("No matches for content.xml");
            } else if (matches.size() != 1) {
                error(matches.size() + " elements named content.xml were found, expected one");
            } else {
                info("Unique content.xml file found, checking it against context.");
                for (final File contentXML : matches) {
                    final Set<String> contextKeys = completeContext(contentXML, scenario);
                    result = checkFileContext(contentXML, scenario, contextKeys) && result;
                }
            }
        } else {
            result = false;
        }
        return result;
    }

    private Set<String> completeContext(final File file, final Scenario scenario) {
        final Set<String> contextKeys = scenario.getTemplateContextKeys();
        if (!file.exists()) {
            error("Reading the content.xml file has failed because the file doesn't exist.");
        } else {
            try {
                final BufferedReader reader = new BufferedReader(new FileReader(file), IOHelper.BUFFER_SIZE);
                while (reader.ready()) {
                    final String readString = reader.readLine();
                    final Matcher setMatcher = setPattern.matcher(readString);
                    while (setMatcher.find()) {
                        final String group = setMatcher.group();
                        final String identifier = setMatcher.group(1);
                        debug("Found SET directive <" + group + "> defining a new identifier " + identifier + ".");
                        contextKeys.add(identifier);
                    }

                    final Matcher foreachMatcher = foreachPattern.matcher(readString);
                    while (foreachMatcher.find()) {
                        final String group = foreachMatcher.group();
                        final String identifier = foreachMatcher.group(1);
                        debug("Found FOREACH directive <" + group + "> defining a new identifier " + identifier + ".");
                        contextKeys.add(identifier);
                    }
                }
            } catch (FileNotFoundException e) {
                error("Reading the content.xml file has failed because the file doesn't exist.", e);
            } catch (IOException e) {
                error("Reading the content.xml file has failed.", e);
            }
        }
        return contextKeys;
    }

    private boolean checkFileContext(final File file, final Scenario scenario, final Set<String> contextKeys) {
        boolean result = true;
        if (!file.exists()) {
            result = false;
            error("Reading the content.xml file has failed because the file doesn't exist.");
        } else {
            try {
                final BufferedReader reader = new BufferedReader(new FileReader(file), IOHelper.BUFFER_SIZE);
                int count = 0;
                while (reader.ready()) {
                    final String readString = reader.readLine();
                    final Matcher matcher = velocityIdentifierPattern.matcher(readString);
                    while (matcher.find()) {
                        final String group = matcher.group();
                        if (group.matches("\\$\\{?hash\\}?") || group.matches("\\$\\{?dollar\\}?")) {
                            debug("Found " + group + ", which is always valid. Skipping...");
                            continue;
                        }
                        count++;
                        final String ctxValue = matcher.group(1);
                        if (!group.startsWith("${")) {
                            debug("Context key " + ctxValue + " in expr " + group + " is NOT CHECKED because it is a $a form and only ${a} are checked.");
                            continue;
                        }
                        if (!contextKeys.contains(ctxValue) && !ctxValue.equals("generator")) {
                            compileError("Context key " + ctxValue + " is used in template " + file.getName() + " but will not be present in runtime context: scenario " + scenario.getKeyname());
                            beginCompileHelpBlock("For helping purpose, here is the full expression you tried to use : " + group);
                            compileHelp("Here are the keys that can be used in this template.");
                            for (String key : contextKeys) {
                                compileHelp("\t$" + key);
                            }
                            endCompileHelpBlock();
                            result = false;
                        } else {
                            debug("Context key " + ctxValue + " in expr " + group + " is valid.");
                        }
                    }
                }
                info("Counted " + count + " contextual expressions in document");
            } catch (FileNotFoundException e) {
                error("Reading the content.xml file has failed because the file doesn't exist.", e);
                result = false;
            } catch (IOException e) {
                error("Reading the content.xml file has failed.", e);
                result = false;
            }
        }
        return result;
    }

    /* For unit tests... */
    public boolean isNumber(final String test) {
        return test.matches("[0-9][0-9]*(\\.[0-9]+)?");
    }

    public boolean isLiteral(final String test) {
        return test.matches("\"([^\"]|(?<=\\\\)\")*\"");
    }

    public boolean isArithmeticExpr(final String test) {
        return arithPattern.matcher(test).matches();
    }

    public boolean isSetDirective(final String test) {
        return setPattern.matcher(test).matches();
    }

    public boolean isForeachDirective(final String test) {
        return foreachPattern.matcher(test).matches();
    }
}
