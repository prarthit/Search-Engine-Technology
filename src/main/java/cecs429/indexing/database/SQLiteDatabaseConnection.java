package cecs429.indexing.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import utils.Utils;

public class SQLiteDatabaseConnection {
    private static Connection conn = null;

    public static Connection getConnection() {
        File dbFileDirectory = Utils.createDirectory("src/main/resources/db");
        String dbFilePath = dbFileDirectory.getAbsolutePath() + "/" + "DiskTermPosition.db";
        String path = "jdbc:sqlite:" + dbFilePath;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(path);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}