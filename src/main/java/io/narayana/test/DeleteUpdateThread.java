package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

public class DeleteUpdateThread implements Callable<Integer> {

    private String name;
    private int randomToDelete;

    public DeleteUpdateThread(int randomToDelete, String name) {
        this.name = name;
        this.randomToDelete = randomToDelete;
    }

    public Integer call() {
        Thread.currentThread().setName(DeleteUpdateThread.class.getSimpleName() + "-" + name);

        Connection conn = DBUtils.getDBConnection();
        int random = FlowControl.RANDOM.nextInt(1_000_000) + 1;

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1Delete = conn.prepareStatement(DBUtils.DELETE_T1);
            ps1Delete.setInt(1, randomToDelete);
            ps1Delete.executeUpdate();

            PreparedStatement ps1Update= conn.prepareStatement(DBUtils.UPDATE_STATEMENT_T1);
            ps1Update.setInt(1, random);
            ps1Update.setString(1, name);
            ps1Update.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn, name, e);
        } finally {
            DBUtils.close(conn, DeleteUpdateThread.class.getName());
        }

        return random;
    }
}
