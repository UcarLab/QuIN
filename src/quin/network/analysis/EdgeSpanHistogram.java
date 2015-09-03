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

public class EdgeSpanHistogram {

	public byte[] getRHistogram(Connection conn, String title, long fid, int binsize, boolean density) throws REngineException, REXPMismatchException, IOException, SQLException{
		File f = writeFile(conn, fid);
		RHistogram h = new RHistogram();
		
        byte[] rv = h.getHistogram(f, title, "Edge Span", binsize, density);
		f.delete();
		return rv;
	}
	
	private File writeFile(Connection conn, long fid) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+fid, tmpdir);
		String sql = "SELECT e.id, (GREATEST(n2.start, n1.start)-LEAST(n1.end, n2.end)+1) AS span FROM chiapet.Nodes_"+fid+" AS n1, chiapet.Nodes_"+fid+" AS n2, chiapet.Edges_"+fid+" AS e WHERE e.n1=n1.id AND e.n2=n2.id AND n1.chr=n2.chr";
		PreparedStatement ps = conn.prepareStatement(sql);

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
