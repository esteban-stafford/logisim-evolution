package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class RiscvMainDecoderExtended extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "long Op = state.getPortValue(nameToId.get(\"Op\")).toLongValue();\n" +
    "long Funct3 = state.getPortValue(nameToId.get(\"Funct3\")).toLongValue();\n" +
    "long RegWrite = 0, ImmSrc = 0, ALUSrc = 0, MemWrite = 0, ResultSrc = 0, Branch = 0, ALUOp = 0, O1 = 0, Op0=0, Jump=0, PcOrReg=0;\n" +
    "\n" +
    "if(Op == 0x3) { // lw\n" +
    "    RegWrite = 1;\n" +
    "    ALUSrc = 1;\n" +
    "    ResultSrc = 1;\n" +
    "} else if(Op == 0x23) { // sw\n" +
    "    ImmSrc = 1;\n" +
    "    ALUSrc = 1;\n" +
    "    MemWrite = 1;\n" +
    "} else if(Op == 0x33) { // R-type\n" +
    "    RegWrite = 1;\n" +
    "    ALUOp = 2;\n" +
    "} else if(Op == 0x63) { // beq\n" +
    "    ImmSrc = 2;\n" +
    "    Branch = 1;\n" +
    "    ALUOp = 1;\n" +
    "} else if(Op == 0x13) { // addi\n" +
    "    RegWrite = 1;\n" +
    "    ALUSrc = 1;\n" +
    "    ResultSrc = 0;\n" +
    "    ALUOp = 2;\n" +
    "} else if(Op == 0x37) { //lui\n"+
    "     Op0=1;\n"+
    "     RegWrite = 1;\n"+
    "     ALUOp = 2;\n"+
    "     ALUSrc = 1;\n"+
    "     ImmSrc=3;\n"+
    "} else if(Op == 0x6F) { //jal\n"+
    "     RegWrite=1;\n"+
    "     ImmSrc=4;\n"+
    "     ResultSrc=2;\n"+
    "     Jump=1;\n"+
    "} else if(Op == 0x67) { //jalr\n"+
    "     RegWrite=1;\n"+
    "     ResultSrc=2;\n"+
    "     Jump=1;\n"+
    "     PcOrReg=1;\n"+
    "}\n"+
    "\n" +
    "Value branchVal = Value.createKnown(BitWidth.create(1), Branch);\n" +
    "state.setPort(nameToId.get(\"Branch\"), branchVal, 1);\n" +
    "Value resultSrcVal = Value.createKnown(BitWidth.create(2), ResultSrc);\n" +
    "state.setPort(nameToId.get(\"ResultSrc\"), resultSrcVal, 2);\n" +
    "Value memWriteVal = Value.createKnown(BitWidth.create(1), MemWrite);\n" +
    "state.setPort(nameToId.get(\"MemWrite\"), memWriteVal, 1);\n" +
    "Value aluSrcVal = Value.createKnown(BitWidth.create(1), ALUSrc);\n" +
    "state.setPort(nameToId.get(\"ALUSrc\"), aluSrcVal, 1);\n" +
    "Value regWriteVal = Value.createKnown(BitWidth.create(1), RegWrite);\n" +
    "state.setPort(nameToId.get(\"RegWrite\"), regWriteVal, 1);\n" +
    "Value immSrcVal = Value.createKnown(BitWidth.create(3), ImmSrc);\n" +
    "state.setPort(nameToId.get(\"ImmSrc\"), immSrcVal, 3);\n" +
    "Value o1Val = Value.createKnown(BitWidth.create(1), O1);\n" +
    "state.setPort(nameToId.get(\"O1\"), o1Val, 1);\n" +
    "Value op0Val = Value.createKnown(BitWidth.create(1), Op0);\n" +
    "state.setPort(nameToId.get(\"Op0\"), op0Val, 1);\n" +
    "Value jumpVal = Value.createKnown(BitWidth.create(1), Jump);\n" +
    "state.setPort(nameToId.get(\"Jump\"), jumpVal, 1);\n" +
    "Value pcOrRegVal = Value.createKnown(BitWidth.create(1), PcOrReg);\n" +
    "state.setPort(nameToId.get(\"PcOrReg\"), pcOrRegVal, 1);\n" +
    "Value aluOpVal = Value.createKnown(BitWidth.create(2), ALUOp);\n" +
    "state.setPort(nameToId.get(\"ALUOp\"), aluOpVal, 2);\n";

    public static final int OP = 0;
    public static final int FUNCT3 = 1;
    public static final int BRANCH = 2;
    public static final int RESULTSRC = 3;
    public static final int MEMWRITE = 4;
    public static final int ALUSRC = 5;
    public static final int IMMSRC = 6;
    public static final int REGWRITE = 7;
    public static final int OP0 = 8;
    public static final int O1 = 9;
    public static final int ALUOP = 10;
    public static final int JUMP = 11;
    public static final int PCORREG = 12;
    private static String[] labels = new String[] { "Op", "Funct3", "Branch", "ResultSrc",
       "MemWrite", "ALUSrc", "ImmSrc", "RegWrite", "Op0", "O1", "ALUOp", "Jump", "PcOrReg" };

    protected RiscvMainDecoderExtended()
    {
       super("RiscvMainDecoderExt", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 14 * spacing;
       int height = 14 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x0,             y0 + 4*spacing, Port.INPUT,  7), // Op (7-bit wide)
          new Port(x0,             y0 + 8*spacing, Port.INPUT,  3), // Funct3 (3-bit wide)
          new Port(x1,             y0 + 3*spacing,   Port.OUTPUT, 1), // Branch
          new Port(x1,             y0 + 4*spacing, Port.OUTPUT, 2), // ResultSrc (2-bit wide)
          new Port(x1,             y0 + 5*spacing, Port.OUTPUT, 1), // MemWrite
          new Port(x1,             y0 + 6*spacing, Port.OUTPUT, 1), // ALUSrc
          new Port(x1,             y0 + 7*spacing, Port.OUTPUT, 3), // ImmSrc (3-bit wide)
          new Port(x1,             y0 + 8*spacing, Port.OUTPUT, 1), // RegWrite
          new Port(x1,             y0 + 9*spacing, Port.OUTPUT, 1),  // Op0
          new Port(x1,             y0 + 11*spacing, Port.OUTPUT, 1), // O1
          new Port(x0 + 7*spacing, y1,             Port.OUTPUT, 2),  // ALUOp (2-bit wide)
          new Port(x0 + 4*spacing, y0,             Port.OUTPUT, 1),  // Jump
          new Port(x0 + 10*spacing, y0,             Port.OUTPUT, 1)  // PcOrReg
       });

       portNameToId=new HashMap<String, Integer>() {{
          put("Op", OP);
          put("Funct3", FUNCT3);
          put("Branch", BRANCH);
          put("ResultSrc", RESULTSRC);
          put("MemWrite", MEMWRITE);
          put("ALUSrc", ALUSRC);
          put("ImmSrc", IMMSRC);
          put("RegWrite", REGWRITE);
          put("O1", O1);
          put("ALUOp", ALUOP);
          put("Op0", OP0);
          put("Jump", JUMP);
          put("PcOrReg", PCORREG);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(OP, labels[OP], Direction.EAST);
        painter.drawPort(FUNCT3, labels[FUNCT3], Direction.EAST);
        painter.drawPort(BRANCH, labels[BRANCH], Direction.WEST);
        painter.drawPort(RESULTSRC, labels[RESULTSRC], Direction.WEST);
        painter.drawPort(MEMWRITE, labels[MEMWRITE], Direction.WEST);
        painter.drawPort(ALUSRC, labels[ALUSRC], Direction.WEST);
        painter.drawPort(IMMSRC, labels[IMMSRC], Direction.WEST);
        painter.drawPort(REGWRITE, labels[REGWRITE], Direction.WEST);
        painter.drawPort(OP0, labels[OP0], Direction.WEST);
        painter.drawPort(O1, labels[O1], Direction.WEST);
        painter.drawPort(ALUOP, labels[ALUOP], Direction.SOUTH);
        painter.drawPort(JUMP, labels[JUMP], Direction.NORTH);
        painter.drawPort(PCORREG, labels[PCORREG], Direction.NORTH);
    }
}
