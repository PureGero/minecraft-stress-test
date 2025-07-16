package com.github.puregero.minecraftstresstest;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

import static com.github.puregero.minecraftstresstest.Bot.PROTOCOL_VERSION;

public class BotAI {

    private Bot mybot;
    private UUID[][] myMemory = new UUID[1000][1000];
    private int memoryX;
    private int memoryZ;

    private double x = 0;
    private double y = 0;
    private double z = 0;
    private float yaw = (float) (Math.random() * 360);

    private boolean goUp = false;
    private boolean goDown = false;
    private boolean isSpawned = false;


    BotAI(Bot mybot) {
        this.mybot = mybot;
    }

    public void resetMemory() {
        myMemory = new UUID[1000][1000];
    }


    public void readOtherPackets(int packetId, FriendlyByteBuf byteBuf, ChannelHandlerContext ctx) {
    }


    public int storePosition(FriendlyByteBuf byteBuf) {

        //check memory Status to decide
        int id = 0;

        if (PROTOCOL_VERSION >= 769) {
            id = byteBuf.readVarInt();
        }

        double dx = byteBuf.readDouble();
        double dy = byteBuf.readDouble();
        double dz = byteBuf.readDouble();

        memoryX = (int) dx;
        memoryZ = (int) dz;

        if (PROTOCOL_VERSION >= 769) {
            double spx = byteBuf.readDouble();
            double spy = byteBuf.readDouble();
            double spz = byteBuf.readDouble();
        }

        float dyaw = byteBuf.readFloat();
        float dpitch = byteBuf.readFloat();
        byte flags = byteBuf.readByte();

        if (PROTOCOL_VERSION < 769) {
            id = byteBuf.readVarInt();
        }

        x = (flags & 0x01) == 0x01 ? x + dx : dx;
        y = (flags & 0x02) == 0x02 ? y + dy : dy;
        z = (flags & 0x04) == 0x04 ? z + dz : dz;

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

        mybot.isSpawned = true;
        return id;
    }


    public void decideAction(ChannelHandlerContext ctx) {
        if (!mybot.Y_AXIS && (goUp || goDown)) {
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
            if (Math.max(Math.abs(x - mybot.CENTER_X), Math.abs(z - mybot.CENTER_Z)) > mybot.RADIUS) {
                double tx = Math.random() * mybot.RADIUS * 2 - mybot.RADIUS + mybot.CENTER_X;
                double tz = Math.random() * mybot.RADIUS * 2 - mybot.RADIUS + mybot.CENTER_Z;

                yaw = (float) Math.toDegrees(Math.atan2(x - tx, tz - z));
            }

            x += mybot.SPEED * -Math.sin(Math.toRadians(yaw));
            z += mybot.SPEED * Math.cos(Math.toRadians(yaw));
        }

        if (mybot.Y_AXIS) {
            y -= mybot.SPEED / 10;
        }

        mybot.sendPacket(ctx, PacketIds.Serverbound.Play.SET_PLAYER_POSITION_AND_ROTATION, buffer -> {
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
            buffer.writeFloat(yaw);
            buffer.writeFloat(0);
            buffer.writeBoolean(true);
        });
    }

}
