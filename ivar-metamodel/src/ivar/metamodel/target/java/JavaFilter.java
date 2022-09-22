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
package ivar.metamodel.target.java;

import ivar.common.AbstractObject;
import ivar.common.fwk.RuntimeFilterElement;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.spec.FilterModes;
import ivar.metamodel.target.TargetFilter;
import ivar.metamodel.target.generic.BlankFilter;
import ivar.metamodel.target.generic.BlankFilterElement;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaFilter extends AbstractObject implements TargetFilter {

    private BlankFilter blankFilter;
    private JavaBusinessObject javaBusinessObject;
    private Set<JavaFilterElement> filterElements = CollectionFactory.newSetWithInsertionOrderPreserved();
    private Set<JavaFilterElement> nonBooleanOperatorFilterElements = CollectionFactory.newSetWithInsertionOrderPreserved();

    public JavaFilter(final BlankFilter blankFilter, final JavaBusinessObject javaBusinessObject) {
        this.blankFilter = blankFilter;
        this.javaBusinessObject = javaBusinessObject;
        for (final BlankFilterElement blankFilterElement : blankFilter.getFilterElements()) {
            if (!blankFilterElement.isBooleanOperator()) {
                final List<BusinessObjectAttribute> objectAttributesPath = blankFilterElement.getAssociatedBusinessObjectAttributesPath();
                JavaBusinessAttribute javaBusinessAttribute = null;

                int correctAttributes = 0;
                Collection<JavaBusinessAttribute> currentJavaAttributes = javaBusinessObject.getAttributes().values();
                for (final BusinessObjectAttribute boAttribute : objectAttributesPath) {
                    debug("searching attribute " + boAttribute.getKeyname());
                    boolean checkNextAttributeInPath = false;
                    for (final JavaBusinessAttribute tmpJavaAttribute : currentJavaAttributes) {
                        if (tmpJavaAttribute.getBusinessObjectAttribute() == boAttribute) {
                            debug("searching attribute " + boAttribute.getKeyname() + " - FOUND.");
                            javaBusinessAttribute = tmpJavaAttribute;
                            correctAttributes++;
                            final JavaBusinessObject attributeType = tmpJavaAttribute.getTypeAsJavaBusinessObject();
                            if (attributeType != null) {
                                debug("preparing to search next attribute");
                                currentJavaAttributes = attributeType.getAttributes().values();
                                checkNextAttributeInPath = true;
                                break;
                            }
                        }
                    }
                    if (!checkNextAttributeInPath) {
                        break;
                    }
                }

                debug("found " + correctAttributes + " correct attributes out of " + objectAttributesPath.size());

                if (javaBusinessAttribute == null || correctAttributes != objectAttributesPath.size()) {
                    BusinessObjectAttribute objectAttribute = objectAttributesPath.get(objectAttributesPath.size() - 1);
                    compileError("JavaFilter " + getKeyname() + ". Unable to find an associated JavaAttribute for the FilterElement " + blankFilterElement.getKeyname());
                    beginCompileHelpBlock("For Helping purpose, we were seeking the BusinessObjectAttribute " + objectAttribute.getTypeName() + " " + objectAttribute.getKeyname() + " [" + objectAttribute.getName() + "] with full attribute path " + blankFilterElement.getAssociatedBusinessObjectAttributesPathAsString() + " among the following JavaBusinessAttribute:");
                    for (final JavaBusinessAttribute javaAttribute : javaBusinessObject.getAttributes().values()) {
                        compileHelp("  " + javaAttribute.getType() + " " + javaAttribute.getKeyname() + " [" + javaAttribute.getName() + "]");
                    }
                    endCompileHelpBlock();
                }
                filterElements.add(new JavaFilterElement(blankFilterElement, javaBusinessAttribute));
            } else {
                filterElements.add(new JavaFilterElement(blankFilterElement, null));
            }
        }

        for (final JavaFilterElement filterElement : filterElements) {
            if (!filterElement.isBooleanOperator()) {
                nonBooleanOperatorFilterElements.add(filterElement);
            }
        }
    }

    public String getServerMethodJavadoc() {
        String doc = blankFilter.getDocumentation();
        if (doc == null || doc.length() == 0) {
            doc = "Not documented in the specification.";
        }
        return doc;
    }

    public Set<JavaFilterElement> getFilterElements() {
        return filterElements;
    }

    public FilterModes getMode() {
        return blankFilter.getMode();
    }

    public String getModeForDoc() {
        return StringHelper.getFirstCapitalizedAndLoweredTheRest(getMode().toString());
    }

    public boolean isDynamic() {
        return FilterModes.dynamicf.equals(blankFilter.getMode());
    }

    public boolean hasRuntimeVariable() {
        return blankFilter.hasRuntimeVariable();
    }

    public List<String> getRuntimeVariables() {
        return blankFilter.getRuntimeVariables();
    }

    public String getKeyname() {
        return blankFilter.getKeyname();
    }

    public Set<JavaFilterElement> getNonBooleanOperatorFilterElements() {
        return nonBooleanOperatorFilterElements;
    }

    public String getStaticRequest(final String variableName) {
        String result = "";
        for (final JavaFilterElement filterElement : filterElements) {
            if (filterElement.isBooleanOperator()) {
                result += filterElement.getOperator();
                result += " ";
            } else {
                if (filterElement.isRuntimeVariable()) {
                    result += variableName + "." + filterElement.getKeyname() + " " + filterElement.getOperator() + " :" + filterElement.getParamName();
                } else {
                    if (filterElement.isBooleanValue()) {
                        if (filterElement.getValue().equalsIgnoreCase("true")) {
                            result += variableName + "." + filterElement.getKeyname() + " " + filterElement.getOperator() + " TRUE";
                        } else {
                            result += variableName + "." + filterElement.getKeyname() + " " + filterElement.getOperator() + " FALSE";
                        }
                    } else {
                        result += variableName + "." + filterElement.getKeyname() + " " + filterElement.getOperator() + " '" + filterElement.getValue() + "'";
                    }
                }
            }
        }
        return result;
    }

    public Map<String, RuntimeFilterElement> getDynamicRequest(final String variableName) {
        final Map<String, RuntimeFilterElement> result = CollectionFactory.newMapWithInsertionOrderPreserved(filterElements.size());
        String booleanOperator = null;

        beginBlock("JavaFilter.getDynamicRequest(" + variableName + ")");

        for (final JavaFilterElement filterElement : filterElements) {
            if (filterElement.isBooleanOperator()) {
                booleanOperator = filterElement.getOperator();
            } else {
                debug("   - Computing request part for FilterElement " + filterElement.toString());
                final String filterKeyname = filterElement.getKeyname();
                final boolean asDot = filterKeyname.contains(".");
                boolean isCollection = false;
                String attributeName = null;
                if (asDot) {
                    attributeName = StringHelper.getLeft(filterKeyname, ".");
                    isCollection = javaBusinessObject.getAttribute(attributeName).isCollection();
                }
                // Still TODO : check if collection SI EMPTY. FilterElement should be cepage is null nothing to do with cepage.nom ...
                if (isCollection) {
                    final String requestJoinElement = variableName + "." + attributeName + " " + attributeName;
                    final String requestWhereElement = filterElement.getKeyname() + " " + filterElement.getOperator() + " :" + filterElement.getParamName();
                    final String requestWhereElementAgainstNull = filterElement.getKeyname() + " IS NULL";
                    result.put(filterElement.getParamName(), new RuntimeFilterElement(booleanOperator, requestJoinElement, requestWhereElement, requestWhereElementAgainstNull));
                } else {
                    final String requestWhereElement = variableName + "." + filterElement.getKeyname() + " " + filterElement.getOperator() + " :" + filterElement.getParamName();
                    final String requestWhereElementAgainstNull = variableName + "." + filterElement.getKeyname() + " IS NULL";
                    result.put(filterElement.getParamName(), new RuntimeFilterElement(booleanOperator, requestWhereElement, requestWhereElementAgainstNull));
                }
                booleanOperator = null;
            }
        }
        endBlock("JavaFilter.getDynamicRequest(" + variableName + ")");
        return result;
    }

    public void free() {
        blankFilter = null;
        javaBusinessObject = null;

        for (final JavaFilterElement filter : filterElements) {
            filter.free();
        }
        filterElements.clear();
        filterElements = null;

        for (final JavaFilterElement filter : nonBooleanOperatorFilterElements) {
            filter.free();
        }
        nonBooleanOperatorFilterElements.clear();
        nonBooleanOperatorFilterElements = null;
    }
}
