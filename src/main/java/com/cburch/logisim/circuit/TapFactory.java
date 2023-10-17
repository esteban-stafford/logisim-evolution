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

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.comp.AbstractComponentFactory;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.IntegerConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.tools.key.KeyConfigurator;
import com.cburch.logisim.tools.key.ParallelConfigurator;
import com.cburch.logisim.util.IconsUtil;
import com.cburch.logisim.util.StringGetter;
import java.awt.Color;
import java.awt.event.InputEvent;
import javax.swing.Icon;

public class TapFactory extends AbstractComponentFactory {

  public static final TapFactory instance = new TapFactory();

  private static final Icon toolIcon = IconsUtil.getIcon("tap.gif");

  private TapFactory() {}

  @Override
  public AttributeSet createAttributeSet() {
    return new TapAttributes();
  }

  @Override
  public Component createComponent(Location loc, AttributeSet attrs) {
    return new Tap(loc, attrs);
  }

  //
  // user interface methods
  //
  @Override
  public void drawGhost(ComponentDrawContext context, Color color, int x, int y, AttributeSet attrsBase) {
    final var attrs = (TapAttributes) attrsBase;
    context.getGraphics().setColor(color);
    final var loc = Location.create(x, y, true);
    TapPainter.drawLines(context, attrs, loc);
  }

  /*@Override
  public Object getDefaultAttributeValue(Attribute<?> attr, LogisimVersion ver) {
    if (attr == TapAttributes.ATTR_APPEARANCE) {
      if (ver.compareTo(new LogisimVersion(2, 6, 4)) < 0) {
        return TapAttributes.APPEAR_LEGACY;
      } else {
        return TapAttributes.APPEAR_LEFT;
      }
    } else if (attr instanceof TapAttributes.BitOutAttribute bitOutAttr) {
      return bitOutAttr.getDefault();
    } else {
      return super.getDefaultAttributeValue(attr, ver);
    }
  } */

  @Override
  public StringGetter getDisplayGetter() {
    return S.getter("tapComponent");
  }

  /*
  @Override
  public Object getFeature(Object key, AttributeSet attrs) {
    if (key == FACING_ATTRIBUTE_KEY) {
      return StdAttr.FACING;
    } else if (key == KeyConfigurator.class) {
      KeyConfigurator altConfig =
          ParallelConfigurator.create(
              new BitWidthConfigurator(TapAttributes.ATTR_WIDTH),
              new IntegerConfigurator(
                  TapAttributes.ATTR_FANOUT, 1, 64, InputEvent.ALT_DOWN_MASK));
      return JoinedConfigurator.create(
          new IntegerConfigurator(TapAttributes.ATTR_FANOUT, 1, 64, 0), altConfig);
    }
    return super.getFeature(key, attrs);
  }
  */

  @Override
  public String getName() {
    return Tap._ID;
  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrsBase) {
    final var width = 20;
    return Bounds.create(0, -width/2, width, width);
  }

  @Override
  public boolean isHDLSupportedComponent(AttributeSet attrs) {
    return true;
  }

  @Override
  public void paintIcon(ComponentDrawContext c, int x, int y, AttributeSet attrs) {
    final var g = c.getGraphics();
    if (toolIcon != null) {
      toolIcon.paintIcon(c.getDestination(), g, x + 2, y + 2);
    }
  }

}
