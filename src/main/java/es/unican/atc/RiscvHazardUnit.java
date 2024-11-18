package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class RiscvHazardUnit extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "       long MemToReg = state.getPortValue(nameToId.get(\"MemToReg\")).toLongValue();\n" +
    "       long Rs1D = state.getPortValue(nameToId.get(\"Rs1D\")).toLongValue();\n" +
    "       long Rs2D = state.getPortValue(nameToId.get(\"Rs2D\")).toLongValue();\n" +
    "       long RdE = state.getPortValue(nameToId.get(\"RdE\")).toLongValue();\n" +
    "       \n" +
    "       long stall = 0;\n" + // MemToReg & ((Rs1D == RdE) | (Rs2D == RdE) ? 1 : 0);\n" +
    "       \n" +
    "       Value stallValue = Value.createKnown(BitWidth.create(1), stall);\n"+
    "       state.setPort(nameToId.get(\"stall\"), stallValue, 1);\n";

    public static final int RESULTSRCE0 = 0;
    public static final int RS1D = 1;
    public static final int RS2D = 2;
    public static final int RDE = 3;
    public static final int STALL = 4;
    private static String[] labels = new String[] { "MemToReg", "Rs1D", "Rs2D", "RdE", "stall" };

    protected RiscvHazardUnit()
    {
       super("RiscvHazardUnit", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 10 * spacing;
       int height = 6 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x1,             y0 + 4*spacing, Port.INPUT,  1), // MemToReg
          new Port(x0,             y0 + 2*spacing, Port.INPUT,  5), // Rs1D
          new Port(x0,             y0 + 4*spacing, Port.INPUT,  5), // Rs2D
          new Port(x1,             y0 + 2*spacing, Port.INPUT,  5), // RdE
          new Port(x0 + 5*spacing,             y0, Port.OUTPUT, 1)  // stall
       });

       portNameToId = new HashMap<String, Integer>() {{
          put("MemToReg", RESULTSRCE0);
          put("Rs1D", RS1D);
          put("Rs2D", RS2D);
          put("RdE", RDE);
          put("stall", STALL);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(RESULTSRCE0, labels[RESULTSRCE0], Direction.WEST);
        painter.drawPort(RS1D, labels[RS1D], Direction.EAST);
        painter.drawPort(RS2D, labels[RS2D], Direction.EAST);
        painter.drawPort(RDE, labels[RDE], Direction.WEST);
        painter.drawPort(STALL, labels[STALL], Direction.NORTH);
    }
}
