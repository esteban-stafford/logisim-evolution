package es.unican.atc;

import java.util.HashMap;


import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;

public interface Behavior {
    void propagate(InstanceState state, HashMap<String, Integer> nameToId);
    String getAsString();
}