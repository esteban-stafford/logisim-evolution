package es.unican.atc;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class Components extends Library {
    private List<Tool> tools;

    public Components() {
        tools = Arrays.asList(new Tool[] {
                new AddTool(new RegisterFile16()),
                new AddTool(new RegisterFile32()),
                new AddTool(new ALU()),
        });
    }

    @Override
    public String getName() { return "ATC-Components"; }
    @Override
    public String getDisplayName() { return "ATC Components"; }
    @Override
    public List<Tool> getTools() { return tools; }
    public boolean removeLibrary(String Name) {
    	return false;
    }
}
