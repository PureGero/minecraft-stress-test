package com.github.puregero.minecraftstresstest;

import java.util.Scanner;

public class CommandLine implements Runnable {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] args = line.split(" ");

            try {
                if (args[0].equalsIgnoreCase("count") || args[0].equalsIgnoreCase("botcount")) {
                    int botCount = Math.max(0, Integer.parseInt(args[1]));
                    System.out.println("Setting bot count to " + botCount);
                    MinecraftStressTest.BOT_COUNT = botCount;
                    MinecraftStressTest.updateBotCount();
                } else if (args[0].equalsIgnoreCase("speed")) {
                    double speed = Math.max(0.0, Double.parseDouble(args[1]));
                    System.out.println("Setting speed to " + speed);
                    Bot.SPEED = speed;
                } else if (args[0].equalsIgnoreCase("radius")) {
                    double radius = Math.max(0.0, Double.parseDouble(args[1]));
                    System.out.println("Setting radius to " + radius);
                    Bot.RADIUS = radius;
                } else if (args[0].equalsIgnoreCase("logindelay")) {
                    int loginDelay = Math.max(0, Integer.parseInt(args[1]));
                    System.out.println("Setting login delay to " + loginDelay);
                    MinecraftStressTest.DELAY_BETWEEN_BOTS_MS = loginDelay;
                } else {
                    System.out.println("Commands:");
                    System.out.println("count <number of bots>   (Default: " + MinecraftStressTest.DEFAULT_BOT_COUNT + ")");
                    System.out.println("speed <value>            (Default: " + Bot.DEFAULT_SPEED + ")");
                    System.out.println("radius <value>           (Default: " + Bot.DEFAULT_RADIUS + ")");
                    System.out.println("logindelay <value>       (Default: " + MinecraftStressTest.DEFAULT_DELAY_BETWEEN_BOTS_MS + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
