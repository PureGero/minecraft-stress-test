package com.github.puregero.minecraftstresstest.packets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import io.netty.buffer.ByteBufAllocator;

public class HandshakePacketTests {

    private static final int PROTOCOL_VERSION = 759;

    @Test
    public void should_write_the_expected_bytes() {
        // Given a buffer and a HandshakePacket
        var buffer = ByteBufAllocator.DEFAULT.heapBuffer(13);
        var target = new HandshakePacket(
            PROTOCOL_VERSION,
            "address",
            (short) 65535,
            HandshakePacket.NextState.Login
        );

        // When the HandshakePacket is written to the buffer
        target.writeBytesTo(buffer);

        // And the result is truncated (trailing unwritten bytes in the buffer are ignored)
        var result = new byte[buffer.writerIndex()];
        buffer.getBytes(0, result, 0, buffer.writerIndex());

        // Then the result contains the expected bytes
        assertArrayEquals(
            new byte[] {
                (byte) 0x00,                                        // Handshake Packet ID
                (byte) 0xf7, (byte) 0x05,                           // 759 encoded as an unsigned LEB128
                (byte) 0x61, (byte) 0x64, (byte) 0x64, (byte) 0x72, // address in UTF-8
                             (byte) 0x65, (byte) 0x73, (byte) 0x73,
                (byte) 0xff, (byte) 0xff,                           // port as a 16-bit integer
                (byte) 0x02                                         // Next state - Login
            },
            result
        );
    }

    @Test
    public void should_throw_an_exception_when_given_an_address_with_more_than_32767_characters() {
        // Given a string with more than 32767 characters
        var chars = new char[32768];
        Arrays.fill(chars, 'a');
        var string = new String(chars);

        // When a HandshakePacket is instantiated with that string for the address
        Executable action = () -> {
            new HandshakePacket(
                PROTOCOL_VERSION,
                string,
                (short) 65535,
                HandshakePacket.NextState.Login
            );
        };

        // Then an IllegalArgumentException is thrown
        var exception = assertThrows(IllegalArgumentException.class, action);

        // And the exception message contains useful information
        var message = exception.getMessage();
        assertTrue(message.contains("Address is too long"));
        assertTrue(message.contains(string));
    }
}
