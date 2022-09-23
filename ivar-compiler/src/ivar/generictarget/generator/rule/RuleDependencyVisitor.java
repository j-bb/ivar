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

import ivar.common.logger.FullLogger;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.target.generic.rule.DependencyDesc;
import ivar.metamodel.target.generic.rule.FunctionDesc;
import ivar.metamodel.target.generic.rule.IdentifierDesc;
import ivar.metamodel.target.generic.rule.ParamDesc;
import ivar.metamodel.target.generic.rule.SumFunctionDesc;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.scriptonite.node.*;

public class RuleDependencyVisitor extends RuleDependencyVisitorWithLog {

    private Set<ParamDesc> resultingVariables = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Stack<String> currentFunction = CollectionFactory.newStack();
    private Stack<Set<IdentifierDesc>> currentfunctionParams = CollectionFactory.newStack();
    private Map<String, FunctionDesc> resultingFunctions = CollectionFactory.newMapWithInsertionOrderPreserved();
    private String initialRule;
    private String finalRule;

    public RuleDependencyVisitor(final FullLogger logger, final String initialRule, final String finalRule) {
        super(logger);
        this.initialRule = initialRule;
        this.finalRule = finalRule;
    }

    private String clean(final String s) {
        return StringHelper.clean(s.trim()).trim();
    }

    private void trace(final String s) {
        debug("             >>> " + s);
    }

//    @Override
//    public void defaultIn(Node node) {
//        trace("defaultIn " + node.toString());
//    }
//
//    @Override
//    public void defaultOut(Node node) {
//        trace("defaultOut " + node.toString());
//    }
//
//    @Override
//    public void defaultCase(Node node) {
//        trace("defaultCase " + node.toString());
//    }
//
//    @Override
//    public void caseEOF(EOF eof) {
//    }
    @Override
    public void outStart(Start start) {
        //finalRule = start.getPProgram().toString().trim();
        //trace("outStart " + finalRule);
    }

    @Override
    public void caseAIdentifierPrimaryExpression(AIdentifierPrimaryExpression node) {
        super.caseAIdentifierPrimaryExpression(node);
        final String dependency = clean(node.getIdentifier().getText());

        if (currentFunction.empty() || !dependency.equals(currentFunction.lastElement())) {
            String futureParamName = dependency;
            if (!currentFunction.empty()) {
                currentfunctionParams.lastElement().add(new ParamDesc(futureParamName));
                trace("caseAIdentifierPrimaryExpression > " + futureParamName + " (added as a function param)");
            }
            resultingVariables.add(new ParamDesc(futureParamName));
            trace("caseAIdentifierPrimaryExpression > " + futureParamName + " (added as a dependency)");
        } else {
            trace("caseAIdentifierPrimaryExpression > " + dependency + " (IGNORE)");
        }
    }

    @Override
    public void caseAMemberDotMemberExpression(AMemberDotMemberExpression aMemberDotMemberExpression) {
        String expression = aMemberDotMemberExpression.toString();
        trace("caseAMemberDotMemberExpression > " + expression);
        TIdentifier right = aMemberDotMemberExpression.getIdentifier();
        trace("caseAMemberDotMemberExpression > getIdentifier = " + right);
        PMemberExpression left = aMemberDotMemberExpression.getMemberExpression();
        trace("caseAMemberDotMemberExpression > getMemberExpression = " + left);
        String futureParamName = left.toString().trim() + "." + right.getText().trim();
        if (!currentFunction.empty()) {
            currentfunctionParams.lastElement().add(new ParamDesc(futureParamName));
            trace("caseAMemberDotMemberExpression > " + futureParamName + " (added as a function param)");
        }
        trace("caseAMemberDotMemberExpression > " + futureParamName + " (added as a dependency)");
        futureParamName = futureParamName.replace(" . ", ".");
        resultingVariables.add(new ParamDesc(futureParamName));
    }

    @Override
    public void caseAMemberCallExpression(AMemberCallExpression aMemberCallExpression) {
        super.caseAMemberCallExpression(aMemberCallExpression);
        final String functionName = currentFunction.lastElement();
        final Set<IdentifierDesc> functionParams = currentfunctionParams.lastElement();
        final FunctionDesc function = "sum".equals(functionName) ? new SumFunctionDesc(functionParams) : new FunctionDesc(functionName, functionParams);
        resultingFunctions.put(function.getName(), function);
        info("  -- preparing for a function. Done : " + functionName);
        currentFunction.pop();
        currentfunctionParams.pop();
        if (!currentFunction.empty()) {
            currentfunctionParams.lastElement().add(function);
        }
    }

    @Override
    public void inAMemberCallExpression(AMemberCallExpression aMemberCallExpression) {
        info("  -- preparing for a function ... ");
        currentFunction.push(clean(aMemberCallExpression.getMemberExpression().toString()));
        currentfunctionParams.push(CollectionFactory.<IdentifierDesc>newSetWithInsertionOrderPreserved());
        trace("inAMemberCallExpression > " + currentFunction.lastElement() + " ---> " + aMemberCallExpression.getArguments().toString());
    }

    @Override
    public void caseAAssignArgumentList(AAssignArgumentList aAssignArgumentList) {
        super.caseAAssignArgumentList(aAssignArgumentList);
        final String paramAndOrExpression = clean(aAssignArgumentList.getAssignmentExpression().toString());
        trace("[DISABLED] caseAAssignArgumentList > " + paramAndOrExpression);
    }

    @Override
    public void caseAAssignListArgumentList(AAssignListArgumentList aAssignListArgumentList) {
        super.caseAAssignListArgumentList(aAssignListArgumentList);
        final String paramAndOrExpression = clean(aAssignListArgumentList.getAssignmentExpression().toString());
        trace("[DISABLED] caseAAssignListArgumentList > " + paramAndOrExpression);
    }

    public DependencyDesc getDependencyDesc() {
        return new DependencyDesc(initialRule, resultingVariables, resultingFunctions, finalRule);
    }
}
