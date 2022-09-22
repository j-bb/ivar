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
 */package ivar.metamodel.target.java;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.StepTypes;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.BusinessObjectType;

public class JavaBusinessAttribute extends AbstractObject implements Comparable<JavaBusinessAttribute> {

    private String name;
    private int length = -1;
    private String type;
    private Object value = null;
    private boolean javaBuiltIn = false;
    private BusinessObjectAttribute businessObjectAttribute = null;
    private JavaBusinessObject typeAsJavaBusinessObject = null;
    private boolean hash = false;

    public JavaBusinessAttribute(final BusinessObjectAttribute businessObjectAttribute) {
        this.businessObjectAttribute = businessObjectAttribute;

        name = businessObjectAttribute.getName();
        type = computeType(businessObjectAttribute);
    }

    private String computeType(final BusinessObjectAttribute businessObjectAttribute) {
        /* TODO
         * If we want to have fixed-point decimals in the database (using precision and
         * scale from constraints), we are supposed to use java.math.BigDecimal instead of
         * Float/Double. However, this would mean that there is a refactoring that needs to
         * be done especially in the RPC lib.
         * see also : https://issues.apache.org/jira/browse/OPENJPA-213
         */
        final BusinessObjectType type = businessObjectAttribute.getType();
        final boolean isDataset = businessObjectAttribute.isDataset();
        String result = null;
        if (isDataset) {
            result = "String";
            length = businessObjectAttribute.getMaxDatasetLength() + 1;
            javaBuiltIn = true;
        } else {
            final String typename = type.getName();
            if (StepTypes.Boolean.name().equals(typename)) {
                result = "Boolean";
                javaBuiltIn = true;
            } else if (StepTypes.Text.name().equals(typename) || StepTypes.Password.name().equals(typename)) {
                result = "String";
                length = 30;
                javaBuiltIn = true;
            } else if (StepTypes.Multitext.name().equals(typename)) {
                result = "String";
                length = 300;
                javaBuiltIn = true;
            } else if (StepTypes.Formatedtext.name().equals(typename)) {
                result = "String";
                length = 300;
                javaBuiltIn = true;
            } else if (StepTypes.Number.name().equals(typename)) {
                // TODO check if it is a number (5) or a number(5, 6) in such a case, it
                // is a float and not an int.
                result = "Float";
                javaBuiltIn = true;
                //length = 10;
            } else if (StepTypes.Real.name().equals(typename)) {
                result = "Float";
                //length = 10;
                javaBuiltIn = true;
            } else if (StepTypes.Integer.name().equals(typename)) {
                result = "Integer";
                //length = 10;
                javaBuiltIn = true;
            } else if (StepTypes.Percentile.name().equals(typename)) {
                result = "Float";
                //length = 10;
                javaBuiltIn = true;
            } else if (StepTypes.Date.name().equals(typename)) {
                result = "java.util.Date";
                javaBuiltIn = true;
            } else if (StepTypes.Time.name().equals(typename)) {
                result = "java.util.Date";
                javaBuiltIn = true;
            } else if (StepTypes.DateTime.name().equals(typename)) {
                result = "java.util.Date";
                javaBuiltIn = true;
            } else if (StepTypes.Amount.name().equals(typename)) {
                result = "Float";
                //length = 10;
                javaBuiltIn = true;
            } else if (StepTypes.File.name().equals(typename) || StepTypes.Image.name().equals(typename)) {
                result = "Long";
                javaBuiltIn = true;
            } else {
                if (type.isBuiltIn()) {
                    result = "\"Type not recognized : \'" + type + "\'\"";
                    error("the following type is not recognize : \'" + type + "\'");
                } else {
                    result = typename;
                }
            }
        }

        return result;
    }

    public void setTypeAsJavaBusinessObject(JavaBusinessObject typeAsJavaBusinessObject) {
        this.typeAsJavaBusinessObject = typeAsJavaBusinessObject;
    }

    public JavaBusinessObject getTypeAsJavaBusinessObject() {
        return typeAsJavaBusinessObject;
    }

    public String getColumnName(final JavaBusinessAttribute attribute) {
        String result = attribute.getType();
        if (!attribute.getName().equalsIgnoreCase(attribute.getType())) {
            result = attribute.getType() + "_" + attribute.getName();
        }
        return result.toLowerCase();
    }

    public BusinessObjectAttribute getBusinessObjectAttribute() {
        return businessObjectAttribute;
    }

    public boolean hasJavadocComments() {
        return businessObjectAttribute.getDoc() != null && businessObjectAttribute.getDoc().length() > 1;
    }

    public String[] getJavadocComments() {
        String[] result = null;

        String doc = businessObjectAttribute.getDoc();
        if (doc != null) {
            result = doc.split("\n");
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getKeyname() {
        return businessObjectAttribute.getKeyname();
    }

    public String getTypeName() {
        return businessObjectAttribute.getTypeName();
    }

    public String getTypeFirstCapitalized() {
        return businessObjectAttribute.getTypeFirstCapitalized();
    }

    // TODO push that to a JavaScreenField class !
    public String getJavaName() {
        final String name = getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isCollection() {
        return businessObjectAttribute.isCollection();
    }

    public String getBusinessType() {
        return businessObjectAttribute.getType().getType().toString();
    }

    // TODO Definition quel terme utiliser mandatory or is null ?
    public boolean isMandatory() {
        return businessObjectAttribute.isMandatory();
    }

    public boolean isNotMandatory() {
        return !isMandatory();
    }

    public int getLength() {
        final boolean hash = businessObjectAttribute.isHash();
        Integer result = (hash ? new Integer(40) : businessObjectAttribute.getMaxLengthConstraint());
        if (result == null) {
            result = length;
        }

        return result;
    }

    public boolean isFixedPoint() {
        return businessObjectAttribute.getMaxPrecisionConstraint() != null
                || businessObjectAttribute.getMaxScaleConstraint() != null;
    }

    public int getPrecision() {
        Integer result = businessObjectAttribute.getMaxPrecisionConstraint();
        if (result == null) {
            result = 0;
        }
        return result;
    }

    public int getScale() {
        Integer result = businessObjectAttribute.getMaxScaleConstraint();
        if (result == null) {
            result = 0;
        }
        return result;
    }

    public boolean isUnique() {
        if (businessObjectAttribute.isUnique() && !isMandatory()) {
            // !! OpenJPA bug. Not resolved as of 10/01/11.
            // @see https://issues.apache.org/jira/browse/OPENJPA-1651
            compileWarning("\"unique\" constraint can not be applied on non-mandatory fields! Uniqueness will not be enforced for field \"" + name + "\".");
            return false;
        }
        return businessObjectAttribute.isUnique();
    }

    public String getMin() {
        return businessObjectAttribute.getMin();
    }

    public String getMax() {
        return businessObjectAttribute.getMax();
    }

    /**
     * @return Returns the javaBuiltIn.
     */
    public boolean isJavaBuiltIn() {
        return javaBuiltIn;
    }

    public boolean isBuiltIn() {
        return businessObjectAttribute.getType().isBuiltIn();
    }

    public boolean isCalculated() {
        return businessObjectAttribute.isCalculated();
    }

    public String getColumnName() {
        return businessObjectAttribute.getColumnName();
    }

    public boolean isFile() {
        return businessObjectAttribute.isFile();
    }

    public boolean isDataset() {
        return businessObjectAttribute.isDataset();
    }

    public int compareTo(JavaBusinessAttribute anotherJBA) {
        return getName().compareTo(anotherJBA.getName());
    }

    public boolean isToMany() {
        return businessObjectAttribute.isToMany();
    }

    public boolean isToOne() {
        return businessObjectAttribute.isToOne();
    }

    public void free() {
        value = null;
        businessObjectAttribute = null;
        typeAsJavaBusinessObject = null;
    }

    @Override
    public String toString() {
        return "JavaBusinessAttribute{"
                + "name='" + name + '\''
                + ", length=" + length
                + ", type='" + type + '\''
                + ", value=" + value
                + ", javaBuiltIn=" + javaBuiltIn
                + ", businessObjectAttribute=" + businessObjectAttribute
                + ", typeAsJavaBusinessObject=" + typeAsJavaBusinessObject
                + ", hash=" + hash
                + "} ";
    }
}
