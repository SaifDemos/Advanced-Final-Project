package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // PostgreSQL connection string format:
    // jdbc:postgresql://[host]:[port]/[database]
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "admin";

    public static Connection dbConnect() {
        Connection con = null;
        try {
            // Load the PostgreSQL JDBC Driver
            // Note: Modern JDBC drivers often load automatically, but keeping this for
            // safety
            Class.forName("org.postgresql.Driver");

            // Establish connection with credentials
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            System.out.println("PostgreSQL connection successful.");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Ensure the .jar is in your Referenced Libraries.");
        } catch (SQLException ex) {
            System.err.println("Failed to connect to the database.");
            System.err.println("SQL Error: " + ex.getMessage());
        }
        return con;
    }

    // Helper method to close connections safely
    public static void dbDisconnect(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing the connection: " + e.getMessage());
        }
    }
}