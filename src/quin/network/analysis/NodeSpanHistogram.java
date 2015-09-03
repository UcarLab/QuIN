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

public class NodeSpanHistogram {

	public byte[] getRHistogram(Connection conn, String title, long fid, int binsize, boolean density, int min, int max) throws REngineException, REXPMismatchException, IOException, SQLException{
		File f = writeFile(conn, fid, min, max);
		RHistogram h = new RHistogram();
		
        byte[] rv =  h.getHistogram(f, title, "Node Span", binsize, density); 
		f.delete();
		return rv;
	}
	
	private File writeFile(Connection conn, long fid, int min, int max) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+fid, tmpdir);
		String sql = "SELECT (n.end-n.start+1) AS span FROM chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" AS cc WHERE n.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("span\n");
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
