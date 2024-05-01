package com.github.puregero.minecraftstresstest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class Bot extends ChannelInboundHandlerAdapter {
    private static final int PROTOCOL_VERSION = Integer.parseInt(System.getProperty("bot.protocol.version", "766")); // 761 is 1.19.3 https://wiki.vg/Protocol_version_numbers
    private static final double CENTER_X = Double.parseDouble(System.getProperty("bot.x", "0"));
    private static final double CENTER_Z = Double.parseDouble(System.getProperty("bot.z", "0"));
    private static final boolean LOGS = Boolean.parseBoolean(System.getProperty("bot.logs", "true"));
    private static final boolean Y_AXIS = Boolean.parseBoolean(System.getProperty("bot.yaxis", "true"));
    private static final int VIEW_DISTANCE = Integer.parseInt(System.getProperty("bot.viewdistance", "2"));
    private static final int RESOURCE_PACK_RESPONSE = Integer.parseInt(System.getProperty("bot.resource.pack.response", "3"));

    private static final Executor ONE_TICK_DELAY = CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS);

    public static final String DEFAULT_SPEED = "0.1";
    public static double SPEED = Double.parseDouble(System.getProperty("bot.speed", DEFAULT_SPEED));
    public static final String DEFAULT_RADIUS = "1000";
    public static double RADIUS = Double.parseDouble(System.getProperty("bot.radius", DEFAULT_RADIUS));

    public SocketChannel channel;
    private String username;
    private final String address;
    private final int port;
    private UUID uuid;
    private boolean loginState = true;
    private boolean configState = false;
    private boolean playState = false;

    private double x = 0;
    private double y = 0;
    private double z = 0;
    private float yaw = (float) (Math.random() * 360);

    private boolean goUp = false;
    private boolean goDown = false;
    private boolean isSpawned = false;

    public Bot(String username, String address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        sendPacket(ctx, PacketIds.Serverbound.Handshaking.HANDSHAKE, buffer -> {
            buffer.writeVarInt(PROTOCOL_VERSION);
            buffer.writeUtf(address);
            buffer.writeShort(port);
            buffer.writeVarInt(2);
        });

        sendPacket(ctx, PacketIds.Serverbound.Login.LOGIN_START, buffer -> {
            buffer.writeUtf(username);
            buffer.writeUUID(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)));
        });
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

            } else if (configState) {
                channelReadConfig(ctx, byteBuf);

            } else if (playState) {
                channelReadPlay(ctx, byteBuf);
            }
        } finally {
            ((ByteBuf) msg).release();
        }
    }


    private void channelReadLogin(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        int packetId = byteBuf.readVarInt();

        if (packetId == PacketIds.Clientbound.Login.DISCONNECT) {
            System.out.println(username + " was disconnected during login due to " + byteBuf.readUtf());
            ctx.close();

        } else if (packetId == PacketIds.Clientbound.Login.ENCRYPTION_REQUEST) {
            System.out.println("Server requesting for ENCRYPTION_REQUEST, so it is on ONLINEMODE, disconnecting");
            ctx.close();

        } else if (packetId == PacketIds.Clientbound.Login.LOGIN_SUCCESS) {

            if (PROTOCOL_VERSION >= 764) {
                sendPacket(ctx, PacketIds.Serverbound.Login.LOGIN_ACKNOWLEDGED, buffer -> {
                });
            }

            loggedIn(ctx, byteBuf);

        } else if (packetId == PacketIds.Clientbound.Login.SET_COMPRESSION) {
            byteBuf.readVarInt();
            ctx.pipeline().addAfter("packetDecoder", "compressionDecoder", new CompressionDecoder());
            ctx.pipeline().addAfter("packetEncoder", "compressionEncoder", new CompressionEncoder());
        } else {
            throw new RuntimeException("Unknown login packet id of " + packetId);
        }
    }


    private void loggedIn(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        UUID uuid = byteBuf.readUUID();
        String username = byteBuf.readUtf();
        int numberElements = byteBuf.readVarInt(); //number of elements after this position
        boolean isSigned = false;

        if (numberElements > 0) {
            try {
                byteBuf.readUtf(); //name
                byteBuf.readUtf(); //value
                isSigned = byteBuf.readBoolean(); //issigned
            } catch (Exception e) {
            }
        }

        this.uuid = uuid;
        this.username = username;

        if (isSigned) {
            System.out.println(username + " (" + uuid + ") has logged in on an ONLINEMODE server, stopping");
            ctx.close();
            return;
        } else
            System.out.println(username + " (" + uuid + ") has logged in");

        loginState = false;
        configState = true;
        //System.out.println("changing to config mode");

        CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS).execute(() -> {
            sendPacket(ctx, PacketIds.Serverbound.Configuration.CLIENT_INFORMATION, buffer -> {
                buffer.writeUtf("en_GB");
                buffer.writeByte(VIEW_DISTANCE);
                buffer.writeVarInt(0);
                buffer.writeBoolean(true);
                buffer.writeByte(0);
                buffer.writeVarInt(0);
                buffer.writeBoolean(false);
                buffer.writeBoolean(true);
            });

            sendPacket(ctx, PacketIds.Serverbound.Configuration.KNOWN_PACKS, buffer -> {
                buffer.writeVarInt(0);
            });

            CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS).execute(() -> tick(ctx));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private void tick(ChannelHandlerContext ctx) {

        if (!ctx.channel().isActive()) return;

        ONE_TICK_DELAY.execute(() -> tick(ctx));

        if (!isSpawned) return; // Don't tick until we've spawned in

        if (!Y_AXIS && (goUp || goDown)) {
            goDown = goUp = false;
            if (Math.random() < 0.1) yaw = (float) (Math.random() * 360);
        }

        if (goUp) {
            y += 0.1;
            goUp = Math.random() < 0.98;
        } else if (goDown) {
            y -= 0.1;
            goDown = Math.random() < 0.98;
        } else {
            if (Math.max(Math.abs(x - CENTER_X), Math.abs(z - CENTER_Z)) > RADIUS) {
                double tx = Math.random() * RADIUS * 2 - RADIUS + CENTER_X;
                double tz = Math.random() * RADIUS * 2 - RADIUS + CENTER_Z;

                yaw = (float) Math.toDegrees(Math.atan2(x - tx, tz - z));
            }

            x += SPEED * -Math.sin(Math.toRadians(yaw));
            z += SPEED * Math.cos(Math.toRadians(yaw));
        }

        if (Y_AXIS) {
            y -= SPEED / 10;
        }

        sendPacket(ctx, PacketIds.Serverbound.Play.SET_PLAYER_POSITION_AND_ROTATION, buffer -> {
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
            buffer.writeFloat(yaw);
            buffer.writeFloat(0);
            buffer.writeBoolean(true);
        });
    }


    private void channelReadConfig(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        int packetId = byteBuf.readVarInt();

        if (packetId == PacketIds.Clientbound.Configuration.DISCONNECT) {
            System.out.println(username + " (" + uuid + ") (config) was kicked due to " + byteBuf.readUtf());
            ctx.close();

        } else if (packetId == PacketIds.Clientbound.Configuration.FINISH_CONFIGURATION) {

            sendPacket(ctx, PacketIds.Serverbound.Configuration.FINISH_CONFIGURATION, buffer -> {
            });

            configState = false;
            playState = true;
            //System.out.println("changing to play mode");

        } else if (packetId == PacketIds.Clientbound.Configuration.KEEP_ALIVE) {
            long id = byteBuf.readLong();
            sendPacket(ctx, PacketIds.Serverbound.Configuration.KEEP_ALIVE, buffer -> buffer.writeLong(id));
            //System.out.println(username + " (" + uuid + ") keep alive config mode");

        } else if (packetId == PacketIds.Clientbound.Configuration.PING) {
            int id = byteBuf.readInt();
            sendPacket(ctx, PacketIds.Serverbound.Configuration.PONG, buffer -> buffer.writeInt(id));
            //System.out.println(username + " (" + uuid + ") ping config mode");

        }
    }


    private void channelReadPlay(ChannelHandlerContext ctx, FriendlyByteBuf byteBuf) {
        int packetId = byteBuf.readVarInt();

        if (packetId == PacketIds.Clientbound.Play.DISCONNECT) {
            System.out.println(username + " (" + uuid + ") was kicked due to " + byteBuf.readUtf());
            ctx.close();
            loginState = true;
            playState = false;

        } else if (packetId == PacketIds.Clientbound.Play.KEEP_ALIVE) {
            long id = byteBuf.readLong();
            sendPacket(ctx, PacketIds.Serverbound.Play.KEEP_ALIVE, buffer -> buffer.writeLong(id));

        } else if (packetId == PacketIds.Clientbound.Play.PING) {
            int id = byteBuf.readInt();
            sendPacket(ctx, PacketIds.Serverbound.Play.PONG, buffer -> buffer.writeInt(id));

        } else if (packetId == PacketIds.Clientbound.Play.SYNCHRONIZE_PLAYER_POSITION) {

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
            if (goDown) {
                goDown = false;
            } else if (!goUp) {
                goUp = true;
            } else {
                // We hit our head on something
                goUp = false;
                goDown = Math.random() < 0.5;
                if (!goDown) yaw = (float) (Math.random() * 360);
            }

            sendPacket(ctx, PacketIds.Serverbound.Play.CONFIRM_TELEPORTATION, buffer -> buffer.writeVarInt(id));

            isSpawned = true;

        } else if (packetId == PacketIds.Clientbound.Play.RESOURCE_PACK) {

            String url = byteBuf.readUtf();
            String hash = byteBuf.readUtf();
            boolean forced = byteBuf.readBoolean();
            String message = null;
            if (byteBuf.readBoolean()) message = byteBuf.readUtf();
            System.out.println("Resource pack info:\n" + url + "\n" + hash + "\n" + forced + "\n" + message);

            sendPacket(ctx, PacketIds.Serverbound.Play.RESOURCE_PACK, buffer -> buffer.writeVarInt(RESOURCE_PACK_RESPONSE));

        } else if (packetId == PacketIds.Clientbound.Play.SET_HEALTH) {

            float health = byteBuf.readFloat();

            if (health <= 0) {
                sendPacket(ctx, PacketIds.Serverbound.Play.CLIENT_RESPAWN, buffer -> buffer.writeVarInt(0));
            }
        }
    }


    public void close() {
        channel.close();
    }


    public void sendPacket(ChannelHandlerContext ctx, int packetId, Consumer<FriendlyByteBuf> applyToBuffer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(ctx.alloc().buffer());
        buffer.writeVarInt(packetId);
        applyToBuffer.accept(buffer);
        ctx.writeAndFlush(buffer);
    }
}