package me.pigalala.trackexchange.butler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger("TrackExchangeButler");

    private static WebServer webServer;
    private static Set<String> keys;

    public static void main(String[] args) {
        logger().info("Hello");

        int port = Integer.parseInt(args[0]);

        Path keyFile = Path.of("keys");
        if (Files.notExists(keyFile)) {
            try {
                Files.createFile(keyFile);
            } catch (Exception e) {
                logger().error("Could not create key file", e);
                return;
            }
        }

        List<String> validKeys;
        try {
            validKeys = Files.readAllLines(keyFile);
            logger().info("Loaded {} keys", validKeys.size());
        } catch (Exception e) {
            logger().error("Could not read key file", e);
            return;
        }
        keys = new HashSet<>(validKeys);

        Path trackExchangeDir = Path.of("trackexchange");
        if (Files.notExists(trackExchangeDir)) {
            try {
                Files.createDirectory(trackExchangeDir);
            } catch (Exception e) {
                logger().error("Could not create ./trackexchange directory", e);
                return;
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Main::onShutdown));

        webServer = new WebServer(port);
        logger().info("TrackExchangeButler started successfully");
    }

    private static void onShutdown() {
        logger().info("Stopping TrackExchangeButler");

        if (webServer != null) {
            webServer.stop();
        }

        logger().info("Bye Bye");
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static Set<String> keys() {
        return keys;
    }
}
