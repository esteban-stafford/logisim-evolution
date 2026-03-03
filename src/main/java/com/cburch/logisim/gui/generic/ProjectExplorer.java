/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import com.cburch.contracts.BaseMouseListenerContract;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.gui.icons.TreeIcon;
import com.cburch.logisim.gui.main.Canvas;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.vhdl.base.VhdlContent;
import com.cburch.logisim.vhdl.base.VhdlEntity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Code taken from Cornell's version of Logisim: http://www.cs.cornell.edu/courses/cs3410/2015sp/
 */
public class ProjectExplorer extends JTree implements LocaleListener {
  public static final Color MAGNIFYING_INTERIOR = new Color(200, 200, 255, 64);
  private static final long serialVersionUID = 1L;

  private final Project proj;
  private final MyListener myListener = new MyListener();
  private final MyCellRenderer renderer = new MyCellRenderer();
  private final DeleteAction deleteAction = new DeleteAction();
  private Listener listener = null;
  private Tool haloedTool = null;

  private final ProjectExplorerModel baseModel;
  private DefaultTreeModel filteredModel = null;
  private String filterTextRaw = "";
  private String filterTextNormalized = "";

  public void setFilterText(String text) {
    filterTextRaw = text == null ? "" : text;
    final var norm = normalizeForSearch(filterTextRaw);
    if (norm.equals(filterTextNormalized)) return;
    filterTextNormalized = norm;

    if (filterTextNormalized.isEmpty()) {
      if (getModel() != baseModel) setModel(baseModel);
      // ensure visuals refresh immediately
      baseModel.fireStructureChanged();
      revalidate();
      repaint();
      return;
    }

    rebuildFilteredModel(); // new helper
  }

  private void rebuildFilteredModel() {
    final var root = baseModel.getRoot();
    if (!(root instanceof DefaultMutableTreeNode baseRoot)) return;

    final var filteredRoot = filterNodeRecursive(baseRoot, filterTextNormalized);
    filteredModel =
        new DefaultTreeModel(
            filteredRoot != null ? filteredRoot : new DefaultMutableTreeNode("(no matches)"));
    setModel(filteredModel);
    expandAll();
    repaint();
  }
  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

  private static String normalizeForSearch(String s) {
    if (s == null) return "";
    var n = Normalizer.normalize(s, Normalizer.Form.NFD);
    n = DIACRITICS.matcher(n).replaceAll("");
    return n.toLowerCase(Locale.ROOT);
  }


  public ProjectExplorer(Project proj, boolean showMouseTools) {
    super();
    this.proj = proj;

    baseModel = new ProjectExplorerModel(proj, this, showMouseTools);
    setModel(baseModel);
    setRootVisible(true);
    addMouseListener(myListener);
    ToolTipManager.sharedInstance().registerComponent(this);

    MySelectionModel selector = new MySelectionModel();
    selector.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(selector);

    // Force our renderer to be installed and configured (don’t rely on getCellRenderer timing)
    setCellRenderer(this.renderer);
    this.renderer.setClosedIcon(new TreeIcon(true));
    this.renderer.setOpenIcon(new TreeIcon(false));
    this.renderer.setLeafIcon(new TreeIcon(false)); // or a dedicated leaf icon if you have one

    addTreeSelectionListener(myListener);

    InputMap imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), deleteAction);
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction);
    ActionMap amap = getActionMap();
    amap.put(deleteAction, deleteAction);

    proj.addProjectListener(myListener);
    AppPreferences.GATE_SHAPE.addPropertyChangeListener(myListener);
    LocaleManager.addLocaleListener(this);
    localeChanged();
  }


  public Tool getSelectedTool() {
    final var path = getSelectionPath();
    if (path == null) return null;

    final var last = unwrap(path.getLastPathComponent());
    return (last instanceof ProjectExplorerToolNode toolNode) ? toolNode.getValue() : null;
  }

  public void updateStructure() {
    baseModel.updateStructure();
    if (filterTextNormalized != null && !filterTextNormalized.isEmpty()) {
      rebuildFilteredModel();
    }
  }
  
  @Override
  public void localeChanged() {
    // Base model must refresh names; renderer uses getDisplayName() at paint-time,
    // but structure-change avoids ellipsis issues as original comment says.
    baseModel.fireStructureChanged();

    if (filterTextNormalized != null && !filterTextNormalized.isEmpty()) {
      rebuildFilteredModel();
    } else {
      repaint();
    }
  }

  public void setHaloedTool(Tool t) {
    haloedTool = t;
  }

  public void setListener(Listener value) {
    listener = value;
  }

  // Expand everything (filtered view should be fully expanded)
  private void expandAll() {
    for (int i = 0; i < getRowCount(); i++) {
      expandRow(i);
    }
  }

  private static Object unwrap(Object value) {
    if (value instanceof ProjectExplorerModel.Node<?>) return value;

    if (value instanceof DefaultMutableTreeNode dmtn && dmtn.getUserObject() != null) {
      return dmtn.getUserObject();
    }
    return value;
  }

  // Returns a cloned node (DefaultMutableTreeNode) containing only matching tool leaves + ancestor path.
  // - Keeps libraries/categories only if they have a matching descendant tool.
  // - Matches only tool leaf nodes based on tool.getDisplayName() (same as renderer uses).
  private DefaultMutableTreeNode filterNodeRecursive(DefaultMutableTreeNode node, String filterNorm) {
    // Leaf tool node?
    if (node instanceof ProjectExplorerToolNode toolNode) {
      final var tool = toolNode.getValue();
      if (tool == null) return null;
      final var labelNorm = normalizeForSearch(tool.getDisplayName());
      if (!labelNorm.contains(filterNorm)) return null;

      // Keep the existing node object so selection + actions still work on real nodes.
      // Wrap it in a DefaultMutableTreeNode to avoid modifying the original tree.
      return new DefaultMutableTreeNode(toolNode);
    }

    // Library/category node?
    if (node instanceof ProjectExplorerLibraryNode libNode) {
      final var out = new DefaultMutableTreeNode(libNode);
      final var children = node.children();
      while (children.hasMoreElements()) {
        final var ch = children.nextElement();
        if (ch instanceof DefaultMutableTreeNode childNode) {
          final var kept = filterNodeRecursive(childNode, filterNorm);
          if (kept != null) out.add(kept);
        }
      }
      return out.getChildCount() == 0 ? null : out;
    }

    // Any other node types (root might be ProjectExplorerLibraryNode, so this is mostly defensive)
    final var out = new DefaultMutableTreeNode(node);
    final var children = node.children();
    while (children.hasMoreElements()) {
      final var ch = children.nextElement();
      if (ch instanceof DefaultMutableTreeNode childNode) {
        final var kept = filterNodeRecursive(childNode, filterNorm);
        if (kept != null) out.add(kept);
      }
    }
    return out.getChildCount() == 0 ? null : out;
  }

  private class DeleteAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent event) {
      final var path = getSelectionPath();
      if (listener != null && path != null && path.getPathCount() == 2) {
        listener.deleteRequested(new Event(path));
      }
      ProjectExplorer.this.requestFocus();
    }
  }

  private class MyCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public java.awt.Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {

      Object actual = unwrap(value);

      java.awt.Component ret = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      final var plainFont = AppPreferences.getScaledFont(ret.getFont());
      final var boldFont = new Font(plainFont.getFontName(), Font.BOLD, plainFont.getSize());
      ret.setFont(plainFont);
      if (ret instanceof JComponent comp) {
        comp.setToolTipText(null);
      }
      if (actual instanceof ProjectExplorerToolNode toolNode) {
        final var tool = toolNode.getValue();
        if (ret instanceof JLabel label) {
          var viewed = false;
          if (tool instanceof AddTool && proj != null && proj.getFrame() != null) {
            Circuit circ = null;
            VhdlContent vhdl = null;
            final var fact = ((AddTool) tool).getFactory(false);
            if (fact instanceof SubcircuitFactory sub) {
              circ = sub.getSubcircuit();
            } else if (fact instanceof VhdlEntity vhdlEntity) {
              vhdl = vhdlEntity.getContent();
            }
            viewed =
                (proj.getFrame().getHdlEditorView() == null)
                    ? (circ != null && circ == proj.getCurrentCircuit())
                    : (vhdl != null && vhdl == proj.getFrame().getHdlEditorView());
          }
          label.setFont(viewed ? boldFont : plainFont);
          label.setText(tool.getDisplayName());
          label.setIcon(new ToolIcon(tool));
          label.setToolTipText(tool.getDescription());
        }
      } else if (actual instanceof ProjectExplorerLibraryNode libNode) {
        final var lib = libNode.getValue();

        if (ret instanceof JLabel) {
          final var baseName = lib.getDisplayName();
          var text = baseName;
          if (lib.isDirty()) {
            // TODO: Would be nice to use DIRTY_MARKER here instead of "*" but it does not render
            // corectly in project tree, font seem to have the character as frame title is fine.
            // Needs to figure out what is different (java fonts?). Keep "*" unless bug is resolved.
            final var DIRTY_MARKER_LOCAL = "*"; // useless var for easy DIRTY_MARKER hunt in future.
            text = DIRTY_MARKER_LOCAL + baseName;
          }

          ((JLabel) ret).setText(text);
        }
      }
      return ret;
    }
  }

  private class MyListener implements BaseMouseListenerContract, TreeSelectionListener, ProjectListener, PropertyChangeListener {
    private void checkForPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        final var path = getPathForLocation(e.getX(), e.getY());
        if (path != null && listener != null) {
          final var menu = listener.menuRequested(new Event(path));
          if (menu != null) {
            menu.show(ProjectExplorer.this, e.getX(), e.getY());
          }
        }
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        final var path = getPathForLocation(e.getX(), e.getY());
        if (path != null && listener != null) {
          listener.doubleClicked(new Event(path));
        }
      }
    }

    //
    // MouseListener methods
    //
    @Override
    public void mousePressed(MouseEvent e) {
      ProjectExplorer.this.requestFocus();
      checkForPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      checkForPopup(e);
    }

    void changedNode(Object o) {
      if (o instanceof Tool tool) {
        final var node = baseModel.findTool(tool);
        if (node != null) node.fireNodeChanged();
      }
      // If filtering, rebuild to reflect updated names (e.g., circuit rename)
      if (filterTextNormalized != null && !filterTextNormalized.isEmpty()) {
        rebuildFilteredModel();
      }
    }

    //
    // project/library file/circuit listener methods
    //
    @Override
    public void projectChanged(ProjectEvent event) {
      final var act = event.getAction();
      if (act == ProjectEvent.ACTION_SET_CURRENT || act == ProjectEvent.ACTION_SET_TOOL) {
        changedNode(event.getOldData());
        changedNode(event.getData());
      }
    }

    //
    // PropertyChangeListener methods
    //
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (AppPreferences.GATE_SHAPE.isSource(event)) {
        ProjectExplorer.this.repaint();
      }
    }

    //
    // TreeSelectionListener methods
    //
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      final var path = e.getNewLeadSelectionPath();
      if (listener != null) {
        listener.selectionChanged(new Event(path));
      }
    }
  }

  private static class MySelectionModel extends DefaultTreeSelectionModel {

    private static final long serialVersionUID = 1L;

    @Override
    public void addSelectionPath(TreePath path) {
      if (isPathValid(path)) super.addSelectionPath(path);
    }

    @Override
    public void addSelectionPaths(TreePath[] paths) {
      paths = getValidPaths(paths);

      if (paths != null) super.addSelectionPaths(paths);
    }

    private TreePath[] getValidPaths(TreePath[] paths) {
      var count = 0;
      for (final var treePath : paths) {
        if (isPathValid(treePath)) ++count;
      }

      if (count == 0) {
        return null;
      } else if (count == paths.length) {
        return paths;
      } else {
        final var ret = new TreePath[count];
        int j = 0;

        for (final var path : paths) {
          if (isPathValid(path)) ret[j++] = path;
        }

        return ret;
      }
    }

    private boolean isPathValid(TreePath path) {
      if (path == null) return false;
      final var last = unwrap(path.getLastPathComponent());
      return last instanceof ProjectExplorerToolNode;
    }

    @Override
    public void setSelectionPath(TreePath path) {
      if (isPathValid(path)) {
        clearSelection();
        super.setSelectionPath(path);
      }
    }

    @Override
    public void setSelectionPaths(TreePath[] paths) {
      paths = getValidPaths(paths);
      if (paths != null) {
        clearSelection();
        super.setSelectionPaths(paths);
      }
    }
  }

  private class ToolIcon implements Icon {
    final Tool tool;
    Circuit circ = null;
    VhdlContent vhdl = null;

    ToolIcon(Tool tool) {
      this.tool = tool;
      if (tool instanceof AddTool addTool) {
        final var fact = addTool.getFactory(false);
        if (fact instanceof SubcircuitFactory sub) {
          circ = sub.getSubcircuit();
        } else if (fact instanceof VhdlEntity vhdlEntity) {
          vhdl = vhdlEntity.getContent();
        }
      }
    }

    @Override
    public int getIconHeight() {
      return AppPreferences.getScaled(AppPreferences.BOX_SIZE);
    }

    @Override
    public int getIconWidth() {
      return AppPreferences.getScaled(AppPreferences.BOX_SIZE);
    }

    @Override
    public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      boolean viewed =
          (proj.getFrame().getHdlEditorView() == null)
              ? (circ != null && circ == proj.getCurrentCircuit())
              : (vhdl != null && vhdl == proj.getFrame().getHdlEditorView());
      final var haloed = !viewed && (tool == haloedTool && AppPreferences.ATTRIBUTE_HALO.getBoolean());
      // draw halo if appropriate
      if (haloed) {
        final var s = g.getClip();
        g.clipRect(
            x,
            y,
            AppPreferences.getScaled(AppPreferences.BOX_SIZE),
            AppPreferences.getScaled(AppPreferences.BOX_SIZE));
        g.setColor(Canvas.HALO_COLOR);
        g.setColor(Color.BLACK);
        g.setClip(s);
      }

      // draw tool icon
      g.setColor(new Color(AppPreferences.COMPONENT_ICON_COLOR.get()));
      final var gfxIcon = g.create();
      final var context = new ComponentDrawContext(ProjectExplorer.this, null, null, g, gfxIcon);
      tool.paintIcon(
          context,
          x + AppPreferences.getScaled(AppPreferences.ICON_BORDER),
          y + AppPreferences.getScaled(AppPreferences.ICON_BORDER));
      gfxIcon.dispose();

      // draw magnifying glass if appropriate
      if (viewed) {
        final var tx = x + AppPreferences.getScaled(AppPreferences.BOX_SIZE - 7);
        final var ty = y + AppPreferences.getScaled(AppPreferences.BOX_SIZE - 7);
        int[] xp = {
          tx - 1,
          x + AppPreferences.getScaled(AppPreferences.BOX_SIZE - 2),
          x + AppPreferences.getScaled(AppPreferences.BOX_SIZE),
          tx + 1
        };
        int[] yp = {
          ty + 1,
          y + AppPreferences.getScaled(AppPreferences.BOX_SIZE),
          y + AppPreferences.getScaled(AppPreferences.BOX_SIZE - 2),
          ty - 1
        };
        g.setColor(MAGNIFYING_INTERIOR);
        g.fillOval(
            x + AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 2),
            y + AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 2),
            AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 1),
            AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 1));
        g.setColor(new Color(139, 69, 19));
        g.drawOval(
            x + AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 2),
            y + AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 2),
            AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 1),
            AppPreferences.getScaled(AppPreferences.BOX_SIZE >> 1));
        g.fillPolygon(xp, yp, xp.length);
      }
    }
  }

  public interface Listener {
    default void deleteRequested(Event event) {
      // no-op implementation
    }

    default void doubleClicked(Event event) {
      // no-op implementation
    }

    JPopupMenu menuRequested(Event event);

    default void moveRequested(Event event, AddTool dragged, AddTool target) {
      // no-op implementation
    }

    default void selectionChanged(Event event) {
      // no-op implementation
    }
  }

  public static class Event {
    private final TreePath path;

    public Event(TreePath p) {
      path = p;
    }

    public TreePath getTreePath() {
      return path;
    }
    
    
    public Object getTarget() {
      if (path == null) return null;
      return unwrap(path.getLastPathComponent());
    }

  }
}
