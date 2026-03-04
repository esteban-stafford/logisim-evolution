package es.unican.atc;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Graphics2D;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import static com.cburch.logisim.std.Strings.S;

import java.util.LinkedList;

public class RiscvALU extends InstanceFactory {
    int xp[], yp[];

    private Value result_timing_wheel[];
    private int wheel_pos;
    private Value lastClock;
    private Value lastValue;
    private int opWidth;
	  private int width = 60;
	  private int height = 100;

    public static final int A = 0;
    public static final int B = 1;
    public static final int OP = 2;
    public static final int Z = 3;
    public static final int C = 4;
    public static final int CLOCK = 5;
    public static final int CLEAR = 6;
    public static final int READY = 7;

    public static final int LAT_BASE = 0;
    public static final int LAT_MUL = 3;
    public static final int LAT_DIV = 7;
    
    public static final int DEFAULT_CONTROL_WIDTH= 3;

    public static final AttributeOption PIPELINED_OFF = new AttributeOption("off", S.getter("Disabled"));
    public static final AttributeOption PIPELINED_ON = new AttributeOption("on", S.getter("Enabled"));

    public static final Attribute<AttributeOption> PIPELINED =
      Attributes.forOption(
        "pipelined",
        S.getter("aluPipelined"),
        new AttributeOption[] { PIPELINED_OFF, PIPELINED_ON }
    );
  
    public static final Attribute<Integer> CONTROL_WIDTH =
      Attributes.forInteger("aluControlWidth", S.getter("controlWidth"));
    

    public RiscvALU() {
        super("RiscvALU");
        create();
    }

  
    
    public RiscvALU(String name) {
        super(name);
        create();
    }
  
    private void create(){ 
        setAttributes(new Attribute[] {
           PIPELINED,
           CONTROL_WIDTH
        }, new Object[] {
           PIPELINED_OFF,
           DEFAULT_CONTROL_WIDTH
        });
        
        Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
        setOffsetBounds(bounds);

        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();

        xp = new int[] { x0, x1,               x1,               x0, x0,              x0 + width/3,  x0 };
        yp = new int[] { y0, y0 + height*3/10, y1 - height*3/10, y1, y1 - height*2/5, y1 - height/2, y1 - height*3/5 };

        int wheel_size=Math.max(Math.max(LAT_BASE, LAT_MUL), LAT_DIV)+1; //Size the maximum of the latencies plus 1
        result_timing_wheel=new Value[wheel_size];
        init_wheel();

        lastClock=null;
        lastValue=Value.createUnknown(BitWidth.create(32));
    }

    private void configurePorts(Instance instance)
    {
        opWidth = instance.getAttributeValue(CONTROL_WIDTH);
        instance.setPorts(new Port[] {
		new Port(interp(xp[6],xp[0],0.5), interp(yp[6],yp[0],0.5), Port.INPUT, 32), // A
		new Port(interp(xp[3],xp[4],0.5), interp(yp[3],yp[4],0.5), Port.INPUT, 32), // B
                new Port(interp(xp[0],xp[1],0.66667), interp(yp[0],yp[1],0.66667), Port.INPUT, opWidth), // OP
		new Port(interp(xp[1],xp[2],0.25), interp(yp[1],yp[2],0.25), Port.OUTPUT, 1), // Z
		new Port(interp(xp[1],xp[2],0.5), interp(yp[1],yp[2],0.5), Port.OUTPUT, 32), // C
                new Port(interp(xp[0],xp[1],0.3334), interp(yp[3],yp[4],0.2), Port.INPUT, 1), // CLOCK
                new Port(interp(xp[0],xp[1],0.6667), interp(yp[3],yp[4],0.4), Port.INPUT, 1),// CL
                new Port(interp(xp[1],xp[2],0.25), interp(yp[1],yp[2],0.75), Port.OUTPUT, 1) });// R
    }

    private void init_wheel()
    {
        wheel_pos=0;
        for(int i=0;i<result_timing_wheel.length;i++){
          result_timing_wheel[i]=Value.createUnknown(BitWidth.create(32));
        }
    }


    protected int interp(int a, int b, double t) {
	    return (int)Math.round((double)a*(1.0-t)+(double)b*t);
    }

    @Override
    public void propagate(InstanceState state) {

        if (state.getPortValue(CLEAR) == Value.TRUE) {
          init_wheel();
          lastClock=null;
          lastValue=Value.createUnknown(BitWidth.create(32));
        }

        long valueA = state.getPortValue(A).toLongValue();
        long valueB = state.getPortValue(B).toLongValue();
        Value newClock = state.getPortValue(CLOCK);
        int operation = (int)state.getPortValue(OP).toLongValue();
        long ans = 0L;
        int latency=LAT_BASE;

        switch (operation) {
           case 0x0:
               ans = valueA + valueB;
               break;
           case 0x1:
               ans = valueA - valueB;
               break;
           case 0x2:
               ans = valueA & valueB;
               break;
           case 0x3:
               ans = valueA | valueB;
               break;
           case 0x4:
               ans = valueA * valueB;
               latency=LAT_MUL;
               break;
           case 0x5:
               ans = valueA / valueB;
               latency=LAT_DIV;
               break;
           case 0x6:
               ans = (valueA < valueB) ? 1 : 0;
               break;
           case 0x8:
               ans = valueA % valueB; //rem
               break;
           case 0x9:
               ans = valueA << valueB; //sll
               break;
        }

        Value valueC = Value.createKnown(BitWidth.create(32), ans);

        var isPipelined = state.getAttributeValue(PIPELINED);
        if (isPipelined == PIPELINED_ON){
          Value oldClock = lastClock;
          lastClock=newClock;
          if(oldClock == Value.FALSE && newClock == Value.TRUE){ //Assuming the clock is trigg rising
            int slot=findTimingWheelSlot((wheel_pos+latency)%result_timing_wheel.length);
            if(slot!=wheel_pos){ // If the slot is not the current position, store the result there and obtain the actual value to be output
              result_timing_wheel[slot]=valueC;
              valueC=result_timing_wheel[wheel_pos];
              result_timing_wheel[wheel_pos]=Value.createUnknown(BitWidth.create(32));
            }
            wheel_pos=(wheel_pos+1)%result_timing_wheel.length;
          }
          else
          {
            valueC=lastValue;
          }
        }

        Value valueZ = Value.createKnown(BitWidth.create(1), valueC.toLongValue() == 0 ? 1 : 0);
        Value valueR = Value.createKnown(BitWidth.create(1), valueC.isUnknown() ? 0 : 1);

        state.setPort(C, valueC, 32);
        state.setPort(Z, valueZ, 1);
        state.setPort(READY, valueR, 1);
        lastValue=valueC;
    }

    private int findTimingWheelSlot(int startPos) {
      int i=startPos;
      int res=-1;

      do {
        if(result_timing_wheel[i].isUnknown()) {
          res=i;
        }
        i=(i+1)%result_timing_wheel.length;
      }while(i!=wheel_pos && res==-1);
      return res;
    }

    @Override
    public void paintInstance(InstancePainter painter) {
	Graphics2D g = (Graphics2D) painter.getGraphics().create();
	Location loc = painter.getLocation();
	g.translate(loc.getX(), loc.getY());
	GraphicsUtil.switchToWidth(g, 2);
        g.drawPolygon(xp, yp, xp.length);
	g.dispose();
        painter.drawPort(A, "A", Direction.EAST);
        painter.drawPort(B, "B", Direction.EAST);
        painter.drawPort(OP, "OP", Direction.NORTH);
        painter.drawPort(Z, "Z", Direction.WEST);
        painter.drawPort(C, "C", Direction.WEST);
        
        var opt = painter.getAttributeValue(PIPELINED);
        if (opt == PIPELINED_ON) {
          painter.drawClock(CLOCK, Direction.NORTH);
          painter.drawPort(CLEAR, "CL", Direction.SOUTH);
          painter.drawPort(READY, "R", Direction.WEST);
        }
    }

    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        g.setColor(Color.BLACK);
        int xp[] = { 0, 15, 15, 0, 0, 3, 0 };
        int yp[] = { 0, 5, 10, 15, 10, 8, 6 };
        g.drawPolygon(xp, yp, 7);
    }


  @Override
  protected void configureNewInstance(Instance instance) {
    // Important: make sure attribute changes get forwarded
    configurePorts(instance);
    instance.addAttributeListener();
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == CONTROL_WIDTH) {
      configurePorts(instance);
    }
    instance.fireInvalidated();
  }
}
