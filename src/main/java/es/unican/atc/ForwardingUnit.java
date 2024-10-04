package es.unican.atc;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import java.util.HashMap;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.InstancePainter;
/*import com.cburch.hex.HexModel;
import com.cburch.hex.HexModelListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.Mem;
import com.cburch.logisim.std.memory.MemContents;
import com.cburch.logisim.std.memory.RamState;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.EventSourceWeakSupport;
import com.cburch.logisim.data.Attribute;

import static com.cburch.logisim.std.Strings.S;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
*/

public class ForwardingUnit extends ProgrammableComponent
{

    private static String behaviorClassImplementationBody=
    "       long RA1E = state.getPortValue(nameToId.get(\"RA1E\")).toLongValue();\n" +
    "       long RA2E = state.getPortValue(nameToId.get(\"RA2E\")).toLongValue();\n" +
    "       long WA3M = state.getPortValue(nameToId.get(\"WA3M\")).toLongValue();\n" +
    "       long RegWriteM = state.getPortValue(nameToId.get(\"RegWriteM\")).toLongValue();\n" +
    "       long WA3W = state.getPortValue(nameToId.get(\"WA3W\")).toLongValue();\n" +
    "       long RegWriteW = state.getPortValue(nameToId.get(\"RegWriteW\")).toLongValue();\n" +
    "       \n" +
    "       \n" +
    "       Value ForwardBE = Value.createKnown(BitWidth.create(2), 0);\n"+
    "       Value ForwardAE= Value.createKnown(BitWidth.create(2), 0);\n"+
    "       \n" +
    "       \n" +
    "       state.setPort(nameToId.get(\"ForwardBE\"), ForwardBE, 2);\n"+
    "       state.setPort(nameToId.get(\"ForwardAE\"), ForwardAE, 2);\n";


    protected ForwardingUnit()
    {
        super("ForwardingUnit", behaviorClassImplementationBody);
        int xp[], yp[];
        int width = 200;
       	int height = 100;
        Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
        setOffsetBounds(bounds);
        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();
        xp = new int[] { x0, x1, x1, x0, x0, x0 + width/3,  x0 };
        yp = new int[] { y0, y0 + height*3/10, y1 - height*3/10, y1, y1 - height*2/5, y1 - height/2, y1 - height*3/5 };
        setPorts(new Port[] {
                 new Port(-width/2, -height/2 +20, Port.INPUT, 1),
                 new Port(-width/2, -height/2 +40, Port.INPUT, 1),
                 new Port(width/2, -height/2 +20, Port.INPUT, 1),
                 new Port(width/2, -height/2 +40, Port.INPUT, 1),
                 new Port(width/2, -height/2 +60, Port.INPUT, 1),
                 new Port(width/2, -height/2 +80, Port.INPUT, 1),
                 new Port(-width/2+30, -height/2, Port.OUTPUT, 1),
                 new Port(-width/2+60, -height/2, Port.OUTPUT, 1),
                 }
                );

       portNameToId=new HashMap<String, Integer>() {{
                                                      put("RA1E", 0);
                                                      put("RA2E", 1);
                                                      put("WA3M", 2);
                                                      put("RegWriteM", 3);
                                                      put("WA3W", 4);
                                                      put("RegWriteW", 5);
                                                      put("ForwardBE", 6);
                                                      put("ForwardAE", 7);
                                                  }};
    }
    

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(0, "RA1E", Direction.EAST);
        painter.drawPort(1, "RA2E", Direction.EAST);
        painter.drawPort(2, "WA3M", Direction.WEST);
        painter.drawPort(3, "RegWriteM", Direction.WEST);
        painter.drawPort(4, "WA3W", Direction.WEST);
        painter.drawPort(5, "RegWriteW", Direction.WEST);
        painter.drawPort(6, "ForwardBE", Direction.WEST);
        painter.drawPort(7, "ForwardAE", Direction.WEST);
    }
}
