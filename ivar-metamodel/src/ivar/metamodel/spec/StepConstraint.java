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

import ivar.metamodel.IdentifiableObject;
import ivar.metamodel.spec.selfdesc.BooleanClassDesc;
import ivar.metamodel.spec.selfdesc.InstanceDesc;
import ivar.metamodel.spec.selfdesc.ObjectSelfDesc;
import ivar.metamodel.spec.selfdesc.QxClassDesc;
import ivar.metamodel.spec.selfdesc.QxClasses;
import ivar.metamodel.target.ConstraintChecker;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map.Entry;

public class StepConstraint extends IdentifiableObject {

    private static final StepTypes[] STEP_TYPEs = StepTypes.values();
    private static final String[] atomicTypes = new String[STEP_TYPEs.length];

    static {
        for (int i = 0; i < STEP_TYPEs.length; i++) {
            atomicTypes[i] = STEP_TYPEs[i].toString();
        }
    }

    private String type;

    /* StepConstraint section */
    private Integer length;

    private Boolean positive;

    private Boolean negative;

    private Boolean nonzero;

    private Double maxValue;

    private Double minValue;

    private Integer precision;

    private Integer scale;

    private Date before;

    private Date after;

    private String regex;

    private String defaultValue;

    private String errorMessage;

    private String dateFormat;

    private Boolean unique;

    private Boolean hash;
    /* End of constraints section */

    protected transient boolean atomic = false;

    protected transient boolean data = false;

    protected transient boolean dataset = false;

    protected transient boolean scenarioReference = false;

    protected transient ivar.metamodel.target.generic.DataSet referencedDataset = null;

    protected transient Scenario referencedscenario = null;

    protected transient boolean computed = false;

    protected ObjectSelfDesc selfDesc = null;

    public StepConstraint() {
        selfDesc = new ConstraintSelfDesc(this);
    }

    public StepConstraint(final String type) {
        this();
        this.type = type;
    }

    public void compute(final Application application) {
        // TODO add trace like begin/end block to be unified from different
        // places that method is called.
        atomic = false;
        data = false;
        dataset = false;
        scenarioReference = false;
        referencedDataset = null;
        referencedscenario = null;
        computed = false;

        // atomic
        for (final String atomicType : atomicTypes) {
            if (type.equals(atomicType)) {
                atomic = true;
                break;
            }
        }

        if (!atomic) {
            // data
            for (final Scenario scenario : application.getScenarios()) {
                if (type.equals(scenario.getData())) {
                    data = true;
                    break;
                }
            }

            if (!data) {
                // dataset
                for (final Dataset possibleDataset : application.getDataSet()) {
                    if (type.equals(possibleDataset.getKeyname())) {
                        dataset = true;
                        referencedDataset = new ivar.metamodel.target.generic.DataSet(application, possibleDataset);
                        break;
                    }
                }

                if (!dataset) { // scenario reference
                    final Scenario scenario = application.getScenarioForKeyname(type);
                    if (scenario != null) {
                        scenarioReference = true;
                        referencedscenario = scenario;
                    }
                }
            }
        }

        computed = true;
    }

    public void check(final String info) {
        if (!computed) {
            compileError(info + " Type error on " + type + ". NOT COMPUTED !!!", true);
        }
        // Check
        final boolean isAllFalse = !(atomic || data || dataset || scenarioReference);
        if (isAllFalse) {
            compileError(info + " Type error on " + type
                    + ". The type must be either atomic or data or dataSet or scenarioReference. This one is none of them !", true);
            beginCompileHelpBlock("For helping purpose, here are the value computed for the type " + getType());
            compileHelp(toString());
            endCompileHelpBlock();
        } else {
            final boolean isOnlyOneTrue = atomic ^ data ^ dataset ^ scenarioReference;
            if (!isOnlyOneTrue) {
                compileError(info + " Type error on " + type
                        + ". The type must be either atomic or data or dataSet or scenarioReference. This more than one of them !", true);
                beginCompileHelpBlock("For helping purpose, here are the value computed for the type " + getType());
                compileHelp(toString());
                endCompileHelpBlock();
            }
        }
    }

    protected void checkComputed() {
        if (type != null && !computed) {
            throw new IllegalStateException("StepConstraint (" + type + ") is not computed !");
        }
    }

    public ivar.metamodel.target.generic.DataSet getReferencedDataset() {
        checkComputed();
        return referencedDataset;
    }

    public Scenario getReferencedscenario() {
        checkComputed();
        return referencedscenario;
    }

    public boolean isAtomic() {
        checkComputed();
        return atomic;
    }

    public boolean isData() {
        checkComputed();
        return data;
    }

    public boolean isDataset() {
        checkComputed();
        return dataset;
    }

    public boolean isScenarioReference() {
        checkComputed();
        return scenarioReference;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StepConstraint{" + "type='" + (type == null ? "null" : type) + '\'' + ", atomic=" + atomic + ", data=" + data + ", dataset=" + dataset
                + ", scenarioReference=" + scenarioReference + '}';
    }

    @Override
    public StepConstraint getJPAClone() {
        return getJPAClone(new StepConstraint());
    }

    protected StepConstraint getJPAClone(final StepConstraint dst) {
        super.getJPAClone(dst);
        if (type != null) {
            dst.type = new String(type);
            dst.atomic = atomic;
            dst.data = data;
            dst.dataset = dataset;
            dst.scenarioReference = scenarioReference;
            dst.referencedDataset = referencedDataset;
            dst.referencedscenario = referencedscenario;
            dst.computed = computed;
        } else {
            dst.type = null;
        }

        dst.after = after;
        dst.before = before;
        dst.dateFormat = dateFormat;
        dst.defaultValue = defaultValue;
        dst.errorMessage = errorMessage;
        dst.length = length;
        dst.maxValue = maxValue;
        dst.minValue = minValue;
        dst.negative = negative;
        dst.nonzero = nonzero;
        dst.positive = positive;
        dst.precision = precision;
        dst.regex = regex;
        dst.scale = scale;
        dst.unique = unique;
        dst.hash = hash;

        return dst;
    }

    public boolean isText() {
        return StepTypes.Text.name().equals(getType());
    }

    public boolean isPassword() {
        return StepTypes.Password.name().equals(getType());
    }

    public boolean isBoolean() {
        return StepTypes.Boolean.name().equals(getType());
    }

    public boolean isNumber() {
        return StepTypes.Number.name().equals(getType());
    }

    public boolean isMultitext() {
        return StepTypes.Multitext.name().equals(getType());
    }

    public boolean isFormatedtext() {
        return StepTypes.Formatedtext.name().equals(getType());
    }

    public boolean isInteger() {
        return StepTypes.Integer.name().equals(getType());
    }

    public boolean isReal() {
        return StepTypes.Real.name().equals(getType());
    }

    public boolean isAmount() {
        return StepTypes.Amount.name().equals(getType());
    }

    public boolean isPercentile() {
        return StepTypes.Percentile.name().equals(getType());
    }

    public boolean isDate() {
        return StepTypes.Date.name().equals(getType());
    }

    public boolean isTime() {
        return StepTypes.Time.name().equals(getType());
    }

    public boolean isDateTime() {
        return StepTypes.DateTime.name().equals(getType());
    }

    public boolean isFile() {
        return getType() != null && (getType().equals(StepTypes.File.name()) || getType().equals(StepTypes.Image.name()));
    }

    public boolean isImage() {
        return getType() != null && StepTypes.Image.name().equals(getType());
    }

    public ObjectSelfDesc getSelfDesc() {
        return selfDesc;
    }

    public void free() {
        referencedDataset = null;
        referencedscenario = null;
    }

    private static final class TypeInstanceDesc extends InstanceDesc {

        private String toolTip;

        public static final BooleanClassDesc BOOLEAN_CLASSDESC = new BooleanClassDesc(QxClasses.CheckBox);
        public static final QxClassDesc STRING_CLASSDESC = new QxClassDesc(QxClasses.TextField) {
        };
        public static final QxClassDesc NUMBER_CLASSDESC = new QxClassDesc(QxClasses.Spinner) {
        };
        public static final QxClassDesc DATE_CLASSDESC = new QxClassDesc(QxClasses.DateField) {
        };

        public TypeInstanceDesc(final QxClassDesc classDesc, final Object value, final String name, final String toolTip) {
            this.classDesc = classDesc;
            this.value = value;
            this.name = name;
            this.toolTip = toolTip;
        }
    }

    private static final class ConstraintSelfDesc extends ObjectSelfDesc {

        public ConstraintSelfDesc(final StepConstraint that) {
            final ConstraintChecker constraintChecker = new ConstraintChecker(that.getClass());
            for (final Field field : constraintChecker.getConstraintFields()) {
                QxClassDesc classDesc = null;
                final Class<?> fieldType = field.getType();
                if (fieldType.equals(String.class)) {
                    classDesc = TypeInstanceDesc.STRING_CLASSDESC;
                } else if (fieldType.equals(Date.class)) {
                    classDesc = TypeInstanceDesc.DATE_CLASSDESC;
                } else if (fieldType.equals(Long.class) || fieldType.equals(Integer.class) || fieldType.equals(Double.class)) {
                    classDesc = TypeInstanceDesc.NUMBER_CLASSDESC;
                } else if (fieldType.equals(Boolean.class)) {
                    classDesc = TypeInstanceDesc.BOOLEAN_CLASSDESC;
                }

                try {
                    final boolean access = field.isAccessible();
                    field.setAccessible(true);
                    final InstanceDesc instanceDesc = new TypeInstanceDesc(classDesc, field.get(that), field.getName(), field.getAnnotation(ConstraintValue.class).description());
                    addAttributeDesc(field.getName(), instanceDesc);
                    if (!access) {
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                    // TODO error... Should not happen.
                }
            }
        }

        @Override
        public String toString() {
            String res = "ObjectSelfDesc {\n";
            for (Entry<String, InstanceDesc> mapEntry : this.selfDesc.entrySet()) {
                res += "\t- " + mapEntry.getKey() + " ... " + mapEntry.getValue() + "\n";
            }
            return res + "}";
        }
    }

    public void removeSelfDesc() {
        this.selfDesc = null;
    }
}
