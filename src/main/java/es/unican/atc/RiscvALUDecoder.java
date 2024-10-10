package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class RiscvALUDecoder extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "       long ALUOp = state.getPortValue(nameToId.get(\"ALUOp\")).toLongValue();\n" +
    "       long Op = state.getPortValue(nameToId.get(\"Op\")).toLongValue();\n" +
    "       long Funct3 = state.getPortValue(nameToId.get(\"Funct3\")).toLongValue();\n" +
    "       long Funct7 = state.getPortValue(nameToId.get(\"Funct7\")).toLongValue();\n" +
    "       \n" +
    "       Value ALUControl = Value.createKnown(BitWidth.create(3), 0);\n"+
    "       \n" +
    "       state.setPort(nameToId.get(\"ALUControl\"), ALUControl, 3);\n";

    public static final int ALUOP = 0;
    public static final int OP = 1;
    public static final int FUNCT3 = 2;
    public static final int FUNCT7 = 3;
    public static final int ALUCONTROL = 4;
    private static String[] labels = new String[] { "ALUOp", "Op", "Funct3", "Funct7", "ALUControl" };

    protected RiscvALUDecoder()
    {
       super("RiscvALUDecoder", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 14 * spacing;
       int height = 8 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x0 + 7*spacing, y0,             Port.INPUT,  2), // ALUOP
          new Port(x0,             y0 + 2*spacing, Port.INPUT,  1), // OP
          new Port(x0,             y0 + 4*spacing, Port.INPUT,  3), // FUNCT3
          new Port(x0,             y0 + 6*spacing, Port.INPUT,  1), // FUNCT7
          new Port(x1,             y0 + 4*spacing, Port.OUTPUT, 3)  // ALUCONTROL
       });

       portNameToId=new HashMap<String, Integer>() {{
          /*for (int i = 0; i < labels.length; i++) {
             put(labels[i], i);
          }*/
          put("ALUOp", ALUOP);
          put("Op", OP);
          put("Funct3", FUNCT3);
          put("Funct7", FUNCT7);
          put("ALUControl", ALUCONTROL);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(ALUOP, labels[ALUOP], Direction.NORTH);
        painter.drawPort(OP, labels[OP], Direction.EAST);
        painter.drawPort(FUNCT3, labels[FUNCT3], Direction.EAST);
        painter.drawPort(FUNCT7, labels[FUNCT7], Direction.EAST);
        painter.drawPort(ALUCONTROL, labels[ALUCONTROL], Direction.WEST);
    }
}
