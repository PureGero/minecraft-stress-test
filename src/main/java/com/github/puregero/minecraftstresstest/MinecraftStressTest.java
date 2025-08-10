package com.github.puregero.minecraftstresstest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MinecraftStressTest {

    private static final String ADDRESS = System.getProperty("bot.ip", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("bot.port", "25565"));
    public static final String DEFAULT_DELAY_BETWEEN_BOTS_MS = "100";
    public static int DELAY_BETWEEN_BOTS_MS = Integer.parseInt(System.getProperty("bot.login.delay.ms", DEFAULT_DELAY_BETWEEN_BOTS_MS));

    public static final String DEFAULT_BOT_COUNT = "1";
    public static int BOT_COUNT = Integer.parseInt(System.getProperty("bot.count", DEFAULT_BOT_COUNT));

    private static final List<Bot> bots = new ArrayList<>();
    private static final Lock botsLock = new ReentrantLock();
    private static final AtomicBoolean addingBots = new AtomicBoolean();

    private static final EventLoopGroup workerGroup;
    private static final Class<? extends SocketChannel> nettyChannelClass;
    static {
        if (Epoll.isAvailable()) {
            workerGroup = new EpollEventLoopGroup();
            nettyChannelClass = EpollSocketChannel.class;
        } else if (KQueue.isAvailable()) {
            workerGroup = new KQueueEventLoopGroup();
            nettyChannelClass = KQueueSocketChannel.class;
        } else {
            workerGroup = new NioEventLoopGroup();
            nettyChannelClass = NioSocketChannel.class;
        }
        System.out.println("Using " + workerGroup.getClass().getSimpleName() + " with " + nettyChannelClass.getSimpleName() + " for network communication.");
    }

    public static void main(String[] args) {
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            printHelp();
            return;
        }

        updateBotCount();

        new CommandLine().run();

        System.out.println("stdin ended");
    }

    private static void printHelp() {
        System.out.println("Minecraft Stress Test");
        System.out.println("Usage: java [options] -jar minecraft-stress-test.jar");
        System.out.println("\nOptions:");
        System.out.println("  -Dbot.ip=<ip>                 Set the server IP (default: 127.0.0.1)");
        System.out.println("  -Dbot.port=<port>             Set the server port (default: 25565)");
        System.out.println("  -Dbot.count=<count>           Set the number of bots (default: 1)");
        System.out.println("  -Dbot.login.delay.ms=<delay>  Set the delay between bot logins in ms (default: 100)");
        System.out.println("  -Dbot.name=<name>             Set the base name for bots (default: Bot)");
        System.out.println("  -Dbot.x=<x>                   Set the center X coordinate (default: 0)");
        System.out.println("  -Dbot.z=<z>                   Set the center Z coordinate (default: 0)");
        System.out.println("  -Dbot.logs=<true|false>       Enable or disable bot logs (default: true)");
        System.out.println("  -Dbot.yaxis=<true|false>      Enable or disable Y-axis movement (default: true)");
        System.out.println("  -Dbot.viewdistance=<distance> Set the view distance (default: 2)");
        System.out.println("  -Dbot.speed=<speed>           Set the bot movement speed (default: 0.1)");
        System.out.println("  -Dbot.radius=<radius>         Set the movement radius (default: 1000)");
        System.out.println("\nRuntime Commands:");
        System.out.println("  count <number>                Change the number of bots");
        System.out.println("  speed <value>                 Change the bot movement speed");
        System.out.println("  radius <value>                Change the movement radius");
        System.out.println("  logindelay <value>            Change the delay between bot logins");
        System.out.println("\nExample:");
        System.out.println("  java -Dbot.ip=localhost -Dbot.port=25565 -Dbot.count=10 -jar minecraft-stress-test.jar");
    }

    public static void updateBotCount() {
        removeBotsIfNeeded();
        addBotIfNeeded(true);
    }

    private static void removeBotsIfNeeded() {
        botsLock.lock();
        try {
            Bot removedBot;
            while (bots.size() > BOT_COUNT) {
                removedBot = bots.remove(bots.size() - 1);
                removedBot.close();
            }
        } finally {
            botsLock.unlock();
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
        b.channel(nettyChannelClass);
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
