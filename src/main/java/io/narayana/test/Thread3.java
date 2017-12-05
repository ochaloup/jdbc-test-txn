package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import io.narayana.test.db.DBUtils;

/**
 * 1. begin TX
 * 2. Insert (node2, random3)
 * 3. Commit TX
 * 4. While loop for some period
 * 5. Begin TX
 * 6. Select from new table where random = 3 and node = 2
 * 7. check result set is 1
 * 8. insert to table 2
 * 9. commit TX
 */
public class Thread3 implements Runnable {

    private String name; 

    public Thread3(String name) {
        this.name = name;
    }

    public void run() {
        Thread.currentThread().setName(name);

        Connection conn = DBUtils.getDBConnection();
        try {
            conn.setAutoCommit(false);

            int random = new Random().nextInt(1_000_000) + 1;
            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
            ps1Insert.setString(1, "node2");
            ps1Insert.setInt(2, random);
            ps1Insert.executeUpdate();

            conn.commit();

            long timeoutMs = 2000 + new Random().nextInt(8000);
            long startedAt = System.currentTimeMillis();
            System.out.printf("Sleeping for %s ms%n", timeoutMs);
            while(System.currentTimeMillis() < startedAt + timeoutMs) Thread.yield();


            conn.setAutoCommit(false);

            PreparedStatement psQuery = conn.prepareStatement(String.format(DBUtils.SELECT_WHERE, DBUtils.TABLE1_NAME));
            psQuery.setString(1, "node2");
            psQuery.setInt(2, random);
            ResultSet result = psQuery.executeQuery();

            int rowReturned = 0;
            while(result.next()) {
                System.out.printf(">>%s> %s, %s%n", Thread.currentThread().getName(),
                        result.getString(2), result.getInt(3));
                rowReturned++;
            }
            if(rowReturned != 1) throw new IllegalStateException(Thread.currentThread().getName()
                    + " : number of rows for random " + random + " has to be 1");

            PreparedStatement psInsert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T2);
            psInsert.setInt(1, 42);
            psInsert.executeUpdate();

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
                System.err.printf("Can't close connection at %s%n", Thread3.class.getName());
                e1.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }
}
