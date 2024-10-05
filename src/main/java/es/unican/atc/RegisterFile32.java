package es.unican.atc; 

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
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

class RegisterFile32 extends InstanceFactory {

   private static final Attribute[] ATTRIBUTES = { StdAttr.TRIGGER };
   public static final int A1 = 0;
   public static final int A2 = 1;
   public static final int A3 = 2;
   public static final int WD3 = 3;
   public static final int R15 = 4;

   public static final int CLK = 5;
   public static final int CLR = 6;
   public static final int WE3 = 7;

   public static final int RD1 = 8;
   public static final int RD2 = 9;

   public static final int NUM_REGISTERS = 32;
   public static final int REGISTER_WIDTH = 32;

   public static final int DEVICE_WIDTH = 140;
   public static final int DEVICE_HEIGHT = 200;

   RegisterFile32() {
      super("RegisterFile32", new SimpleStringGetter("32x32 Register File"));
      int address_width = (int)(Math.log(NUM_REGISTERS)/Math.log(2));
      setAttributes(new Attribute[] { StdAttr.TRIGGER }, new AttributeOption[] { StdAttr.TRIG_RISING });
      setOffsetBounds(Bounds.create(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2, DEVICE_WIDTH, DEVICE_HEIGHT));
      Port[] ports = new Port[10];
      ports[A1] = new Port(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +20, Port.INPUT, address_width);
      ports[A2] = new Port(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +40, Port.INPUT, address_width);
      ports[A3] = new Port(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +60, Port.INPUT, address_width);
      ports[WD3] = new Port(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +80, Port.INPUT, REGISTER_WIDTH);
      ports[R15] = new Port(-DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +100, Port.INPUT, REGISTER_WIDTH);

      ports[CLK] = new Port(-DEVICE_WIDTH/2 +50, DEVICE_HEIGHT/2, Port.INPUT, 1);
      ports[CLR] = new Port(-DEVICE_WIDTH/2 +30, DEVICE_HEIGHT/2, Port.INPUT, 1);
      ports[WE3] = new Port(-DEVICE_WIDTH/2 +60, -DEVICE_HEIGHT/2, Port.INPUT, 1);

      ports[RD1] = new Port(DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +20, Port.OUTPUT, REGISTER_WIDTH);
      ports[RD2] = new Port(DEVICE_WIDTH/2, -DEVICE_HEIGHT/2 +40, Port.OUTPUT, REGISTER_WIDTH);
      setPorts(ports);

      //setInstancePoker(RegisterPoker.class);
   }

   @Override
   public void propagate(InstanceState state) {
      RegisterData data = RegisterData.get(state, NUM_REGISTERS, 32);
      AttributeOption triggerType = state.getAttributeValue(StdAttr.TRIGGER);
      BitWidth WIDTH = BitWidth.create(32);

      if (state.getPortValue(CLR) == Value.TRUE) {
         // System.out.println("CLR");
         data.reset(Value.createKnown(32, 0));
      }
      if (data.updateClock(state.getPortValue(CLK), triggerType)) {
         int a3 = (int)state.getPortValue(A3).toLongValue();
         Value wr3 = state.getPortValue(WD3);
         Value r15 = state.getPortValue(R15);
         Value we3 = state.getPortValue(WE3);

         if(we3 == Value.TRUE && a3 >= 0) {
            // System.out.println("WR3");
            data.regs[a3] = wr3;
         }
         data.regs[15] = r15;
      }
      /* for (int i = 0; i < NUM_REGISTERS; i++) {
         System.out.println("R"+i+" = "+data.regs[i]);
      } */

      int a1 = (int)state.getPortValue(A1).toLongValue();
      int a2 = (int)state.getPortValue(A2).toLongValue();
      Value rd1 = a1 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a1];
      Value rd2 = a2 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a2];
      state.setPort(RD1, rd1, 9);
      state.setPort(RD2, rd2, 9); 
   }

   @Override
   public void paintInstance(InstancePainter painter) {
      painter.drawRectangle(painter.getBounds(), "");
      painter.drawPort(A1, "A1", Direction.EAST);
      painter.drawPort(A2, "A2", Direction.EAST);
      painter.drawPort(A3, "A3", Direction.EAST);
      painter.drawPort(WD3, "WD3", Direction.EAST);
      painter.drawPort(R15, "R15", Direction.EAST);

      painter.drawClock(CLK, Direction.NORTH);
      painter.drawPort(CLR, "CLR", Direction.SOUTH);
      painter.drawPort(WE3, "WE3", Direction.NORTH);

      painter.drawPort(RD1, "RD1", Direction.WEST);
      painter.drawPort(RD2, "RD2", Direction.WEST);

      Graphics g = painter.getGraphics();
      Bounds bounds = painter.getBounds();

      Font font = g.getFont().deriveFont(9f);

      /*
      // draw some rectangles
      for (int i = 0; i < NUM_REGISTERS; i++) {
      drawBox(g, bounds, Color.GRAY, i);
      }
      */
      // draw register labels
      for (int i = 0; i < NUM_REGISTERS/2; i++) {
         GraphicsUtil.drawText(g, font, "x"+i,
            bounds.getX() + 50,
            bounds.getY() + 25 + i*10,
            GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      }
      if (!painter.getShowState()) {
         return;
      }

      // draw state
      //g.setColor(Color.LIGHT_GRAY);
      //g.fillRect(bounds.getX() + boxX(0)+1, bounds.getY() + boxY(0)+1, BOX_WIDTH-1, BOX_HEIGHT-1);
      //g.setColor(Color.BLACK); 
      RegisterData data = RegisterData.get(painter, NUM_REGISTERS, 32);
      for (int i = 0; i < NUM_REGISTERS/2; i++) {
         long v = data.regs[i].toLongValue();
         String s = (data.regs[i].isFullyDefined() ? StringUtil.toHexString(REGISTER_WIDTH, v) : "?");
         GraphicsUtil.drawText(g, font, s,
         bounds.getX() + 80,    
         bounds.getY() + 25 + i*10,
         GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
      } 
   }

}
