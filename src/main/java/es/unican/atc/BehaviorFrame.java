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
import javax.swing.JComponent;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Font;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Element;

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

  private final JComponent lineNumbers;

  public BehaviorFrame(Project project, Instance instance, ProgrammableComponent model) {
    super(project);
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    this.model = model;
    this.instance = instance;

    final var buttonPanel = new JPanel();

    //buttonPanel.add(open);
    //buttonPanel.add(close);
    //open.addActionListener(myListener);

    buttonPanel.add(save);
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

    display = new JTextArea ( 16, 64 );
    Font font = new Font(Font.MONOSPACED, Font.BOLD, 12);
    display.setFont(font);

    JScrollPane scroll = new JScrollPane ();


    lineNumbers = new JComponent() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Use the font and metrics from the text area
            FontMetrics fm = display.getFontMetrics(display.getFont());
            int lineHeight = fm.getHeight();
            int ascent = fm.getAscent();

            // Set background color
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);

            // Get the visible area of the component
            Rectangle clip = g.getClipBounds();
        
            // Determine the first and last line numbers to draw
            int startLine = clip.y / lineHeight;
            int endLine = (clip.y + clip.height) / lineHeight + 1;
            
            Element root = display.getDocument().getDefaultRootElement();
            int maxLines = root.getElementCount();
            endLine = Math.min(endLine, maxLines);

            // Draw the line numbers
            for (int i = startLine; i < endLine; i++) {
                String num = Integer.toString(i + 1);
                int y = (i * lineHeight) + ascent;
                int x = getWidth() - fm.stringWidth(num) - 5; // 5px padding
                g.drawString(num, x, y);
            }
        }
    };

    lineNumbers.setFont(display.getFont());

    // This listener now updates the size of the line number component
    // whenever the text changes, which is the key to fixing the scrolling.
    display.getDocument().addDocumentListener(new DocumentListener() {
    private void updateLineNumbers() {
        try {
            Element root = display.getDocument().getDefaultRootElement();
            int lineCount = root.getElementCount();
            int digits = Math.max(String.valueOf(lineCount).length(), 1);

            // Calculate the required width based on the number of digits
            FontMetrics fm = lineNumbers.getFontMetrics(lineNumbers.getFont());
            int width = 10 + digits * fm.charWidth('0'); // 10px padding

            // *** THIS IS THE CRITICAL PART ***
            // Set the preferred size. The height must match the JTextArea's preferred height.
            Dimension preferredSize = new Dimension(width, display.getPreferredSize().height);
            lineNumbers.setPreferredSize(preferredSize);

            // Re-layout the scroll pane and repaint
            SwingUtilities.invokeLater(() -> {
                scroll.setRowHeaderView(lineNumbers); // Re-set the view to apply size change
                lineNumbers.repaint();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void changedUpdate(DocumentEvent e) { updateLineNumbers(); }
    @Override public void insertUpdate(DocumentEvent e) { updateLineNumbers(); }
    @Override public void removeUpdate(DocumentEvent e) { updateLineNumbers(); }
});

   /* lineNumbers = new JComponent() {
       @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
               Element root = display.getDocument().getDefaultRootElement();
               int lineCount = root.getElementCount();
               int digits = String.valueOf(lineCount).length();               
               setPreferredSize(new Dimension(20 + digits * 8, 0));               
               g.setColor(Color.LIGHT_GRAY);
               g.fillRect(0, 0, getWidth(), getHeight());               
               g.setColor(Color.BLACK);
               FontMetrics fm = g.getFontMetrics();
               int lineHeight = display.getFontMetrics(display.getFont()).getHeight();               
               Rectangle clip = g.getClipBounds();
               int startY = clip.y;
               int endY = clip.y + clip.height;             
               int startLine = startY / lineHeight;
               int endLine = Math.min(lineCount, startLine + (endY / lineHeight) + 1);              
               for (int i = startLine; i < endLine; i++) {
                  String num = Integer.toString(i + 1);
                  int y = (i * lineHeight) + fm.getAscent();
                  g.drawString(num, getWidth() - fm.stringWidth(num) - 3, y);
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
    };

    lineNumbers.setFont(display.getFont());     
    display.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void changedUpdate(DocumentEvent e) { lineNumbers.repaint(); }
      @Override public void insertUpdate(DocumentEvent e) { lineNumbers.repaint(); }
      @Override public void removeUpdate(DocumentEvent e) { lineNumbers.repaint(); }
    });

*/
    display.setText(model.getBehavior().getAsString());
    display.setEditable ( true );

    scroll.getViewport().add(display);
    scroll.setRowHeaderView(lineNumbers);
    scroll.getViewport().addChangeListener(e -> lineNumbers.repaint());

    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
    
    contents.add(scroll, BorderLayout.CENTER);
    contents.add(buttonPanel, BorderLayout.SOUTH);
    pack();

  }

  public void closeAndDispose() {
    WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
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
        model.newBehavior(display.getText(), instance);
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
