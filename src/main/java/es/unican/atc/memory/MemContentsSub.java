/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package es.unican.atc;

import java.util.Arrays;

class MemContentsSub {
    private MemContentsSub() { }

    static ContentsInterface createContents(int size, int bits) {
        if (bits <= 8)       return new ByteContents(size);
        else if (bits <= 16) return new ShortContents(size);
        else                return new IntContents(size);
    }

    static abstract class ContentsInterface implements Cloneable {
        @Override
        public ContentsInterface clone() {
            try {
                return (ContentsInterface) super.clone();
            } catch (CloneNotSupportedException e) {
                return this;
            }
        }
        abstract int getLength();
        abstract long get(int addr);
        abstract void set(int addr, long value);
        abstract void clear();
        abstract void load(int start, long[] values, int mask);

        boolean matches(long[] values, int start, int mask) {
            for (int i = 0; i < values.length; i++) {
                if (get(start + i) != (values[i] & mask)) return false;
            }
            return true;
        }

        long[] get(int start, int len) {
            long[] ret = new long[len];
            for (int i = 0; i < ret.length; i++) ret[i] = get(start + i);
            return ret;
        }

        boolean isClear() {
            for (int i = 0, n = getLength(); i < n; i++) {
                if (get(i) != 0) return false;
            }
            return true;
        }
    }

    private static class ByteContents extends ContentsInterface {
        private byte[] data;

        public ByteContents(int size) {
            data = new byte[size];
        }

        @Override
        public ByteContents clone() {
            ByteContents ret = (ByteContents) super.clone();
            ret.data = new byte[this.data.length];
            System.arraycopy(this.data, 0, ret.data, 0, this.data.length);
            return ret;
        }

        //
        // methods for accessing data within memory
        //
        @Override
        int getLength() {
            return data.length;
        }

        @Override
        long get(int addr) {
            return addr >= 0 && addr < data.length ? data[addr] : 0;
        }

        @Override
        void set(int addr, long value) {
            if (addr >= 0 && addr < data.length) {
                byte oldValue = data[addr];
                if (value != oldValue) {
                    data[addr] = (byte) value;
                }
            }
        }

        @Override
        void clear() {
            Arrays.fill(data, (byte) 0);
        }

        @Override
        void load(int start, long[] values, int mask) {
            int n = Math.min(values.length, data.length - start);
            for (int i = 0; i < n; i++) {
                data[start + i] = (byte) (values[i] & mask);
            }
        }
    }

    private static class ShortContents extends ContentsInterface {
        private short[] data;

        public ShortContents(int size) {
            data = new short[size];
        }

        @Override
        public ShortContents clone() {
            ShortContents ret = (ShortContents) super.clone();
            ret.data = new short[this.data.length];
            System.arraycopy(this.data, 0, ret.data, 0, this.data.length);
            return ret;
        }

        //
        // methods for accessing data within memory
        //
        @Override
        int getLength() {
            return data.length;
        }

        @Override
        long get(int addr) {
            return addr >= 0 && addr < data.length ? data[addr] : 0;
        }

        @Override
        void set(int addr, long value) {
            if (addr >= 0 && addr < data.length) {
                short oldValue = data[addr];
                if (value != oldValue) {
                    data[addr] = (short) value;
                }
            }
        }

        @Override
        void clear() {
            Arrays.fill(data, (short) 0);
        }

        @Override
        void load(int start, long[] values, int mask) {
            int n = Math.min(values.length, data.length - start);
            for (int i = 0; i < n; i++) {
                data[start + i] = (short) (values[i] & mask);
            }
        }
    }

    private static class IntContents extends ContentsInterface {
        private long[] data;

        public IntContents(int size) {
            data = new long[size];
        }

        @Override
        public IntContents clone() {
            IntContents ret = (IntContents) super.clone();
            ret.data = new long[this.data.length];
            System.arraycopy(this.data, 0, ret.data, 0, this.data.length);
            return ret;
        }

        //
        // methods for accessing data within memory
        //
        @Override
        int getLength() {
            return data.length;
        }

        @Override
        long get(int addr) {
            return addr >= 0 && addr < data.length ? data[addr] : 0;
        }

        @Override
        void set(int addr, long value) {
            if (addr >= 0 && addr < data.length) {
                long oldValue = data[addr];
                if (value != oldValue) {
                    data[addr] = value;
                }
            }
        }

        @Override
        void clear() {
            Arrays.fill(data, 0);
        }

        @Override
        void load(int start, long[] values, int mask) {
            int n = Math.min(values.length, data.length - start);
            for (int i = 0; i < n; i++) {
                data[i] = values[i] & mask;
            }
        }
    }
}
