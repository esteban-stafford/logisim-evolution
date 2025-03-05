package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Port;

public class ArmALUDecoder extends RectangularProgrammableComponent {
   public ArmALUDecoder() {
      super("ArmALUDecoder");

      int spacing = 10;
      int width = 14 * spacing;
      int height = 8 * spacing;
      Bounds bounds = Bounds.create(-width / 2, -height / 2, width, height);
      setOffsetBounds(bounds);

      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();

      portProperties = new PortProperties[] {
         new PortProperties(0, "AluOp", x0 + 7 * spacing, y0, Port.INPUT, Direction.NORTH, 1),
         new PortProperties(1, "Funct", x0, y0 + 4 * spacing, Port.INPUT, Direction.EAST, 5),
         new PortProperties(2, "FlagW", x1, y0 + 2 * spacing, Port.OUTPUT, Direction.WEST, 2),
         new PortProperties(3, "AluControl", x1, y0 + 6 * spacing, Port.OUTPUT, Direction.WEST, 2)
      };

      initializePorts();
      initializeBehavior();
   }
}

