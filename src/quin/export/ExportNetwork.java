package quin.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;

public class ExportNetwork {
	

	public void toGML(Connection conn, long fid, String file, int min, int max, Integer[] indices) throws SQLException, IOException{
		ExportNetworkInfo network = getNetworkInfo(conn, fid);
		ExportNode[] nodes = getNodes(conn, fid, min, max);
		ExportEdge[] edges = getEdges(conn, fid, min, max);
		ExportInteraction[] interactions = getInteractions(conn, fid);
		ExportComponent[] components = getComponents(conn, fid, min, max);
		
		String[] datasets = new String[indices.length];
		@SuppressWarnings("unchecked")
		TreeMap<Integer,Integer>[] annotations = new TreeMap[indices.length];
		Util u = new Util();
		for(int i = 0; i < indices.length; i++){
			datasets[i] = u.getDataset(conn, fid, indices[i]);
			annotations[i] = u.getAnnotationNodeIds(conn, fid, indices[i]);
		}

		File f = new File(file);
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(f));
		bw.write("graph [\ndirected 0\n");

		bw.write("cccount "+network.getCCCount()+"\n");
		bw.write("nodecount "+network.getNodeCount()+"\n");
		bw.write("edgecount "+network.getEdgeCount()+"\n");
		bw.write("avgcountnode "+network.getAvgCountN()+"\n");
		bw.write("avgcountedge "+network.getAvgCountE()+"\n");
		bw.write("extend "+network.getExtend()+"\n");
		bw.write("selfligation "+network.getSL()+"\n");
		bw.write("minpet "+network.getMinPET()+"\n");
		bw.write("maxdist "+network.getMaxDist()+"\n");
		bw.write("minpetmax "+network.getMinPETMax()+"\n");
		bw.write("minpetinterchrom "+network.getMinPETInterchrom()+"\n");
		bw.write("minsize "+network.getMinSize()+"\n");
		bw.write("minsizepet "+network.getMinSizePET()+"\n");

		bw.write("annotations [\n");
		for(int j = 0; j < datasets.length; j++){
			bw.write("a"+j+" \""+datasets[j]+"\"\n");
		}
		bw.write("]\n");
		
		for(int i = 0; i < nodes.length; i++){
			ExportNode cn = nodes[i];
			bw.write("node [\n");
			bw.write("id "+cn.getNodeId()+"\n");
			bw.write("ccid "+cn.getCCId()+"\n");
			bw.write("chr \""+cn.getChr()+"\"\n");
			bw.write("start "+cn.getStart()+"\n");
			bw.write("end "+cn.getEnd()+"\n");
			bw.write("count "+cn.getRCount()+"\n");
			bw.write("interactioncount "+cn.getICount()+"\n");
			bw.write("degree "+cn.getDegree()+"\n");
			bw.write("closeness "+cn.getCloseness()+"\n");
			bw.write("harmonic "+cn.getHarmonic()+"\n");
			bw.write("betweenness "+cn.getBetweenness()+"\n");
			bw.write("label \""+cn.getNodeId()+"\"\n");
			
			bw.write("annotations [\n");
			for(int j = 0; j < datasets.length; j++){
				bw.write("a"+j+" "+(annotations[j].containsKey(cn.getNodeId()) ? annotations[j].get(cn.getNodeId()) : 0)+"\n");
			}
			bw.write("]\n");

			
			bw.write("]\n");
		}
		
		for(int i = 0; i < edges.length; i++){
			ExportEdge ce = edges[i];
			bw.write("edge [\n");
			bw.write("id "+ce.getId()+"\n");
			bw.write("ccid "+ce.getCCId()+"\n");
			bw.write("source "+ce.getSource()+"\n");
			bw.write("target "+ce.getDest()+"\n");
			bw.write("count "+ce.getPETCount()+"\n");
			bw.write("interactioncount "+ce.getInteractionCount()+"\n");
			bw.write("label \""+ce.getId()+"\"\n");
			bw.write("]\n");
		}
		
		bw.write("interactions [\n");
		for(int i = 0; i < interactions.length; i++){
			ExportInteraction ci = interactions[i];
			bw.write("interaction_anchor [\n");
			bw.write("id "+ci.getId()+"\n");
			bw.write("iid "+ci.getIId()+"\n");
			bw.write("chr \""+ci.getChr()+"\"\n");
			bw.write("start "+ci.getStart()+"\n");
			bw.write("end "+ci.getEnd()+"\n");
			bw.write("count "+ci.getCount()+"\n");
			bw.write("nodeid "+ci.getNodeId()+"\n");
			bw.write("edgeid "+ci.getEdgeId()+"\n");
			bw.write("reason "+ci.getReason()+"\n");
			bw.write("distancetonode "+ci.getDistanceToNode()+"\n");
			bw.write("]\n");
		}
		bw.write("]\n");
		
		bw.write("components [\n");
		for(int i = 0; i < components.length; i++){
			ExportComponent cc = components[i];
			bw.write("component [\n");
			bw.write("id "+cc.getId()+"\n");
			bw.write("nodecount "+cc.getNodeCount()+"\n");
			bw.write("edgecount "+cc.getEdgeCount()+"\n");
			bw.write("]\n");
		}
		bw.write("]\n");
		
		bw.write("]\n");
		bw.flush();
		bw.close();
		
	}
	
	private ExportNetworkInfo getNetworkInfo(Connection conn, long fid) throws SQLException{
		String table = "chiapet.Network_"+fid;
		String sql = "SELECT n.cccount, n.nodecount, n.edgecount, n.avgpetnode, n.avgpetedge, n.extend, n.selfligation, n.minpet, n.maxdist, n.minpetmax, n.minpetinterchrom, n.minsize, n.minsizepet FROM "+table+" AS n";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		ExportNetworkInfo rv = null;
		if(rs.next()){
			rv = new ExportNetworkInfo(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getDouble(4), rs.getDouble(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12), rs.getInt(13));
		}
		rs.close();
		ps.close();
		return rv;
	}
	
	private ExportNode[] getNodes(Connection conn, long fid, int min, int max) throws SQLException{
		String table = "chiapet.Nodes_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;
		String sql = "SELECT n.id, n.ccid, n.chr, n.start, n.end, n.petcount, n.interactioncount, n.degree, n.closeness, n.harmonic, n.betweenness FROM "+table+" AS n, "+cctable+" AS cc WHERE n.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportNode> rv = new LinkedList<ExportNode>();
		while(rs.next()){
			rv.add(new ExportNode(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getDouble(9), rs.getDouble(10), rs.getDouble(11)));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportNode[0]);
	}
	
	private ExportInteraction[] getInteractions(Connection conn, long fid) throws SQLException{
		String table = "chiapet.Interactions_"+fid;
		String sql = "SELECT i.id, i.iid, i.chr, i.start, i.end, i.petcount, i.nodeid, i.edgeid, i.reason, i.dtonode  FROM "+table+" AS i";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportInteraction> rv = new LinkedList<ExportInteraction>();
		while(rs.next()){
			rv.add(new ExportInteraction(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getString(7), rs.getString(8), rs.getInt(9), rs.getInt(10)));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportInteraction[0]);
	}
	
	private ExportEdge[] getEdges(Connection conn, long fid, int min, int max) throws SQLException{
		String table = "chiapet.Edges_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;

		String sql = "SELECT e.id, e.ccid, e.n1, e.n2, e.petcount, e.interactioncount FROM "+table+" AS e, "+cctable+" AS cc WHERE e.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportEdge> rv = new LinkedList<ExportEdge>();
		while(rs.next()){
			rv.add(new ExportEdge(rs.getInt(1),rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportEdge[0]);
	}
	
	private ExportComponent[] getComponents(Connection conn, long fid, int min, int max) throws SQLException{
		String table = "chiapet.ConnectedComponents_"+fid;
		String sql = "SELECT cc.id, cc.nodecount, cc.edgecount FROM "+table+" AS cc WHERE cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportComponent> rv = new LinkedList<ExportComponent>();
		while(rs.next()){
			rv.add(new ExportComponent(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportComponent[0]);
	}
	
}
