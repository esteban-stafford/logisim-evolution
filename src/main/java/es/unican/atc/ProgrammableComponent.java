package es.unican.atc;

import com.cburch.hex.HexModel;
import com.cburch.hex.HexModelListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.MemContents;
import com.cburch.logisim.std.memory.RamState;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.EventSourceWeakSupport;

import static com.cburch.logisim.std.Strings.S;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ProgrammableComponent extends InstanceFactory
{
    private JavaCompiler compiler; // Java compiler
    private Behavior behavior;     // Behavior of the component
    private HashMap<String, Integer> portNameToId; // Map from port names to port ids
    private EventSourceWeakSupport<HexModelListener> listeners = null;
    private static long behaviorCounter = 0;
    private static final WeakHashMap<ProgrammableComponent, BehaviorFrame> windowRegistry = new WeakHashMap<>();

    private static String behaviorClassImplementationHeaderTemplate = "package es.unican.atc;\n\n" +
        "import java.util.HashMap;\n" +
        "import com.cburch.logisim.instance.InstanceState;\n"+
        "import com.cburch.logisim.data.BitWidth;\n"+
        "import com.cburch.logisim.data.Value;\n"+
        "\n"+
        "public class ActualBehaviorXX implements Behavior{\n"+
        "   private String behaviorBody;\n" + 
        "\n"+
        "   public String getAsString(){return behaviorBody;}\n"+
        "   public ActualBehaviorXX(String behaviorBody){\n"+
        "    this.behaviorBody=behaviorBody;\n"+
        "   }\n"+
        "   public void propagate(InstanceState state, HashMap<String, Integer> nameToId){\n";
        
    
    private static String behaviorClassImplementationBody="     System.out.println(\"HEYY\");\n" +
    "       long v = state.getPortValue(0).toLongValue();\n" +
    "       Value out = Value.createKnown(BitWidth.create(32), v);\n"+
    "       state.setPort(2, out, 32);\n";

    private static String behaviorClassImplementationTail= "    }\n"+
        "}";

    protected ProgrammableComponent()
    {
        super("ProgrammableComponent");
        int xp[], yp[];
	    int width = 60;
       	int height = 100;
        Bounds bounds = Bounds.create(-width/2, -height/2, width, height);
        setOffsetBounds(bounds);
        int x0 = bounds.getX();
        int x1 = x0 + bounds.getWidth();
        int y0 = bounds.getY();
        int y1 = y0 + bounds.getHeight();
        xp = new int[] { x0, x1,               x1,               x0, x0,              x0 + width/3,  x0 };
        yp = new int[] { y0, y0 + height*3/10, y1 - height*3/10, y1, y1 - height*2/5, y1 - height/2, y1 - height*3/5 };
        setPorts(new Port[] {
                 new Port(-width/2, -height/2 +33, Port.INPUT, 1),
                 new Port(-width/2, -height/2 +66, Port.INPUT, 1),
                 new Port(width/2, 0, Port.OUTPUT, 1),});

        newBehavior(behaviorClassImplementationBody);
    }
    

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
        painter.drawPort(0, "In1", Direction.EAST);
        painter.drawPort(1, "In2", Direction.EAST);
        painter.drawPort(2, "Out", Direction.WEST);
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub
        behavior.propagate(state, portNameToId);
    }

    public Behavior getBehavior(){
        return behavior;
    }

    public boolean newBehavior(String behaviorBody)
    {
        System.out.println("Construyendo\n");
        String behaviorClassImplementationHeader=behaviorClassImplementationHeaderTemplate.replace("XX", Long.toString(behaviorCounter));
        String newBehaviorClassName="ActualBehaviorXX".replace("XX", Long.toString(behaviorCounter));
        File sourceFile=null;
        try{
            String fileName = newBehaviorClassName+".java";
            sourceFile = new File("./src/main/java/es/unican/atc/"+fileName);
            Files.write(sourceFile.toPath(), (behaviorClassImplementationHeader+behaviorBody+behaviorClassImplementationTail).getBytes());
        } catch (Exception e) {
                System.out.println("Cagada en fichero\n");
                throw new RuntimeException("Error compiling class: " + e.getMessage());
        }

        /** Compilation Requirements *********************************************************************************************/
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        
        // This sets up the class path that the compiler will use.
        List<String> optionList = new ArrayList<String>();
        //optionList.addAll(Arrays.asList("-d","../../../../../../../build/classes/java/main/es/unican/atc/"));

        //optionList.add("-classpath");
        //optionList.add(System.getProperty("java.class.path") + File.pathSeparator + "dist/InlineCompiler.jar");

        Iterable<? extends JavaFileObject> compilationUnit
                = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
        JavaCompiler.CompilationTask task = compiler.getTask(
            null, 
            fileManager, 
            diagnostics, 
            optionList, 
            null, 
            compilationUnit);
        /********************************************************************************************* Compilation Requirements **/
        if (task.call()) {
            
            /** Load and execute *************************************************************************************************/
            System.out.println("Yipe");
            // Create a new custom class loader, pointing to the directory that contains the compiled
            // classes, this should point to the top of the package structure!
            //URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
            ClassLoader classLoader = getClass().getClassLoader();
            // Load the class from the classloader by name....

            try{
                System.out.println("./src/main/java/es/unican/atc/"+newBehaviorClassName+".class");
                System.out.println("./build/classes/java/main/es/unican/atc/"+newBehaviorClassName+".class");
                Files.move(Paths.get("./src/main/java/es/unican/atc/"+newBehaviorClassName+".class"), Paths.get("./build/classes/java/main/es/unican/atc/"+newBehaviorClassName+".class"), StandardCopyOption.REPLACE_EXISTING);
                System.out.println(newBehaviorClassName.split("\\.")[0]);
                //Class<?> loadedClass=classLoader.loadClass("es.unican.atc."+newBehaviorClassName.split("\\.")[0]);
                
                Class<?> loadedClass=classLoader.loadClass("es.unican.atc."+newBehaviorClassName);
                System.out.println("Cargado\n");
                 Class[] cArg = new Class[1];
                 cArg[0] = String.class;
                 Object obj = loadedClass.getDeclaredConstructor(cArg).newInstance(behaviorBody);
                // Santity check
                if (obj instanceof Behavior) {
                    System.out.println("Es comportamiento!!!!!!!!!!!!!!!!\n");
                    System.out.println(((Behavior)obj).getAsString());
                    // Cast to the DoStuff interface
                    behavior = (Behavior)obj;
                    System.out.println("Construido\n");
                }
            } catch (Exception e) {
                System.out.println("Cagada en el path de la clase\n");
                sourceFile.delete();
                throw new RuntimeException("Error compiling class: " + e.getMessage());
            }
            behaviorCounter++;
             
            /************************************************************************************************* Load and execute **/
        } else {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.format("Error on line %d of component behavior: %s%n",
                        diagnostic.getLineNumber()-behaviorClassImplementationHeaderTemplate.split("\r\n|\r|\n").length,
                        diagnostic.getMessage(null));
            }
            sourceFile.delete();
        }
        return true;
    }

     private static BehaviorFrame getBehaviorFrame(ProgrammableComponent p, Project proj, Instance instance) {
        synchronized (windowRegistry) {
            System.out.println("Sincronizado");
            BehaviorFrame ret = windowRegistry.get(p.getBehavior().getAsString());
            if (ret == null) {
                System.out.println("En if");
                ret = new BehaviorFrame(proj, instance, p);
                windowRegistry.put(p, ret);
            }
            return ret;
        }
    }

    public BehaviorFrame getBehaviorFrame(Project proj, Instance instance, CircuitState circState) {
        System.out.println("getHexFrame\n");
        return getBehaviorFrame(this, proj, instance);
    }

    public void addHexModelListener(HexModelListener l) {
        if (listeners == null) listeners = new EventSourceWeakSupport<>();
        listeners.add(l);
    }

    protected Object getInstanceFeature(Instance instance, Object key) {
    return (key == MenuExtender.class)
        ? new ProgrammableComponentMenu(this, instance)
        : super.getInstanceFeature(instance, key);
    }   
}
