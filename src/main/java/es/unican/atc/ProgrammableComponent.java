package es.unican.atc;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    private static String behaviorClassImplementationHeader = "package es.unican.atc;\n\n" +
        "import java.util.HashMap;\n" +
        "import com.cburch.logisim.instance.InstanceState;\n"+
        "import com.cburch.logisim.data.BitWidth;\n"+
        "import com.cburch.logisim.data.Value;\n"+
        "public class ActualBehaviorXX implements Behavior{\n" +
        "   public void propagate(InstanceState state, HashMap<String, Integer> nameToId){\n" +
        "       System.out.println(\"HEYY\");\n" +
        "       long v = state.getPortValue(0).toLongValue();\n" +
        "       Value out = Value.createKnown(BitWidth.create(32), v);\n"+
        "       state.setPort(2, out, 32);\n" +
        "   }\n"+
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
        System.out.println("Construyendo\n");
        File sourceFile=null;
        try{
            String fileName = "ActualBehaviorXX.java";
            sourceFile = new File("./src/main/java/es/unican/atc/"+fileName);
            Files.write(sourceFile.toPath(), behaviorClassImplementationHeader.getBytes());
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
                Class<?> loadedClass=classLoader.loadClass("es.unican.atc.ActualBehaviorXX");
                System.out.println("Cargado\n");
                 Object obj = loadedClass.getDeclaredConstructor().newInstance();
                // Santity check
                if (obj instanceof Behavior) {
                    // Cast to the DoStuff interface
                    behavior = (Behavior)obj;
                    System.out.println("Construido\n");
                }
            } catch (Exception e) {
                System.out.println("Cagada en el path de la clase\n");
                throw new RuntimeException("Error compiling class: " + e.getMessage());
            }
            
           
            /************************************************************************************************* Load and execute **/
        } else {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.format("Error on line %d in %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getSource().toUri());
            }
        }
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

    public void setBehavior(Behavior b) {
        behavior=b;
    }
}
