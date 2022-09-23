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

public enum RuleKinds {
    calculated, // this imply @transient attribute. contain a piece of js that can be evaluated as a value
    check, // a piece of js that must be true
    statecheckset, // on a scenario or a step. Contain a list of state that must be there for the scenario to start or the step to show
    statechecknotset, // on a scenario or a step. Contain a list of state that must not be there for the scenario to start or the step to show
    stateset, // at the end of a successfull scenario, contain a list of state to set.

    regexp, // A regular expression
    format, // ex : date format like yyy-mm-dd or the number of digit of a number like 5,2 for 5 digit and 2 digit for decimal

    runscenario, // a list a scenario to run

    composite, // A composite constraint is a constraint that contain other constraint, like a procedure. As a consequence, the constraint constraint has no value.
    inherited; // An inherited constraint take all from its parent and add behavior before or after.
}

/*
Note on list of states : it could be a list of simple word applying to the current entity
 but it could also apply to entity link to the current scenario. In that case use the dot notation.
 use partial XML
 <state name="aaa'/>
 <state name="bbb'/>
 <state name="person.aaa'/>
 This will set the state aaa and bbb on the scenario's current entity and state aaa to the person link via the step keyname 'person'
 Partial XML because a <states> ... </states> should be added but is not needed.
 */

/*
Note on list of scenario to run : with or without link
 with link, it mean that a parameters transmition will occurs.
 use XML
 <scenario name="aaa" />
 <scenario name="bbb" link="true"/>
 <scenario name="ccc" link="true">
    <param name="nom" value="@nomdefamille" />
    <param name="prenom" value="Jean" />
 <?scenario>

 This will run (call) the scenario aaa then bbb then ccc.
 aaa is just called,
 bbb is cslled with link with default link behavior. This mean all step in bbb will try to get thei values from caller's scenario based on step name correspondance
 ccc is called but without default link behavior, correspondances from caller's scenario step are given
 Question : what about not specified correspondance ? Does it mean no link has to be done ? Or does it mean automated one should be performed ?
*/
