package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
            DBUtils.rollback(conn, name, e);
        } finally {
            DBUtils.close(conn, InsertThread.class.getName());
        }

        return random;
    }
}
