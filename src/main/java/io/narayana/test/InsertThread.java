package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

public class InsertThread implements Callable<Integer> {

    private String name;

    public InsertThread(String name) {
        this.name = name;
    }

    public Integer call() {
        Thread.currentThread().setName(InsertThread.class.getSimpleName() + "-" + name);

        Connection conn = DBUtils.getDBConnection();
        int random = FlowControl.RANDOM.nextInt(1_000_000) + 1;

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
            ps1Insert.setString(1, name);
            ps1Insert.setInt(2, random);
            ps1Insert.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if(conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    System.err.println(String.format("Can't rollback " + ));
                    e1.printStackTrace();
                }
            }
            try {
                conn.close();
            } catch (SQLException e1) {
                System.err.printf("Can't close connection at %s%n", InsertThread.class.getName());
                e1.printStackTrace();
            }
            throw new RuntimeException(e);
        }

        return random;
    }
}
