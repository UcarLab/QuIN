package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class SearchQuery {

	public int[][] searchByPosition(Connection conn, String schema, long fid, String chr, int start, int end) throws SQLException{
		String nodetable = schema+".Nodes_"+fid;
		
		String sql = "SELECT DISTINCT n.ccid, n.id FROM "+nodetable+" AS n WHERE n.chr=? AND n.end >= ? AND n.start <= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, chr);
		ps.setInt(2, start);
		ps.setInt(3, end);
		ResultSet rs = ps.executeQuery();
		LinkedList<int[]> l  = new LinkedList<int[]>();
		while(rs.next()){
			l.add(new int[]{ rs.getInt(1), rs.getInt(2) });
		}
		
		return l.toArray(new int[0][]);
	}
	
	
	public int[][] searchByRSID(Connection conn, String schema, long fid, String dbsnptable, int rsid) throws SQLException{
		String nodetable = schema+".Nodes_"+fid;
		String sql = "SELECT DISTINCT n.ccid, n.id FROM "+nodetable+" AS n, (SELECT * FROM "+dbsnptable+" WHERE rsid=?) AS d WHERE n.chr=d.chr AND n.end >= d.start AND n.start <= d.end";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, rsid);
		ResultSet rs = ps.executeQuery();
		LinkedList<int[]> l  = new LinkedList<int[]>();
		while(rs.next()){
			l.add(new int[]{ rs.getInt(1), rs.getInt(2) });
		}
		return l.toArray(new int[0][]);
	}
	
	public int[][] searchByGeneName(Connection conn, String schema, long fid, String genedb, String genename, int upstream, int downstream) throws SQLException{
		String nodetable = schema+".Nodes_"+fid;
		
		String sql;
		if(upstream > 0 && downstream > 0){
			sql = "SELECT DISTINCT n.ccid, n.id FROM "
				+ nodetable + " AS n, "+genedb+" AS g WHERE "
				+ "g.genename=? AND g.chrom=n.chr AND ((n.start <= g.txstart+"+downstream+" AND g.txstart-"+upstream+" <= n.end AND g.strand='+') || (n.start <= g.txend+"+upstream+" AND g.txend-"+downstream+" <= n.end AND g.strand='-'))";
		}
		else{
			sql = "SELECT DISTINCT n.ccid, n.id FROM "
					+ nodetable + " AS n, "+genedb+" AS g WHERE "
					+ "g.genename=? AND cc.id = n.ccid AND g.chrom=n.chr AND n.start <= g.txend AND g.txstart <= n.end";
		}
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, genename);
		
		ResultSet rs = ps.executeQuery();
		LinkedList<int[]> l  = new LinkedList<int[]>();
		while(rs.next()){
			l.add(new int[]{ rs.getInt(1), rs.getInt(2) });
		}
		
		return l.toArray(new int[0][]);
	}
}
