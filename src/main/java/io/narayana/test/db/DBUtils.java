package io.narayana.test.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    private static final String DB_NAME = "crashrec";
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/%s";
    private static final String DB_USER = "crashrec";
    private static final String DB_PASSWORD = "crashrec";

    public static String TABLE1_NAME = "TABLE1";
    public static String TABLE2_NAME = "TABLE2";

    public static String INSERT_STATEMENT_T1 = String.format("INSERT INTO %s (node_name, random) values (?, ?)", TABLE1_NAME);
    public static String UPDATE_STATEMENT_T1 = String.format("UPDATE %s SET random = ? WHERE node_name = ?", TABLE1_NAME);
    public static String INSERT_STATEMENT_T2 = String.format("INSERT INTO %s (value) values (?)", TABLE2_NAME);
    public static String SELECT_ALL = "SELECT * FROM %s" ;
    public static String SELECT_WHERE = "SELECT * FROM %s WHERE node_name = ? AND random = ?" ;

    public static Connection getDBConnection() {
        return getDBConnection(DB_NAME);
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
            return dbConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Can't get connection to inmem H2 database " + dbname, e);
        }
    }
}
