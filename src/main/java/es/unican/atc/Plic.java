package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class Plic extends ProgrammableComponent
{
    private static String behaviorClassImplementationBody=
    "long Int0 = state.getPortValue(nameToId.get(\"Int0\")).toLongValue();\n" +
    "long Int1 = state.getPortValue(nameToId.get(\"Int1\")).toLongValue();\n" +
    "long Int2 = state.getPortValue(nameToId.get(\"Int2\")).toLongValue();\n" +
    "long Int3 = state.getPortValue(nameToId.get(\"Int3\")).toLongValue();\n" +
    "long RoutAddr = state.getPortValue(nameToId.get(\"RoutAddr\")).toLongValue();\n" +
    "long ReadMip = state.getPortValue(nameToId.get(\"ReadMip\")).toLongValue();\n" +
    "long ReadMie = state.getPortValue(nameToId.get(\"ReadMie\")).toLongValue();\n" +
    "long Mret = state.getPortValue(nameToId.get(\"Mret\")).toLongValue();\n" +
    "long Cause=0, HandleI=0, WriteMip=ReadMip, Int0_s=0, Int1_s=0, Int2_s=0, Int3_s=0, WriteMie=0;\n" +
    "\n//TU CODIGO AQUI\n" +
    "//Sugerencia: puedes dar valor 1 a un bit particular de una variable mediante el operador |= y valor 0 con &=. Ejemplos (contando los bits desde 0):\n" +
    "//x |= 0x4; // bit 2 a 1\n" +
    "//x &= ~0x4; // bit 2 a 0\n" +
    "\n" +
    "Value CauseVal = Value.createKnown(BitWidth.create(32), Cause);\n" +
    "state.setPort(nameToId.get(\"Cause\"), CauseVal, 1);\n" +
    "Value HandleIVal = Value.createKnown(BitWidth.create(1), HandleI);\n" +
    "state.setPort(nameToId.get(\"HandleI\"), HandleIVal, 1);\n" +
    "Value WriteMipVal = Value.createKnown(BitWidth.create(32), WriteMip);\n" +
    "state.setPort(nameToId.get(\"WriteMip\"), WriteMipVal, 1);\n"+
    "Value Int0_sVal = Value.createKnown(BitWidth.create(1), Int0_s);\n" +
    "state.setPort(nameToId.get(\"Int0_s\"),Int0_sVal, 1);\n"+
    "Value Int1_sVal = Value.createKnown(BitWidth.create(1), Int1_s);\n" +
    "state.setPort(nameToId.get(\"Int1_s\"), Int1_sVal, 1);\n"+
    "Value Int2_sVal = Value.createKnown(BitWidth.create(1), Int2_s);\n" +
    "state.setPort(nameToId.get(\"Int2_s\"), Int2_sVal, 1);\n"+
    "Value Int3_sVal = Value.createKnown(BitWidth.create(1), Int3_s);\n" +
    "state.setPort(nameToId.get(\"Int3_s\"), Int3_sVal, 1);\n" +
    "Value WriteMieVal = Value.createKnown(BitWidth.create(32), WriteMie);\n" +
    "state.setPort(nameToId.get(\"WriteMie\"), WriteMieVal, 1);\n";

    public static final int INT0 = 0;
    public static final int INT0S = 1;
    public static final int INT1 = 2;
    public static final int INT1S = 3;
    public static final int INT2 = 4;
    public static final int INT2S = 5;
    public static final int INT3 = 6;
    public static final int INT3S = 7;
    public static final int CAUSE = 8;
    public static final int READMIE = 9;
    public static final int WRITEMIE = 10;
    public static final int READMIP = 11;
    public static final int WRITEMIP = 12;
    public static final int ROUTINEADDR = 13;
    public static final int HANDLEI = 14;
    public static final int MRET = 15;
    private static String[] labels = new String[] { "Int0", "Int0_s", "Int1", "Int1_s", "Int2", "Int2_s", "Int3", "Int3_s", "Cause", "ReadMie", "WriteMie",
       "ReadMip", "WriteMip", "RoutAddr", "HandleI", "Mret"};

    protected Plic()
    {
       super("Platform Level Interrupt Controller", behaviorClassImplementationBody);
       int spacing = 10;
       int width = 12 * spacing;
       int height = 18 * spacing;
       Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
       setOffsetBounds(bounds);
       int x0 = bounds.getX();
       int x1 = x0 + bounds.getWidth();
       int y0 = bounds.getY();
       int y1 = y0 + bounds.getHeight();
       setPorts(new Port[] {
          new Port(x0,             y0 + 2*spacing, Port.INPUT,  1), // Int0
          new Port(x0,             y0 + 4*spacing, Port.OUTPUT,  1), // Int0_s
          new Port(x0,             y0 + 6*spacing, Port.INPUT,  1), // int1
          new Port(x0,             y0 + 8*spacing, Port.OUTPUT,  1), // Int1_s
          new Port(x0,             y0 + 10*spacing, Port.INPUT,  1), // Int2
          new Port(x0,             y0 + 12*spacing, Port.OUTPUT,  1), // Int2_s
          new Port(x0,             y0 + 14*spacing, Port.INPUT,  1), // Int3
          new Port(x0,             y0 + 16*spacing, Port.OUTPUT,  1), // Int3_s

          new Port(x1,             y0 + 4*spacing, Port.OUTPUT, 32), // Cause
          new Port(x1,             y0 + 6*spacing,   Port.INPUT, 32), // ReadMie
          new Port(x1 ,            y0 + 8*spacing, Port.OUTPUT, 32),  // WriteMie
          new Port(x1 ,            y0 + 10*spacing, Port.OUTPUT, 32),  // ReadMip
          new Port(x1 ,            y0 + 12*spacing, Port.OUTPUT, 32),  // WriteMip
          new Port(x1 ,            y0 + 14*spacing, Port.INPUT, 32),  // RoutAddr
          
          new Port(x0 + 6*spacing, y1, Port.OUTPUT, 1),  // HandleI
          new Port(x0 + 6*spacing, y0, Port.INPUT, 1),  // Mret
       });

       portNameToId=new HashMap<String, Integer>() {{
          put("Int0", INT0);
          put("Int0_s", INT0S);
          put("Int1", INT1);
          put("Int1_s", INT1S);
          put("Int2", INT2);
          put("Int2_s", INT2S);
          put("Int3", INT3);
          put("Int3_s", INT3S);
          put("Cause", CAUSE);
          put("ReadMie", READMIE);
          put("WriteMie", WRITEMIE);
          put("RoutAddr", ROUTINEADDR);
          put("ReadMip", READMIP);
          put("WriteMip", WRITEMIP);
          put("HandleI", HANDLEI);
          put("Mret", MRET);
       }};
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(INT0, labels[INT0], Direction.WEST);
        painter.drawPort(INT0S, labels[INT0S], Direction.WEST);
        painter.drawPort(INT1, labels[INT1], Direction.WEST);
        painter.drawPort(INT1S, labels[INT1S], Direction.WEST);
        painter.drawPort(INT2, labels[INT2], Direction.WEST);
        painter.drawPort(INT2S, labels[INT2S], Direction.WEST);
        painter.drawPort(INT3, labels[INT3], Direction.WEST);
        painter.drawPort(INT3S, labels[INT3S], Direction.WEST);
        painter.drawPort(CAUSE, labels[CAUSE], Direction.EAST);
        painter.drawPort(READMIE, labels[READMIE], Direction.EAST);
        painter.drawPort(WRITEMIE, labels[WRITEMIE], Direction.EAST);
        painter.drawPort(ROUTINEADDR, labels[ROUTINEADDR], Direction.EAST);
        painter.drawPort(READMIP, labels[READMIP], Direction.SOUTH);
        painter.drawPort(WRITEMIP, labels[WRITEMIP], Direction.SOUTH);
        painter.drawPort(HANDLEI, labels[HANDLEI], Direction.SOUTH);
        painter.drawPort(MRET, labels[MRET], Direction.NORTH);
    }
}
