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
package ivar.metamodel.target.generic;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.Rule;
import java.util.Collection;
import ivar.helper.CollectionFactory;

public class WorkflowRule extends AbstractObject {

    private Rule rule;
    private Collection<String> states = CollectionFactory.newList();

    public WorkflowRule(Rule rule) {
        this.rule = rule;
        parseStates();
    }

    public Rule getRule() {
        return rule;
    }

    public Collection<String> getStates() {
        return states;
    }

    private void addState(final String state) {
        states.add(state);
    }

    private void parseStates() {
        final String value = getRule().getValue();
        for (String state : value.split(",")) {
            final String newState = state.trim();
            addState(newState);
            info("[Workflow] new state discovered " + newState);
            if (newState.length() > 20) {
                error("[Workflow] state too long. Length is limited to 20 for " + newState, true);
            }
        }
    }

}
