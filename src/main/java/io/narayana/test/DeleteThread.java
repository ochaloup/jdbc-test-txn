package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.narayana.test.db.DBUtils;

public class DeleteThread implements Runnable {

    private String name;

    public DeleteThread(String name) {
        this.name = name;
    }

    public void run() {
        Thread.currentThread().setName(DeleteThread.class.getSimpleName() + "-" + name);

        Connection conn = DBUtils.getDBConnection();

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.DELETE_ALL_T1);
            ps1Insert.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn, name, e);
        } finally {
            DBUtils.close(conn, DeleteThread.class.getName());
        }
    }
}
