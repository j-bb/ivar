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
package ivar.metamodel.target.generic.rule;

import java.util.Map;
import java.util.Set;

public class DependencyDesc {

    private String initialRule;
    private Set<ParamDesc> variables;
    private Map<String, FunctionDesc> functions;
    private String rule;

    public DependencyDesc(final String initialRule, final Set<ParamDesc> variables, final Map<String, FunctionDesc> functions, final String rule) {
        this.initialRule = initialRule;
        this.variables = variables;
        this.functions = functions;
        this.rule = rule;
    }

    public Set<ParamDesc> getVariables() {
        return variables;
    }

    public Map<String, FunctionDesc> getFunctions() {
        return functions;
    }

    public Set<IdentifierDesc> getFunctionParams(final String functionName) {
        return functions.get(functionName).getParams();
    }

    public String getInitialRule() {
        return initialRule;
    }

    public String getRule() {
        return rule;
    }

    public boolean isError() {
        boolean result = false;
        for (final ParamDesc paramDesc : variables) {
            if (paramDesc.isError()) {
                result = true;
                break;
            }
        }
        if (!result) {
            for (final FunctionDesc function : functions.values()) {
                if (function.isError()) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
