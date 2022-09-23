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

import ivar.metamodel.spec.FilterOperators;
import ivar.metamodel.target.TargetFilterElement;
import ivar.metamodel.target.generic.BlankFilterElement;
import ivar.metamodel.target.generic.DataSet;

public class JavaFilterElement implements TargetFilterElement {

    private BlankFilterElement blankFilterElement;
    private JavaBusinessAttribute javabusinessAttribute;

    private String javaOperator;

    public JavaFilterElement(final BlankFilterElement blankFilterElement, final JavaBusinessAttribute javaBusinessAttribute) {
        this.blankFilterElement = blankFilterElement;
        this.javabusinessAttribute = javaBusinessAttribute;
        javaOperator = computeJavaOperator(blankFilterElement);

        if (javaBusinessAttribute == null && !isBooleanOperator()) {
            throw new NullPointerException("JavaFilterElement (" + blankFilterElement.getKeyname() + "): JavaBusinessAttribute is null");
        }
    }

    public boolean isBooleanOperator() {
        return blankFilterElement.isBooleanOperator();
    }

    public boolean isBooleanValue() {
        return blankFilterElement.isBooleanValue();
    }

    public Integer getPos() {
        return blankFilterElement.getPos();
    }

    public String getKeyname() {
        return blankFilterElement.getKeyname();
    }

    public String getOperator() {
        return javaOperator;
    }

    private String computeJavaOperator(final BlankFilterElement blankFilterElement) {
        final FilterOperators op = blankFilterElement.getOperator();
        String result = "Unknown operator : " + op.toString();
        if (FilterOperators.eq.equals(op)) {
            result = "=";
        } else if (FilterOperators.dt.equals(op)) {
            result = "!=";
        } else if (FilterOperators.gt.equals(op)) {
            result = ">";
        } else if (FilterOperators.gteq.equals(op)) {
            result = ">=";
        } else if (FilterOperators.like.equals(op)) {
            result = "like";
        } else if (FilterOperators.lt.equals(op)) {
            result = "<";
        } else if (FilterOperators.lteq.equals(op)) {
            result = "<=";
        } else if (FilterOperators.and.equals(op)) {
            result = "and";
        } else if (FilterOperators.or.equals(op)) {
            result = "or";
        }
        return result;
    }

//    public boolean isLike() {
//        return FilterOperators.like.equals(blankFilterElement.getOperator());
//    }
    public String getValue() {
        return blankFilterElement.getValue();
    }

    public boolean hasValue() {
        return blankFilterElement.hasValue();
    }

    public String getParamName() {
        return blankFilterElement.getParamName();
    }

    public JavaBusinessAttribute getJavabusinessAttribute() {
        return javabusinessAttribute;
    }

    public DataSet getDataset() {
        return blankFilterElement.getDataset();
    }

    public boolean isDataset() {
        return blankFilterElement.isDataset();
    }

    public boolean isMandatory() {
        return blankFilterElement.isMandatory();
    }

    public boolean isCollection() {
        return javabusinessAttribute.isCollection();
    }

    public String getBusinessObjectName() {
        return javabusinessAttribute.getTypeAsJavaBusinessObject().getName();
    }

    @Override
    public String toString() {
        return "JavaFilterElement{"
                + "pos='" + getPos() + '\''
                + ", keyname='" + getKeyname() + '\''
                + ", javaOperator='" + javaOperator + '\''
                + ", param name='" + getParamName() + '\''
                + ", value='" + (hasValue() ? getValue() : "no value") + '\''
                + ", isCollection='" + isCollection() + '\''
                + ", isBooleanOperator='" + isBooleanOperator() + '\''
                + ", JavabusinessAttribute = " + getJavabusinessAttribute()
                + '}';
    }

    public void free() {
        blankFilterElement = null;
        javabusinessAttribute = null;
    }

    public boolean isRuntimeVariable() {
        return blankFilterElement.isRuntimeVariable();
    }

    public String getRuntimeVariable() {
        return blankFilterElement.getRuntimeVariable();
    }
}
