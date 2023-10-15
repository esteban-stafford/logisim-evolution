/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.circuit;

import static com.cburch.logisim.circuit.Strings.S;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentEvent;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentUserEvent;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.comp.ManagedComponent;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.tools.ToolTipMaker;
import com.cburch.logisim.tools.WireRepair;
import com.cburch.logisim.tools.WireRepairData;
import com.cburch.logisim.util.GraphicsUtil;
import javax.swing.JPopupMenu;

public class Tap extends Splitter {

  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all tools.
   */
  public static final String _ID = "Tap";

  private static void appendBuf(StringBuilder buf, int start, int end) {
    if (buf.length() > 0) buf.append(",");
    if (start == end) {
      buf.append(start);
    } else {
      buf.append(start).append("-").append(end);
    }
  }

  private boolean isMarked = false;

  public void setMarked(boolean value) {
    isMarked = value;
  }

  public boolean isMarked() {
    return isMarked;
  }

  public Tap(Location loc, AttributeSet attrs) {
    super(loc, attrs, 2);
    configureComponent();
    attrs.addAttributeListener(this);
  }

  //
  // AttributeListener methods
  //
  @Override
  public void attributeListChanged(AttributeEvent e) {}

  @Override
  public void attributeValueChanged(AttributeEvent e) {
    configureComponent();
  }

  private synchronized void configureComponent() {
    final var attrs = (TapAttributes) getAttributeSet();
    final var parms = attrs.getParameters();

    // compute end positions
    final var ends = new EndData[2];
    final var origin = getLocation();
    ends[0] = new EndData(origin, BitWidth.create(attrs.width), EndData.INPUT_OUTPUT);
    var x = origin.getX() + parms.getEnd0X();
    var y = origin.getY();
    ends[1] = new EndData(Location.create(x, y, true), BitWidth.create(attrs.to-attrs.from+1), EndData.INPUT_OUTPUT);
    wireData = new CircuitWires.SplitterData(1);

    setEnds(ends);
    recomputeBounds();
    fireComponentInvalidated(new ComponentEvent(this));
  }

  @Override
  public void configureMenu(JPopupMenu menu, Project proj) { }

  @Override
  public boolean contains(Location loc) {
    if (super.contains(loc)) {
      final var myLoc = getLocation();
      final var facing = getAttributeSet().getValue(StdAttr.FACING);
      if (facing == Direction.EAST || facing == Direction.WEST) {
        return Math.abs(loc.getX() - myLoc.getX()) > 5 || loc.manhattanDistanceTo(myLoc) <= 5;
      } else {
        return Math.abs(loc.getY() - myLoc.getY()) > 5 || loc.manhattanDistanceTo(myLoc) <= 5;
      }
    } else {
      return false;
    }
  }

  //
  // user interface methods
  //
  @Override
  public void draw(ComponentDrawContext context) {
    final var attrs = (TapAttributes) getAttributeSet();
    final var loc = getLocation();
    TapPainter.drawLines(context, attrs, loc);
    TapPainter.drawLabels(context, attrs, loc);
    context.drawPins(this);
    if (isMarked) {
      final var g = context.getGraphics();
      final var bds = this.getBounds();
      g.setColor(Netlist.DRC_INSTANCE_MARK_COLOR);
      GraphicsUtil.switchToWidth(g, 2);
      g.drawRoundRect(bds.getX() - 10, bds.getY(), bds.getWidth() + 20, bds.getHeight() + 20, 20, 20);
    }
  }

  public byte[] getEndpoints() {
    final var attrs = (TapAttributes) getAttributeSet();
    byte[] result = new byte[attrs.width];
    for (int i = 0; i < result.length; i++) {
      result[i] = (byte) (i >= attrs.from && i <= attrs.to ? 1 : 0);
    }
    return result;
  }

  public byte[] getThreads() {
    final var attrs = (TapAttributes) getAttributeSet();
    byte[] result = new byte[attrs.width];
    byte j = 0;
    for (int i = 0; i < result.length; i++) {
      result[i] = (byte) (i >= attrs.from && i <= attrs.to ? j++ : -1);
    }
    return result;
  }

  //
  // abstract ManagedComponent methods
  //
  @Override
  public ComponentFactory getFactory() {
    return TapFactory.instance;
  }

  @Override
  public void setFactory(ComponentFactory fact) {}

  @Override
  public Object getFeature(Object key) {
    if (key == WireRepair.class) return this;
    if (key == ToolTipMaker.class) return this;
    if (key == MenuExtender.class) return this;
    else return super.getFeature(key);
  }

  @Override
  public String getToolTip(ComponentUserEvent e) {
    return new String("Hello");
  }

  @Override
  public void propagate(CircuitState state) {
    // handled by CircuitWires, nothing to do
  }

  @Override
  public boolean shouldRepairWire(WireRepairData data) {
    return true;
  }
}
