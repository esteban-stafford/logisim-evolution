/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.uc;

import static com.cburch.logisim.uc.Strings.S;

import com.cburch.logisim.uc.gray.SimpleGrayCounter;
import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;
import java.util.List;

public class UC extends Library {

  /**
   * Unique identifier of the library, used as reference in project files. Do NOT change as it will
   * prevent project files from loading.
   *
   * <p>Identifier value must MUST be unique string among all libraries.
   */
  public static final String _ID = "UC";

  private static final FactoryDescription[] DESCRIPTIONS = {
    new FactoryDescription(SimpleGrayCounter.class, S.getter("SimpleGrayCounter")),
  };

  private List<Tool> tools = null;

  @Override
  public String getDisplayName() {
    return S.get("ucLibrary");
  }

  @Override
  public List<Tool> getTools() {
    if (tools == null) {
      tools = FactoryDescription.getTools(UC.class, DESCRIPTIONS);
    }
    return tools;
  }
}
