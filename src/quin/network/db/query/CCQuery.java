package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;

import quin.web.networkjson.Edge;
import quin.web.networkjson.Network;
import quin.web.networkjson.Node;

public class CCQuery {

	public Network getCC(Connection conn, String schema, long networkfid, int ccid, int upstream, int downstream, Integer[] siids) throws SQLException{
		Node[] nodes = getNodes(conn, networkfid, "ucsc.hg19", ccid, upstream, downstream);
		Edge[] edges = getEdges(conn, networkfid, ccid);
		Network n = new Network();
		n.setNodes(nodes);
		n.setEdges(edges);
		addAnnotations(conn, nodes, schema, networkfid, siids);
		dropTMPNodeTable(conn);
		return n;
	}
	
	private void addAnnotations(Connection conn, Node[] nodes, String schema, long nfid, Integer[] siids) throws SQLException{
		String iltable = schema+".SIIndexList_"+nfid;
		String itable = schema+".SIIndex_"+nfid;
		
		TreeMap<Integer, Node> nodeindex = new TreeMap<Integer, Node>();
		for(int i = 0; i < nodes.length; i++){
			nodeindex.put(nodes[i].getId(), nodes[i]);
		}
		
		String 	vals = "it.iid IN(-1";
		if(siids.length > 0){
			for(int i = 0; i < siids.length; i++){
				vals += ",?";
			}
		}
		vals += ") AND";

		
		String sql = "SELECT DISTINCT n.id, il.dtype, il.did FROM TMP_nodes AS n, "+iltable+" AS il, "+itable+" AS it WHERE "+vals+" n.id=it.nid AND il.id=it.iid";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i = 0; i < siids.length; i++){
			ps.setInt(i+1, siids[i]);
		}
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){
			int nid = rs.getInt(1);
			int type = rs.getInt(2);
			long fid = rs.getLong(3);
			nodeindex.get(nid).addAnnotation(type+"_"+fid);
		}
		rs.close();
		ps.close();
		
		for(int i = 0; i < nodes.length; i++){
			nodes[i].finalizeAnnotations();
		}
	}
	
	private Edge[] getEdges(Connection conn, long fid, int ccid) throws SQLException {
		String edgetable = "chiapet.Edges_"+fid; // TODO
		String sql = "SELECT id, n1, n2, petcount, interactioncount FROM "
				+ edgetable + " WHERE ccid = ?";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, ccid);
		ResultSet rs = ps.executeQuery();

		LinkedList<Edge> l = new LinkedList<Edge>();
		while (rs.next()) {
			int id = rs.getInt(1);
			int n1 = rs.getInt(2);
			int n2 = rs.getInt(3);
			int petcount = rs.getInt(4);
			int icount = rs.getInt(5);

			Edge e = new Edge();
			e.setId(id);
			e.setNode1(n1);
			e.setNode2(n2);
			e.setPETCount(petcount);
			e.setInteractionCount(icount);

			l.add(e);
		}

		rs.close();
		ps.close();

		return l.toArray(new Edge[0]);
	}
	
	private void createTMPNodeTable(Connection conn, String nodetable, String genedb, int ccid, int upstream, int downstream) throws SQLException{		
		String sql;
		String genedbgid = genedb;
		if(upstream > 0 && downstream > 0){
			sql = "CREATE TEMPORARY TABLE TMP_nodes AS (SELECT DISTINCT n.id, n.chr, n.start, n.end, n.petcount, n.interactioncount, n.degree, n.closeness, n.harmonic, n.betweenness, g.genename, g.name FROM "
				+ " (SELECT * FROM "+nodetable +" WHERE ccid = ?) AS n LEFT OUTER JOIN "+genedbgid+" AS g ON "
				+ "g.chrom=n.chr AND ((n.start <= g.txstart+"+downstream+" AND g.txstart-"+upstream+" <= n.end AND g.strand='+') || (n.start <= g.txend+"+upstream+" AND g.txend-"+downstream+" <= n.end AND g.strand='-')))";
		}
		else{
			sql = "CREATE TEMPORARY TABLE TMP_nodes AS (SELECT DISTINCT n.id, n.chr, n.start, n.end, n.petcount, n.interactioncount, n.degree, n.closeness, n.harmonic, n.betweenness, g.genename, g.name FROM "
					+ " (SELECT * FROM "+nodetable +" WHERE ccid = ?) AS n LEFT OUTER JOIN "+genedbgid+" AS g ON "
					+ "g.chrom=n.chr AND n.start <= g.txend AND g.txstart <= n.end)";
		}
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, ccid);
		ps.execute();
		ps.close();
		
		sql = "ALTER TABLE TMP_nodes ADD geneid int";
		ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();

		sql = "UPDATE TMP_nodes AS n SET geneid=(SELECT DISTINCT g2refseq.gene_id FROM ncbi.gene2refseq AS g2refseq WHERE g2refseq.accession=n.name)";
		ps = conn.prepareStatement(sql);
		ps.executeUpdate();
		ps.close();
	}
	
	private void dropTMPNodeTable(Connection conn) throws SQLException{
		String sql = "DROP TABLE TMP_nodes";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}

	
	private Node[] getNodes(Connection conn, long fid, String genedb, int ccid, int upstream, int downstream) throws SQLException {
		String nodetable = "chiapet.Nodes_"+fid; // TODO
		
		createTMPNodeTable(conn, nodetable, genedb, ccid, upstream, downstream);
		
		String sql = "SELECT * FROM TMP_nodes";

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		TreeMap<Integer, Node> l = new TreeMap<Integer,Node>();
		while (rs.next()) {
			int id = rs.getInt(1);
			String chr = rs.getString(2);
			int start = rs.getInt(3);
			int end = rs.getInt(4);
			int petcount = rs.getInt(5);
			int icount = rs.getInt(6);
			int degree = rs.getInt(7);
			double closeness = rs.getDouble(8);
			double harmonic = rs.getDouble(9);
			double bc = rs.getDouble(10);
			String gene = rs.getString(11);
			String name = rs.getString(12);
			String geneid = rs.getString(13);
			if(l.containsKey(id)){
				Node n = l.get(id);
				if(gene != null ){
					n.addGeneSymbol(gene);
				}
				
				if(name != null){
					n.addGeneName(name);
				}
				
				if(geneid != null){
					n.addGeneId(geneid);
				}
			}
			else{
				Node n = new Node();
				n.setId(id);
				n.setChr(chr);
				n.setStart(start);
				n.setEnd(end);
				n.setPETCount(petcount);
				n.setInteractionCount(icount);
				n.setDegree(degree);
				n.setCloseness(closeness);
				n.setHarmonic(harmonic);
				n.setBetweenness(bc);
				if(gene != null ){
					n.addGeneSymbol(gene);
				}
				
				if(name != null){
					n.addGeneName(name);
				}
				if(geneid != null){
					n.addGeneId(geneid);
				}
				l.put(id,n);
			}
		}

		rs.close();
		ps.close();
		
		Node[] rv = l.values().toArray(new Node[0]);
		///addAnnotations(conn, rv, ext);

		return rv;
	}
	
	
}
