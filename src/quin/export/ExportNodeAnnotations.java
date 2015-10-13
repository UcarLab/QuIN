package quin.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

public class ExportNodeAnnotations {

	
	//Exports a file where each row is a node and each column is 0 or 1 value for the annotations on that node
	
	public void writeFile(Connection conn, long fid, Integer[] sids, String sf) throws SQLException, IOException{
		String sql = "SELECT n.id, n.chr, n.start, n.end, si.iid";
		sql += " FROM chiapet.Nodes_"+fid+" AS n LEFT OUTER JOIN chiapet.SIIndex_"+fid+" AS si ON n.id=si.nid AND ";
		sql += "si.iid IN (-1";
		for(int i = 0; i < sids.length; i++){
			sql += ", ?";
		}
		sql += ")";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		for(int i = 0; i < sids.length; i++){
			ps.setInt((i+1), sids[i]);
		}
		
		ResultSet rs = ps.executeQuery();

		TreeMap<Integer, boolean[]> annotations = new TreeMap<Integer, boolean[]>();
		TreeMap<Integer, String[]> nodeinformation = new TreeMap<Integer, String[]>();
		
		while(rs.next()){
			int nid = rs.getInt(1);
			String chr = rs.getString(2);
			int start = rs.getInt(3);
			int end = rs.getInt(4);
			int siid = rs.getInt(5);
			
			if(!nodeinformation.containsKey(nid)){
				nodeinformation.put(nid, new String[] {Integer.toString(nid), chr, Integer.toString(start), Integer.toString(end)});
				annotations.put(nid, new boolean[sids.length]);
			}
			
			boolean[] na = annotations.get(nid);
			for(int i = 0; i < sids.length; i++){
				if(siid == sids[i]){
					na[i] = true;
				}
			}
			
		}
		
		rs.close();
		ps.close();
		
		Util u = new Util();
		String[] alabels = u.getLabels(conn, fid, sids);

		File f = new File(sf);
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("Node ID");
		bw.write("\tChr");
		bw.write("\tStart");
		bw.write("\tEnd");

		for(int i = 0; i < alabels.length; i++){
			bw.write("\t"+alabels[i]);
		}
		bw.write("\n");
		
		while(!annotations.isEmpty()){
			String[] ninfo = nodeinformation.pollFirstEntry().getValue();
			boolean[] ainfo = annotations.pollFirstEntry().getValue();

			bw.write(ninfo[0]);
			bw.write("\t"+ninfo[1]);
			bw.write("\t"+ninfo[2]);
			bw.write("\t"+ninfo[3]);

			for(int i = 0; i < ainfo.length; i++){
				if(ainfo[i]){
					bw.write("\t1");
				}
				else{
					bw.write("\t0");
				}
			}
			bw.write("\n");
		}
		
		bw.flush();
		bw.close();
	}
	
}
