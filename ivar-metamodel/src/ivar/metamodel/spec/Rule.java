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
package ivar.metamodel.spec;

import ivar.metamodel.SpecElement;
import ivar.helper.StringHelper;

public class Rule extends SpecElement {

    /**
     * This is the rule's value like * 30, given that the keyname is 'length', then the length is 30 :-)
     */
    private String value;
    private RuleKinds kind = RuleKinds.check;
    private RuleHooks hook = RuleHooks.stepbefore;
    private boolean constantRule;

    public Rule() {
    }

    public Rule(final RuleKinds kind, final RuleHooks hook) {
        this.kind = kind;
        this.hook = hook;
    }

    public String getValue() {
        String result = value;
        if (kind == RuleKinds.calculated) {
            result = StringHelper.ensureOneLastSemicolon(result);
        }
        return result;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RuleKinds getKind() {
        return kind;
    }

    public RuleHooks getHook() {
        return hook;
    }

    @Override
    public Rule getJPAClone() {
        return getJPAClone(new Rule());
    }

    protected Rule getJPAClone(Rule dst) {
        super.getJPAClone(dst);

        if (value != null) {
            dst.value = new String(value);
        } else {
            dst.value = null;
        }

        dst.kind = kind;
        dst.hook = hook;

        return dst;
    }

    public void free() {
        kind = null;
        hook = null;
    }

    public void setConstantRule() {
        this.constantRule = true;
    }

    public boolean isConstantRule() {
        return constantRule;
    }
}
