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

class RegisterFile16 extends InstanceFactory {

   private static final Attribute[] ATTRIBUTES = { StdAttr.TRIGGER };

   RegisterFile16() {
      super("RegisterFile16", new SimpleStringGetter("16x32 Register File"));
      int width = 32;
      int length = 16;
      int address_width = (int)(Math.log(length)/Math.log(2));
      int device_width = 100;
      int device_height = 160;
      setAttributes(new Attribute[] { StdAttr.TRIGGER }, new AttributeOption[] { StdAttr.TRIG_RISING });
      setOffsetBounds(Bounds.create(-device_width/2, -device_height/2, device_width, device_height));
      setPorts(new Port []{
         new Port(-device_width/2, -device_height/2 +20, Port.INPUT, address_width),
         new Port(-device_width/2, -device_height/2 +40, Port.INPUT, address_width),
         new Port(-device_width/2, -device_height/2 +60, Port.INPUT, address_width),
         new Port(-device_width/2, -device_height/2 +80, Port.INPUT, width),
         new Port(-device_width/2, -device_height/2 +100, Port.INPUT, width),

         new Port(-device_width/2 +50, device_height/2, Port.INPUT, 1),
         new Port(-device_width/2 +30, -device_height/2, Port.INPUT, 1),
         new Port(-device_width/2 +60, -device_height/2, Port.INPUT, 1),

         new Port(device_width/2, -device_height/2 +20, Port.OUTPUT, width),
         new Port(device_width/2, -device_height/2 +40, Port.OUTPUT, width),
      });

      //setInstancePoker(RegisterPoker.class);
   }

   @Override
   public void propagate(InstanceState state) {
      RegisterData data = RegisterData.get(state, 16, 32);
      AttributeOption triggerType = state.getAttributeValue(StdAttr.TRIGGER);
      BitWidth WIDTH = BitWidth.create(32);

      if (data.updateClock(state.getPortValue(5), triggerType)) {
         int a3 = (int)state.getPortValue(2).toLongValue();
         Value wr3 = state.getPortValue(3);
         Value r15 = state.getPortValue(4);
         Value clr = state.getPortValue(6);
         Value we3 = state.getPortValue(7);

         if (clr == Value.TRUE) {
            System.out.println("CLR");
            data.reset(Value.createKnown(32, 0));
         }
         else {
            if(we3 == Value.TRUE && a3 >= 0) {
               System.out.println("WR3");
               data.regs[a3] = wr3;
            }
            data.regs[15] = r15;
         }
      }
      for (int i = 0; i < 16; i++) {
         System.out.println("R"+i+" = "+data.regs[i]);
      }

      int a1 = (int)state.getPortValue(0).toLongValue();
      int a2 = (int)state.getPortValue(1).toLongValue();
      Value rd1 = a1 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a1];
      Value rd2 = a2 < 0 ?  Value.createUnknown(WIDTH) : data.regs[a2];
      state.setPort(8, rd1, 9);
      state.setPort(9, rd2, 9); 
   }

   @Override
   public void paintInstance(InstancePainter painter) {
      painter.drawRectangle(painter.getBounds(), "");
      painter.drawPort(0, "A1", Direction.EAST);
      painter.drawPort(1, "A2", Direction.EAST);
      painter.drawPort(2, "A3", Direction.EAST);
      painter.drawPort(3, "WD3", Direction.EAST);
      painter.drawPort(4, "R15", Direction.EAST);

      painter.drawClock(5, Direction.NORTH);
      painter.drawPort(6, "CLR", Direction.NORTH);
      painter.drawPort(7, "WE3", Direction.NORTH);

      painter.drawPort(8, "RD1", Direction.WEST);
      painter.drawPort(9, "RD2", Direction.WEST);
      /*
      Graphics g = painter.getGraphics();
      Bounds bounds = painter.getBounds();

      Font font = g.getFont().deriveFont(9f);

      // draw some pin labels
      int left = bounds.getX();
      int right = bounds.getX() + CHIP_WIDTH;
      int top = bounds.getY();
      int bottom =  bounds.getY() + CHIP_DEPTH;
      GraphicsUtil.drawText(g, font, "W", left+2, top+CHIP_DEPTH/2-10,
      GraphicsUtil.H_LEFT, GraphicsUtil.V_CENTER);
      GraphicsUtil.drawText(g, font, "A", right-2, top+40,
      GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      GraphicsUtil.drawText(g, font, "B", right-2, bottom-40,
      GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      GraphicsUtil.drawText(g, "WE", left+CHIP_WIDTH/2-40, bottom-1,
      GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
      GraphicsUtil.drawText(g, "xW", left+CHIP_WIDTH/2-10, bottom-1,
      GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
      GraphicsUtil.drawText(g, "xA", left+CHIP_WIDTH/2+30, bottom-1,
      GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);
      GraphicsUtil.drawText(g, "xB", left+CHIP_WIDTH/2+50, bottom-1,
      GraphicsUtil.H_CENTER, GraphicsUtil.V_BOTTOM);

      // draw some rectangles
      for (int i = 0; i < NUM_REGISTERS; i++) {
      drawBox(g, bounds, Color.GRAY, i);
      }

      // draw register labels
      for (int i = 0; i < NUM_REGISTERS; i++) {
      GraphicsUtil.drawText(g, font, "x"+i,
      bounds.getX() + boxX(i) - 1,
      bounds.getY() + boxY(i) + (BOX_HEIGHT-1)/2,
      GraphicsUtil.H_RIGHT, GraphicsUtil.V_CENTER);
      }
      
      if (!painter.getShowState()) {
      return;
      }

      // draw state
      g.setColor(Color.LIGHT_GRAY);
      g.fillRect(bounds.getX() + boxX(0)+1, bounds.getY() + boxY(0)+1, BOX_WIDTH-1, BOX_HEIGHT-1);
      g.setColor(Color.BLACK); */
      RegisterData data = RegisterData.get(painter, 16, 32);
      /*
      for (int i = 0; i < NUM_REGISTERS; i++) {
         long v = data.regs[i].toLongValue();
         String s = (data.regs[i].isFullyDefined() ? StringUtil.toHexString(WIDTH.getWidth(), v) : "?");
         GraphicsUtil.drawText(g, font, s,
         bounds.getX() + boxX(i) + BOX_WIDTH/2,
         bounds.getY() + boxY(i) + (BOX_HEIGHT-1)/2,
         GraphicsUtil.H_CENTER, GraphicsUtil.V_CENTER);
      } */
   }

}
