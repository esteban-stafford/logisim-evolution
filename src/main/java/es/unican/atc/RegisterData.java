package es.unican.atc;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;

/**
 * This class handles internal state for RegisterFile32.
 */
public class RegisterData implements InstanceData, Cloneable {
    /** Retrieves the state associated with this register in the circuit state,
     * generating the state if necessary.
     */
    public static RegisterData get(InstanceState state, int length, int width) {
        RegisterData ret = (RegisterData) state.getData();
        if (ret == null) {
            // If it doesn't yet exist, then we'll set it up with our default
            // values and put it into the circuit state so it can be retrieved
            // in future propagations.
            ret = new RegisterData(null, length, width);
            state.setData(ret);
        }
        return ret;
    }

    private Value lastClock;
    Value[] regs;

    private RegisterData(Value lastClock, int length, int width) {
        this.lastClock = lastClock;
        this.regs = new Value[length];
        this.regs[0] = Value.createKnown(width, 0);
        reset(Value.createKnown(width, 0));
    }

    public void reset(Value val) {
        for (int i = 1; i < regs.length; i++) {
            regs[i] = val;
        }
    }

    @Override
    public RegisterData clone() {
        try {
            // Not sure this works if registers is an array...
            // But KWalsh did it and it seemed to work okay.
            return (RegisterData) super.clone();
        }
        catch(CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean updateClock(Value newClock, Object trigger) {
        Value oldClock = lastClock;
        lastClock = newClock;
        if (trigger == null || trigger == StdAttr.TRIG_RISING) {
            return oldClock == Value.FALSE && newClock == Value.TRUE;
        }
        else if (trigger == StdAttr.TRIG_FALLING) {
            return oldClock == Value.TRUE && newClock == Value.FALSE;
        }
        else if (trigger == StdAttr.TRIG_HIGH) {
            return newClock == Value.TRUE;
        }
        else if (trigger == StdAttr.TRIG_LOW) {
            return newClock == Value.FALSE;
        }
        else {
            return oldClock == Value.FALSE && newClock == Value.TRUE;
        }
    }
}
