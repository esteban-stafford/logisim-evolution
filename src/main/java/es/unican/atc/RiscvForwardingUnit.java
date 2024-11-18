package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.data.BitWidth;

public class RiscvForwardingUnit extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "       long Rs1E = state.getPortValue(nameToId.get(\"Rs1E\")).toLongValue();\n" +
    "       long Rs2E = state.getPortValue(nameToId.get(\"Rs2E\")).toLongValue();\n" +
    "       long RdM = state.getPortValue(nameToId.get(\"RdM\")).toLongValue();\n" +
    "       long RdW = state.getPortValue(nameToId.get(\"RdW\")).toLongValue();\n" +
    "       long RegWriteM = state.getPortValue(nameToId.get(\"RegWriteM\")).toLongValue();\n" +
    "       long RegWriteW = state.getPortValue(nameToId.get(\"RegWriteW\")).toLongValue();\n" +
    "       \n" +
    "       long FwA = 0;\n" +
/*    "       if ((Rs1E == RdM) && (RegWriteM == 1) && (Rs1E != 0)) {\n" +
    "           FwA = 2;\n" +
    "       } else if ((Rs1E == RdW) && (RegWriteW == 1) && (Rs1E != 0)) {\n" +
    "           FwA = 1;\n" +
    "       } else {\n" +
    "           FwA = 0;\n" +
    "       }\n" +
    "       \n" + */
    "       long FwB = 0;\n" +
/*    "       if ((Rs2E == RdM) && (RegWriteM == 1) && (Rs2E != 0)) {\n" +
    "           FwB = 2;\n" +
    "       } else if ((Rs2E == RdW) && (RegWriteW == 1) && (Rs2E != 0)) {\n" +
    "           FwB = 1;\n" +
    "       } else {\n" +
    "           FwB = 0;\n" +
    "       }\n" + */
    "       \n" +
    "       state.setPort(nameToId.get(\"FwA\"), Value.createKnown(BitWidth.create(2), FwA), 2);\n" +
    "       state.setPort(nameToId.get(\"FwB\"), Value.createKnown(BitWidth.create(2), FwB), 2);\n";

    public static final int RS1E = 0;
    public static final int RS2E = 1;
    public static final int RDM = 2;
    public static final int RDW = 3;
    public static final int REGWRITEM = 4;
    public static final int REGWRITEW = 5;
    public static final int FORWARDAE = 6;
    public static final int FORWARDBE = 7;
    private static String[] labels = new String[] { "Rs1E", "Rs2E", "RdM", "RdW", "RegWriteM", "RegWriteW", "FwA", "FwB" };

    protected RiscvForwardingUnit()
    {
       super("RiscvForwardingUnit", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 12 * spacing;
       int height = 8 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x0,             y0 + 3*spacing, Port.INPUT,  5), // Rs1E
          new Port(x0,             y0 + 5*spacing, Port.INPUT,  5), // Rs2E
          new Port(x1,             y0 + 1*spacing, Port.INPUT,  5), // RdM
          new Port(x1,             y0 + 3*spacing, Port.INPUT,  5), // RdW
          new Port(x1,             y0 + 5*spacing, Port.INPUT,  1), // RegWriteM
          new Port(x1,             y0 + 7*spacing, Port.INPUT,  1), // RegWriteW
          new Port(x0 + 3*spacing,             y0, Port.OUTPUT, 2), // FwA
          new Port(x0 + 6*spacing,             y0, Port.OUTPUT, 2)  // FwB
       });

       portNameToId=new HashMap<String, Integer>() {{
          put("Rs1E", RS1E);
          put("Rs2E", RS2E);
          put("RdM", RDM);
          put("RdW", RDW);
          put("RegWriteM", REGWRITEM);
          put("RegWriteW", REGWRITEW);
          put("FwA", FORWARDAE);
          put("FwB", FORWARDBE);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(RS1E, labels[RS1E], Direction.EAST);
        painter.drawPort(RS2E, labels[RS2E], Direction.EAST);
        painter.drawPort(RDM, labels[RDM], Direction.WEST);
        painter.drawPort(RDW, labels[RDW], Direction.WEST);
        painter.drawPort(REGWRITEM, labels[REGWRITEM], Direction.WEST);
        painter.drawPort(REGWRITEW, labels[REGWRITEW], Direction.WEST);
        painter.drawPort(FORWARDAE, labels[FORWARDAE], Direction.NORTH);
        painter.drawPort(FORWARDBE, labels[FORWARDBE], Direction.NORTH);
    }
}
