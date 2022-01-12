# minecraft-stress-test

Automate the stress testing of your 1.18 Minecraft server with bots.
This project will log offline-mode bots into the specified server which will
fly around and explore the world.

## Building

Download the source code with

```shell
git clone https://github.com/PureGero/minecraft-stress-test.git
```

Build the source code with

```shell
mvn
```

## Running

Ensure the following values are set in your server.properties:

```properties
online-mode=false
allow-flight=true
```

Run the bot with

```shell
java -jar target/minecraft-stress-test-1.0.0-SNAPSHOT.jar
```

Or, specify optional parameters:

```shell
java
    -Dbot.count=1
    -Dbot.ip=127.0.0.1
    -Dbot.port=25565
    -Dbot.login.delay.ms=100
    -Dbot.radius=1000
    -jar target/minecraft-stress-test-1.0.0-SNAPSHOT.jar
```

Note that bots are unable to respawn, we recommend giving them creative mode.