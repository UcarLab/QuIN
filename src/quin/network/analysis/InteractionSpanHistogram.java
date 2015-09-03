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


public class InteractionSpanHistogram {

	public byte[] getRHistogram(Connection conn, String title, long fid, int binsize, boolean density) throws REngineException, REXPMismatchException, IOException, SQLException{
		File f = writeFile(conn, fid);
		//RHistogram h = new RHistogram();
		
        byte[] rv = new byte[0];//h.getHistogram(f, title, "Interaction Span", binsize, density);
		f.delete();
		return rv;
	}
	
	private File writeFile(Connection conn, long fid) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+fid, tmpdir);
		String sql = "SELECT (GREATEST(a1.start, a2.start)-LEAST(a1.end, a2.end)+1) AS span FROM (SELECT * FROM chiapet.Interactions_"+fid+" AS a1 WHERE id%2 = 0) AS a1, (SELECT * FROM chiapet.Interactions_"+fid+" AS a2 WHERE id%2=1) AS a2 WHERE a1.iid=a2.iid AND a1.chr=a2.chr";
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
