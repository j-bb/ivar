
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

import java.util.Set;

public class SumFunctionDesc extends FunctionDesc {

    public SumFunctionDesc(final IdentifierDesc... params) {
        super("sum", params);
        check();
    }

    public SumFunctionDesc(final Set<IdentifierDesc> params) {
        super("sum", params);
        check();
    }

    public IdentifierDesc getFirstParam() {
        final Set<IdentifierDesc> params = getParams();
        return params.toArray(new IdentifierDesc[params.size()])[0];
    }

    private void check() throws IllegalArgumentException {
        final int size = getParams().size();
        if (size != 1) {
            error();
            compileError("Method sum must be called with only one argument, this one is call using " + size + " arguments : the path to the step to sum up. Example : sum(bills.price)");
            beginCompileHelpBlock("For helping purpose, here the wrong sum function encountered");
            compileHelp(toString());
            endCompileHelpBlock();
        } else if (size == 1) {
            if (!getFirstParam().getName().contains(".")) {
                error();
                compileError("Method sum must be called with a dot argument : the path to the step to sum up. Example : sum(bills.price)");
                beginCompileHelpBlock("For helping purpose, here the wrong sum function encountered");
                compileHelp(toString());
                endCompileHelpBlock();
            }
        }
    }

    @Override
    public String toString() {
        return "Sum FunctionDesc{ "
                + super.toString()
                + " } ";
    }
}
