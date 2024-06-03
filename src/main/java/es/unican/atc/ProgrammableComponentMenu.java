

package es.unican.atc;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.gui.generic.OptionPane;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.MemContents;
import com.cburch.logisim.tools.MenuExtender;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.WeakHashMap;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class ProgrammableComponentMenu implements ActionListener, MenuExtender {
  private final ProgrammableComponent factory;
  private final Instance instance;
  private Project proj;
  private Frame frame;
  private CircuitState circState;
  private JMenuItem edit;
  private JMenuItem clear;
  private JMenuItem load;
  private JMenuItem save;



  ProgrammableComponentMenu(ProgrammableComponent factory, Instance instance) {
    this.factory = factory;
    this.instance = instance;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == edit)
    {
      doEdit();
    }
    else if (src == clear) doClear();
    else if (src == load) doLoad();
    else if (src == save) doSave();
  }

  @Override
  public void configureMenu(JPopupMenu menu, Project proj) {
    this.proj = proj;
    this.frame = proj.getFrame();
    this.circState = proj.getCircuitState();

    Object attrs = instance.getAttributeSet();
    /*if (attrs instanceof RomAttributes) {
      ((RomAttributes) attrs).setProject(proj);
    }*/

    var enabled = circState != null;
    edit = createItem(enabled, S.get("ramEditMenuItem"));

    menu.addSeparator();
    menu.add(edit);
  }

  private JMenuItem createItem(boolean enabled, String label) {
    final var ret = new JMenuItem(label);
    ret.setEnabled(enabled);
    ret.addActionListener(this);
    return ret;
  }

  private void doClear() {
    throw new UnsupportedOperationException("doClear");
  }

  private void doEdit() {
    //if (factory.getState(instance, circState) == null) return;
    final var frame = factory.getBehaviorFrame(proj, instance, circState);
    frame.setVisible(true);
    frame.toFront();
  }

  private void doLoad() {throw new UnsupportedOperationException("doLoad");
  }

  private void doSave() {
    throw new UnsupportedOperationException("Unimplemented method 'doSave'");
  }
}
