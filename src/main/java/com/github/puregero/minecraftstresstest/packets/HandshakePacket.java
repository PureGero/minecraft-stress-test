package com.github.puregero.minecraftstresstest.packets;

import java.nio.charset.StandardCharsets;

import com.github.puregero.minecraftstresstest.packets.datatypes.UnsignedVarInt;

import io.netty.buffer.ByteBuf;

public final class HandshakePacket implements Packet {
    private static final byte HANDSHAKE_PACKET_ID = (byte) 0x00;

    private final UnsignedVarInt protocalVersion;
    private final String address;
    private final short port;
    private final NextState nextState;

    public HandshakePacket(
        final int protocalVersion,
        final String address,
        final short port,
        final NextState nextState
    ) {
        if (address.length() > 32767) {
            throw new IllegalArgumentException(
                String.format("Address is too long: %s", address)
            );
        }

        this.protocalVersion = new UnsignedVarInt(protocalVersion);
        this.address = address;
        this.port = port;
        this.nextState = nextState;
    }

    @Override
    public void writeBytesTo(final ByteBuf buffer) {
        buffer.writeByte(HANDSHAKE_PACKET_ID);
        this.protocalVersion.writeBytesTo(buffer);
        buffer.writeBytes(this.address.getBytes(StandardCharsets.UTF_8));
        buffer.writeShort(this.port);
        buffer.writeByte(this.nextState.code());
    }

    public static enum NextState {
        Status((byte) 0x01),
        Login((byte) 0x02);

        private final byte code;

        NextState(final byte code) {
            this.code = code;
        }

        public byte code() {
            return this.code;
        }
    }
}
