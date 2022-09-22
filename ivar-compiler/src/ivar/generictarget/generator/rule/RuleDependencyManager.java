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
package ivar.generictarget.generator.rule;

import ivar.common.AbstractObject;
import ivar.helper.StringHelper;
import ivar.metamodel.target.generic.rule.DependencyDesc;
import ivar.metamodel.target.generic.rule.FunctionDesc;
import ivar.metamodel.target.generic.rule.IdentifierDesc;
import ivar.metamodel.target.generic.rule.ParamDesc;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scriptonite.lexer.Lexer;
import org.scriptonite.lexer.LexerException;
import org.scriptonite.node.Start;
import org.scriptonite.parser.Parser;
import org.scriptonite.parser.ParserException;

public class RuleDependencyManager extends AbstractObject {

    private static final String regex = "(.*)sum\\((.*)\\.(.*)\\)(.*)";
    private static final Pattern pattern = Pattern.compile(regex);

    public DependencyDesc getDependencyDesc(final String originalRule, final String ruleKeyname) throws LexerException, IOException, ParserException {
        emptyLine();
        beginBlock("Parse rule for dependencies " + ruleKeyname);
        info("----- Begin original rule dump -----");
        info(originalRule);
        info("----- End original rule dump -------");

        //final String rule = engineFunction(originalRule);
        // Create a Parser instance.
        final PushbackReader reader = new PushbackReader(new StringReader(originalRule), 1024);
        DependencyDesc dependencyDesc = null;
        try {
            // create lexer
            final Lexer lexer = new Lexer(reader);

            // parser program
            final Parser parser = new Parser(lexer);
            final Start start = parser.parse();

            final RuleDependencyVisitor visitor = new RuleDependencyVisitor(getLogger(), originalRule, engineFunction(originalRule));
            if (start != null) {
                start.apply(visitor);
            }

            dependencyDesc = visitor.getDependencyDesc();

            final Set<ParamDesc> variables = dependencyDesc.getVariables();
            info("Found " + variables.size() + " dependencies :");
            for (final ParamDesc dependency : variables) {
                info("  - " + dependency.getName());
            }
            final Map<String, FunctionDesc> functions = dependencyDesc.getFunctions();
            info("Found " + functions.size() + " functions :");
            for (final String functionKey : functions.keySet()) {
                String params = "";
                boolean first = true;
                for (final IdentifierDesc param : functions.get(functionKey).getParams()) {
                    if (first) {
                        first = false;
                    } else {
                        params += ", ";
                    }
                    params += param;
                }
                info("  - " + functionKey + "(" + params + ")");
            }
            beginBlock("Initial rule", false);
            debug(dependencyDesc.getInitialRule());
            endBlock("Initial rule ", false);

            beginBlock("Final rule", false);
            info(dependencyDesc.getRule());
            endBlock("Final rule ", false);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        endBlock("Parse rule for dependencies " + ruleKeyname);
        return dependencyDesc;
    }

    private String engineFunction(String rule) {
        beginBlock("Compiling Function ");
        final Matcher matcher = pattern.matcher(rule);
        if (matcher.matches()) {
            final int groupCount = matcher.groupCount();
            debug("   GROUP COUNT = " + groupCount);
            for (int i = 0; i <= groupCount; i++) {
                debug("   group " + i + " = " + matcher.group(i));

            }
            rule = matcher.replaceAll("$1this.sum" + StringHelper.getFirstCapitalized(matcher.group(2)) + StringHelper.getFirstCapitalized(matcher.group(3)) + "()$4");
        }
        debug("----- Begin rule dump after having compile Function -----");
        debug(rule);
        debug("----- End rule dump after having compile Function -----");
        endBlock("Compiling Function ");
        return rule;
    }
}
