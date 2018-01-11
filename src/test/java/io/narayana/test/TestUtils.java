package io.narayana.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.narayana.test.db.DBUtils;

public final class TestUtils {

    public static void createTables() {
        try(Connection conn = DBUtils.getDBConnection()) {
            conn.createStatement().executeUpdate(DBUtils.CREATE_TABLE1);
        } catch (SQLException sqle) {
            if(sqle.getSQLState().equals("42P07")) return;
            throw new RuntimeException(sqle);
        }
        try(Connection conn = DBUtils.getDBConnection()) {
            conn.createStatement().executeUpdate(DBUtils.CREATE_TABLE2);
        } catch (SQLException sqle) {
            if(sqle.getSQLState().equals("42P07")) return;
            throw new RuntimeException(sqle);
        }
    }

    public static void waitToEnd(Set<Future<?>> futures, ExecutorService es) {
        try {
            // waiting for all threads will be finished
            for(Future<?> f: futures) {
                f.get();
            }
            es.shutdown();
            es.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Waiting on service " + es + " failed", e);
        }
    }
}
