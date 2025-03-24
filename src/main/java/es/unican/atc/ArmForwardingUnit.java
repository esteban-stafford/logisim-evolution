package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Port;

public class ArmForwardingUnit extends RectangularProgrammableComponent {
   public ArmForwardingUnit() {
      super("ArmForwardingUnit");

      int spacing = 10;
      int width = 12 * spacing;
      int height = 8 * spacing;
      Bounds bounds = Bounds.create(-width / 2, -height / 2, width, height);
      setOffsetBounds(bounds);

      int x0 = bounds.getX();
      int x1 = x0 + bounds.getWidth();
      int y0 = bounds.getY();
      int y1 = y0 + bounds.getHeight();

      portProperties = new PortProperties[] {
         new PortProperties(0, "RsE", x0, y0 + 3 * spacing, Port.INPUT, Direction.EAST, 4),
         new PortProperties(1, "RtE", x0, y0 + 5 * spacing, Port.INPUT, Direction.EAST, 4),
         new PortProperties(2, "WaM", x1, y0 + 1 * spacing, Port.INPUT, Direction.WEST, 4),
         new PortProperties(3, "WaW", x1, y0 + 3 * spacing, Port.INPUT, Direction.WEST, 4),
         new PortProperties(4, "RegWriteM", x1, y0 + 5 * spacing, Port.INPUT, Direction.WEST, 1),
         new PortProperties(5, "RegWriteW", x1, y0 + 7 * spacing, Port.INPUT, Direction.WEST, 1),
         new PortProperties(6, "FwA", x0 + 3 * spacing, y0, Port.OUTPUT, Direction.NORTH, 2),
         new PortProperties(7, "FwB", x0 + 6 * spacing, y0, Port.OUTPUT, Direction.NORTH, 2)
      };

      initializePorts();
      initializeBehavior();
   }
}
