package com.github.puregero.minecraftstresstest;

import com.github.puregero.minecraftstresstest.packets.datatypes.UnsignedVarInt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketLengthEncoder extends MessageToByteEncoder<ByteBuf> {
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf inBuf, ByteBuf outBuf) {
        new UnsignedVarInt(inBuf.readableBytes())
            .writeBytesTo(outBuf);
        outBuf.writeBytes(inBuf);
    }
}
