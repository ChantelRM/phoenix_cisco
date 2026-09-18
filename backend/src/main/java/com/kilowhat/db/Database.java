package com.kilowhat.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// SQLite by default - zero setup, no server or password to configure.
// The DB file lives at DB_PATH (defaults to backend/kilowhat.db) and its
// schema is (re-)applied automatically on every startup via initSchema().
public class Database {

    private static final String DB_PATH =
        System.getenv().getOrDefault("DB_PATH", "kilowhat.db");
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
            // WAL lets one writer and multiple readers coexist without the
            // "database is locked" contention that plain rollback-journal
            // mode causes when a request briefly opens more than one
            // connection to the same file.
            pragma.execute("PRAGMA journal_mode = WAL");
            pragma.execute("PRAGMA busy_timeout = 5000");
        }
        return conn;
    }

    // Runs schema.sql against the DB file. Safe to call on every startup -
    // every statement is CREATE TABLE IF NOT EXISTS. sqlite-jdbc executes a
    // semicolon-separated script like this in a single call.
    public static void initSchema() {
        try (Connection conn = getConnection();
             InputStream in = Database.class.getResourceAsStream("/schema.sql");
             Statement st = conn.createStatement()) {
            if (in == null) throw new IllegalStateException("schema.sql not found on classpath");
            String sql = new String(in.readAllBytes());
            st.executeUpdate(sql);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
