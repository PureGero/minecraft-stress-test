package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBufDest) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBufDest);
        friendlyByteBuf.writeVarInt(0);
        friendlyByteBuf.writeBytes(byteBuf);

    }
}
