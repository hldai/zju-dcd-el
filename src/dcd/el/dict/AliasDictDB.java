// author: DHL brnpoem@gmail.com

package dcd.el.dict;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class AliasDictDB implements AliasDict {
	private static final String QUERY = "select e_mid from name_mid where e_name=?";

	public AliasDictDB() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/el?"
					+ "user=root&password=dhldhl");

			prepStmt = conn.prepareStatement(QUERY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public LinkedList<String> getMids(String alias) {
		try {
			prepStmt.setString(1, alias);
			rs = prepStmt.executeQuery();
			if (rs != null) {
				LinkedList<String> mids = new LinkedList<String>();

				while (rs.next()) {
					mids.add(rs.getString(1));
					// System.out.println(rs.getString("e_mid"));
				}

				rs.close();
				rs = null;
				
				return mids;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection conn = null;
	private PreparedStatement prepStmt = null;
	private ResultSet rs = null;

}
