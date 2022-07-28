package com.github.puregero.minecraftstresstest.packets;

import io.netty.buffer.ByteBuf;

public interface Packet {
    void writeBytesTo(ByteBuf buffer);
}
