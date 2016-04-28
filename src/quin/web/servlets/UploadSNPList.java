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
public class UploadSNPList extends AbstractUploadServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadSNPList(){
		_dataschema = "snplists";
		_sessionschema = "usersessions";
		_sessiontable = "SNPLists";
		_validextensions = new String[]{".txt", ".tsv"};
	}
	
	@Override
	protected void insertValues(Connection conn, long fid, InputStream is,
			String filename) throws SQLException, IOException {

		String sql = "INSERT INTO snplists.d_" + fid + " VALUES(?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 1;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (br.ready()) {
			String oline = br.readLine();
			String line = oline.toLowerCase();
			line = line.replace("rs", "");
			if(oline.trim().startsWith("rs")){
				try {
					int rsid = Integer.parseInt(line.trim());
					ps.setInt(1, rsid);
					ps.addBatch();
					count++;
					if (count % MAXBATCH == 0) {
						ps.executeBatch();
					}
				} catch(NumberFormatException e){
					if(oline.contains("rs")){
						System.out.println(oline);
					}
				}
			}
		}
		ps.executeBatch();
		ps.close();
		br.close();
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
		String sql = "INSERT INTO snplists.d_" + fid + " VALUES(?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 1;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].toLowerCase();
			line = line.toLowerCase();
			line = line.replace("rs", "");
			try {
				int rsid = Integer.parseInt(line.trim());
				ps.setInt(1, rsid);
				ps.addBatch();
				count++;
				if (count % MAXBATCH == 0) {
					ps.executeBatch();
				}
			} catch(NumberFormatException e){
			}
		}
		ps.executeBatch();
		ps.close();
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE snplists.d_" + fid
				+ " (rsid INT NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
}
