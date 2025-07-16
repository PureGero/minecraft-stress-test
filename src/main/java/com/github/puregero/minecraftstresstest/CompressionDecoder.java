package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
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

            ByteBuffer bs;
            if (friendlyByteBuf.nioBufferCount() > 0) {
                bs = friendlyByteBuf.nioBuffer();
                friendlyByteBuf.skipBytes(friendlyByteBuf.readableBytes());
            } else {
                bs = ByteBuffer.allocateDirect(friendlyByteBuf.readableBytes());
                friendlyByteBuf.readBytes(bs);
                bs.flip();
            }
            this.inflater.setInput(bs);

            ByteBuf cs = channelHandlerContext.alloc().directBuffer(length);
            ByteBuffer csInternal = cs.internalNioBuffer(0, length);
            int startPos = csInternal.position();
            this.inflater.inflate(csInternal);
            int csLength = csInternal.position() - startPos;
            if (csLength != length) {
                throw new IllegalStateException("Decompressed length " + csLength + " does not match expected length " + length);
            }
            cs.writerIndex(cs.writerIndex() + csLength);

            this.inflater.reset();
            list.add(cs);
        }
    }
}
