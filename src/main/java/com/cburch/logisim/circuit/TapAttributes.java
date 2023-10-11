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
  public static class BitOutAttribute extends Attribute<Integer> {
    final int which;
    BitOutOption[] options;

    private BitOutAttribute(int which, BitOutOption[] options) {
      super("bit" + which, S.getter("splitterBitAttr", "" + which));
      this.which = which;
      this.options = options;
    }

    private BitOutAttribute createCopy() {
      return new BitOutAttribute(which, options);
    }

    public boolean sameOptions(BitOutAttribute other) {
      if (options.length != other.options.length) return false;
      for (final var a : options) {
        var found = false;
        for (final var b : other.options) {
          if (a.toString().equals(b.toString())) {
            found = true;
            break;
          }
        }
        if (!found) return false;
      }
      return true;
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public java.awt.Component getCellEditor(Integer value) {
      final var index = value;
      final var combo = new ComboBox<>(options);
      combo.setSelectedIndex(index);
      combo.setMaximumRowCount(options.length);
      return combo;
    }

    public Object getDefault() {
      return which + 1;
    }

    @Override
    public Integer parse(String value) {
      if (value.equals(UNCHOSEN_VAL)) {
        return 0;
      } else {
        return 1 + Integer.parseInt(value);
      }
    }

    @Override
    public String toDisplayString(Integer value) {
      return options[value].toString();
    }

    @Override
    public String toStandardString(Integer value) {
      final var index = value;
      if (index == 0) {
        return UNCHOSEN_VAL;
      } else {
        return "" + (index - 1);
      }
    }
  }

  private static class BitOutOption {
    final int value;
    final boolean isVertical;
    final boolean isLast;

    BitOutOption(int value, boolean isVertical, boolean isLast) {
      this.value = value;
      this.isVertical = isVertical;
      this.isLast = isLast;
    }

    @Override
    public String toString() {
      if (value < 0) {
        return S.get("splitterBitNone");
      } else {
        var ret = "" + value;
        Direction noteDir;
        if (value == 0) {
          noteDir = isVertical ? Direction.NORTH : Direction.EAST;
        } else if (isLast) {
          noteDir = isVertical ? Direction.SOUTH : Direction.WEST;
        } else {
          noteDir = null;
        }
        if (noteDir != null) {
          ret += " (" + noteDir.toVerticalDisplayString() + ")";
        }
        return ret;
      }
    }
  }


  public static final Attribute<Integer> ATTR_FROM = Attributes.forIntegerRange("from", S.getter("tapFrom"),0,31);
  public static final Attribute<Integer> ATTR_TO = Attributes.forIntegerRange("to", S.getter("tapTo"),0,31);

  public static final Attribute<BitWidth> ATTR_WIDTH = Attributes.forBitWidth("incoming", S.getter("splitterBitWidthAttr"));

  private static final List<Attribute<?>> INIT_ATTRIBUTES = Arrays.asList(StdAttr.FACING, ATTR_WIDTH, ATTR_FROM, ATTR_TO);

  private static final String UNCHOSEN_VAL = "none";
  private ArrayList<Attribute<?>> attrs = new ArrayList<>(INIT_ATTRIBUTES);
  private TapParameters parameters;
  Direction facing = Direction.EAST;
  int from = 0;
  int to = 0;
  byte[] bitEnd = new byte[2]; // how each bit maps to an end (0 if nowhere);

  // other values will be between 1 and fanout
  BitOutOption[] options = null;

  TapAttributes() {
    configureOptions();
    configureDefaults();
    parameters = new TapParameters(this);
  }

  public boolean isNoConnect(int index) {
    for (final var b : bitEnd) {
      if (b == index)
        return false;
    }
    return true;
  }

  private void configureDefaults() {
    final var offs = INIT_ATTRIBUTES.size();
    var curNum = attrs.size() - offs;

    // compute default values
    //final var dflt = computeDistribution(fanout, bitEnd.length, 1);

    var changed = curNum != bitEnd.length;

    // remove excess attributes
    while (curNum > bitEnd.length) {
      curNum--;
      attrs.remove(offs + curNum);
    }
    /*
    // set existing attributes
    for (var i = 0; i < curNum; i++) {
      if (bitEnd[i] != dflt[i]) {
        final var attr = (BitOutAttribute) attrs.get(offs + i);
        bitEnd[i] = dflt[i];
        fireAttributeValueChanged(attr, (int) bitEnd[i], null);
      }
    }

    // add new attributes
    for (var i = curNum; i < bitEnd.length; i++) {
      final var attr = new BitOutAttribute(i, options);
      bitEnd[i] = dflt[i];
      attrs.add(attr);
    }
    */

    if (changed) fireAttributeListChanged();
  }

  private void configureOptions() {
  /*  // compute the set of options for BitOutAttributes
    options = new BitOutOption[fanout + 1];
    var isVertical = facing == Direction.EAST || facing == Direction.WEST;
    for (var i = -1; i < fanout; i++) {
      options[i + 1] = new BitOutOption(i, isVertical, i == fanout - 1);
    }

    // go ahead and set the options for the existing attributes
    final var offs = INIT_ATTRIBUTES.size();
    final var curNum = attrs.size() - offs;
    for (var i = 0; i < curNum; i++) {
      final var attr = (BitOutAttribute) attrs.get(offs + i);
      attr.options = options;
    } */
  }

  @Override
  protected void copyInto(AbstractAttributeSet destObj) {
    final var dest = (TapAttributes) destObj;
    dest.parameters = this.parameters;
    dest.attrs = new ArrayList<>(this.attrs.size());
    dest.attrs.addAll(INIT_ATTRIBUTES);
    for (int i = INIT_ATTRIBUTES.size(), n = this.attrs.size(); i < n; i++) {
      final var attr = (BitOutAttribute) this.attrs.get(i);
      dest.attrs.add(attr.createCopy());
    }

    dest.facing = this.facing;
    dest.from = this.from;
    dest.to = this.to;
    dest.bitEnd = this.bitEnd.clone();
    dest.options = this.options;
  }

  @Override
  public List<Attribute<?>> getAttributes() {
    return attrs;
  }

  Attribute<?> getBitOutAttribute(int index) {
    return attrs.get(INIT_ATTRIBUTES.size() + index);
  }

  public TapParameters getParameters() {
    if (parameters == null) parameters = new TapParameters(this);
    return parameters;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(Attribute<V> attr) {
    if (attr == StdAttr.FACING) {
      return (V) facing;
    } else if (attr == ATTR_WIDTH) {
      return (V) BitWidth.create(bitEnd.length);
    } else if (attr == ATTR_FROM) {
      return (V) Integer.valueOf(from);
    } else if (attr == ATTR_TO) {
      return (V) Integer.valueOf(to);
    } else if (attr instanceof BitOutAttribute bitOut) {
      return (V) Integer.valueOf(bitEnd[bitOut.which]);
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
      configureOptions();
      parameters = null;
    } else if (attr == ATTR_WIDTH) {
      final var width = (BitWidth) value;
      if (bitEnd.length == width.getWidth()) return;
      bitEnd = new byte[width.getWidth()];
      configureOptions();
      configureDefaults();
    } else if (attr == ATTR_FROM) {
      final var newFrom = (int) value;
      if (newFrom == from) return;
      from = newFrom;
      configureOptions();
      //configureDefaults();
    } else if (attr == ATTR_TO) {
      final var newTo = (int) value;
      if (newTo == to) return;
      to = newTo;
      configureOptions();
      //configureDefaults();
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

    final var answer = new ArrayList<Attribute<?>>(bitEnd.length);
    for (int index = 0; index < bitEnd.length; index++) {
      answer.add(getBitOutAttribute(index));
    }
    return answer;
  }
}
