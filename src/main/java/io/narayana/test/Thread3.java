package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

/**
 * <ol>
 *  <li> <!-- 1 --> begin TX</li>
 *  <li> <!-- 2 --> Insert (node2, random3)</li>
 *  <li> <!-- 3 --> Commit TX</li>
 *  <li> <!-- 4 --> While loop for some period</li>
 *  <li> <!-- 5 --> Begin TX</li>
 *  <li> <!-- 6 --> Select from new table where random = 3 and node = 2</li>
 *  <li> <!-- 7 --> check result set is 1</li>
 *  <li> <!-- 8 --> insert to table 2</li>
 *  <li> <!-- 9 --> commit TX</li>
 * </ol>
 */
public class Thread3 implements Runnable {

    private String name;

    public Thread3(String name) {
        this.name = name;
    }

    public void run() {
        Thread.currentThread().setName(Thread3.class.getSimpleName() + "-" + name);

        String nodeName = name + FlowControl.NODE2;

        Connection conn = DBUtils.getDBConnection();
        try {
            conn.setAutoCommit(false);

            int random = new Random().nextInt(1_000_000) + 1;
            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
            ps1Insert.setString(1, nodeName);
            ps1Insert.setInt(2, random);
            ps1Insert.executeUpdate();

            conn.commit();

            long timeoutMs = 1000 + FlowControl.RANDOM.nextInt(2000);
            long startedAt = System.currentTimeMillis();
            System.out.printf(">>%s> while sleeping for %s ms%n", Thread.currentThread().getName(), timeoutMs);
            while(System.currentTimeMillis() < startedAt + timeoutMs);


            conn.setAutoCommit(false);

            PreparedStatement psQuery = conn.prepareStatement(String.format(DBUtils.SELECT_WHERE, DBUtils.TABLE1_NAME));
            psQuery.setString(1, nodeName);
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
            psInsert.setInt(1, 3);
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
