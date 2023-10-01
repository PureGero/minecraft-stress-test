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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MinecraftStressTest {

    private static final String ADDRESS = System.getProperty("bot.ip", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("bot.port", "25565"));
    private static final int DELAY_BETWEEN_BOTS_MS = Integer.parseInt(System.getProperty("bot.login.delay.ms", "100"));

    private static final String DEFAULT_BOT_COUNT = "1";
    private static int BOT_COUNT = Integer.parseInt(System.getProperty("bot.count", DEFAULT_BOT_COUNT));

    private static final List<Bot> bots = new ArrayList<>();
    private static final Lock botsLock = new ReentrantLock();
    private static final AtomicBoolean addingBots = new AtomicBoolean();

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] a) {
        updateBotCount();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] args = line.split(" ");

            try {
                if (args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("botcount")) {
                    int botCount = Math.max(0, Integer.parseInt(args[1]));
                    System.out.println("Setting bot count to " + botCount);
                    BOT_COUNT = botCount;
                    updateBotCount();
                } else if (args[0].equalsIgnoreCase("speed")) {
                    double speed = Math.max(0.0, Double.parseDouble(args[1]));
                    System.out.println("Setting speed to " + speed);
                    Bot.SPEED = speed;
                } else if (args[0].equalsIgnoreCase("radius")) {
                    double radius = Math.max(0.0, Double.parseDouble(args[1]));
                    System.out.println("Setting radius to " + radius);
                    Bot.RADIUS = radius;
                } else {
                    System.out.println("Commands:");
                    System.out.println("count <number of bots>   (Default: " + DEFAULT_BOT_COUNT + ")");
                    System.out.println("speed <value>            (Default: " + Bot.DEFAULT_SPEED + ")");
                    System.out.println("radius <value>           (Default: " + Bot.DEFAULT_RADIUS + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("stdin ended");
    }

    private static void updateBotCount() {
        removeBotsIfNeeded();
        addBotIfNeeded(true);
    }

    private static void removeBotsIfNeeded() {
        while (true) {
            Bot removedBot;
            botsLock.lock();
            try {
                if (bots.size() <= BOT_COUNT) {
                    break;
                }
                removedBot = bots.remove(bots.size() - 1);
            } finally {
                botsLock.unlock();
            }
            removedBot.close();
        }
    }

    private static void addBotIfNeeded(boolean firstCall) {
        if (!firstCall || !addingBots.getAndSet(true)) {
            boolean scheduledNextCall = false;
            try {
                botsLock.lock();
                try {
                    if (bots.size() < BOT_COUNT) {
                        bots.add(connectBot(System.getProperty("bot.name", "Bot") + (bots.size() + 1), ADDRESS, PORT));
                        CompletableFuture.delayedExecutor(DELAY_BETWEEN_BOTS_MS, TimeUnit.MILLISECONDS).execute(() -> addBotIfNeeded(false));
                        scheduledNextCall = true;
                    }
                } finally {
                    botsLock.unlock();
                }
            } finally {
                if (!scheduledNextCall) {
                    addingBots.set(false);
                }
            }
        }
    }

    private static Bot connectBot(String name, String address, int port) {
        Bot bot = new Bot(name, address, port);

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                bot.channel = ch;
                ch.pipeline().addLast("packetEncoder", new PacketEncoder());
                ch.pipeline().addLast("packetDecoder", new PacketDecoder());
                ch.pipeline().addLast("bot", bot);
            }
        });

        b.connect(address, port);

        return bot;
    }

}
