package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

public class UpdateThread implements Callable<Integer> {

    private String name;

    public UpdateThread(String name) {
        this.name = name;
    }

    public Integer call() {
        Thread.currentThread().setName(UpdateThread.class.getSimpleName() + "-" + name);

        Connection conn = DBUtils.getDBConnection();
        int random = FlowControl.RANDOM.nextInt(1_000_000) + 1;

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1Update= conn.prepareStatement(DBUtils.UPDATE_STATEMENT_T1);
            ps1Update.setInt(1, random);
            ps1Update.setString(2, name);
            ps1Update.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn, name, e);
        } finally {
            DBUtils.close(conn, UpdateThread.class.getName());
        }

        return random;
    }
}
