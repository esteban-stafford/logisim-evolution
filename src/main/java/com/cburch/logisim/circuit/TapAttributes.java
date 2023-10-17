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

import com.cburch.logisim.data.AbstractAttributeSet;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.gui.generic.ComboBox;
import com.cburch.logisim.instance.StdAttr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TapAttributes extends AbstractAttributeSet {

  public static final Attribute<Integer> ATTR_FROM = Attributes.forIntegerRange("from", S.getter("tapFrom"),0,31);
  public static final Attribute<Integer> ATTR_TO = Attributes.forIntegerRange("to", S.getter("tapTo"),0,31);

  public static final Attribute<BitWidth> ATTR_WIDTH = Attributes.forBitWidth("incoming", S.getter("splitterBitWidthAttr"));

  private static final List<Attribute<?>> INIT_ATTRIBUTES = Arrays.asList(StdAttr.FACING, ATTR_WIDTH, ATTR_FROM, ATTR_TO);

  private static final String UNCHOSEN_VAL = "none";
  private ArrayList<Attribute<?>> attrs = new ArrayList<>(INIT_ATTRIBUTES);
  Direction facing = Direction.EAST;
  int from = 0;
  int to = 0;
  int width = 1;

  public boolean isNoConnect(int index) {
    if (index == 0)
      return false;
    return true;
  }

  @Override
  protected void copyInto(AbstractAttributeSet destObj) {
    final var dest = (TapAttributes) destObj;
    dest.attrs = new ArrayList<>(this.attrs.size());
    dest.attrs.addAll(INIT_ATTRIBUTES);

    dest.facing = this.facing;
    dest.from = this.from;
    dest.to = this.to;
    dest.width = this.width;
  }

  @Override
  public List<Attribute<?>> getAttributes() {
    return attrs;
  }

  /*Attribute<?> getBitOutAttribute(int index) {
    return attrs.get(INIT_ATTRIBUTES.size() + index);
  }*/

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(Attribute<V> attr) {
    if (attr == StdAttr.FACING) {
      return (V) facing;
    } else if (attr == ATTR_WIDTH) {
      return (V) BitWidth.create(width);
    } else if (attr == ATTR_FROM) {
      return (V) Integer.valueOf(from);
    } else if (attr == ATTR_TO) {
      return (V) Integer.valueOf(to);
    } else {
      return null;
    }
  }

  @Override
  public <V> void setValue(Attribute<V> attr, V value) {
    if (attr == StdAttr.FACING) {
      final var newFacing = (Direction) value;
      if (facing.equals(newFacing)) return;
      facing = (Direction) value;
    } else if (attr == ATTR_WIDTH) {
      final var newWidth = (BitWidth) value;
      if (newWidth.getWidth() == width) return;
      width = newWidth.getWidth();
      from = Math.min(from, width - 1);
      to = Math.min(to, width - 1);
    } else if (attr == ATTR_FROM) {
      var newFrom = (int) value;
      newFrom = Math.min(newFrom, width - 1);
      if (newFrom == from) return;
      to = Math.min(to + newFrom - from, width - 1);
      from = newFrom;
    } else if (attr == ATTR_TO) {
      var newTo = (int) value;
      newTo = Math.min(newTo, width - 1);
      if (newTo == to) return;
      to = newTo;
    } else {
      throw new IllegalArgumentException("unknown attribute " + attr);
    }
    fireAttributeValueChanged(attr, value, null);
  }

  @Override
  public <V> List<Attribute<?>> attributesMayAlsoBeChanged(Attribute<V> attr, V value) {
    if (attr != ATTR_WIDTH) {
      return null;
    }
    if (Objects.equals(getValue(attr), value)) {
      return null;
    }

    final var answer = new ArrayList<Attribute<?>>(1);
    /*for (int index = 0; index < bitEnd.length; index++) {
      answer.add(getBitOutAttribute(index));
    }*/
    return answer;
  }
}
