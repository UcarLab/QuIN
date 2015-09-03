package quin.network.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import rdiagrams.RHistogram;

public class EdgeInteractionHistogram {

	public byte[] getRHistogram(Connection conn, String title, long fid, int binsize, boolean density) throws REngineException, REXPMismatchException, IOException, SQLException{
		File f = writeFile(conn, fid);

		RHistogram h = new RHistogram();
		
        byte[] rv = h.getHistogram(f, title, "Interaction Count", binsize, density);
		f.delete();
		return rv;
	}
	
	private File writeFile(Connection conn, long fid) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+fid, tmpdir);
		String sql = "SELECT e.interactioncount FROM chiapet.Edges_"+fid+" AS e";
		PreparedStatement ps = conn.prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("interactioncount\n");
		while(rs.next()){
			bw.write(rs.getString(1)+"\n");
		}
		
		bw.flush();
		bw.close();
		rs.close();
		ps.close();
		return f;
	}
	
}
