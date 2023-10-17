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
    final var size = attrs.size;
    final var g = context.getGraphics().create();
    final var font = g.getFont();
    g.setFont(font.deriveFont(7.0f));
    int x = origin.getX();
    int y = origin.getY();
    int xv = 0;
    int yv = 0;
    int xp = 0;
    int yp = 0;
    var hAlign = GraphicsUtil.H_LEFT;
    var vAlign = GraphicsUtil.V_BASELINE;
    if( attrs.facing == Direction.EAST ) {
       xv = 1;
       yp = -1;
    } else if( attrs.facing == Direction.WEST ) {
       hAlign = GraphicsUtil.H_RIGHT;
       xv = -1;
       yp = -1;
    } else if( attrs.facing == Direction.NORTH ) {
       yv = -1;
       xp = -1;
    } else if( attrs.facing == Direction.SOUTH ) {
       yv = 1;
       xp = -1;
       vAlign = GraphicsUtil.V_TOP;
    }
    GraphicsUtil.drawText(g, attrs.from +"-"+ attrs.to, x + xv*size/2 -2*Wire.WIDTH*xp, y + yv*size/2 + 2*Wire.WIDTH*yp, hAlign, vAlign);
    g.dispose();
  }

  static void drawLegacy(ComponentDrawContext context, TapAttributes attrs, Location origin) {
     drawLines(context, attrs, origin);
  }

  static void drawLines(ComponentDrawContext context, TapAttributes attrs, Location origin) {
    final var size = attrs.size;
    final var x0 = origin.getX();
    final var y0 = origin.getY();

    final var g = context.getGraphics();
    if(attrs.to - attrs.from > 0) {
      GraphicsUtil.switchToWidth(g, Wire.WIDTH_BUS);
    } else {
      GraphicsUtil.switchToWidth(g, Wire.WIDTH);
    }

    int xv = 0;
    int yv = 0;
    if( attrs.facing == Direction.EAST ) {
       xv = size;
    } else if( attrs.facing == Direction.WEST ) {
       xv = -size;
    } else if( attrs.facing == Direction.NORTH ) {
       yv = -size;
    } else if( attrs.facing == Direction.SOUTH ) {
       yv = size;
    }
    int xp = yv;
    int yp = -xv;
    g.drawLine(x0, y0, x0 + xv, y0 + yv);
    int[] xTap = {x0 - xp/2, x0 + xp/2, x0 + xv/2, x0 - xp/2 };
    int[] yTap = {y0 - yp/2, y0 + yp/2, y0 + yv/2, y0 - yp/2 };
    g.fillPolygon(xTap, yTap, 4);
  }
}
