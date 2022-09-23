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

/*
Note on list of states : it could be a list of simple word applying to the current entity
 */
public enum RuleHooks {

    scenariobefore, // before a ascenario start
    scenarioafter, // after a ascenario successfully finished
    scenarioerror, // after a scenario failed
    scenariofinally, // after scenario after or scenarioerror. Like finally in Java.
    stepbefore, // before a step (just when the field gain the focus)
    stepafter, // after a step (just after the field lose the focus)
    useraction, // when user decide (for example end user click on a button)

    inheritedcomposite, // This correspond to a child composite constraint. A child constraint inherit its hook from the root constraint.
    inheritedbefore, // This apply to inherited constraint kind that add behavior before an existing constraint.
    inheritedafter; // This apply to inherited constraint kind that add behavior after an existing constraint.

    public boolean notStep() {
        return !RuleHooks.stepbefore.equals(this) && !RuleHooks.stepafter.equals(this);
    }
}
