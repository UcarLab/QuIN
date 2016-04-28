package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import quin.network.Anchor;
import quin.network.Node;
import nearestgene.NearestTSSUtil;
import nearestgene.TSS;
import nearestgene.TSSGene;

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
	
		String sql = "SELECT DISTINCT n.chr, n.start, n.end, n.ccid FROM chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" as cc WHERE cc.nodecount <= ? AND cc.nodecount >= ? AND cc.id = n.ccid ORDER BY "+orderby+" DESC";
		
		ps = conn.prepareStatement(sql);
		
		ps.setInt(1, maxsize);
		ps.setInt(2, minsize);

		ResultSet rs = ps.executeQuery();
		LinkedList<Node> l = new LinkedList<Node>();
		while(rs.next()){
			Node n = new Node(0, rs.getString(1), rs.getInt(2), rs.getInt(3), new Anchor[0]);
			n.setCCId(rs.getInt(4));
			l.add(n);
		}
		
		NearestTSSUtil nu = new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
		
		TreeSet<Integer> rv = new TreeSet<Integer>();
		
		for(Iterator<Node> it = l.iterator(); it.hasNext();){
			Node next = it.next();
			if(inTSS(nu, upstream, downstream, next.getChr(), next.getStart(), next.getEnd())){
				rv.add(next.getCCId());
			}
		}
		
		return rv.toArray(new Integer[0]);
	}
	
	private boolean inTSS(NearestTSSUtil nu, int upstream, int downstream, String chr, int start, int end){
		TSSGene[] genes = nu.getNearestGene(chr, start, end);
		for(int i = 0; i < genes.length; i++){
			TSSGene cg = genes[i];
			TSS[] tss = nu.getTSS(cg.getGene());
			
			for(int j = 0; j < tss.length; j++){
				TSS ctss = tss[j];
				int gs, ge;
				if(ctss.strand.equals("+")){
					gs = ctss.tss-upstream;
					ge = ctss.tss+downstream;
				}
				else{
					gs = ctss.tss-downstream;
					ge = ctss.tss+upstream;
				}
				
				if(end >= gs && start <= ge){
					return true;
				}
			}

		}
		
		return false;
	}
	
}
