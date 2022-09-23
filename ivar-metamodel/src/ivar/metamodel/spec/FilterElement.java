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

import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import ivar.metamodel.IdentifiableObject;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import ivar.metamodel.target.generic.BusinessObjectType;
import ivar.metamodel.target.generic.DataSet;
import java.util.List;
import java.util.Set;

public class FilterElement extends IdentifiableObject {

    private Integer pos;
    private String keyname;
    private FilterOperators operator;
    private String value;
    private boolean mandatory = false;
    private transient List<BusinessObjectAttribute> associatedBusinessObjectAttributesPath = CollectionFactory.newList();
    private transient List<TemporalFilterElementParameterCompilation> temporalFilterElementParameterCompilations = CollectionFactory.newList();
    private transient DataSet dataset = null;
    private transient boolean againstNull = false;
    private transient boolean againstNullCollection = false;
    private transient String UIName = null;
    private transient String paramName = null;

    public void checked(final List<BusinessObjectAttribute> associatedBusinessObjectAttributesPath) {
        if (this.associatedBusinessObjectAttributesPath != null && this.associatedBusinessObjectAttributesPath.size() > 0) {
            throw new NullPointerException("This is really bad because there is already an associatedBusinessObjectAttributesPath (" + getAssociatedBusinessObjectAttributesPathAsString() + ") on this Filterelement " + getKeyname());
        }
        this.associatedBusinessObjectAttributesPath.addAll(associatedBusinessObjectAttributesPath);
    }

    public boolean isChecked() {
        return associatedBusinessObjectAttributesPath != null && associatedBusinessObjectAttributesPath.size() > 0;
    }

    public List<BusinessObjectAttribute> getAssociatedBusinessObjectAttributesPath() {
        return associatedBusinessObjectAttributesPath;
    }

    public BusinessObjectAttribute getAssociatedBusinessObjectAttribute() {
        BusinessObjectAttribute result = null;
        if (associatedBusinessObjectAttributesPath.size() > 0) {
            result = associatedBusinessObjectAttributesPath.get(associatedBusinessObjectAttributesPath.size() - 1);
        }
        return result;
    }

    public String getAssociatedBusinessObjectAttributesPathAsString() {
        return getAssociatedBusinessObjectAttributesPathAsString(associatedBusinessObjectAttributesPath);
    }

    public String getAssociatedBusinessObjectAttributesPathAsString(final List<BusinessObjectAttribute> associatedBusinessObjectAttributesPath) {
        StringBuilder builder = new StringBuilder();
        final int sizeMinusOne = associatedBusinessObjectAttributesPath.size() - 1;
        for (int i = 0; i < sizeMinusOne; i++) {
            builder.append(associatedBusinessObjectAttributesPath.get(i).getKeyname());
            builder.append('.');
        }
        builder.append(associatedBusinessObjectAttributesPath.get(sizeMinusOne).getKeyname());
        return builder.toString();
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    public FilterOperators getOperator() {
        return operator;
    }

    public boolean hasValue() {
        return value != null && value.length() > 0;
    }

    public boolean isBooleanOperator() {
        return operator.isBoolean();
    }

    public boolean isOperatorLike() {
        return operator.isLike();
    }

    public boolean isTextValue() {
        final BusinessObjectType bot = associatedBusinessObjectAttributesPath.get(associatedBusinessObjectAttributesPath.size() - 1).getType();
        return bot.isText();
    }

    public boolean isRuntimeVariable() {
        return value != null && value.length() > 3 && value.startsWith("${") && value.endsWith("}");
    }

    public String getRuntimeVariable() {
        String result = null;
        if (isRuntimeVariable()) {
            result = value.substring(2, value.length() - 1);
        }
        return result;
    }

    public boolean isDotRuntimeVariable() {
        final String variableName = getRuntimeVariable();
        return variableName != null && variableName.contains(".");
    }

    public String getDotRuntimeVariableLeftPart() {
        final String variableName = getRuntimeVariable();
        String result = null;
        if (variableName != null && variableName.contains(".")) {
            result = StringHelper.getLeft(variableName, ".");
        }
        return result;
    }

    public String getDotRuntimeVariableRightPart() {
        final String variableName = getRuntimeVariable();
        return variableName.substring(getDotRuntimeVariableLeftPart().length() + 1, variableName.length());
    }

    public boolean isBooleanValue() {
        return getAssociatedBusinessObjectAttribute().isBoolean();
    }

    public String getOperatorForUI() {
        String result = "operator undefined";
        FilterOperators op = operator;

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

        return "(" + result + ")";
    }

    public void setOperator(FilterOperators operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FilterElement getJPAClone() {
        return getJPAClone((new FilterElement()));
    }

    protected FilterElement getJPAClone(FilterElement dst) {
        super.getJPAClone(dst);
        if (pos != null) {
            dst.pos = new Integer(pos);
        } else {
            dst.pos = null;
        }

        if (keyname != null) {
            dst.keyname = new String(keyname);
        } else {
            dst.keyname = null;
        }

        dst.operator = operator;

        if (value != null) {
            dst.value = new String(value);
        } else {
            dst.value = null;
        }

        return dst;
    }

    public boolean isTemporal() {
        return temporalFilterElementParameterCompilations.size() > 0 || getAssociatedBusinessObjectAttribute().getType().isTemporal();
    }

    public void addTemporalFilterElementParameterCompilation(final TemporalFilterElementParameterCompilation temporalFilterElementParameterCompilation) {
        temporalFilterElementParameterCompilations.add(temporalFilterElementParameterCompilation);
    }

    public String getNameForJs() {
        return StringHelper.getCamel(keyname, ".");
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(final Set<FilterElement> filterElements) {
        if (!isBooleanOperator()) {
            paramName = StringHelper.getFirstLowerCase(StringHelper.getCamel(keyname, "."));

            for (final FilterElement filterElement : filterElements) {
                if (filterElement.equals(this) || filterElement.isBooleanOperator()) {
                    continue;
                }

                if (filterElement.getKeyname().equals(keyname)) {
                    //TODO start from 1 and increment by 1. The code would be easier to read.
                    paramName = paramName + "_" + pos;
                    break;
                }
            }
        }
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public boolean isAgainstNull() {
        return againstNull;
    }

    public void setAgainstNull(boolean againstNull) {
        this.againstNull = againstNull;
    }

    public boolean isAgainstNullCollection() {
        return againstNullCollection;
    }

    public void setAgainstNullCollection(boolean againstNullCollection) {
        this.againstNullCollection = againstNullCollection;
    }

    public String getNameForUI() {
        return UIName + (isMandatory() ? " *" : "");
    }

    public void setUIName(String UIName) {
        this.UIName = UIName;
    }

    public boolean isDataset() {
        return dataset != null;
    }

    public DataSet getDataset() {
        return dataset;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void free() {
        operator = null;
        associatedBusinessObjectAttributesPath.clear();
        associatedBusinessObjectAttributesPath = null;

        for (final TemporalFilterElementParameterCompilation tmp : temporalFilterElementParameterCompilations) {
            tmp.free();
        }
        temporalFilterElementParameterCompilations.clear();
        temporalFilterElementParameterCompilations = null;
    }

    public String toStringForTrace() {
        return "[" + pos + "] " + keyname + " " + operator + " " + value;
    }
}
