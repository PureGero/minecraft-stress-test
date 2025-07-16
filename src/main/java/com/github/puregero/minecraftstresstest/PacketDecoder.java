package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int i = byteBuf.readableBytes();
        if (i >= 3) {
            FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
            friendlyByteBuf.markReaderIndex();

            int length = friendlyByteBuf.readVarInt();

            if (friendlyByteBuf.readableBytes() < length) {
                friendlyByteBuf.resetReaderIndex();
                return;
            }

            list.add(friendlyByteBuf.readBytes(length));
        }
    }
}
