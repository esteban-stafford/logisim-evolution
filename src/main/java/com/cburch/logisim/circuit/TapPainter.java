/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Graphics2D;

class TapPainter {
  static void drawLabels(ComponentDrawContext context, TapAttributes attrs, Location origin) {
    final var width = 20;
    final var g = context.getGraphics().create();
    final var font = g.getFont();
    g.setFont(font.deriveFont(7.0f));
    int x = origin.getX();
    int y = origin.getY();
    GraphicsUtil.drawText(g, attrs.from +"-"+ attrs.to, x + width, y - 2*Wire.WIDTH, GraphicsUtil.H_RIGHT, GraphicsUtil.V_BASELINE);
    g.dispose();
  }

  static void drawLegacy(ComponentDrawContext context, TapAttributes attrs, Location origin) {
     drawLines(context, attrs, origin);
  }

  static void drawLines(ComponentDrawContext context, TapAttributes attrs, Location origin) {
    final var width = 20;
    final var x0 = origin.getX();
    final var y0 = origin.getY();

    final var g = context.getGraphics();
    GraphicsUtil.switchToWidth(g, Wire.WIDTH);
    g.drawLine(x0, y0, x0 + width, y0);

    int[] xTap = {x0, x0, x0 + width / 2, x0 };
    int[] yTap = {y0 - width / 2, y0 + width / 2, y0, y0 - width / 2 };
    g.fillPolygon(xTap, yTap, 4);
  }

  private static final int SPINE_WIDTH = Wire.WIDTH + 2;

  private static final int SPINE_DOT = Wire.WIDTH + 4;
}
