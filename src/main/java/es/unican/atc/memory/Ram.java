/* Adapted by kwalsh from Logisim's standard RAM, which is... */
/* Copyright (c) 2006, 2009, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package es.unican.atc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.AttributeSets;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.util.GraphicsUtil;

public class Ram extends Mem {

    private static final int DATA_ATTR = 32;
    private static final BitWidth dataBits = BitWidth.create(32);

    private static Attribute<?>[] ATTRIBUTES = { Mem.ADDR_ATTR };
    private static Object[] DEFAULTS = { BitWidth.create(20) };

    private static final int OE = MEM_INPUTS + 0;
    private static final int CLR = MEM_INPUTS + 1;
    private static final int CLK = MEM_INPUTS + 2;
    private static final int WE = MEM_INPUTS + 3;
    private static final int DIN = MEM_INPUTS + 4;

    private static Object[][] logOptions = new Object[9][];

    public Ram() {
        super("CSRAM", new SimpleStringGetter("CSRAM"), 3);
        setInstanceLogger(Logger.class);
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        super.configureNewInstance(instance);
        instance.addAttributeListener();
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        super.instanceAttributeChanged(instance, attr);
        configurePorts(instance);
    }

    @Override
    void configurePorts(Instance instance) {
        boolean asynch = false;
        boolean separate = true;
        Bounds bounds = instance.getBounds();
        bounds = bounds.translate(-bounds.getX()-bounds.getWidth()/2, -bounds.getY()-bounds.getHeight()/2);

        int portCount = MEM_INPUTS;
        if (asynch)
            portCount += 2;
        else if (separate)
            portCount += 5;
        else
            portCount += 3;
        Port[] ps = new Port[portCount];

        configureStandardPorts(instance, ps);
        ps[OE] = new Port(bounds.getRightwards(0.75f),bounds.getY(), Port.INPUT, 1);
        ps[CLR] = new Port(bounds.getRightwards(0.25f), bounds.getY(), Port.INPUT, 1);
        if (!asynch) {
            ps[CLK] = new Port(bounds.getRightwards(0.5f), bounds.getBottom(), Port.INPUT, 1);
        }
        if (separate) {
            ps[WE] = new Port(bounds.getRightwards(0.25f), bounds.getBottom(), Port.INPUT, 1);
            ps[DIN] = new Port(bounds.getX(), bounds.getBottomwards(0.75f), Port.INPUT, DATA_ATTR);
        }
        instance.setPorts(ps);
    }

    @Override
    public AttributeSet createAttributeSet() {
        return AttributeSets.fixedSet(ATTRIBUTES, DEFAULTS);
    }

    @Override
    MemState getState(InstanceState state) {
        BitWidth addrBits = state.getAttributeValue(ADDR_ATTR);

        RamState myState = (RamState) state.getData();
        if (myState == null) {
            MemContents contents = MemContents.create(addrBits.getWidth(), dataBits.getWidth());
            Instance instance = state.getInstance();
            myState = new RamState(instance, contents, new MemListener(instance));
            state.setData(myState);
        } else {
            myState.setRam(state.getInstance());
        }
        return myState;
    }

    @Override
    MemState getState(Instance instance, CircuitState state) {
        BitWidth addrBits = instance.getAttributeValue(ADDR_ATTR);

        RamState myState = (RamState) instance.getData(state);
        if (myState == null) {
            MemContents contents = MemContents.create(addrBits.getWidth(), dataBits.getWidth());
            myState = new RamState(instance, contents, new MemListener(instance));
            instance.setData(state, myState);
        } else {
            myState.setRam(instance);
        }
        return myState;
    }

    @Override
    HexFrame getHexFrame(Project proj, Instance instance, CircuitState circState) {
       // TODO: implement this
        return null;
       /*
        RamState state = (RamState) getState(instance, circState);
        return state.getHexFrame(proj);
        */
    }

    static final Value[] vmask = new Value[] { /* 0:xxxxxxxx */ Value.createUnknown(BitWidth.create(32)),
            /* 1:xxxxxx00 */ Value.createKnown(BitWidth.create(8), 0).extendWidth(32, Value.UNKNOWN),
            /* 2:xxxx00xx */ Value.createUnknown(BitWidth.create(8)).extendWidth(16, Value.FALSE).extendWidth(32,
                    Value.UNKNOWN),
            /* 3:xxxx0000 */ Value.createKnown(BitWidth.create(16), 0).extendWidth(32, Value.UNKNOWN),
            /* 4:xx00xxxx */ Value.createUnknown(BitWidth.create(16)).extendWidth(24, Value.FALSE).extendWidth(32,
                    Value.UNKNOWN),
            /* 5:xx00xx00 */ Value.createKnown(BitWidth.create(8), 0).extendWidth(16, Value.UNKNOWN)
                    .extendWidth(24, Value.FALSE).extendWidth(32, Value.UNKNOWN),
            /* 6:xx0000xx */ Value.createUnknown(BitWidth.create(8)).extendWidth(24, Value.FALSE).extendWidth(32,
                    Value.UNKNOWN),
            /* 7:xx000000 */ Value.createKnown(BitWidth.create(24), 0).extendWidth(32, Value.UNKNOWN),
            /* 8:00xxxxxx */ Value.createUnknown(BitWidth.create(24)).extendWidth(32, Value.FALSE),
            /* 9:00xxxx00 */ Value.createKnown(BitWidth.create(8), 0).extendWidth(24, Value.UNKNOWN).extendWidth(32,
                    Value.FALSE),
            /* a:00xx00xx */ Value.createUnknown(BitWidth.create(8)).extendWidth(16, Value.FALSE)
                    .extendWidth(24, Value.UNKNOWN).extendWidth(32, Value.FALSE),
            /* b:00xx0000 */ Value.createKnown(BitWidth.create(16), 0).extendWidth(24, Value.UNKNOWN).extendWidth(32,
                    Value.FALSE),
            /* c:0000xxxx */ Value.createUnknown(BitWidth.create(16)).extendWidth(32, Value.FALSE),
            /* d:0000xx00 */ Value.createKnown(BitWidth.create(8), 0).extendWidth(16, Value.UNKNOWN).extendWidth(32,
                    Value.FALSE),
            /* e:000000xx */ Value.createUnknown(BitWidth.create(8)).extendWidth(32, Value.FALSE),
            /* f:00000000 */ Value.createKnown(BitWidth.create(32), 0) };

    @Override
    public void propagate(InstanceState state) {
        RamState myState = (RamState) getState(state);
        boolean asynch = false;
        boolean separate = true;

        Value addrValue = state.getPortValue(ADDR);
        Value maskValue = state.getPortValue(CS);
        boolean triggered = asynch || myState.setClock(state.getPortValue(CLK), StdAttr.TRIG_RISING);
        boolean outputEnabled = state.getPortValue(OE) != Value.FALSE;
        boolean shouldClear = state.getPortValue(CLR) == Value.TRUE;

        if (shouldClear) {
            myState.getContents().clear();
        }

        int mask = 0, bmask = 0;
        if (maskValue.get(0) != Value.FALSE) {
            mask |= 0x1 << 0;
            bmask |= 0xff << 0;
        }
        if (maskValue.get(1) != Value.FALSE) {
            mask |= 0x1 << 1;
            bmask |= 0xff << 8;
        }
        if (maskValue.get(2) != Value.FALSE) {
            mask |= 0x1 << 2;
            bmask |= 0xff << 16;
        }
        if (maskValue.get(3) != Value.FALSE) {
            mask |= 0x1 << 3;
            bmask |= 0xff << 24;
        }

        if (mask == 0) {
            myState.setCurrent(-1, 0);
            state.setPort(DATA, Value.createUnknown(dataBits), DELAY);
            return;
        }

        long addr = addrValue.toLongValue();
        if (!addrValue.isFullyDefined() || addr < 0)
            return;
        if (addr != myState.getCurrent()) {
            myState.setCurrent(addr, mask);
            myState.scrollToShow(addr);
        } else if (mask != myState.getCurrentMask()) {
            myState.setCurrent(addr, mask);
        }

        if (!shouldClear && triggered) {
            boolean shouldStore;
            if (separate) {
                shouldStore = state.getPortValue(WE) != Value.FALSE;
            } else {
                shouldStore = !outputEnabled;
            }
            if (shouldStore) {
                Value dataValue = state.getPortValue(separate ? DIN : DATA);
                long newVal = dataValue.toLongValue();
                long oldVal = myState.getContents().get(addr);
                newVal = (newVal & bmask) | (oldVal & ~bmask);
                myState.getContents().set(addr, newVal);
            }
        }

        if (outputEnabled) {
            long val = myState.getContents().get(addr);
            Value[] vals = vmask[mask].getAll();
            // vmask[mask] is x's and zeroes right now.
            // Just need to change any zeroes to ones if they are 1 in val.
            // For every group of 4 bits:
            for (int i = 0; i < 4; ++i) {
                // Masked out. This group of bits can be skipped.
                if ((mask & (1 << i)) == 0) {
                    continue;
                }
                // For each of the 8 bits in this group,
                // set if this bit is set in val.
                for (int j = 0, pos = i * 8; j < 8; ++j, ++pos) {
                    if ((val & (1 << pos)) != 0) {
                        vals[pos] = Value.TRUE;
                    }
                }
            }
            state.setPort(DATA, Value.create(vals), DELAY);
        } else {
            state.setPort(DATA, vmask[0], DELAY);
        }
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        super.paintInstance(painter);
        boolean asynch = false;
        boolean separate = true;

        if (!asynch)
            painter.drawClock(CLK, Direction.NORTH);
        painter.drawPort(OE, "OE", Direction.NORTH);
        painter.drawPort(CLR, "CLR", Direction.NORTH);

        if (separate) {
            painter.drawPort(WE, "WE", Direction.SOUTH);
            painter.getGraphics().setColor(Color.BLACK);
            painter.drawPort(DIN, "DIN", Direction.EAST);
        }
    }

    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Font old = g.getFont();
        g.setFont(old.deriveFont(9.0f));
        GraphicsUtil.drawCenteredText(g, "CSRAM", 10, 9);
        g.setFont(old);
        g.drawRect(0, 4, 19, 12);
        for (int dx = 2; dx < 20; dx += 5) {
            g.drawLine(dx, 2, dx, 4);
            g.drawLine(dx, 16, dx, 18);
        }
    }

    private static class RamState extends MemState implements InstanceData, AttributeListener {
        private Instance parent;
        private MemListener listener;
        private HexFrame hexFrame = null;
        private ClockState clockState;

        RamState(Instance parent, MemContents contents, MemListener listener) {
            super(contents);
            this.parent = parent;
            this.listener = listener;
            this.clockState = new ClockState();
            if (parent != null)
                parent.getAttributeSet().addAttributeListener(this);
            contents.addHexModelListener(listener);
        }

        void setRam(Instance value) {
            if (parent == value)
                return;
            if (parent != null)
                parent.getAttributeSet().removeAttributeListener(this);
            parent = value;
            if (value != null)
                value.getAttributeSet().addAttributeListener(this);
        }

        @Override
        public RamState clone() {
            RamState ret = (RamState) super.clone();
            ret.parent = null;
            ret.clockState = this.clockState.clone();
            ret.getContents().addHexModelListener(listener);
            return ret;
        }

        // Retrieves a HexFrame for editing within a separate window
        public HexFrame getHexFrame(Project proj) {
           // TODO: implement this
            return null;
           /*
            if (hexFrame == null) {
                hexFrame = new HexFrame(proj, getContents());
                hexFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        hexFrame = null;
                    }
                });
            }
            return hexFrame; */
        }

        //
        // methods for accessing the write-enable data
        //
        public boolean setClock(Value newClock, Object trigger) {
            return clockState.updateClock(newClock, trigger);
        }

        public void attributeListChanged(AttributeEvent e) {
        }

        public void attributeValueChanged(AttributeEvent e) {
            AttributeSet attrs = e.getSource();
            BitWidth addrBits = attrs.getValue(Mem.ADDR_ATTR);
            getContents().setDimensions(addrBits.getWidth(), dataBits.getWidth());
        }
    }

    public static class Logger extends InstanceLogger {
        @Override
        public BitWidth getBitWidth(InstanceState state, Object option) {
            return BitWidth.create(state.getAttributeValue(ADDR_ATTR).getWidth());
        }

        @Override
        public Object[] getLogOptions(InstanceState state) {
            int addrBits = state.getAttributeValue(ADDR_ATTR).getWidth();
            if (addrBits >= logOptions.length)
                addrBits = logOptions.length - 1;
            synchronized (logOptions) {
                Object[] ret = logOptions[addrBits];
                if (ret == null) {
                    ret = new Object[1 << addrBits];
                    logOptions[addrBits] = ret;
                    for (int i = 0; i < ret.length; i++) {
                        ret[i] = Integer.valueOf(i);
                    }
                }
                return ret;
            }
        }

        @Override
        public String getLogName(InstanceState state, Object option) {
            if (option instanceof Integer) {
                String disp = "CSRAM";
                Location loc = state.getInstance().getLocation();
                return disp + loc + "[" + option + "]";
            } else {
                return null;
            }
        }

        @Override
        public Value getLogValue(InstanceState state, Object option) {
            if (option instanceof Integer) {
                MemState s = (MemState) state.getData();
                int addr = ((Integer) option).intValue();
                return Value.createKnown(BitWidth.create(s.getDataBits()), s.getContents().get(addr));
            } else {
                return Value.NIL;
            }
        }
    }
}
