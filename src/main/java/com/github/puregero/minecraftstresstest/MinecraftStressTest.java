package com.github.puregero.minecraftstresstest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MinecraftStressTest {

    private static final int BOT_COUNT = Integer.parseInt(System.getProperty("bot.count", "1"));
    private static final String ADDRESS = System.getProperty("bot.ip", "127.0.0.1");
    private static final short PORT = (short) Integer.parseInt(System.getProperty("bot.port", "25565"));
    private static final int DELAY_BETWEEN_BOTS_MS = Integer.parseInt(System.getProperty("bot.login.delay.ms", "100"));

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] a) {
        List<Bot> bots = new ArrayList<>();

        updateBotCount(bots, BOT_COUNT);

        try(Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] args = line.split(" ");

                try {
                    if (args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("botcount")) {
                        int botCount = Integer.parseInt(args[1]);
                        System.out.println("Setting bot count to " + botCount);
                        updateBotCount(bots, botCount);
                    } else if (args[0].equalsIgnoreCase("speed")) {
                        double speed = Double.parseDouble(args[1]);
                        System.out.println("Setting speed to " + speed);
                        Bot.SPEED = speed;
                    } else {
                        System.out.println("Commands:");
                        System.out.println("count <number of bots>");
                        System.out.println("speed <0.2>");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("stdin ended");
    }

    private static void updateBotCount(List<Bot> bots, int botCount) {
        while (bots.size() > botCount && !bots.isEmpty()) {
            bots.remove(bots.size() - 1).close();
        }

        while (bots.size() < botCount) {
            bots.add(connectBot(System.getProperty("bot.name", "Bot") + (bots.size() + 1), ADDRESS, PORT));
            try {
                Thread.sleep(DELAY_BETWEEN_BOTS_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bot connectBot(String name, String address, short port) {
        Bot bot = new Bot(name, address, port);

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                bot.channel = ch;
                ch.pipeline().addLast("packetDataEncoder", new PacketDataEncoder());
                ch.pipeline().addLast("packetLengthEncoder", new PacketLengthEncoder());
                ch.pipeline().addLast("packetDecoder", new PacketDecoder());
                ch.pipeline().addLast("bot", bot);
            }
        });

        b.connect(address, port);

        return bot;
    }

}
