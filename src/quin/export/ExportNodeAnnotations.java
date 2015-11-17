package quin.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ExportNodeAnnotations {

	
	//Exports a file where each row is a node and each column is 0 or 1 value for the annotations on that node
	
	public void writeFile(Connection conn, long fid, Integer[] sids, String sf) throws SQLException, IOException{
		String nodetable = "chiapet.Nodes_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;

		//String genedbgid = "ucsc."+genedb;
		
		String sql = "SELECT DISTINCT n.id, n.chr, n.start, n.end, n.degree, n.closeness, n.harmonic, n.betweenness, CAST(n.degree AS decimal(64,10))/(cc.nodecount-1), n.closeness*(cc.nodecount-1), n.harmonic/(cc.nodecount-1), (CASE WHEN cc.nodecount < 3 THEN 0 ELSE (n.betweenness/((cc.nodecount-1)*(cc.nodecount-2))) END)  FROM "+nodetable+" AS n, "+cctable+" AS cc WHERE n.ccid = cc.id";
		
		String[] datasets = new String[sids.length];
		@SuppressWarnings("unchecked")
		TreeMap<Integer, Integer>[] na = new TreeMap[sids.length];
		Util u = new Util();
		for(int i = 0; i < sids.length; i++){
			datasets[i] = u.getDataset(conn, fid, sids[i]);
			na[i] = u.getAnnotationNodeIds(conn, fid, sids[i]);
		}
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();

		TreeMap<Integer, String[]> nodeinformation = new TreeMap<Integer, String[]>();
		
		while(rs.next()){
			int nid = rs.getInt(1);
			String chr = rs.getString(2);
			int start = rs.getInt(3);
			int end = rs.getInt(4);
			int degree = rs.getInt(5);
			double closeness = rs.getDouble(6);
			double harmonic = rs.getDouble(7);
			double betweenness = rs.getDouble(8);
			double ndegree = rs.getInt(9);
			double ncloseness = rs.getDouble(10);
			double nharmonic = rs.getDouble(11);
			double nbetweenness = rs.getDouble(12);
			
			if(!nodeinformation.containsKey(nid)){
				nodeinformation.put(nid, new String[] {Integer.toString(nid), chr, Integer.toString(start), Integer.toString(end), Integer.toString(degree), Double.toString(closeness), Double.toString(harmonic), Double.toString(betweenness), Double.toString(ndegree), Double.toString(ncloseness), Double.toString(nharmonic), Double.toString(nbetweenness)});
			}
		}
		
		rs.close();
		ps.close();
		
		File f = new File(sf);
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("Node ID");
		bw.write("\tChr");
		bw.write("\tStart");
		bw.write("\tEnd");
		bw.write("\tDegree");
		bw.write("\tCloseness");
		bw.write("\tHarmonic");
		bw.write("\tBetweenness");
		bw.write("\tNormalized Degree");
		bw.write("\tNormalized Closeness");
		bw.write("\tNormalized Harmonic");
		bw.write("\tNomralized Betweenness");

		for(int i = 0; i < datasets.length; i++){
			bw.write("\t"+datasets[i]);
		}
		bw.write("\n");
		
		while(!nodeinformation.isEmpty()){
			Entry<Integer, String[]> entry = nodeinformation.pollFirstEntry();
			int nid = entry.getKey();
			String[] ninfo = entry.getValue();

			bw.write(ninfo[0]);
			bw.write("\t"+ninfo[1]);
			bw.write("\t"+ninfo[2]);
			bw.write("\t"+ninfo[3]);
			bw.write("\t"+ninfo[4]);
			bw.write("\t"+ninfo[5]);
			bw.write("\t"+ninfo[6]);
			bw.write("\t"+ninfo[7]);
			bw.write("\t"+ninfo[8]);
			bw.write("\t"+ninfo[9]);
			bw.write("\t"+ninfo[10]);
			bw.write("\t"+ninfo[11]);

			for(int i = 0; i < datasets.length; i++){
				bw.write("\t"+(na[i].containsKey(nid) ? na[i].get(nid) : 0));
			}
			bw.write("\n");
		}
		
		bw.flush();
		bw.close();
	}
	
}
