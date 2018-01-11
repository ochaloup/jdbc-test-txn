package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.narayana.test.db.DBUtils;

public class SelectThread implements Callable<ResultSet> {

    private String name;

    public SelectThread(String name) {
        this.name = name;
    }

    public ResultSet call() {
        Thread.currentThread().setName(SelectThread.class.getSimpleName() + "-" + name);

        Connection conn = DBUtils.getDBConnection();
        ResultSet result = null;

        try {
            conn.setAutoCommit(false);

            PreparedStatement ps1Query = conn.prepareStatement(
                    String.format(DBUtils.SELECT_WHERE_BY_NODENAME, DBUtils.TABLE1_NAME));
            ps1Query.setString(1, name);
            result = ps1Query.executeQuery();

            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn, name, e);
        } finally {
            DBUtils.close(conn, SelectThread.class.getName());
        }

        return result;
    }
}
