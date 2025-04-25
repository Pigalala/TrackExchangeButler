package me.pigalala.trackexchange.butler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.util.JavalinLogger;
import me.pigalala.trackexchange.butler.exception.UnauthorizedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class WebServer {

    private final Javalin javalin;

    public WebServer(int port) {
        JavalinLogger.enabled = false;
        this.javalin = Javalin.create(this::configureJavalin).start(port);

        javalin.exception(UnauthorizedException.class, this::handleUnauthorized);
        javalin.exception(Exception.class, this::handleException);

        javalin.get("/get/{file}", this::handleGetFile);
        javalin.post("/upload/", this::handleUploadFile);
    }

    private void configureJavalin(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.http.defaultContentType = "application/json";
    }

    private void handleUnauthorized(UnauthorizedException exception, Context ctx) {
        ctx.status(HttpStatus.UNAUTHORIZED);
        Main.logger().warn("Denied access from {}", ctx.host());
    }

    private void handleException(Exception exception, Context ctx) {
        var json = new JsonObject();
        json.addProperty("error", exception.getMessage());

        Main.logger().error("Whoops...", exception);

        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .json(json.toString());
    }

    private void handleGetFile(Context ctx) {
        checkAuthorized(ctx);

        String fileName = ctx.pathParam("file");
        if (!fileName.toLowerCase().endsWith(".trackexchange")) {
            fileName = fileName.concat(".trackexchange");
        }

        Path filePath = Path.of("trackexchange");
        if (Files.notExists(filePath)) {
            try {
                Files.createDirectory(filePath);
            } catch (Exception e) {
                throw new RuntimeException("Could not create directory", e);
            }
        }

        Path trackExchangeFile = filePath.resolve(fileName);
        if (Files.notExists(trackExchangeFile)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }

        Main.logger().info("Preparing to send {}", fileName);

        var json = new JsonObject();
        json.addProperty("name", trackExchangeFile.getFileName().toString().substring(0, trackExchangeFile.getFileName().toString().lastIndexOf('.')));

        String trackExchangeAsString;
        try {
            trackExchangeAsString = new String(Base64.getEncoder().encode(Files.readAllBytes(trackExchangeFile)));
        } catch (Exception e) {
            throw new RuntimeException("Could not encode trackexchange file", e);
        }
        json.addProperty("track_exchange", trackExchangeAsString);

        ok(ctx, json);
    }

    private void handleUploadFile(Context ctx) {
        checkAuthorized(ctx);

        JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();

        String trackName = body.get("name").getAsString();
        if (!trackName.endsWith(".trackexchange")) {
            trackName += ".trackexchange";
        }

        Main.logger().info("Preparing to upload {}", trackName);

        Path trackExchangeFile = Path.of("trackexchange", trackName);
        try {
            Files.write(trackExchangeFile, Base64.getDecoder().decode(body.get("track_exchange").getAsString()));
        } catch (Exception e) {
            throw new RuntimeException("Could not write track exchange file", e);
        }

        ok(ctx);
        Main.logger().info("Successfully uploaded {}", trackName);
    }

    public void stop() {
        javalin.stop();
    }

    private void ok(Context ctx) {
        ctx.status(HttpStatus.OK);
    }

    private void ok(Context ctx, JsonObject json) {
        ctx.status(HttpStatus.OK)
                .json(json.toString());
    }

    private void checkAuthorized(Context ctx) {
        // Authorized if no keys are set
        if (Main.keys().isEmpty()) {
            return;
        }

        try {
            String authHeader = ctx.header("Authorization");
            if (authHeader == null) {
                throw new UnauthorizedException();
            }

            String[] authParts = authHeader.split("\\s");
            if (authParts[0].equals("Key")) {
                String key = authParts[1];
                if (!Main.keys().contains(key)) {
                    throw new UnauthorizedException();
                }

                return;
            }

            throw new UnauthorizedException();
        } catch (Exception e) {
            throw new UnauthorizedException();
        }
    }
}
