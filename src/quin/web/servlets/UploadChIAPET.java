package quin.web.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.annotation.MultipartConfig;

import quin.filereader.BEDReader;
import quin.filereader.ChIAPETFileReader;
import quin.filereader.ChIAPETRead;
import quin.filereader.TextReader;

@MultipartConfig
public class UploadChIAPET extends AbstractUploadServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UploadChIAPET(){
		_dataschema = "chiapetdata";
		_sessionschema = "usersessions";
		_sessiontable = "ChiapetData";
		_validextensions = new String[]{".txt", ".bed"};
		_allowlist = false;
	}
	
	@Override
	protected void insertValues(Connection conn, long fid, InputStream is, String filename) throws Exception {
		ChIAPETFileReader reader = getReader(is, filename);
		if (reader == null) {
			throw new Exception("error");
		}
		String sql = "INSERT INTO chiapetdata.d_" + fid
				+ " VALUES(?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;

		int count = 0;
		while (reader.ready()) {
			try {
				ChIAPETRead read = reader.readLine();

				ps.setString(1, read.getLChr());
				ps.setInt(2, read.getLStart());
				ps.setInt(3, read.getLEnd());
				ps.setString(4, read.getRChr());
				ps.setInt(5, read.getRStart());
				ps.setInt(6, read.getREnd());
				ps.setInt(7, read.getCount());

				count++;
				if (count % MAXBATCH == 0) {
					ps.executeBatch();
				}
				ps.addBatch();
			} catch (Exception e) {
			}
		}
		ps.executeBatch();
		ps.close();		
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
		//Do nothing.  Not supporting uploading chiapet by line here
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE chiapetdata.d_"
				+ fid
				+ " (chr1 VARCHAR(50) NOT NULL, start1 INT NOT NULL, end1 INT NOT NULL, chr2 VARCHAR(50) NOT NULL, start2 INT NOT NULL, end2 INT NOT NULL, petcount INT NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();		
	}

	private ChIAPETFileReader getReader(InputStream istream, String filename)
			throws IOException {
		if (filename != null && filename.endsWith(".bed")) {
			return new BEDReader(istream);
		} else if (filename != null && filename.endsWith(".txt")) {
			return new TextReader(istream);
		} else {
			return null;
		}
	}
}
