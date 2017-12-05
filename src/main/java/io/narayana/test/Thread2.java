package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import io.narayana.test.db.DBUtils;

/**
 * 1. begin TX
 * 2. Insert (node1, random2)
 * 3. Commit TX
 */
public class Thread2 implements Runnable {

    private String name; 

    public Thread2(String name) {
        this.name = name;
    }

    public void run() {
        Thread.currentThread().setName(name);

        Connection conn = DBUtils.getDBConnection();
        try {
            conn.setAutoCommit(false);

            int random = new Random().nextInt(1_000_000) + 1;
            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
            ps1Insert.setString(1, "node1");
            ps1Insert.setInt(2, random);
            ps1Insert.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            if(conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    System.err.println("Can't rollback");
                    e1.printStackTrace();
                }
            }
            try {
                conn.close();
            } catch (SQLException e1) {
                System.err.println("Can't close connection at " + Thread2.class.getName());
                e1.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }
}
