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
package ivar.metamodel.target;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.DateHelper;
import ivar.metamodel.spec.ConstraintValue;
import ivar.metamodel.spec.Step;
import ivar.metamodel.spec.StepConstraint;
import ivar.metamodel.spec.StepTypes;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConstraintChecker extends AbstractObject {

    private final Class<?> constraintsHolder;
    private final Map<StepTypes, Set<String>> allowedConstraints = CollectionFactory.newMap();
    private final Set<String> allowingConstraintsTypes = CollectionFactory.newSet();
    private Set<Field> constraintFields;

    public ConstraintChecker(final Class<?> constraintsHolder) {
        this.constraintsHolder = constraintsHolder;
        fillAllowedConstraintsMap();
        for (final StepTypes t : allowedConstraints.keySet()) {
            allowingConstraintsTypes.add(t.name());
        }
    }

    public Map<StepTypes, Set<String>> getAllowedConstraintsByType() {
        return allowedConstraints;
    }

    public Set<Field> getConstraintFields() {
        return constraintFields;
    }

    private void fillAllowedConstraintsMap() {
        constraintFields = getAllConstraintFields();
        for (final Field f : constraintFields) {
            final StepTypes[] types = f.getAnnotation(ConstraintValue.class).allowedTypes();
            final String fieldName = f.getName();

            for (final StepTypes type : types) {
                if (!allowedConstraints.containsKey(type)) {
                    final Set<String> newSet = CollectionFactory.newSet();
                    allowedConstraints.put(type, newSet);
                }
                allowedConstraints.get(type).add(fieldName);
            }
        }
    }

    private Set<Field> getAllConstraintFields() {
        final Set<Field> resultHolder = CollectionFactory.newSet();
        return getAllConstraintFields(constraintsHolder, resultHolder);
    }

    private Set<Field> getAllConstraintFields(final Class<?> clazz, final Set<Field> result) {
        for (final Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(ConstraintValue.class)) {
                result.add(f);
            }
        }

        if (clazz.getSuperclass() != null) {
            getAllConstraintFields(clazz.getSuperclass(), result);
        }

        return result;
    }

    public void validate(final Step step) {
        final StepConstraint constraints = step.getStepConstraint();
        final String type = constraints.getType();
        final Map<String, Object> constraintValues = CollectionFactory.newMap();
        if (type != null) {
            if (!allowingConstraintsTypes.contains(type)) {
                for (final Field field : constraintFields) {
                    final boolean isPublic = field.isAccessible();
                    if (!isPublic) {
                        field.setAccessible(true);
                    }
                    try {
                        if (field.get(constraints) != null) {
                            compileError("Type " + type + "on step " + step.getKeyname() + " can't be constrained.");
                            beginCompileHelpBlock("For helping purpose, here is a list of constrainable types");
                            for (final String constrainableType : allowingConstraintsTypes) {
                                compileHelp("\t - " + constrainableType);
                            }
                            endCompileHelpBlock();
                            break;
                        }
                    } catch (IllegalAccessException e) {
                        // this error should not happen.
                        error(e, true);
                    } finally {
                        if (!isPublic) {
                            field.setAccessible(false);
                        }
                    }
                }
            } else {
                for (final Field field : constraintFields) {
                    final boolean isPublic = field.isAccessible();
                    if (!isPublic) {
                        field.setAccessible(true);
                    }
                    try {
                        final Object value = field.get(constraints);
                        if (value != null) {
                            if (!allowedConstraints.get(StepTypes.valueOf(type)).contains(field.getName())) {
                                compileError("StepConstraint " + field.getName() + " is not a valid constraint for type " + type + " on step " + step.getKeyname());
                                beginCompileHelpBlock("For helping purpose, here is a list of possible constraints for type " + type);
                                for (final String constraint : allowedConstraints.get(StepTypes.valueOf(type))) {
                                    compileHelp("\t - " + constraint);
                                }
                                endCompileHelpBlock();
                            }
                            constraintValues.put(field.getName(), value);
                        }
                    } catch (IllegalAccessException e) {
                        // this error should not happen.
                        error(e, true);
                    } finally {
                        if (!isPublic) {
                            field.setAccessible(false);
                        }
                    }
                }
                checkLogic(constraintValues, step);
            }
        }
    }

    private void checkLogic(final Map<String, Object> constraintValues, final Step step) {
        /*  /!\ defaultValue type consistency is NOT CHECKED. */
        if (constraintValues.containsKey("precision") && constraintValues.containsKey("scale")) {
            checkScalePrecision((Integer) constraintValues.get("scale"), (Integer) constraintValues.get("precision"), step);
        }

        if (constraintValues.containsKey("minValue") && constraintValues.containsKey("maxValue")) {
            checkMinMax((Double) constraintValues.get("minValue"), (Double) constraintValues.get("maxValue"), step);
        }

        if (constraintValues.containsKey("before") && constraintValues.containsKey("after")) {
            checkBeforeAfter((Date) constraintValues.get("before"), (Date) constraintValues.get("after"), step);
        }

        if (constraintValues.containsKey("regex")) {
            checkRegex((String) constraintValues.get("regex"), step);
        }

        if (constraintValues.containsKey("dateFormat")) {
            checkFormat((String) constraintValues.get("dateFormat"), step);
        }
    }

    private void checkFormat(final String string, final Step step) {
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string);
        } catch (IllegalArgumentException e) {
            compileError("Date format validation has failed for step " + step.getKeyname() + "!");
            compileHelp("The format you provided was: " + string);
            compileHelp("The following exception has been thrown: " + e.getMessage());
        }
    }

    private void checkRegex(final String regexp, final Step step) {
        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            compileError("Regular expression validation has failed for step " + step.getKeyname() + "!");
            compileHelp("The expression you provided was: " + regexp);
            compileHelp("The following exception has been thrown: " + e.getMessage());
        }
    }

    private void checkBeforeAfter(final Date before, final Date after, final Step step) {
        if (before.before(after)) {
            compileError("\"Before\" constraint has a lesser value than corresponding \"after\" value on step " + step.getKeyname() + "!");
            compileHelp("Before value: " + DateHelper.getLogDate(before) + ", after value: " + DateHelper.getLogDate(after) + "; Rule is (before GREATER OR EQUAL after)");
        }
    }

    private void checkMinMax(final Double min, final Double max, final Step step) {
        if (min > max) {
            compileError("Min value constraint has a higher value than corresponding max value on step " + step.getKeyname() + "!");
            compileHelp("Min value: " + min + ", max value: " + max + "; Rule is (min LESSER OR EQUAL max)");
        }
    }

    private void checkScalePrecision(final Integer scale, final Integer precision, final Step step) {
        if (scale > precision) {
            compileError("Scale constraint has a higher value than corresponding precision value on step " + step.getKeyname() + "!");
            compileHelp("Scale value: " + scale + ", precision value: " + precision + "; Rule is (scale LESSER OR EQUAL precision)");
        }
    }
}
