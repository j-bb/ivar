package ivar.metamodel.spec;

import ivar.common.AbstractObject;

/**
 * Created by IntelliJ IDEA.
 * User: jbb
 * Date: Dec 3, 2009
 * Time: 12:13:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemporalFilterElementParameterCompilation extends AbstractObject {
    private boolean addition;
    private int quantity;
    private TimePeriodUnit timeUnit;

    public boolean isAddition() {
        return addition;
    }

    public void setAddition(final boolean addition) {
        this.addition = addition;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
        info("Parameter duration is " + quantity);
    }

    public TimePeriodUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimePeriodUnit timeUnit) {
        this.timeUnit = timeUnit;
        info("Parameter time unit is " + this.timeUnit.toString());
    }

    public void free() {
        timeUnit = null;
    }
}
