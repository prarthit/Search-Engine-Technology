package cecs429.indexing.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabaseConnection {
    private static Connection conn = null;

    static {
        String fileName = "DiskTermPosition.db";
        String path = "jdbc:sqlite:" + fileName;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(path);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();

                // createTable(conn);
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection getConnection() {
        return conn;
    }
}