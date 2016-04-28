package quin.web.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.annotation.MultipartConfig;

@MultipartConfig
public class UploadRegionList extends AbstractUploadServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UploadRegionList(){
		_dataschema = "regionlists";
		_sessionschema = "usersessions";
		_sessiontable = "RegionLists";
		_validextensions = new String[]{".txt", ".tsv", ".bed"};
	}
	
	@Override
	protected void insertValues(Connection conn, long fid, InputStream is,
			String filename) throws SQLException, IOException {

		String sql = "INSERT INTO regionlists.d_" + fid + " VALUES(?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (br.ready()) {
			String line = br.readLine();
			try {
				String[] split = line.split("\\s");
				ps.setString(1, split[0]);
				ps.setInt(2, Integer.parseInt(split[1]));
				ps.setInt(3, Integer.parseInt(split[2]));
				count++;
				if (count % MAXBATCH == 0) {
					ps.executeBatch();
				}
				ps.addBatch();
			} catch (NumberFormatException e) {
			}
		}
		ps.executeBatch();
		ps.close();
		br.close();
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
		String sql = "INSERT INTO regionlists.d_" + fid + " VALUES(?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (!line.equals("")) {
				try {
					String[] split = line.split("\\s");
					ps.setString(1, split[0]);
					ps.setInt(2, Integer.parseInt(split[1]));
					ps.setInt(3, Integer.parseInt(split[2]));
					count++;
					if (count % MAXBATCH == 0) {
						ps.executeBatch();
					}
					ps.addBatch();
				} catch (NumberFormatException e) {
				}
			}
		}
		ps.executeBatch();
		ps.close();
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE regionlists.d_"
				+ fid
				+ " (chr VARCHAR(50) NOT NULL, start INT NOT NULL, end INT NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
}
