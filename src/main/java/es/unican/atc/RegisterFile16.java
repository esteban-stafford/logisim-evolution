package es.unican.atc; 

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;

class RegisterFile16 extends InstanceFactory {

   private static final Attribute[] ATTRIBUTES = { StdAttr.TRIGGER };
   public static final int A1 = 0;
   public static final int A2 = 1;
   public static final int A3 = 2;
   public static final int WD3 = 3;
   public static final int R15 = 4;

   public static final int CLR = 5;
   public static final int CLK = 6;
   public static final int WE3 = 7;

   public static final int RD1 = 8;
   public static final int RD2 = 9;
   private static String[] labels = { "A1", "A2", "A3", "WD3", "R15", "CLK", "CLR", "WE3", "RD1", "RD2" };

   public static final int NUM_REGISTERS = 16;
   public static final int REGISTER_WIDTH = 32;

   public static final Attribute<Boolean> CLEAR_TO_ZERO =
      Attributes.forBoolean("clearToZero", S.getter("Clear to Zero"));
   public static final Object DEFAULT_CLEAR_TO_ZERO = Boolean.TRUE;

   RegisterFile16() {
      super("RegisterFile16", new SimpleStringGetter("16x32 Register File"));
      int spacing = 10;
      int width = 14 * spacing;
      int height = 20 * spacing;
      int address_width = (int)(Math.log(NUM_REGISTERS)/Math.log(2));
      setAttributes(new Attribute[] {
         StdAttr.TRIGGER,
         CLEAR_TO_ZERO
      }, new Object[] {
         StdAttr.TRIG_RISING,
         DEFAULT_CLEAR_TO_ZERO
      });
      Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
      setOffsetBounds(bounds);
      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();
      Port[] ports = new Port[labels.length];
      ports[A1]  = new Port(x0, y0 + 2*spacing, Port.INPUT, address_width);
      ports[A2]  = new Port(x0, y0 + 4*spacing, Port.INPUT, address_width);
      ports[A3]  = new Port(x0, y0 + 6*spacing, Port.INPUT, address_width);
      ports[WD3] = new Port(x0, y0 + 8*spacing, Port.INPUT, REGISTER_WIDTH);
      ports[R15] = new Port(x0, y0 + 10*spacing, Port.INPUT, REGISTER_WIDTH);

      ports[CLR] = new Port(x0 + 3*spacing, y1, Port.INPUT, 1);
      ports[CLK] = new Port(x0 + 5*spacing, y1, Port.INPUT, 1);
      ports[WE3] = new Port(x0 + 7*spacing, y0, Port.INPUT, 1);

      ports[RD1] = new Port(x1, y0 + 2*spacing, Port.OUTPUT, REGISTER_WIDTH);
      ports[RD2] = new Port(x1, y0 + 8*spacing, Port.OUTPUT, REGISTER_WIDTH);
      setPorts(ports);
   }

   @Override
   public void propagate(InstanceState state) {
      RegisterData data = RegisterData.get(state, NUM_REGISTERS, 32);
      AttributeOption triggerType = state.getAttributeValue(StdAttr.TRIGGER);
      Object clearToZero = state.getAttributeValue(CLEAR_TO_ZERO);
      BitWidth WIDTH = BitWidth.create(32);

      if (state.getPortValue(CLR) == Value.TRUE) {
         if(clearToZero == Boolean.TRUE) {
            for (int i = 0; i < NUM_REGISTERS; i++) {
               data.regs[i] = Value.createKnown(32, 0);
            }
         } else {
            for (int i = 0; i < NUM_REGISTERS; i++) {
               data.regs[i] = Value.createKnown(32, i*16);
            }
         }
      }
      if (data.updateClock(state.getPortValue(CLK), triggerType)) {
         int a3 = (int)state.getPortValue(A3).toLongValue();
         Value wr3 = state.getPortValue(WD3);
         Value we3 = state.getPortValue(WE3);

         if(we3 == Value.TRUE && a3 >= 0) {
            data.regs[a3] = wr3;
         }
      }
      Value r15 = state.getPortValue(R15);
      data.regs[15] = r15;

      int a1 = (int)state.getPortValue(A1).toLongValue();
      int a2 = (int)state.getPortValue(A2).toLongValue();
      Value rd1 = a1 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a1];
      Value rd2 = a2 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a2];
      state.setPort(RD1, rd1, 9);
      state.setPort(RD2, rd2, 9); 
   }

   @Override
   public void paintInstance(InstancePainter painter) {
      Bounds bounds = painter.getBounds();
      painter.drawRectangle(bounds, "");
      painter.drawPort(A1, "A1", Direction.EAST);
      painter.drawPort(A2, "A2", Direction.EAST);
      painter.drawPort(A3, "A3", Direction.EAST);
      painter.drawPort(WD3, "WD3", Direction.EAST);
      painter.drawPort(R15, "R15", Direction.EAST);

      painter.drawPort(CLR, "CLR", Direction.SOUTH);
      painter.drawClock(CLK, Direction.NORTH);
      painter.drawPort(WE3, "WE3", Direction.NORTH);

      painter.drawPort(RD1, "RD1", Direction.WEST);
      painter.drawPort(RD2, "RD2", Direction.WEST);

      Graphics g = painter.getGraphics();

      Font font = g.getFont().deriveFont(9f);

      for (int i = 0; i < NUM_REGISTERS; i++) {
         GraphicsUtil.drawText(g, font, "R"+i,
            bounds.getX() + 50,
            bounds.getY() + 25 + i*10,
            GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      }
      if (!painter.getShowState()) {
         return;
      }

      RegisterData data = RegisterData.get(painter, NUM_REGISTERS, 32);
      for (int i = 0; i < NUM_REGISTERS; i++) {
         long v = data.regs[i].toLongValue();
         String s = (data.regs[i].isFullyDefined() ? StringUtil.toHexString(REGISTER_WIDTH, v) : "?");
         GraphicsUtil.drawText(g, font, s,
         bounds.getX() + 80,    
         bounds.getY() + 25 + i*10,
         GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
      } 
   }
}
