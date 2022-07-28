package com.github.puregero.minecraftstresstest.packets.datatypes;

import io.netty.buffer.ByteBuf;

public class UnsignedVarInt {
    private static final int SEGMENT_BITS = 0x7f;
    private static final int CONTINUE_BIT = 0x80;

    private final int number;

    public UnsignedVarInt(final int number) {
        this.number = number;
    }

    public void writeBytesTo(final ByteBuf buffer) {
        int workingCopy = this.number;
        while (true) {
            final byte b = (byte) (workingCopy & SEGMENT_BITS);
            workingCopy >>>= 7;
            if (workingCopy == 0) {
                buffer.writeByte(b);
                break;
            } else {
                buffer.writeByte((byte) (b | CONTINUE_BIT));
            }
        }
    }
}
