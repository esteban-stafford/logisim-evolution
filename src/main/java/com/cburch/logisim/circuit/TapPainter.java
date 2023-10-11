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
    final var g = context.getGraphics().create();
    final var font = g.getFont();
    g.setFont(font.deriveFont(7.0f));

    final var parms = attrs.getParameters();
    int x = origin.getX() + parms.getEnd0X() + parms.getEndToSpineDeltaX();
    int y = origin.getY() + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
    int dx = parms.getEndToEndDeltaX();
    int dy = parms.getEndToEndDeltaY();
    if (parms.getTextAngle() != 0) {
      ((Graphics2D) g).rotate(Math.PI / 2.0);
      int t;
      t = -x;
      x = y;
      y = t;
      t = -dx;
      dx = dy;
      dy = t;
    }
    final var halign = parms.getTextHorzAlign();
    final var valign = parms.getTextVertAlign();
    x += (halign == GraphicsUtil.H_RIGHT ? -1 : 1) * (SPINE_WIDTH / 2 + 1);
    y += valign == GraphicsUtil.V_TOP ? 0 : -3;
    GraphicsUtil.drawText(g, attrs.from +"-"+ attrs.to, x, y, halign, valign);

    g.dispose();
  }

  static void drawLegacy(ComponentDrawContext context, TapAttributes attrs, Location origin) {
     drawLines(context, attrs, origin);
  }

  static void drawLines(ComponentDrawContext context, TapAttributes attrs, Location origin) {
    var showState = context.getShowState();
    final var state = showState ? context.getCircuitState() : null;
    if (state == null) showState = false;

    final var parms = attrs.getParameters();
    final var x0 = origin.getX();
    final var y0 = origin.getY();
    var x = x0 + parms.getEnd0X();
    var y = y0 + parms.getEnd0Y();
    var dx = parms.getEndToEndDeltaX();
    var dy = parms.getEndToEndDeltaY();
    final var dxEndSpine = parms.getEndToSpineDeltaX();
    final var dyEndSpine = parms.getEndToSpineDeltaY();

    final var g = context.getGraphics();
    final var oldColor = g.getColor();
    GraphicsUtil.switchToWidth(g, Wire.WIDTH);
    /*for (int i = 0, n = attrs.fanout; i < n; i++) {
      if (showState) {
        final var val = state.getValue(Location.create(x, y, true));
        g.setColor(val.getColor());
      }
      g.drawLine(x, y, x + dxEndSpine, y + dyEndSpine);
      x += dx;
      y += dy;
    } */
    GraphicsUtil.switchToWidth(g, SPINE_WIDTH);
    g.setColor(Value.multiColor);
    var spine0x = x0 + parms.getSpine0X();
    var spine0y = y0 + parms.getSpine0Y();
    var spine1x = x0 + parms.getSpine1X();
    var spine1y = y0 + parms.getSpine1Y();
    if (spine0x == spine1x && spine0y == spine1y) { // centered
      final var fanout = 2;
      spine0x = x0 + parms.getEnd0X() + parms.getEndToSpineDeltaX();
      spine0y = y0 + parms.getEnd0Y() + parms.getEndToSpineDeltaY();
      spine1x = spine0x + (fanout - 1) * parms.getEndToEndDeltaX();
      spine1y = spine0y + (fanout - 1) * parms.getEndToEndDeltaY();
      if (parms.getEndToEndDeltaX() == 0) { // vertical spine
        if (spine0y < spine1y) {
          spine0y++;
          spine1y--;
        } else {
          spine0y--;
          spine1y++;
        }
        g.drawLine(x0 + parms.getSpine1X() / 4, y0, spine0x, y0);
      } else {
        if (spine0x < spine1x) {
          spine0x++;
          spine1x--;
        } else {
          spine0x--;
          spine1x++;
        }
        g.drawLine(x0, y0 + parms.getSpine1Y() / 4, x0, spine0y);
      }
      if (fanout <= 1) { // spine is empty
        int diam = SPINE_DOT;
        g.fillOval(spine0x - diam / 2, spine0y - diam / 2, diam, diam);
      } else {
        g.drawLine(spine0x, spine0y, spine1x, spine1y);
      }
    } else {
      int[] xSpine = {spine0x, spine1x, x0 + parms.getSpine1X() / 4};
      int[] ySpine = {spine0y, spine1y, y0 + parms.getSpine1Y() / 4};
      g.drawPolyline(xSpine, ySpine, 3);
    }
    g.setColor(oldColor);
  }

  private static final int SPINE_WIDTH = Wire.WIDTH + 2;

  private static final int SPINE_DOT = Wire.WIDTH + 4;
}
