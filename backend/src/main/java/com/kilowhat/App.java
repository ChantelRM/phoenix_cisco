package com.kilowhat;

import com.kilowhat.controllers.*;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

public class App {
    public static void main(String[] args) {
        com.kilowhat.db.Database.initSchema();

        Javalin app = Javalin.create(config -> {
            // Lets the frontend (opened as a plain file or a different port)
            // call this API during development.
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(7000);

        app.get("/", ctx -> ctx.result("KiloWhat API is running"));

        // simple centralized error handling: SQL/unexpected errors become
        // a clean 500 with a message instead of a raw stack trace
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of("error", e.getMessage()));
        });

        AuthController.register(app);
        ApplianceCategoryController.register(app);
        UsageController.register(app);
        NotificationController.register(app);
        BackupController.register(app);
        OutageController.register(app);

        System.out.println("KiloWhat backend running on http://localhost:7000");
    }
}
