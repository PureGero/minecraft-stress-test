package com.github.puregero.minecraftstresstest;

public class MinecraftStressTest {

    private static final int BOT_COUNT = Integer.parseInt(System.getProperty("bot.count", "1"));
    private static final String ADDRESS = System.getProperty("bot.ip", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("bot.port", "25565"));
    private static final int DELAY_BETWEEN_BOTS_MS = Integer.parseInt(System.getProperty("bot.login.delay.ms", "100"));

    public static void main(String[] a) {
        for (int i = 0; i < BOT_COUNT; i ++) {
            new Bot("Bot" + (i + 1), ADDRESS, PORT).start();
            try {
                Thread.sleep(DELAY_BETWEEN_BOTS_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
