package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Port;

public class ArmMainDecoder extends RectangularProgrammableComponent {
   public ArmMainDecoder() {
      super("ArmMainDecoder");

      int spacing = 10;
      int width = 14 * spacing;
      int height = 12 * spacing;
      Bounds bounds = Bounds.create(-width / 2, -height / 2, width, height);
      setOffsetBounds(bounds);

      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();
      portProperties = new PortProperties[] {
         new PortProperties(0,  "Op",       x0,             y0 + 4 * spacing, Port.INPUT,  Direction.EAST, 2),
         new PortProperties(1,  "Funct",    x0,             y0 + 8 * spacing, Port.INPUT,  Direction.EAST, 6),
         new PortProperties(2,  "Branch",   x0  + 7 * spacing, y0,            Port.OUTPUT, Direction.NORTH, 1),
         new PortProperties(3,  "RegWrite", x1,             y0 + 2 * spacing, Port.OUTPUT, Direction.WEST, 1),
         new PortProperties(4,  "MemWrite", x1,             y0 + 3 * spacing, Port.OUTPUT, Direction.WEST, 1),
         new PortProperties(5,  "MemToReg", x1,             y0 + 4 * spacing, Port.OUTPUT, Direction.WEST, 1),
         new PortProperties(6,  "ALUSrc",   x1,             y0 + 5 * spacing, Port.OUTPUT, Direction.WEST, 1),
         new PortProperties(7,  "ImmSrc",   x1,             y0 + 6 * spacing, Port.OUTPUT, Direction.WEST, 2),
         new PortProperties(8,  "RegSrc",   x1,             y0 + 7 * spacing, Port.OUTPUT, Direction.WEST, 2),
         new PortProperties(9,  "O1",       x1,             y0 + 10* spacing, Port.OUTPUT, Direction.WEST, 1),
         new PortProperties(10, "ALUOp",    x0 + 7 * spacing, y1,             Port.OUTPUT, Direction.SOUTH, 1)
      };

      initializePorts();
      initializeBehavior();
   }
}

