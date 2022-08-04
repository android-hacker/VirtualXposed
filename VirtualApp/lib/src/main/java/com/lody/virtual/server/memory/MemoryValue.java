package com.lody.virtual.server.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Lody
 */
public abstract class MemoryValue {
    
    private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    public enum ValueType {
        INT2, // short
        INT4, // int
        INT8, // long
    }

    public abstract byte[] toBytes();


    public static class INT2 extends MemoryValue {

        private short val;

        public INT2(short val) {
            this.val = val;
        }

        @Override
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            return buffer.putShort(val).order(BYTE_ORDER).array();
        }
    }

    public static class INT4 extends MemoryValue {

        private int val;

        public INT4(int val) {
            this.val = val;
        }

        @Override
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            return buffer.order(BYTE_ORDER).putInt(val).array();
        }
    }

    public static class INT8 extends MemoryValue {

        private long val;

        public INT8(long val) {
            this.val = val;
        }

        @Override
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            return buffer.order(BYTE_ORDER).putLong(val).array();
        }
    }

}
