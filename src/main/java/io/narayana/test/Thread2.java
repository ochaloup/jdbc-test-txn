package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

/**
 * <ol>
 *  <li> <!-- 1 --> begin TX</li>
 *  <li> <!-- 2 --> Update (node1, random2)</li>
 *  <li> <!-- 3 --> Commit TX</li>
 * </ol>
 */
public class Thread2 implements Runnable {

    private String name;

    public Thread2(String name) {
        this.name = name;
    }

    public void run() {
        Thread.currentThread().setName(Thread2.class.getSimpleName() + "-" + name);

        String nodeName = name + FlowControl.NODE1;

        // ---- STAND POINT -----
        FlowControl.thread2WaitingThread1();

        Connection conn = DBUtils.getDBConnection();
        try {
            conn.setAutoCommit(false);

            int random = FlowControl.RANDOM.nextInt(1_000_000) + 1;
            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.UPDATE_STATEMENT_T1);
            ps1Insert.setInt(1, random); // update - set
            ps1Insert.setString(2, nodeName); // where condition
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
