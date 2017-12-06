package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.narayana.test.byteman.FlowControl;
import io.narayana.test.db.DBUtils;

/**
 * <ol>
 *  <li> <!-- 1 --> begin TX</li>
 *  <li> <!-- 2 --> Insert (node1, random1)</li>
 *  <li> <!-- 3 --> Commit TX</li>
 *  <li> <!-- 4 --> Begin TX</li>
 *  <li> <!-- 5 --> Select from new table where random = 1 and node = 1</li>
 *  <li> <!-- 6 --> check result set is 1</li>
 *  <li> <!-- 7 --> insert to table 2</li>
 *  <li> <!-- 8 --> commit TX</li>
 *  <li> <!-- 9 --> Begin TX</li>
 *  <li> <!-- 10 --> Select from new table where random = 1 and node = 1</li>
 *  <li> <!-- 11 --> check result set is 0</li>
 *  <li> <!-- 12 --> Log error</li>
 * </ol>
 */
public class Thread1 implements Callable<Exception> {

    private String name;

    public Thread1(String name) {
        this.name = name;
    }

    public Exception call() {
        Thread.currentThread().setName(Thread1.class.getSimpleName() + "-" + name);

        String nodeName = name + FlowControl.NODE1;

        Connection conn = DBUtils.getDBConnection();
        try {
            conn.setAutoCommit(false);

            int random = FlowControl.RANDOM.nextInt(1_000_000) + 1;
            PreparedStatement ps1Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
            ps1Insert.setString(1, nodeName);
            ps1Insert.setInt(2, random);
            ps1Insert.executeUpdate();

            conn.commit();
            ps1Insert.close();


            conn.setAutoCommit(false);

            PreparedStatement ps1Query = conn.prepareStatement(String.format(DBUtils.SELECT_WHERE, DBUtils.TABLE1_NAME));
            ps1Query.setString(1, nodeName);
            ps1Query.setInt(2, random);
            ResultSet result = ps1Query.executeQuery();

            int rowReturned = 0;
            while(result.next()) {
                System.out.printf(">>%s> %s, %s%n", Thread.currentThread().getName(),
                        result.getString(2), result.getInt(3));
                rowReturned++;
            }
            // != 1 - is empty
            if(rowReturned != 1) throw new IllegalStateException(Thread.currentThread().getName()
                    + " : number of rows for random " + random + " has to be 1 but it's " + rowReturned);

            PreparedStatement ps2Insert = conn.prepareStatement(DBUtils.INSERT_STATEMENT_T2);
            ps2Insert.setInt(1, 1);
            ps2Insert.executeUpdate();

            conn.commit();


            // ---- STAND POINT -----
            FlowControl.thread1WaitingThread2();


            conn.setAutoCommit(false);

            result = ps1Query.executeQuery();

            rowReturned = 0;
            while(result.next()) {
                System.out.printf(">>%s> %s, %s%n", Thread.currentThread().getName(),
                        result.getString(2), result.getInt(3));
                rowReturned++;
            }
            // != 0 - is not empty
            if(rowReturned != 1) {
                System.err.printf("[ERROR] inserted random value %s for '%s' but there is no such record in DB, "
                        + "rows returned %s%n", random, nodeName, rowReturned);
                return new IllegalStateException(Thread.currentThread().getName()
                    + " : number of rows for random " + random + " has to be 0, but it's " + rowReturned);
            }
            return null;
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
                System.err.printf("Can't close connection at %s%n", Thread1.class.getName());
                e1.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }
}
