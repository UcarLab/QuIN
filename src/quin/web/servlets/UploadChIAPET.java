package quin.web.servlets;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.servlet.annotation.MultipartConfig;

import quin.filereader.BEDReader;
import quin.filereader.ChIAPETFileReader;
import quin.filereader.ChIAPETRead;
import quin.filereader.Gene2GeneReader;
import quin.filereader.TextReader;


//TODO refactor this to interaction data
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
		ChIAPETRead[] data = getData(is, filename, conn);
		String sql = "INSERT INTO chiapetdata.d_" + fid
				+ " VALUES(?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		final int MAXBATCH = 1000;

		int count = 0;
		for(int i = 0; i < data.length; i++) {
			try {
				ChIAPETRead read = data[i];

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
		//Do nothing.  Not supporting uploading interaction data by line here
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

	private ChIAPETRead[] getData(InputStream istream, String filename, Connection conn)
			throws Exception {
		ChIAPETFileReader r = null;
		if (filename != null && filename.endsWith(".bed")) {
			r = new BEDReader(istream);
		}
		else if(filename != null && filename.endsWith(".ggi.txt")){
			Gene2GeneReader gr = new Gene2GeneReader(istream, conn);
			LinkedList<ChIAPETRead> rv = new LinkedList<ChIAPETRead>();
			while(gr.ready()){
				LinkedList<ChIAPETRead> rl = gr.readLine();
				if(rl != null){
					rv.addAll(rl);
				}
			}
			gr.close();
			return rv.toArray(new ChIAPETRead[0]);
		}
		else if (filename != null && filename.endsWith(".txt")) {
			r =  new TextReader(istream);
		}
		else{
			r = null;
		}
		
		LinkedList<ChIAPETRead> rv = new LinkedList<ChIAPETRead>();
		
		if(r == null){
				throw new Exception("error");
		}
		else{
			while (r.ready()) {
				rv.add(r.readLine());
			}
		}
		r.close();
		return rv.toArray(new ChIAPETRead[0]);
	}
	
}
