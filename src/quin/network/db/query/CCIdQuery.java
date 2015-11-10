package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class CCIdQuery {

	public Integer[] getCCIds(Connection conn, String schema,  long fid, Integer[] indices, int sortby, int maxsize, int minsize, boolean include) throws SQLException{
		String cctable = schema+".ConnectedComponents_"+fid;
		String nodetable = schema+".Nodes_"+fid;
		String indextable = schema+".SIIndex_"+fid;
		
		PreparedStatement ps;
		if(indices.length > 0){
			String orderby = "cc.nodecount";
			if(sortby == 2){
				orderby = "cc.edgecount";
			}
			else if(sortby == 3){
				if(include){
					orderby = "acount";
				}
				else{
					orderby = "count(DISTINCT i.nid)";
				}
			}
			
			String vals = "?";
			for(int i = 1; i < indices.length; i++){
				vals += ",?";
			}
			
			String sql;
			if(include){
				String subtable = "(SELECT DISTINCT cc.id AS id, cc.nodecount AS nodecount, cc.edgecount AS edgecount, (SELECT count(DISTINCT i.nid) FROM "+nodetable+" AS n, "+indextable+" AS i WHERE i.nid=n.id AND i.iid IN("+vals+") AND cc.nodecount <= ? AND cc.nodecount >= ? AND cc.id = n.ccid) AS acount FROM "+cctable+" AS cc)";
				sql = "SELECT cc.id FROM "+subtable+" AS cc ORDER BY "+orderby+" DESC";
			}
			else{
				sql = "SELECT DISTINCT cc.id FROM "+nodetable+" AS n, "+cctable+" AS cc, "+indextable+" AS i WHERE i.nid=n.id AND i.iid IN("+vals+") AND cc.nodecount <= ? AND cc.nodecount >= ? AND cc.id = n.ccid GROUP BY cc.id ORDER BY "+orderby+" DESC";
			}
			
			ps = conn.prepareStatement(sql);
			for(int i = 0; i < indices.length; i++){
				ps.setInt(i+1, indices[i]);
			}
		}
		else{
			String orderby = "nodecount";
			if(sortby == 2){
				orderby = "edgecount";
			}
			
			String sql = "SELECT id FROM "+cctable+" WHERE nodecount <= ? AND nodecount >= ? ORDER BY "+orderby+" DESC";
			ps = conn.prepareStatement(sql);
		}
		
		ps.setInt(indices.length+1, maxsize);
		ps.setInt(indices.length+2, minsize);

		ResultSet rs = ps.executeQuery();
		LinkedList<Integer> l = new LinkedList<Integer>();
		while(rs.next()){
			l.add(rs.getInt(1));
		}
		
		return l.toArray(new Integer[0]);
	}
	
	
	
	public Integer[] getPromoterCCIds(Connection conn, String schema,  long fid, int sortby, int maxsize, int minsize, boolean include, int upstream, int downstream) throws SQLException{
//		String cctable = schema+".ConnectedComponents_"+fid;
//		String nodetable = schema+".Nodes_"+fid;
//		String indextable = schema+".SIIndex_"+fid;

		PreparedStatement ps;

		String orderby = "nodecount";
		if(sortby == 2){
			orderby = "edgecount";
		}
	
		String sql = "SELECT DISTINCT n.ccid FROM chiapet.Nodes_"+fid+" AS n, ucsc.hg19 AS g, chiapet.ConnectedComponents_"+fid+" as cc WHERE cc.nodecount <= ? AND cc.nodecount >= ? AND cc.id = n.ccid AND "
				+ "g.chrom=n.chr AND "
				+"((n.start <= g.txstart+"+upstream+" AND g.txstart-"+downstream+" <= n.end AND g.strand='+')"
				+"|| (n.start <= g.txend+"+downstream+" AND g.txend-"+upstream+" <= n.end AND g.strand='-'))  ORDER BY "+orderby+" DESC";
		
		ps = conn.prepareStatement(sql);
		
		ps.setInt(1, maxsize);
		ps.setInt(2, minsize);

		ResultSet rs = ps.executeQuery();
		LinkedList<Integer> l = new LinkedList<Integer>();
		while(rs.next()){
			l.add(rs.getInt(1));
		}
		
		return l.toArray(new Integer[0]);
	}
	
}
