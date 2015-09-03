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
public class UploadGeneList extends AbstractUploadServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadGeneList(){
		_dataschema = "genelists";
		_sessionschema = "usersessions";
		_sessiontable = "GeneLists";
		_validextensions = new String[]{".txt"};
	}
	
	@Override
	protected void insertValues(Connection conn, long fid, InputStream is,
			String filename) throws SQLException, IOException {

		String sql = "INSERT INTO genelists.d_" + fid + " VALUES(?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (br.ready()) {
			String line = br.readLine();
			if (line.length() < 255) {
				count++;
				if (count % MAXBATCH == 0) {
					ps.executeBatch();
				}
				ps.setString(1, line);
				ps.addBatch();
			}
		}
		ps.executeBatch();
		ps.close();
		br.close();
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
		String sql = "INSERT INTO genelists.d_" + fid + " VALUES(?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 0;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!line.equals("")) {
				if (line.length() < 255) {
					count++;
					if (count % MAXBATCH == 0) {
						ps.executeBatch();
					}
					ps.setString(1, line);
					ps.addBatch();

				}
			}
		}
		ps.executeBatch();
		ps.close();
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE genelists.d_" + fid
				+ " (gene VARCHAR(255) NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
}
