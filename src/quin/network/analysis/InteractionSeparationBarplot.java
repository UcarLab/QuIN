package quin.network.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.TreeMap;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import rdiagrams.RBarplot;

public class InteractionSeparationBarplot {

	public byte[] getRBarplot(Connection conn, String title, long fid) throws REngineException, REXPMismatchException, IOException, SQLException{
		File f = writeFile(conn, fid);

		RBarplot h = new RBarplot();
		
        byte[] rv = h.getBarplot(f, getTitle(conn, fid));
		f.delete();
		return rv;
	}
	
	private String getTitle(Connection conn, long fid) throws SQLException{
		String sql = "SELECT count(reason)/2 FROM chiapet.Interactions_"+fid+" ";
		PreparedStatement ps = conn.prepareStatement(sql);

		String title = "Interaction Classification ";
		ResultSet rs = ps.executeQuery();
		rs.next();
		title += "("+NumberFormat.getInstance().format(rs.getInt(1))+" Interactions)";
		rs.close();
		ps.close();
		return title;
	}
	
	private File writeFile(Connection conn, long fid) throws IOException, SQLException{
		String[] labels = new String[] {"Edge", "Self-loop", "Orphan Anchor", "Orphan Intera", "Distal Intra", "Excluded Inter", "Excluded Intra", "Ambiguous"};
		int[] index = new int[] {0,1,3,2,4,5,9,6};
		TreeMap<Integer, Integer> tm = new TreeMap<Integer,Integer>();
		for(int i = 0; i < index.length; i++){
			tm.put(index[i], 0);
		}
		
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+fid, tmpdir);
		String sql = "SELECT reason, count(reason)/2 FROM chiapet.Interactions_"+fid+" GROUP BY REASON";
		PreparedStatement ps = conn.prepareStatement(sql);

				
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			int r = rs.getInt(1);
			int c = rs.getInt(2);
			if(tm.containsKey(r)){
				tm.put(r, c);
			}
		}
		rs.close();
		ps.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(labels[0]);
		for(int i = 1; i < index.length; i++){
			String l = labels[i];
			bw.write(","+l);
		}
		bw.write("\n");
		bw.write(tm.get(index[0]));
		for(int i = 0; i < index.length; i++){
			int c = tm.get(index[i]);
			bw.write(","+c);
		}
		bw.write("\n");
		
		bw.flush();
		bw.close();

		return f;
	}
	
}
