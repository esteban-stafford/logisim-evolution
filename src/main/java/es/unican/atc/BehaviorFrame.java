/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package es.unican.atc;

import static com.cburch.logisim.gui.Strings.S;

import com.cburch.hex.HexEditor;
import com.cburch.hex.HexModel;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.MemContents;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BehaviorFrame extends LFrame.SubWindow {
  private static final long serialVersionUID = 1L;
  private final WindowMenuManager windowManager = new WindowMenuManager();
  private final EditListener editListener = new EditListener();
  private final MyListener myListener = new MyListener();
  private final ProgrammableComponent model;
  private final JButton open = new JButton();
  private final JButton save = new JButton();
  private final JButton close = new JButton();
  private final Instance instance;
  private final JTextArea display;

  public BehaviorFrame(Project project, Instance instance, ProgrammableComponent model) {
    super(project);
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    this.model = model;
    this.instance = instance;

    final var buttonPanel = new JPanel();
    //buttonPanel.add(open);
    buttonPanel.add(save);
    //buttonPanel.add(close);
    //open.addActionListener(myListener);
    save.addActionListener(myListener);
    close.addActionListener(myListener);


    Container contents = getContentPane();

    LocaleManager.addLocaleListener(myListener);
    myListener.localeChanged();

    Dimension size = getSize();
    Dimension screen = getToolkit().getScreenSize();
    if (size.width > screen.width || size.height > screen.height) {
      size.width = Math.min(size.width, screen.width);
      size.height = Math.min(size.height, screen.height);
      setSize(size);
    }

    editListener.register(menubar);
    setLocationRelativeTo(project.getFrame());

    // create the middle panel components

    display = new JTextArea ( 16, 58 );
    display.setText(model.getBehavior().getAsString());
    display.setEditable ( true );
    JScrollPane scroll = new JScrollPane ( display );
    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
    
    contents.add(scroll, BorderLayout.CENTER);
    contents.add(buttonPanel, BorderLayout.SOUTH);
    pack();

  }

  public void closeAndDispose() {
    WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
    System.out.println("Cerrando\n");
    processWindowEvent(e);
    dispose();
  }

  @Override
  public void setVisible(boolean value) {
    if (value && !isVisible()) {
      windowManager.frameOpened(this);
    }
    super.setVisible(value);
  }

  private class EditListener implements ActionListener, ChangeListener {
    //private Clip clip = null;

    @Override
    public void actionPerformed(ActionEvent e) {
      /*Object src = e.getSource();
      if (src == LogisimMenuBar.CUT) {
        getClip().copy();
        editor.delete();
      } else if (src == LogisimMenuBar.COPY) {
        getClip().copy();
      } else if (src == LogisimMenuBar.PASTE) {
        getClip().paste();
      } else if (src == LogisimMenuBar.DELETE) {
        editor.delete();
      } else if (src == LogisimMenuBar.SELECT_ALL) {
        editor.selectAll();
      }*/
    }

    private void enableItems(LogisimMenuBar menubar) {
      final var sel = true;
      final var clip = true; // TODO editor.clipboardExists();
      menubar.setEnabled(LogisimMenuBar.CUT, sel);
      menubar.setEnabled(LogisimMenuBar.COPY, sel);
      menubar.setEnabled(LogisimMenuBar.PASTE, clip);
      menubar.setEnabled(LogisimMenuBar.DELETE, sel);
      menubar.setEnabled(LogisimMenuBar.SELECT_ALL, true);
    }

    /*private Clip getClip() {
      if (clip == null) clip = new Clip(editor);
      return clip;
    }
  */
    private void register(LogisimMenuBar menubar) {
      menubar.addActionListener(LogisimMenuBar.CUT, this);
      menubar.addActionListener(LogisimMenuBar.COPY, this);
      menubar.addActionListener(LogisimMenuBar.PASTE, this);
      menubar.addActionListener(LogisimMenuBar.DELETE, this);
      menubar.addActionListener(LogisimMenuBar.SELECT_ALL, this);
      enableItems(menubar);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      //enableItems((LogisimMenuBar) getJMenuBar());
    }
  }

  private class MyListener implements ActionListener, LocaleListener {
    @Override
    public void actionPerformed(ActionEvent event) {
      final var src = event.getSource();
      /*if (src == open) {
        HexFile.open((MemContents) model, BehaviorFrame.this, project, instance);
        */
      if (src == save) {
        System.out.println("Guardado\n");
        model.newBehavior(display.getText());
        //HexFile.save((MemContents) model, BehaviorFrame.this, project, instance);
      } else if (src == close) {
        WindowEvent e = new WindowEvent(BehaviorFrame.this, WindowEvent.WINDOW_CLOSING);
        BehaviorFrame.this.processWindowEvent(e);
      }
    }

    @Override
    public void localeChanged() {
      setTitle(S.get("behaviorFrameTitle"));
      open.setText(S.get("openButton"));
      save.setText(S.get("saveButton"));
      close.setText(S.get("closeButton"));
    }
  }

  private class WindowMenuManager extends WindowMenuItemManager implements LocaleListener {
    WindowMenuManager() {
      super(S.get("hexFrameMenuItem"), false);
      LocaleManager.addLocaleListener(this);
    }

    @Override
    public JFrame getJFrame(boolean create, java.awt.Component parent) {
      return BehaviorFrame.this;
    }

    @Override
    public void localeChanged() {
      setText(S.get("hexFrameMenuItem"));
    }
  }
}
