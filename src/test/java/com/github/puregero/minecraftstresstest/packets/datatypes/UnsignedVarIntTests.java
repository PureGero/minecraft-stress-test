package com.github.puregero.minecraftstresstest.packets.datatypes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBufAllocator;

public class UnsignedVarIntTests {

    @Test
    public void should_write_0_as_0x00() {
        // Given a buffer and an UnsignedVarInt of value 0
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(0);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x00 }, result);
    }

    @Test
    public void should_write_1_as_0x01() {
        // Given a buffer and an UnsignedVarInt of value 1
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(1);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x01 }, result);
    }

    @Test
    public void should_write_2_as_0x02() {
        // Given a buffer and an UnsignedVarInt of value 2
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(2);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x02 }, result);
    }

    @Test
    public void should_write_127_as_0x7f() {
        // Given a buffer and an UnsignedVarInt of value 127
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(127);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x7f }, result);
    }

    @Test
    public void should_write_128_as_0x80_0x01() {
        // Given a buffer and an UnsignedVarInt of value 128
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(128);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x80, (byte) 0x01 }, result);
    }

    @Test
    public void should_write_255_as_0xff_0x01() {
        // Given a buffer and an UnsignedVarInt of value 255
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(255);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0xff, (byte) 0x01 }, result);
    }

    @Test
    public void should_write_25565_as_0xdd_0xc7_0x01() {
        // Given a buffer and an UnsignedVarInt of value 25565
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(25565);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0xdd, (byte) 0xc7, (byte) 0x01 }, result);
    }

    @Test
    public void should_write_2097151_as_0xff_0xff_0x7f() {
        // Given a buffer and an UnsignedVarInt of value 2097151
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(2097151);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0xff, (byte) 0xff, (byte) 0x7f }, result);
    }

    @Test
    public void should_write_2147483647_as_0xff_0xff_0xff_0xff_0x07() {
        // Given a buffer and an UnsignedVarInt of value 2147483647
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(2147483647);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x07 }, result);
    }

    @Test
    public void should_write_negative_1_as_0xff_0xff_0xff_0xff_0x0f() {
        // Given a buffer and an UnsignedVarInt of value -1
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(-1);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x0f }, result);
    }

    @Test
    public void should_write_negative_2147483648_as_0x80_0x80_0x80_0x80_0x08() {
        // Given a buffer and an UnsignedVarInt of value -2147483648
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(5);
        var target = new UnsignedVarInt(-2147483648);

        // When the UnsignedVarInt writes to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(new byte[]{ (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x08 }, result);
    }
}
