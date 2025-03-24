package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.Port;

public class ArmHazardUnit extends RectangularProgrammableComponent {
    public ArmHazardUnit() {
        super("ArmHazardUnit");

        int spacing = 10;
        int width = 10 * spacing;
        int height = 6 * spacing;
        Bounds bounds = Bounds.create(-width / 2, -height / 2, width, height);
        setOffsetBounds(bounds);

        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();

        portProperties = new PortProperties[] {
            new PortProperties(0, "MemToReg", x1, y0 + 4 * spacing, Port.INPUT, Direction.WEST, 1),
            new PortProperties(1, "RsD", x0, y0 + 2 * spacing, Port.INPUT, Direction.EAST, 4),
            new PortProperties(2, "RtD", x0, y0 + 4 * spacing, Port.INPUT, Direction.EAST, 4),
            new PortProperties(3, "RtE", x1, y0 + 2 * spacing, Port.INPUT, Direction.WEST, 4),
            new PortProperties(4, "stall", x0 + 5 * spacing, y0, Port.OUTPUT, Direction.NORTH, 1)
        };

        initializePorts();
        initializeBehavior();
    }
}
