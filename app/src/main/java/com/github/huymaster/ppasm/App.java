package com.github.huymaster.ppasm;

import com.github.huymaster.ppasm.core.LogicFlow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class App {
    public static final AtomicReference<Connection> connection = new AtomicReference<>(null);

    private App() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (connection.get() != null) {
                try {
                    if (!connection.get().isClosed())
                        connection.get().close();
                    System.out.println("Disconnected from database.");
                } catch (Exception ignore) {
                }
            }
        }));
        prepareDatabase();
    }

    public static void main(String[] args) {
        new App();
        new LogicFlow();
    }

    private String getDatabaseFileName() {
        var defaultName = "database.db";
        File dbNameContainer = new File("config.properties");
        Properties properties = new Properties();
        if (dbNameContainer.exists() && dbNameContainer.isFile()) {
            try {
                properties.load(new FileInputStream(dbNameContainer));
                var tmp = properties.getProperty("databaseName");
                if (tmp != null && !tmp.isBlank() && !tmp.contains("/") && tmp.endsWith(".db")) {
                    defaultName = tmp;
                } else {
                    properties.put("databaseName", defaultName);
                    properties.store(new FileOutputStream(dbNameContainer), null);
                }
            } catch (Exception ignore) {
            }
        } else {
            try {
                var created = dbNameContainer.createNewFile();
                properties.put("databaseName", defaultName);
                properties.store(new FileOutputStream(dbNameContainer), null);
            } catch (Exception ignore) {
            }
        }
        return defaultName;
    }

    private void prepareDatabase() {
        var name = getDatabaseFileName();
        var dbFile = new File(name);
        System.out.println("Database path: " + dbFile.getAbsolutePath());
        if (!dbFile.exists()) {
            try {
                var result = dbFile.createNewFile();
                if (!result) {
                    System.out.println("Cannot create database file");
                    System.exit(1);
                }
            } catch (Exception ignore) {
                System.out.println("Cannot create database file");
                System.exit(1);
            }
        }
        if (!dbFile.isFile()) {
            System.err.println("Database file is not a file.");
            System.exit(1);
        }
        if (!dbFile.canWrite() || !dbFile.canRead()) {
            System.err.println("Database file is not writable or readable.");
            System.exit(1);
        }
        try {
            var conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getName());
            connection.set(conn);
            System.out.println("Connected to database.");
            prepareTable();
        } catch (Exception e) {
            System.err.println("Cannot connect to database: " + e.getMessage());
            System.exit(1);
        }
    }

    private void prepareTable() {
        var conn = connection.get();
        if (conn == null) {
            System.err.println("Cannot connect to database.");
            System.exit(1);
        }
        try {
            var studentTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS students (id TEXT PRIMARY KEY, name TEXT, age INTEGER, email TEXT, phone TEXT, gender INTEGER, grade REAL)");
            studentTable.execute();
            studentTable.close();
            System.out.println("Students table created.");
        } catch (Exception ignore) {
        }
    }
}
