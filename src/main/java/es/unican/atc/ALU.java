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

public class ALU extends InstanceFactory {
    int xp[], yp[];
    public ALU() {
        super("ALU");
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
		new Port(interp(xp[6],xp[0],0.5), interp(yp[6],yp[0],0.5), Port.INPUT, 32),
		new Port(interp(xp[3],xp[4],0.5), interp(yp[3],yp[4],0.5), Port.INPUT, 32),
                new Port(interp(xp[2],xp[3],0.33333), interp(yp[2],yp[3],0.33333), Port.INPUT, 4),
		new Port(interp(xp[2],xp[3],0.66667), interp(yp[2],yp[3],0.66667), Port.INPUT, 5),
		new Port(interp(xp[1],xp[2],0.5), interp(yp[1],yp[2],0.5), Port.OUTPUT, 32), });
    }
    private int interp(int a, int b, double t) {
	    return (int)Math.round((double)a*(1.0-t)+(double)b*t);
    }

    // Shifts A for risc-v not B
    @Override
    public void propagate(InstanceState state) {
        long A = state.getPortValue(0).toLongValue();
        long B = state.getPortValue(1).toLongValue();
        int op = (int)state.getPortValue(2).toLongValue();
        long shift = state.getPortValue(3).toLongValue();
        long ans = 0L;
        switch (op) {

        /*
        case 0x0: //0b0000
        case 0x1: //0b0001
        case 0x2: //0b0010
        case 0x3: //0b0011
        case 0x4: //0b0100
        case 0x5: //0b0101
        case 0x6: //0b0110
        case 0x7: //0b0111
        case 0x8: //0b1000
        case 0x9: //0b1001
        case 0xa: //0b1010
        case 0xb: //0b1011
        case 0xc: //0b1100
        case 0xd: //0b1101
        case 0xe: //0b1110
        case 0xf: //0b1111
        */

        case 0x0: //0b0000
        case 0x1: //0b0001
            //ADD
            ans = A + B;
            break;

        case 0x2: //0b0010
        case 0x3: //0b0011
            //SLL
            ans = A << shift;
            break;

        case 0x4: //0b0100
        case 0x5: //0b0101
            //SUB
            ans = A - B;
            break;

        case 0x6: //0b0110
            //SRL
            ans = A >>> shift;
            break;

        case 0x7: //0b0111
            //SRA
            ans = A >> shift;
            break;

        case 0x8: //0b1000
            //LE
            ans = (A <= 0) ? 0x1 : 0x0;
            break;

        case 0x9: //0b1001
            //GT
            ans = (A > 0) ? 0x1 : 0x0;
            break;

        case 0xa: //0b1010
            //NE
            ans = (A != B) ? 0x1 : 0x0;
            break;

        case 0xb: //0b1011
            //EQ
            ans = (A == B) ? 0x1 : 0x0;
            break;

        case 0xc: //0b1100
            //XOR
            ans = A ^ B;
            break;

        case 0xd: //0b1101
            //NOR
            ans = ~(A | B);
            break;

        case 0xe: //0b1110
            //OR
            ans = A | B;
            break;

        case 0xf: //0b1111
            //AND
            ans = A & B;
            break;
        }
        Value out = Value.createKnown(BitWidth.create(32), ans);
        // Eh, delay of 32? Sure...
        state.setPort(4, out, 32);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
	Graphics2D g = (Graphics2D) painter.getGraphics().create();
	Location loc = painter.getLocation();
	g.translate(loc.getX(), loc.getY());
	GraphicsUtil.switchToWidth(g, 2);
        g.drawPolygon(xp, yp, xp.length);
	g.dispose();
        painter.drawPort(0, "A", Direction.EAST);
        painter.drawPort(1, "B", Direction.EAST);
        painter.drawPort(2, "OP", Direction.SOUTH);
        painter.drawPort(3, "SA", Direction.SOUTH);
        painter.drawPort(4, "C", Direction.WEST);
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
