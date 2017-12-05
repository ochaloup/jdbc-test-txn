package io.narayana.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 *
 * 1. begin TX
 * 2. Insert (node1, random1)
 * 3. Commit TX
 * 4. Begin TX
 * 5. Select from new table where random = 1 and node = 1
 * 6. check result set is 1
 * 7. insert to table 2
 * 8. commit TX
 * 9. Begin TX
 * 10. Select from new table where random = 1 and node = 1
 * 11. check result set is 0
 * 12. Log error
 */
public class JdbcApp {
    public static void main( String[] args )  {
    	Connection conn1 = DBUtils.getDBConnection(DBUtils.TABLE1_NAME);
    	try {
    		conn1.setAutoCommit(false);

    		int random = new Random().nextInt(1000) + 1;
    		PreparedStatement ps = conn1.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
    		ps.setInt(1, 1);
    		ps.setString(2, "node1");
    		ps.setInt(3, random);
    		ps.executeUpdate();

    		conn1.commit();

    		
    		conn1.setAutoCommit(false);

    		PreparedStatement psQuery = conn1.prepareStatement(String.format(DBUtils.SELECT_WHERE, DBUtils.TABLE1_NAME));
    		psQuery.setString(1, "node1");
    		psQuery.setInt(2, random);
    		ResultSet result = psQuery.executeQuery();

    		while(result.next()) {
    			System.out.println(">>> " + result.getString(2) + ", " + result.getInt(3));
    		}

    		conn1.commit();

/*    		PreparedStatement ps2 = conn1.prepareStatement(DBUtils.INSERT_STATEMENT_T1);
    		ps.setInt(1, 1);
    		ps.setInt(2, 42);
    		*/
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    	
    }
}
