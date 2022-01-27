package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder {

    private final Inflater inflater = new Inflater();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() != 0) {
            FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
            int length = friendlyByteBuf.readVarInt();

            if (length == 0) {
                list.add(friendlyByteBuf.readBytes(friendlyByteBuf.readableBytes()));
                return;
            }

            byte[] bs = new byte[friendlyByteBuf.readableBytes()];
            friendlyByteBuf.readBytes(bs);
            this.inflater.setInput(bs);
            byte[] cs = new byte[length];
            this.inflater.inflate(cs);
            list.add(Unpooled.wrappedBuffer(cs));
            this.inflater.reset();
        }
    }
}
