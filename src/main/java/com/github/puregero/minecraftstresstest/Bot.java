package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class Bot extends ChannelInboundHandlerAdapter {
    private static final int PROTOCOL_VERSION = Integer.parseInt(System.getProperty("bot.protocol.version", "757")); // 757 is 1.18
    private static final double RADIUS = Double.parseDouble(System.getProperty("bot.radius", "1000"));
    private static final boolean LOGS = Boolean.parseBoolean(System.getProperty("bot.logs", "true"));

    private static final Executor ONE_TICK_DELAY = CompletableFuture.delayedExecutor(50,TimeUnit.MILLISECONDS);

    public static double SPEED = 0.2;

    public SocketChannel channel;
    private String username;
    private final String address;
    private final int port;
    private UUID uuid;
    private boolean loginState = true;

    private double x = 0;
    private double y = 0;
    private double z = 0;
    private float yaw = 0;

    private boolean goUp = false;

    public Bot(String username, String address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        FriendlyByteBuf handshakePacket = new FriendlyByteBuf(ctx.alloc().buffer());
        handshakePacket.writeVarInt(0x00);
        handshakePacket.writeVarInt(PROTOCOL_VERSION);
        handshakePacket.writeUtf(address);
        handshakePacket.writeShort(port);
        handshakePacket.writeVarInt(2);
        ctx.write(handshakePacket);

        FriendlyByteBuf loginStartPacket = new FriendlyByteBuf(ctx.alloc().buffer());
        loginStartPacket.writeVarInt(0x00);
        loginStartPacket.writeUtf(username);
        ctx.writeAndFlush(loginStartPacket);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        if (uuid != null) {
            System.out.println(username + " has disconnected from " + address + ":" + port);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FriendlyByteBuf byteBuf = new FriendlyByteBuf((ByteBuf) msg);
            if (loginState) {
                channelReadLogin(ctx, byteBuf);
            } else {
                channelReadPlay(ctx, byteBuf);
            }
        } finally {
            ((ByteBuf) msg).release();
        }
    }

    private void channelReadLogin(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        int packetId = byteBuf.readVarInt();

        if (packetId == 0) {
            System.out.println(username + " was disconnected during login due to " + byteBuf.readUtf());
            ctx.close();
        } else if (packetId == 2) {
            UUID uuid = byteBuf.readUUID();
            String username = byteBuf.readUtf();
            loggedIn(ctx, uuid, username);
        } else if (packetId == 3) {
            byteBuf.readVarInt();
            ctx.pipeline().addAfter("packetDecoder", "compressionDecoder", new CompressionDecoder());
            ctx.pipeline().addAfter("packetEncoder", "compressionEncoder", new CompressionEncoder());
        } else {
            throw new RuntimeException("Unknown login packet id of " + packetId);
        }
    }

    private void loggedIn(ChannelHandlerContext ctx, UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        System.out.println(username + " (" + uuid + ") has logged in");
        loginState = false;

        FriendlyByteBuf settingsPacket = new FriendlyByteBuf(ctx.alloc().buffer());
        settingsPacket.writeVarInt(0x05);
        settingsPacket.writeUtf("en_GB");
        settingsPacket.writeByte(2);
        settingsPacket.writeVarInt(0);
        settingsPacket.writeBoolean(true);
        settingsPacket.writeByte(0);
        settingsPacket.writeVarInt(0);
        settingsPacket.writeBoolean(false);
        settingsPacket.writeBoolean(true);
        ctx.writeAndFlush(settingsPacket);

        CompletableFuture.delayedExecutor(1000,TimeUnit.MILLISECONDS).execute(() -> tick(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void tick(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive()) return;

        ONE_TICK_DELAY.execute(() -> tick(ctx));

        if (x == 0 && y == 0 && z == 0) return; // Don't tick until we've spawned in

        if (goUp) {
            y += 0.1;
            goUp = Math.random() < 0.98;
        } else {
            if (Math.max(Math.abs(x), Math.abs(z)) > RADIUS) {
                double tx = Math.random() * RADIUS * 2 - RADIUS;
                double tz = Math.random() * RADIUS * 2 - RADIUS;

                yaw = (float) Math.toDegrees(Math.atan2(x - tx, tz - z));
            }

            x += SPEED * -Math.sin(Math.toRadians(yaw));
            z += SPEED * Math.cos(Math.toRadians(yaw));
        }

        y -= SPEED / 20;

        FriendlyByteBuf movePacket = new FriendlyByteBuf(ctx.alloc().buffer());
        movePacket.writeVarInt(0x12);
        movePacket.writeDouble(x);
        movePacket.writeDouble(y);
        movePacket.writeDouble(z);
        movePacket.writeFloat(yaw);
        movePacket.writeFloat(0);
        movePacket.writeBoolean(true);
        ctx.writeAndFlush(movePacket);
    }

    private void channelReadPlay(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        int packetId = byteBuf.readVarInt();
//        System.out.println("id " + packetId + " (" + (dataLength == 0 ? length : dataLength) + ")");

        if (packetId == 0x1A) {
            System.out.println(username + " (" + uuid + ") was kicked due to " + byteBuf.readUtf());
            ctx.close();
        } else if (packetId == 0x21) {
            long id = byteBuf.readLong();

            FriendlyByteBuf keepAlivePacket = new FriendlyByteBuf(ctx.alloc().buffer());
            keepAlivePacket.writeVarInt(0x0F);
            keepAlivePacket.writeLong(id);
            ctx.writeAndFlush(keepAlivePacket);
        } else if (packetId == 0x38) {
            double dx = byteBuf.readDouble();
            double dy = byteBuf.readDouble();
            double dz = byteBuf.readDouble();
            float dyaw = byteBuf.readFloat();
            float dpitch = byteBuf.readFloat();
            byte flags = byteBuf.readByte();
            int id = byteBuf.readVarInt();

            x = (flags & 0x01) == 0x01 ? x + dx : dx;
            y = (flags & 0x02) == 0x02 ? y + dy : dy;
            z = (flags & 0x04) == 0x04 ? z + dz : dz;

            if (LOGS) {
                System.out.println("Teleporting " + username + " to " + x + "," + y + "," + z);
            }

            // Try going up to go over the block, or turn around and go a different way
            goUp = Math.random() < 0.5;
            if (!goUp) yaw = (float) (Math.random() * 360);

            FriendlyByteBuf teleportConfirmPacket = new FriendlyByteBuf(ctx.alloc().buffer());
            teleportConfirmPacket.writeVarInt(0x00);
            teleportConfirmPacket.writeVarInt(id);
            ctx.writeAndFlush(teleportConfirmPacket);
        }
    }

    public void close() {
        channel.close();
    }
}
