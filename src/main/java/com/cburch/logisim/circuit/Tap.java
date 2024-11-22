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

  // basic data
  byte[] bitEnd;

  public Tap(Location loc, AttributeSet attrs) {
    super(loc, attrs, 2);
    configureComponent();
    attrs.addAttributeListener(this);
  }

  @Override
  public void attributeValueChanged(AttributeEvent e) {
    configureComponent();
  }
  private synchronized void configureComponent() {
    final var attrs = (TapAttributes) getAttributeSet();
    final var size = attrs.size;

    bitEnd = new byte[attrs.width];
    for (int i = 0; i < bitEnd.length; i++) {
      bitEnd[i] = (byte) (i >= attrs.from && i <= attrs.to ? 1 : 0);
    }

    // compute width of both ends
    bitThread = new byte[bitEnd.length];
    // for (int i = 0; i < result.length; i++) { result[i] = (byte) (i >= attrs.from && i <= attrs.to ? 1 : 0); }
    byte j = 0;
    for (var i = 0; i < bitEnd.length; i++) {
      bitThread[i] = (byte) (i >= attrs.from && i <= attrs.to ? j++ : -1);
    }


    // compute end positions
    final var ends = new EndData[2];
    final var origin = getLocation();
    ends[0] = new EndData(origin, BitWidth.create(attrs.width), EndData.INPUT_OUTPUT);
    var x = origin.getX();
    var y = origin.getY();
    if( attrs.facing == Direction.EAST ) {
       x += size;
    } else if( attrs.facing == Direction.WEST ) {
       x += -size;
    } else if( attrs.facing == Direction.NORTH ) {
       y += -size;
    } else if( attrs.facing == Direction.SOUTH ) {
       y += size;
    }
    ends[1] = new EndData(Location.create(x, y, true), BitWidth.create(attrs.to-attrs.from+1), EndData.INPUT_OUTPUT);
    wireData = new CircuitWires.SplitterData(1);

    setEnds(ends);
    recomputeBounds();
    fireComponentInvalidated(new ComponentEvent(this));
  }

  @Override
  public void configureMenu(JPopupMenu menu, Project proj) { }

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
    if (isMarked()) {
      final var g = context.getGraphics();
      final var bds = this.getBounds();
      g.setColor(Netlist.DRC_INSTANCE_MARK_COLOR);
      GraphicsUtil.switchToWidth(g, 2);
      g.drawRoundRect(bds.getX() - 10, bds.getY(), bds.getWidth() + 20, bds.getHeight() + 20, 20, 20);
    }
  }

  public byte[] getEndpoints() {
    return bitEnd;
  }
  //
  // abstract ManagedComponent methods
  //
  @Override
  public ComponentFactory getFactory() {
    return TapFactory.instance;
  }

}
