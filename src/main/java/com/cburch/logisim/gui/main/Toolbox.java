/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import com.cburch.draw.toolbar.Toolbar;
import com.cburch.logisim.gui.generic.ProjectExplorer;
import com.cburch.logisim.gui.menu.MenuListener;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.Tool;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Box;
import javax.swing.BoxLayout;

class Toolbox extends JPanel {
  private static final long serialVersionUID = 1L;
  private final ProjectExplorer toolbox;

  private final JTextField searchField = new JTextField();

  Toolbox(Project proj, Frame frame, MenuListener menu) {
    super(new BorderLayout());

    final var toolbarModel = new ToolboxToolbarModel(frame, menu);
    final var toolbar = new Toolbar(toolbarModel);

    toolbox = new ProjectExplorer(proj, false);
    toolbox.setListener(new ToolboxManip(proj, toolbox));

    // NEW: search field wiring (as before)
    searchField.setColumns(16);
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      private void update() { toolbox.setFilterText(searchField.getText()); }
      @Override public void insertUpdate(DocumentEvent e) { update(); }
      @Override public void removeUpdate(DocumentEvent e) { update(); }
      @Override public void changedUpdate(DocumentEvent e) { update(); }
    });

    // NEW: top container so we don't overwrite NORTH
    final var top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    top.add(toolbar);
    top.add(searchField);

    add(top, BorderLayout.NORTH);
    add(new JScrollPane(toolbox), BorderLayout.CENTER);

    toolbarModel.menuEnableChanged(menu);
  }

  void setHaloedTool(Tool value) {
    toolbox.setHaloedTool(value);
  }

  public void updateStructure() {
    toolbox.updateStructure();
  }
}
