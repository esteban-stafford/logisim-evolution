package es.unican.atc;

import com.cburch.hex.HexModel;
import com.cburch.hex.HexModelListener;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import java.net.URL; 


public class ProgrammableComponent extends InstanceFactory implements AttributeListener
{

    class BehaviorAttributes extends AbstractAttributeSet 
    {
        private BehaviorAttribute behaviorBodyAttr;
        String behavior;

        
        public BehaviorAttributes(BehaviorAttribute b){
            behaviorBodyAttr = b;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V> V getValue(Attribute<V> attr) {
            V ret=null;
            if (attr == behaviorBodyAttr)
            {
                ret=(V)behavior;
            }
            return ret;
        }

        @Override
        public List<Attribute<?>> getAttributes() {
            return List.of(behaviorBodyAttr);
        }
        
        @Override
        public <V> void setValue(Attribute<V> attr, V value) {
            if (attr == behaviorBodyAttr) {
                behavior=(String)value;
            }
        }
  
        @Override
        protected void copyInto(AbstractAttributeSet dest) {
            BehaviorAttributes d=(BehaviorAttributes) dest;
            d.behavior=behavior;
        }
    }

    static class BehaviorAttribute extends Attribute<String> {
        public BehaviorAttribute() {
        super("behavior", S.getter("behaviorAttr"));
        }

        //@Override
        /*public MemContents parse(String value) {
        final var lineBreak = value.indexOf('\n');
        final var first = lineBreak < 0 ? value : value.substring(0, lineBreak);
        final var rest = lineBreak < 0 ? "" : value.substring(lineBreak + 1);
        final var toks = new StringTokenizer(first);
        try {
            final var header = toks.nextToken();
            if (!header.equals("addr/data:")) return null;
            final var addr = Integer.parseInt(toks.nextToken());
            final var data = Integer.parseInt(toks.nextToken());
            return HexFile.parseFromCircFile(rest, addr, data);
        } catch (IOException | NoSuchElementException | NumberFormatException e) {
            return null;
        }
        }*/

        @Override
        public String toDisplayString(String b) {
            return "Custom behavior";
        }

        @Override
        public String toStandardString(String b) {
            //final var addr = state.getLogLength();
            //final var data = state.getWidth();
            //final var contents = HexFile.saveToString(state);
            return b;
        }

        @Override
        public String parse(String value) {
            // TODO Auto-generated method stub
            return value;
        }
    }

    Path behaviorTempFolder=null;

    public static final BehaviorAttribute behaviorAttr = new BehaviorAttribute();

    private BehaviorAttributes attributes;

    private JavaCompiler compiler; // Java compiler
    private Behavior behavior;     // Behavior of the component
    protected HashMap<String, Integer> portNameToId; // Map from port names to port ids
    private EventSourceWeakSupport<HexModelListener> listeners = null;
    private static long behaviorCounter = 0;
    private static final WeakHashMap<ProgrammableComponent, BehaviorFrame> windowRegistry = new WeakHashMap<>();

    private static String behaviorClassImplementationHeaderTemplate = "import es.unican.atc.Behavior\n;" +
        "import java.util.HashMap;\n" +
        "import com.cburch.logisim.instance.InstanceState;\n"+
        "import com.cburch.logisim.data.BitWidth;\n"+
        "import com.cburch.logisim.data.Value;\n"+
        "\n"+
        "public class ActualBehaviorXX implements Behavior{\n"+
        "   private String behaviorBody;\n" + 
        "\n"+
        "   public String getAsString(){return behaviorBody;}\n"+
        "@Override\n"+
        "   public String toString() {return getAsString();}\n;"+
        "   public ActualBehaviorXX(String behaviorBody){\n"+
        "    this.behaviorBody=behaviorBody;\n"+
        "   }\n"+
        "   public void propagate(InstanceState state, HashMap<String, Integer> nameToId){\n";
        
    
    private String behaviorClassImplementationBody="     System.out.println(\"HEY\");\n"; 

    private static String behaviorClassImplementationTail= "    }\n"+
        "}";

    Attribute<String> behaviorBodyAttr;
    
    public ProgrammableComponent(String name)
    {
        super(name);
    }

    protected ProgrammableComponent()
    {
      super("ProgrammableComponent");
    }
    
    public ProgrammableComponent(String name, String b)
    {
        super(name);
        behaviorClassImplementationBody=b;
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawRectangle(painter.getBounds(), "");
    }

    @Override
    public void propagate(InstanceState state) {
        // TODO Auto-generated method stub
        behavior.propagate(state, portNameToId);
    }

    public Behavior getBehavior(){
        return behavior;
    }

    public boolean newBehavior(String behaviorBody, Instance instance)
    {
        try
        {
            if(behaviorTempFolder==null)
            {
                behaviorTempFolder=Files.createTempDirectory(null);
                behaviorTempFolder.toFile().deleteOnExit();
            }
        }catch (IOException e) {
                throw new RuntimeException("Error creating temp folder " + e.getMessage());
        }   


        String behaviorClassImplementationHeader=behaviorClassImplementationHeaderTemplate.replace("XX", Long.toString(behaviorCounter));
        String newBehaviorClassName="ActualBehaviorXX".replace("XX", Long.toString(behaviorCounter));
        File sourceFile=null;
        try{
            String fileName = newBehaviorClassName+".java";
            sourceFile = new File(behaviorTempFolder.toString(), fileName);
            Files.write(sourceFile.toPath(), (behaviorClassImplementationHeader+behaviorBody+behaviorClassImplementationTail).getBytes());
        } catch (Exception e) {
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
            try{
                URL url = behaviorTempFolder.toUri().toURL();
                URL[] urls = new URL[]{url};
                ClassLoader cl = new URLClassLoader(urls);
                Class<?> loadedClass=cl.loadClass(newBehaviorClassName);
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Object obj = loadedClass.getDeclaredConstructor(cArg).newInstance(behaviorBody);
                // Santity check
                if (obj instanceof Behavior) {
                    // Cast to the DoStuff interface
                    behavior = (Behavior)obj;
                }
            } catch (Exception e) {
                sourceFile.delete();
                throw new RuntimeException("Error compiling class: " + e.getMessage());
            }
            behaviorCounter++;
            

            if (attributes!=null) { //It may be null when the Behavior is first created
                instance.getAttributeSet().setValue(behaviorAttr, behaviorBody);    
            }
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
            BehaviorFrame ret = windowRegistry.get(p.getBehavior().getAsString());
            if (ret == null) {
                ret = new BehaviorFrame(proj, instance, p);
                windowRegistry.put(p, ret);
            }
            return ret;
        }
    }

    public BehaviorFrame getBehaviorFrame(Project proj, Instance instance, CircuitState circState) {
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

    @Override
    public AttributeSet createAttributeSet() {
        attributes = new BehaviorAttributes(behaviorAttr);
        //attributes.setValue(behaviorAttr, (Object)behavior.getAsString());
        attributes.addAttributeListener(this);
        return attributes;
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    }
  
    @Override
    protected void configureNewInstance(Instance instance) {
        super.configureNewInstance(instance);
        String b=(String)instance.getAttributeSet().getValue(behaviorAttr);
        if(b==null)
        {
          b=behaviorClassImplementationBody;
        }
        newBehavior(b, instance);
        instance.addAttributeListener();
    }

}
