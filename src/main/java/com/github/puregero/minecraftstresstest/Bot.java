package com.github.puregero.minecraftstresstest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.zip.InflaterInputStream;

public class Bot extends Thread {
    private static final int PROTOCOL_VERSION = Integer.parseInt(System.getProperty("bot.protocol.version", "757")); // 757 is 1.18
    private static final double RADIUS = Double.parseDouble(System.getProperty("bot.radius", "1000"));

    private static final Executor ONE_TICK_DELAY = CompletableFuture.delayedExecutor(50,TimeUnit.MILLISECONDS);

    private final String address;
    private final int port;
    private String username;
    private UUID uuid;
    private boolean compressed = false;

    private double x = 0;
    private double y = 0;
    private double z = 0;
    private float yaw = 0;

    private boolean goUp = false;

    public Bot(String username, String address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
        setName("BotThread-" + username);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(address, port)) {
            FriendlyDataInputStream in = new FriendlyDataInputStream(socket.getInputStream());
            FriendlyDataOutputStream out = new FriendlyDataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            sendLoginPackets(in, out);

            FriendlyDataOutputStream settingsPacket = new FriendlyDataOutputStream();
            settingsPacket.write(0x05);
            settingsPacket.writeString("en_GB");
            settingsPacket.writeByte(2);
            settingsPacket.writeVarInt(0);
            settingsPacket.writeBoolean(true);
            settingsPacket.writeByte(0);
            settingsPacket.writeVarInt(0);
            settingsPacket.writeBoolean(false);
            settingsPacket.writeBoolean(true);

            writePacket(out, settingsPacket.toByteArray());

            CompletableFuture.delayedExecutor(1000,TimeUnit.MILLISECONDS).execute(() -> tick(socket));

            while (!socket.isClosed()) {
                byte[] buffer = new byte[in.readVarInt()];
                in.readFully(buffer);

                readPacket(buffer.length, new FriendlyDataInputStream(new ByteArrayInputStream(buffer)), out);

                out.flush();
            }

        } catch (EOFException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(username + " has disconnected from " + address + ":" + port);
    }

    public void writePacket(FriendlyDataOutputStream out, byte[] bytes) throws IOException {
        if (compressed) {
            out.writeVarInt(bytes.length + 1);
            out.writeVarInt(0);
        } else {
            out.writeVarInt(bytes.length);
        }
        out.write(bytes);
    }

    private void tick(Socket socket) {
        try {
            if (socket.isClosed()) return;

            ONE_TICK_DELAY.execute(() -> tick(socket));

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

                double speed = 0.2;
                x += speed * -Math.sin(Math.toRadians(yaw));
                z += speed * Math.cos(Math.toRadians(yaw));
            }

            y -= 0.01;

            FriendlyDataOutputStream movePacket = new FriendlyDataOutputStream();
            movePacket.write(0x12);
            movePacket.writeDouble(x);
            movePacket.writeDouble(y);
            movePacket.writeDouble(z);
            movePacket.writeFloat(yaw);
            movePacket.writeFloat(0);
            movePacket.writeBoolean(true);

            FriendlyDataOutputStream out = new FriendlyDataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            writePacket(out, movePacket.toByteArray());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPacket(int length, FriendlyDataInputStream in, FriendlyDataOutputStream out) throws IOException {
        int dataLength = compressed ? in.readVarInt() : 0;

        if (dataLength != 0) {
            byte[] buffer = new byte[in.remaining()];
            in.readFully(buffer);
            in = new FriendlyDataInputStream(new InflaterInputStream(new ByteArrayInputStream(buffer)));
        }

        int packetId = in.readVarInt();
//        System.out.println("id " + packetId + " (" + (dataLength == 0 ? length : dataLength) + ")");

        if (packetId == 0x1A) {
            System.out.println(username + " (" + uuid + ") was kicked due to " + in.readString());
        } else if (packetId == 0x21) {
            long id = in.readLong();

            FriendlyDataOutputStream keepAlivePacket = new FriendlyDataOutputStream();
            keepAlivePacket.write(0x0F);
            keepAlivePacket.writeLong(id);

            out.writeVarInt(keepAlivePacket.size() + 1);
            out.writeVarInt(0);
            out.write(keepAlivePacket.toByteArray());
        } else if (packetId == 0x38) {
            double dx = in.readDouble();
            double dy = in.readDouble();
            double dz = in.readDouble();
            float dyaw = in.readFloat();
            float dpitch = in.readFloat();
            byte flags = in.readByte();
            int id = in.readVarInt();

            x = (flags & 0x01) == 0x01 ? x + dx : dx;
            y = (flags & 0x02) == 0x02 ? y + dy : dy;
            z = (flags & 0x04) == 0x04 ? z + dz : dz;

            System.out.println("Teleporting " + username + " to " + x + "," + y + "," + z);

            // Try going up to go over the block, or turn around and go a different way
            goUp = Math.random() < 0.5;
            if (!goUp) yaw = (float) (Math.random() * 360);

            FriendlyDataOutputStream teleportConfirmPacket = new FriendlyDataOutputStream();
            teleportConfirmPacket.write(0x00);
            teleportConfirmPacket.writeVarInt(id);

            out.writeVarInt(teleportConfirmPacket.size() + 1);
            out.writeVarInt(0);
            out.write(teleportConfirmPacket.toByteArray());
        }
    }

    private void sendLoginPackets(FriendlyDataInputStream in, FriendlyDataOutputStream out) throws IOException {
        FriendlyDataOutputStream handshakePacket = new FriendlyDataOutputStream();
        handshakePacket.write(0x00);
        handshakePacket.writeVarInt(PROTOCOL_VERSION);
        handshakePacket.writeString(address);
        handshakePacket.writeShort(port);
        handshakePacket.writeVarInt(2);

        out.writeVarInt(handshakePacket.size());
        out.write(handshakePacket.toByteArray());

        FriendlyDataOutputStream loginStartPacket = new FriendlyDataOutputStream();
        loginStartPacket.writeVarInt(0x00);
        loginStartPacket.writeString(username);

        out.writeVarInt(loginStartPacket.size());
        out.write(loginStartPacket.toByteArray());

        out.flush();

        while (true) {
            int packetLength = in.readVarInt();
            int dataLength = compressed ? in.readVarInt() : 0;
            int packetId = in.readVarInt();

            if (packetId == 0) {
                System.out.println(username + " was disconnected during login due to " + in.readString());
                in.close();
                return;
            } else if (packetId == 2) {
                uuid = in.readUUID();
                username = in.readString();
                System.out.println(username + " (" + uuid + ") has logged in");
                return;
            } else if (packetId == 3) {
                in.readVarInt();
                compressed = true;
            } else {
                throw new IOException("Unknown login packet id of " + packetId);
            }
        }
    }
}
