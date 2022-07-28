package com.github.puregero.minecraftstresstest;

import com.github.puregero.minecraftstresstest.packets.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketDataEncoder extends MessageToByteEncoder<Packet>{

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet msg, final ByteBuf out) throws Exception {
        msg.writeBytesTo(out);
    }

}
