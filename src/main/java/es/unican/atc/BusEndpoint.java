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
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringUtil;


class BusEndpoint extends InstanceFactory {

   public static final int ADDR_OUT = 0;
   public static final int DATA_OUT = 1;
   public static final int DATA_IN = 2;
   public static final int WRITE_ENABLE_OUT = 3;

   public static final int DEV_ADDR = 4;
   public static final int DEV_IN = 5;
   public static final int DEV_OUT = 6;

   public static final int BUS_REQ = 7;
   public static final int BUS_GRANT = 8;
   
   public static final int WRITE_ENABLE_DEV = 9;
   
   private static String[] labels = { "A_O", "D_O", "D_I", "WE_O", "DV_A", "DV_I", "DV_O", "B_R", "B_G", "WE_D" };

   /*public static final Attribute<Boolean> CLEAR_TO_ZERO =
      Attributes.forBoolean("clearToZero", S.getter("Clear to Zero"));
   public static final Object DEFAULT_CLEAR_TO_ZERO = Boolean.TRUE;*/

  public static final Attribute<Long> ATTR_RANGE_START =
      Attributes.forHexLong("addressRangeStart", S.getter("deviceRangeStart"));
  public static final Attribute<Long> ATTR_RANGE_END =
      Attributes.forHexLong("addressRangeEnd", S.getter("deviceRangeEnd"));
    
   //private Long range_start = 0xFFFFFFFFL;
   //private Long range_end = 0xFFFFFFFFL;

   BusEndpoint() {
      super("BusEndpoint", new SimpleStringGetter("Bus endpoint"));
      setAttributes(new Attribute[] {
         ATTR_RANGE_START,
         ATTR_RANGE_END
      }, new Object[] {
         0xFFFFFFFFL,
         0xFFFFFFFFL
      });

      int spacing = 10;
      int width = 16 * spacing;
      int height = 6 * spacing;
      //int address_width = (int)(Math.log(NUM_REGISTERS)/Math.log(2));
      
      Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
      setOffsetBounds(bounds);
      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();
      Port[] ports = new Port[labels.length];
      ports[WRITE_ENABLE_OUT]  = new Port(x0 + 3 * spacing, y0, Port.OUTPUT, 1);
      ports[ADDR_OUT]  = new Port(x0 + 6 * spacing, y0, Port.OUTPUT, 32);
      ports[DATA_OUT]  = new Port(x0 + 9 * spacing, y0, Port.OUTPUT, 32);
      ports[DATA_IN] = new Port(x0 + 12 *spacing, y0, Port.INPUT, 32);

      ports[WRITE_ENABLE_DEV] = new Port(x0 + 3*spacing, y1, Port.INPUT, 1);
      ports[DEV_ADDR] = new Port(x0 + 6*spacing, y1, Port.INPUT, 32);
      ports[DEV_IN] = new Port(x0 + 9*spacing, y1, Port.INPUT, 32);
      ports[DEV_OUT] = new Port(x0 + 12*spacing, y1, Port.OUTPUT, 32);

      ports[BUS_REQ] = new Port(x0, y0 + 2*spacing, Port.OUTPUT, 1);
      ports[BUS_GRANT] = new Port(x0, y0 + 4*spacing, Port.INPUT, 1);


      setPorts(ports);
   }

   @Override
   public void propagate(InstanceState state) {
      AttributeOption triggerType = state.getAttributeValue(StdAttr.TRIGGER);
      BitWidth WIDTH = BitWidth.create(32);
    
      final var attrs = state.getAttributeSet();
 
      Value addr_out=state.getPortValue(ADDR_OUT);
      Value data_out=state.getPortValue(DATA_OUT);
      Value write_enable=state.getPortValue(WRITE_ENABLE_OUT);
      Value dev_out=state.getPortValue(DEV_OUT);
      Value bus_grant=state.getPortValue(BUS_GRANT);

      Value dev_addr=Value.createUnknown(WIDTH);
      Value data_in=Value.createUnknown(WIDTH);
      Value dev_in=Value.createUnknown(WIDTH);
      Value bus_req=Value.createUnknown(WIDTH);
      Value write_enable_dev=Value.createUnknown(WIDTH);

      long range_start=attrs.getValue(ATTR_RANGE_START);
      long range_end=attrs.getValue(ATTR_RANGE_END);

      if(range_start<=addr_out.toLongValue() && range_end>addr_out.toLongValue())
      {
          dev_addr=Value.createKnown(BitWidth.create(32), addr_out.toLongValue()-range_start);
          dev_in=data_out;
          data_in=dev_out;
          bus_req=Value.createUnknown(WIDTH); //This needs to be set
          write_enable_dev=write_enable;
      }

      state.setPort(DEV_ADDR, dev_addr, 1); 
      state.setPort(DEV_IN, dev_in, 1);
      state.setPort(DATA_IN, data_in, 1);
      state.setPort(BUS_REQ, bus_req, 1);
      state.setPort(WRITE_ENABLE_DEV, write_enable_dev, 1);
   }
   
   @Override
   public void paintInstance(InstancePainter painter) {
      Bounds bounds = painter.getBounds();
      painter.drawRectangle(bounds, "");
      painter.drawPort(DEV_ADDR, labels[DEV_ADDR], Direction.NORTH);
      painter.drawPort(DATA_IN, labels[DATA_IN], Direction.NORTH);
      painter.drawPort(DATA_OUT, labels[DATA_OUT], Direction.NORTH);
      painter.drawPort(WRITE_ENABLE_OUT, labels[WRITE_ENABLE_OUT], Direction.NORTH);

      painter.drawPort(ADDR_OUT, labels[ADDR_OUT], Direction.SOUTH);
      painter.drawPort(DEV_IN, labels[DEV_IN], Direction.SOUTH);
      painter.drawPort(DEV_OUT, labels[DEV_OUT], Direction.SOUTH);

      painter.drawPort(BUS_REQ, labels[BUS_REQ], Direction.WEST);
      painter.drawPort(BUS_GRANT, labels[BUS_GRANT], Direction.WEST);
      
      painter.drawPort(WRITE_ENABLE_DEV, labels[WRITE_ENABLE_DEV], Direction.NORTH);

      Graphics g = painter.getGraphics();

      Font font = g.getFont().deriveFont(7f);

      if (!painter.getShowState()) {
         return;
      }

   }
  
}
