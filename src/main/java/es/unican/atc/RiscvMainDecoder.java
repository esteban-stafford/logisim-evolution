package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class RiscvMainDecoder extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "long Op = state.getPortValue(nameToId.get(\"Op\")).toLongValue();\n" +
    "\n" +
    "long Branch = 0, ResultSrc = 0, MemWrite = 0, ALUSrc = 0, ImmSrc = 0, RegWrite = 0, ALUOp = 0, O1 = 0;\n" +
    "\n" +
    "\n" +
    "Value p_Branch = Value.createKnown(BitWidth.create(1), 0);\n"+
    "state.setPort(nameToId.get(\"Branch\"), p_Branch, 1);\n" +
    "Value p_ResultSrc = Value.createKnown(BitWidth.create(1), 0);\n" +
    "state.setPort(nameToId.get(\"ResultSrc\"), p_ResultSrc, 1);\n" +
    "Value p_MemWrite = Value.createKnown(BitWidth.create(1), 0);\n" +
    "state.setPort(nameToId.get(\"MemWrite\"), p_MemWrite, 1);\n" +
    "Value p_ALUSrc = Value.createKnown(BitWidth.create(1), 0);\n" +
    "state.setPort(nameToId.get(\"ALUSrc\"), p_ALUSrc, 1);\n" +
    "Value p_ImmSrc = Value.createKnown(BitWidth.create(2), 0);\n" +
    "state.setPort(nameToId.get(\"ImmSrc\"), p_ImmSrc, 2);\n" +
    "Value p_RegWrite = Value.createKnown(BitWidth.create(1), 0);\n" +
    "state.setPort(nameToId.get(\"RegWrite\"), p_RegWrite, 1);\n" +
    "Value p_ALUOp = Value.createKnown(BitWidth.create(2), 0);\n" +
    "state.setPort(nameToId.get(\"ALUOp\"), p_ALUOp, 2);\n" +
    "Value p_O1 = Value.createKnown(BitWidth.create(1), 0);\n" +
    "state.setPort(nameToId.get(\"O1\"), p_O1, 1); \n";

    public static final int OP = 0;
    public static final int BRANCH = 1;
    public static final int RESULTSRC = 2;
    public static final int MEMWRITE = 3;
    public static final int ALUSRC = 4;
    public static final int IMMSRC = 5;
    public static final int REGWRITE = 6;
    public static final int O1 = 7;
    public static final int ALUOP = 8;
    private static String[] labels = new String[] { "Op", "Branch", "ResultSrc", "MemWrite", "ALUSrc", "ImmSrc", "RegWrite", "O1", "ALUOp" };

    protected RiscvMainDecoder()
    {
       super("RiscvMainDecoder", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 14 * spacing;
       int height = 12 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x0,             y0 + 6*spacing, Port.INPUT,  7), // Op (7-bit wide)
          new Port(x1,             y0 + spacing,   Port.OUTPUT, 1), // Branch
          new Port(x1,             y0 + 2*spacing, Port.OUTPUT, 1), // ResultSrc
          new Port(x1,             y0 + 3*spacing, Port.OUTPUT, 1), // MemWrite
          new Port(x1,             y0 + 4*spacing, Port.OUTPUT, 1), // ALUSrc
          new Port(x1,             y0 + 5*spacing, Port.OUTPUT, 2), // ImmSrc (2-bit wide)
          new Port(x1,             y0 + 6*spacing, Port.OUTPUT, 1), // RegWrite
          new Port(x1,             y0 + 8*spacing, Port.OUTPUT, 1), // O1
          new Port(x0 + 7*spacing, y1,             Port.OUTPUT, 2)  // ALUOp (2-bit wide)
       });

       portNameToId=new HashMap<String, Integer>() {{
          put("Op", OP);
          put("Branch", BRANCH);
          put("ResultSrc", RESULTSRC);
          put("MemWrite", MEMWRITE);
          put("ALUSrc", ALUSRC);
          put("ImmSrc", IMMSRC);
          put("RegWrite", REGWRITE);
          put("ALUOp", ALUOP);
          put("O1", O1);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(OP, labels[OP], Direction.EAST);
        painter.drawPort(BRANCH, labels[BRANCH], Direction.WEST);
        painter.drawPort(RESULTSRC, labels[RESULTSRC], Direction.WEST);
        painter.drawPort(MEMWRITE, labels[MEMWRITE], Direction.WEST);
        painter.drawPort(ALUSRC, labels[ALUSRC], Direction.WEST);
        painter.drawPort(IMMSRC, labels[IMMSRC], Direction.WEST);
        painter.drawPort(REGWRITE, labels[REGWRITE], Direction.WEST);
        painter.drawPort(O1, labels[O1], Direction.WEST);
        painter.drawPort(ALUOP, labels[ALUOP], Direction.NORTH);
    }
}
