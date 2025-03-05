package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;

public class RectangularProgrammableComponent extends ProgrammableComponent {

    protected static class PortProperties {
        private final int id;
        private final String label;
        private final int x;
        private final int y;
        private final String type;
        private final Direction facing;
        private final int width;

        public PortProperties(int id, String label, int x, int y, String type, Direction facing, int width) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
            this.type = type;
            this.facing = facing;
            this.width = width;
        }

        public int getId() { return id; }
        public String getLabel() { return label; }
        public String getVarName() { return label.toLowerCase(); }
        public int getX() { return x; }
        public int getY() { return y; }
        public String getType() { return type; }
        public Direction getFacing() { return facing; }
        public int getWidth() { return width; }
    }

    protected PortProperties[] portProperties;

    public RectangularProgrammableComponent(String name) {
        super(name, "");
    }

    protected void initializePorts() {
        Port[] ports = new Port[portProperties.length];
        portNameToId = new HashMap<>();

        for (PortProperties port : portProperties) {
            ports[port.getId()] = new Port(port.getX(), port.getY(), port.getType(), port.getWidth());
            portNameToId.put(port.getLabel(), port.getId());
        }
        setPorts(ports);
    }

    protected void initializeBehavior() {
        StringBuilder behaviorBuilder = new StringBuilder();

        for (PortProperties port : portProperties) {
            if (port.getType() == Port.INPUT) {
                behaviorBuilder.append("long ")
                        .append(port.getVarName())
                        .append(" = state.getPortValue(nameToId.get(\"")
                        .append(port.getLabel())
                        .append("\")).toLongValue();\n");
            }
        }

        behaviorBuilder.append("\n");

        for (PortProperties port : portProperties) {
            if (port.getType() == Port.OUTPUT) {
                behaviorBuilder.append("long ")
                        .append(port.getVarName())
                        .append(" = 0;\n");
            }
        }

        behaviorBuilder.append("\n");

        for (PortProperties port : portProperties) {
            if (port.getType() == Port.OUTPUT) {
                behaviorBuilder.append("state.setPort(nameToId.get(\"")
                        .append(port.getLabel())
                        .append("\"), Value.createKnown(BitWidth.create(")
                        .append(port.getWidth())
                        .append("), ")
                        .append(port.getVarName())
                        .append("), ")
                        .append(port.getWidth())
                        .append(");\n");
            }
        }

        behaviorClassImplementationBody = behaviorBuilder.toString();
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        for (PortProperties port : portProperties) {
            painter.drawPort(port.getId(), port.getLabel(), port.getFacing());
        }
    }
}
