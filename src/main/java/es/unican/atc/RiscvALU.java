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

public class RiscvALU extends InstanceFactory {
    int xp[], yp[];

    public static final int A = 0;
    public static final int B = 1;
    public static final int OP = 2;
    public static final int Z = 3;

    public RiscvALU() {
        super("RiscvALU");
	int width = 60;
	int height = 100;
        Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
        setOffsetBounds(bounds);
        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();
        xp = new int[] { x0, x1,               x1,               x0, x0,              x0 + width/3,  x0 };
        yp = new int[] { y0, y0 + height*3/10, y1 - height*3/10, y1, y1 - height*2/5, y1 - height/2, y1 - height*3/5 };
        setPorts(new Port[] {
		new Port(interp(xp[6],xp[0],0.5), interp(yp[6],yp[0],0.5), Port.INPUT, 32), // A
		new Port(interp(xp[3],xp[4],0.5), interp(yp[3],yp[4],0.5), Port.INPUT, 32), // B
                new Port(interp(xp[0],xp[1],0.66667), interp(yp[0],yp[1],0.66667), Port.INPUT, 3), // OP
		new Port(interp(xp[1],xp[2],0.25), interp(yp[1],yp[2],0.25), Port.OUTPUT, 1) }); // Z
    }
    private int interp(int a, int b, double t) {
	    return (int)Math.round((double)a*(1.0-t)+(double)b*t);
    }

    @Override
    public void propagate(InstanceState state) {
        long valueA = state.getPortValue(A).toLongValue();
        long valueB = state.getPortValue(B).toLongValue();
        int operation = (int)state.getPortValue(OP).toLongValue();
        long ans = 0L;
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
        }
        Value valueZ = Value.createKnown(BitWidth.create(1), ans == 0 ? 1 : 0);
        state.setPort(Z, valueZ, 1);
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
    }

    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        g.setColor(Color.BLACK);
        int xp[] = { 0, 15, 15, 0, 0, 3, 0 };
        int yp[] = { 0, 5, 10, 15, 10, 8, 6 };
        g.drawPolygon(xp, yp, 7);
    }
}
