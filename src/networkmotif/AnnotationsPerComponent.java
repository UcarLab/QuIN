package networkmotif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import quin.export.Util;

public class AnnotationsPerComponent {
	
	public void writeFile(Connection conn, long fid, int upstream, int downstream, Integer[] sids, String sf, int min, int max) throws SQLException, IOException{
		String sql = "SELECT c.id, c.nodecount, COALESCE(p.promoternodes, 0) AS promoternodes, p.promoters ";
		for(int i = 0; i < sids.length; i++){
			sql += ","+getAnnotationSQL(fid, sids[i]);
		}
		sql += " FROM chiapet.ConnectedComponents_"+fid+" AS c LEFT OUTER JOIN (SELECT n.ccid, count( DISTINCT n.id) AS promoternodes, GROUP_CONCAT(DISTINCT g.genename SEPARATOR ',') AS promoters "
		+ " FROM chiapet.Nodes_"+fid+" AS n, ucsc.hg19 AS g  WHERE "
		+ "	g.chrom=n.chr AND "
		+ "	((n.start <= g.txstart+2000 AND g.txstart-2000 <= n.end AND g.strand='+')"
		+ " || (n.start <= g.txend+2000 AND g.txend-2000 <= n.end AND g.strand='-')) GROUP BY n.ccid) AS p ON p.ccid=c.id WHERE c.nodecount <= ? AND c.nodecount >= ?";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);

		ResultSet rs = ps.executeQuery();
		File f = new File(sf);
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("Component ID\t");
		bw.write("Nodes in Component\t");
		bw.write("Promoters in Component\t");
		
		Util u = new Util();
		String[] labels = u.getLabels(conn, fid, sids);
		
		for(int i = 0; i < sids.length; i++){
			bw.write(labels[i]+" in Component\t");
		}
		bw.write("Promoter Genes\n");

		while(rs.next()){
			bw.write(rs.getString(1)+"\t");
			bw.write(rs.getString(2)+"\t");
			bw.write(rs.getString(3)+"\t");
			for(int i = 0; i < sids.length; i++){
				bw.write(rs.getString(5+i)+"\t");
			}
			bw.write(rs.getString(4)+"\n");
		}
		bw.flush();
		bw.close();
		rs.close();
		ps.close();
		
	}
	
	private String getAnnotationSQL(long fid, int index){
		return " (SELECT count(DISTINCT n.id) FROM chiapet.ConnectedComponents_"+fid+" AS cc, chiapet.Nodes_"+fid+" AS n, chiapet.SIIndex_"+fid+" AS si WHERE si.iid="+index+" AND n.id = si.nid AND n.ccid=cc.id AND c.id=cc.id) AS si_"+index+" ";
	}
	

	
}
