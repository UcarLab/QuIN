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
public class UploadEdgeAnnotation extends AbstractUploadServlet {
	/**
	 * Was going to add a feature to annotate edges, but there are current ways of doing this, just a little more work.
	 * Not enough time right now to implement this correctly.
	 */
	private static final long serialVersionUID = 1L;
	
	public UploadEdgeAnnotation(){
		_dataschema = "edgeannotationlists";
		_sessionschema = "usersessions";
		_sessiontable = "EdgeAnnotationLists";
		_validextensions = new String[]{".txt"};
	}
	
	@Override
	protected void insertValues(Connection conn, long fid, InputStream is,
			String filename) throws SQLException, IOException {
		
		String type1s = _req.getParameter("type1");
		String type2s = _req.getParameter("type2");
		
		int type1 = Integer.parseInt(type1s);
		int type2 = Integer.parseInt(type2s);
		
		String sql = "INSERT INTO edgeannotationlists.d_" + fid + " VALUES(?,?,?, ?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;
		int count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (br.ready()) {
			String line = br.readLine();
			try {
				String[] split = line.split("\t");
				
				try{
					int index = setValue(split, 0, type1, ps, 1);
					setValue(split, index, type2, ps, 4);
						
				}
				catch(ArrayIndexOutOfBoundsException e){
					continue;
				}
				
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
		
		//TODO update usersession types

	}
	
	private int setValue(String[] split, int sindex, int type, PreparedStatement ps, int pindex) throws ArrayIndexOutOfBoundsException, SQLException{
		if(type == 1){
			try{
				String chr = split[sindex];
				int start = Integer.parseInt(split[sindex+1]);
				int end = Integer.parseInt(split[sindex+2]);
				ps.setString(pindex, chr);
				ps.setInt(pindex+1, start);
				ps.setInt(pindex+2, end);
				return sindex+3;
			}
			catch(NumberFormatException e2){
				return -1;
			}
		}
		else if(type >= 2 && type <= 4){
			ps.setString(pindex, split[sindex]);
			ps.setNull(pindex+1, java.sql.Types.INTEGER);
			ps.setNull(pindex+2, java.sql.Types.INTEGER);
			return sindex+1;
		}
		else{
			return -1;
		}
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE edgeannotationlists.d_"
				+ fid
				+ " (object1 VARCHAR(255) NOT NULL, start1 INT NULL, end1 INT NULL, object2 VARCHAR(255) NOT NULL, start2 INT NULL, end2 INT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
}
