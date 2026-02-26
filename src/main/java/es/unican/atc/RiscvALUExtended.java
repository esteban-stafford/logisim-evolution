package es.unican.atc;

import java.awt.Color;
import java.awt.Graphics;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Graphics2D;

public class RiscvALUExtended extends RiscvALU {

    public RiscvALUExtended() {
        super("RiscvALUExt");
        setPorts(new Port[] {
		      new Port(interp(xp[6],xp[0],0.5), interp(yp[6],yp[0],0.5), Port.INPUT, 32), // A
		      new Port(interp(xp[3],xp[4],0.5), interp(yp[3],yp[4],0.5), Port.INPUT, 32), // B
          new Port(interp(xp[0],xp[1],0.66667), interp(yp[0],yp[1],0.66667), Port.INPUT, 4), // OP
		      new Port(interp(xp[1],xp[2],0.25), interp(yp[1],yp[2],0.25), Port.OUTPUT, 1), // Z
		      new Port(interp(xp[1],xp[2],0.5), interp(yp[1],yp[2],0.5), Port.OUTPUT, 32) }); // C
    }

    @Override
    public void propagate(InstanceState state) {
        int operation = (int)state.getPortValue(OP).toLongValue();
        if(operation<=7)
        {
          super.propagate(state);
        }
        else
        {
          long valueA = state.getPortValue(A).toLongValue();
          long valueB = state.getPortValue(B).toLongValue();
          long ans = 0L;
          switch (operation) {
             case 0x8:
                 ans = valueA % valueB; //rem
                 break;
             case 0x9:
                 ans = valueA << valueB; //sll
                 break;
          }
          Value valueC = Value.createKnown(BitWidth.create(32), ans);
          Value valueZ = Value.createKnown(BitWidth.create(1), ans == 0 ? 1 : 0);
          state.setPort(C, valueC, 32);
          state.setPort(Z, valueZ, 1);
        }
    }
}
