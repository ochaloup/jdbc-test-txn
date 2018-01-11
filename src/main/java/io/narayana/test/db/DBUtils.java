package io.narayana.test.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    public static final String DB_NAME = System.getProperty("db", "test");
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/%s";
    private static final String DB_USER = System.getProperty("user", "test");
    private static final String DB_PASSWORD = System.getProperty("password", "test");

    public static String TABLE1_NAME = "TABLE1";
    public static String TABLE2_NAME = "TABLE2";

    public static String CREATE_TABLE1 = String.format("CREATE TABLE %s (node_name VARCHAR(50), random INTEGER)", TABLE1_NAME);
    public static String CREATE_TABLE2 = String.format("CREATE TABLE %s (value INTEGER)", TABLE2_NAME);

    public static String INSERT_STATEMENT_T1 = String.format("INSERT INTO %s (node_name, random) values (?, ?)", TABLE1_NAME);
    public static String UPDATE_STATEMENT_T1 = String.format("UPDATE %s SET random = ? WHERE node_name = ?", TABLE1_NAME);
    public static String INSERT_STATEMENT_T2 = String.format("INSERT INTO %s (value) values (?)", TABLE2_NAME);
    public static String SELECT_ALL = "SELECT * FROM %s";
    public static String SELECT_WHERE = "SELECT * FROM %s WHERE node_name = ? AND random = ?";
    public static String SELECT_WHERE_BY_NODENAME = "SELECT * FROM %s WHERE node_name = ?";
    public static String DELETE_T1 = String.format("DELETE FROM %s WHERE random = ?", TABLE1_NAME);
    public static String DELETE_ALL_T1 = String.format("DELETE FROM %s", TABLE1_NAME);

    public static Connection getDBConnection() {
        return getDBConnection(DB_NAME);
    }

    public static void rollback(Connection conn, String name, Exception cause) {
        if(conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.printf("Can't rollback name: %s%n", name);
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                System.err.printf("The conn '%s' was rolled back of name '%s'", conn, name);
                cause.printStackTrace();
            }
        }
    }

    public static void close(Connection conn, String where) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.printf("Can't close connection at %s%n", where);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Connection getDBConnection(String dbname) {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            String dbConnectionUrl = String.format(DB_CONNECTION, dbname);
            dbConnection = DriverManager.getConnection(dbConnectionUrl, DB_USER, DB_PASSWORD);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            // dbConnection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            return dbConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Can't get connection to inmem H2 database " + dbname, e);
        }
    }
}
