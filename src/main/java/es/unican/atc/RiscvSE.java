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

public class RiscvSE extends InstanceFactory {
    int xp[], yp[];

    public static final int Imm = 0;  // Input Immediate (left)
    public static final int Mode = 1; // Control Signal (top)
    public static final int SignExtImm = 2; // Output (right)

    public RiscvSE() {
        super("RiscvSE");
	int width = 60;
	int height = 40;
        Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
        setOffsetBounds(bounds);
        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();

        xp = new int[] { x0,            x0, x1, x1, x0 };
        yp = new int[] { y0 + height/2, y1, y1, y0, y0 + height/2 };

        setPorts(new Port[]{
            new Port(x0,           y0 + 3*height/4, Port.INPUT, 25), // Imm
            new Port(x0 + width/2, y0 + height/4,   Port.INPUT, 2), // Mode
            new Port(x1,           y0 + height/2,   Port.OUTPUT, 32) // SignExtImm
        });
    }

    @Override
    public void propagate(InstanceState state) {
        long imm = state.getPortValue(Imm).toLongValue();
        int mode = (int) state.getPortValue(Mode).toLongValue();
        long signExtImm = 0;

        switch (mode) {
            case 0x0: // I-type
                signExtImm = imm & 0xFFF; // Keep 12 bits
                if ((imm & 0x800) != 0) {
                    signExtImm |= 0xFFFFF000; // Sign-extend
                }
                break;

            case 0x1: // S-type
            case 0x2: // B-type
                signExtImm = imm & 0xFFF;
                if ((imm & 0x800) != 0) {
                    signExtImm |= 0xFFFFF000;
                }
                break;

            case 0x3: // U-type
                signExtImm = imm << 12; // Shift left by 12 bits
                break;

            case 0x4: // J-type
                signExtImm = imm & 0xFFFFF;
                if ((imm & 0x80000) != 0) {
                    signExtImm |= 0xFFF00000;
                }
                break;
        }

        Value signExtImmValue = Value.createKnown(BitWidth.create(32), signExtImm);
        state.setPort(SignExtImm, signExtImmValue, 32);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
	Graphics2D g = (Graphics2D) painter.getGraphics().create();
	Location loc = painter.getLocation();
	g.translate(loc.getX(), loc.getY());
	GraphicsUtil.switchToWidth(g, 2);
        g.drawPolygon(xp, yp, xp.length);
	g.dispose();
        painter.drawPort(Imm, "", Direction.EAST);
        painter.drawPort(Mode, "", Direction.NORTH);
        painter.drawPort(SignExtImm, "", Direction.WEST);
    }

    @Override
    public void paintIcon(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        g.setColor(Color.BLACK);
        int ixp[] = new int[xp.length];
        int iyp[] = new int[yp.length];
        for (int i = 0; i < xp.length; i++) {
            ixp[i] = xp[i] * 15 / 60;
            iyp[i] = yp[i] * 15 / 60;
        }
        g.drawPolygon(ixp, iyp, ixp.length);
    }
}
