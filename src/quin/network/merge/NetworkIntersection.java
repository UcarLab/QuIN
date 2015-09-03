package quin.network.merge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import quin.network.Anchor;
import quin.network.ChIAPETNetwork;
import quin.network.ConnectedComponent;
import quin.network.ConnectedComponentExtraction;
import quin.network.Edge;
import quin.network.Interaction;
import quin.network.NetworkParameters;
import quin.network.Node;
import quin.network.Util;

public class NetworkIntersection {
	
	private int _aid;
	private int _iid;
	
	public ChIAPETNetwork getIntersection(Connection conn, long n1, long n2) throws Exception{
		_aid = 0;
		_iid = 0;
		
		InteractionData i1 = getAnchors(conn, n1);
		InteractionData i2 = getAnchors(conn, n2);
		
		TreeMap<Integer, LinkedList<Anchor>> n1anchors = i1.nodetoanchors;
		TreeMap<Integer, LinkedList<Anchor>> n2anchors = i2.nodetoanchors;
		
		String t1 = "chiapet.Nodes_"+n1;
		String t2 = "chiapet.Nodes_"+n2;

		String sql = "SELECT DISTINCT n1.id AS n1id, n2.id AS n2id FROM "+t1+" AS n1, "+t2+" AS n2 WHERE n1.chr=n2.chr AND n1.end >= n2.start AND n2.end >= n1.start";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		NodeIntersection ni = new NodeIntersection();
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			int n1id = rs.getInt(1);
			int n2id = rs.getInt(2);
			ni.addIntersection(n1id, n2id);
		}
		rs.close();
		ps.close();
		
		TreeMap<Integer, Integer> c1 = ni.getN1EdgeMapping();
		TreeMap<Integer, Integer> c2 = ni.getN2EdgeMapping();
		TreeMap<Integer, Node> nodes = ni.getOuterNodes(n1anchors, n2anchors);
		
		int[] counts = ni.getTotalIntersectingNodes();
		System.out.println("N1 Intersection: "+counts[0]+"N2 Intersection: "+counts[1]);
		
		String[] iedges = getIntersectionEdgeMap(conn, n1, toArray(c1), n2, toArray(c2));

		LinkedList<Edge> edges = new LinkedList<Edge>();
		int eid = 0;
		for(int i = 0; i < iedges.length; i++){
			String cur = iedges[i];
			String[] split = cur.split("_");
			Node node1 = nodes.get(Integer.parseInt(split[0]));
			Node node2 = nodes.get(Integer.parseInt(split[1]));

			TreeMap<Integer, Interaction> interactions = new TreeMap<Integer,Interaction>();
			Anchor[] a = node1.getAnchors();
			for(int j = 0; j < a.length; j++){
				Interaction inter = a[j].getInteraction();
				interactions.put(inter.getId(), inter);
			}
			a = node2.getAnchors();
			for(int j = 0; j < a.length; j++){
				Interaction inter = a[j].getInteraction();
				interactions.put(inter.getId(), inter);
			}
			
			LinkedList<Interaction> edgeinteractions = new LinkedList<Interaction>();
			for(Iterator<Interaction> it = interactions.values().iterator(); it.hasNext();){
				Interaction next = it.next();
				Anchor[] ia = next.getAnchors();
				Node a0n = ia[0].getNode();
				Node a1n = ia[1].getNode();
				if(a0n != null && a0n.equals(node1)){
					if(a1n != null && a1n.equals(node2)){
						edgeinteractions.add(next);
					}
				}
				else if(a0n != null && a0n.equals(node2)){
					if(a1n != null && a1n.equals(node1)){
						edgeinteractions.add(next);
					}
				}
			}
			if(edgeinteractions.size() > 1){
				Edge e = new Edge(eid++, node1, node2, edgeinteractions.toArray(new Interaction[0]));
				for(Iterator<Interaction> it = edgeinteractions.iterator(); it.hasNext();){
					it.next().setEdge(e);
				}
				edges.add(e);
				node1.addEdge(e);
				node2.addEdge(e);
			}
			else {
				throw new Exception("Expecting at least two interactions.");
			}
		}
		
		i1.anchors.addAll(i2.anchors);
		Anchor[] allanchors = i1.anchors.toArray(new Anchor[0]);
		i1.interactions.addAll(i2.interactions);
		Interaction[] allinteractions = i1.interactions.toArray(new Interaction[0]);

		Node[] nodearray = nodes.values().toArray(new Node[0]);
		Util u = new Util();
		nodearray = u.filterNodes(nodearray);
		
		ConnectedComponentExtraction cce = new ConnectedComponentExtraction();
		ConnectedComponent[] components = cce.getCCs(nodearray, allinteractions.length+1, 0, 0);
		
		
		return new ChIAPETNetwork(filterAnchors(allanchors), filterInteractions(allinteractions), nodearray, edges.toArray(new Edge[0]), components, new NetworkParameters(-1, -1, -1, -1, -1, -1, -1, -1));
	}
	
	private Anchor[] filterAnchors(Anchor[] anchors){
		LinkedList<Anchor> l = new LinkedList<Anchor>();
		for(int i = 0; i < anchors.length; i++){
			Anchor a = anchors[i];
			if(a.getNode() != null){
				l.add(a);
			}
		}
		return l.toArray(new Anchor[0]);
	}
	
	private Interaction[] filterInteractions(Interaction[] interactions){
		LinkedList<Interaction> l = new LinkedList<Interaction>();
		for(int i = 0; i < interactions.length; i++){
			Interaction inter = interactions[i];
			if(inter.getEdge() != null){
				l.add(inter);
			}
		}
		return l.toArray(new Interaction[0]);
	}
	
	private int[] toArray(TreeMap<Integer, Integer> map){
		int[] rv = new int[map.lastKey()+1];
		for(int i = 0; i < rv.length; i++){
			rv[i] = -1;
		}
		while(!map.isEmpty()){
			Entry<Integer, Integer> e = map.pollFirstEntry();
			rv[e.getKey()] = e.getValue();
		}
		return rv;
	}
	
	
	private InteractionData getAnchors(Connection conn, long n) throws SQLException{
		String t = "chiapet.Interactions_"+n;
		String sql = "SELECT i.nodeid, i.chr, i.start, i.end, i.petcount FROM "+t+" AS i ORDER BY i.id";
		
		TreeMap<Integer, LinkedList<Anchor>> nta = new TreeMap<Integer, LinkedList<Anchor>>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		LinkedList<Anchor> anchors = new LinkedList<Anchor>();
		LinkedList<Interaction> interactions = new LinkedList<Interaction>();
		
		while(rs.next()){
			int nodeid1 = rs.getInt(1);
			String chr1 = rs.getString(2);
			int start1 = rs.getInt(3);
			int end1 = rs.getInt(4);
			
			if(!nta.containsKey(nodeid1)){
				nta.put(nodeid1, new LinkedList<Anchor>());
			}
			
			Anchor a1 = new Anchor(_aid++, chr1, start1, end1);
			anchors.add(a1);
			nta.get(nodeid1).add(a1);
			
			rs.next();
			int nodeid2 = rs.getInt(1);
			String chr2 = rs.getString(2);
			int start2 = rs.getInt(3);
			int end2 = rs.getInt(4);
			int petcount = rs.getInt(5);
			
			if(!nta.containsKey(nodeid2)){
				nta.put(nodeid2, new LinkedList<Anchor>());
			}
			
			Anchor a2 = new Anchor(_aid++, chr2, start2, end2);
			anchors.add(a2);
			nta.get(nodeid2).add(a2);

			interactions.add(new Interaction(_iid++, a1, a2, petcount));			
		}
		
		rs.close();
		ps.close();
		
		InteractionData rv = new InteractionData();
		rv.nodetoanchors = nta;
		rv.anchors = anchors;
		rv.interactions = interactions;
		
		return rv;
	}
	
	private String[] getIntersectionEdgeMap(Connection conn, long n1, int[] c1, long n2, int[] c2) throws SQLException{
		
		TreeSet<String> em1 = getEdgeMap(conn, c1, n1);
		TreeSet<String> em2 = getEdgeMap(conn, c2, n2);
		
		TreeSet<String> rv = new TreeSet<String>();
		while(!em1.isEmpty()){
			String next = em1.pollFirst();
			if(em2.contains(next)){
				rv.add(next);
			}
		}
		return rv.toArray(new String[0]);
	}
	
	private TreeSet<String> getEdgeMap(Connection conn, int[] conversion, long fid) throws SQLException{
		String edgetable = "chiapet.Edges_"+fid;
		String sql = "SELECT n1, n2 FROM "+edgetable;
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		TreeSet<String> rv = new TreeSet<String>();
		
		while(rs.next()){
			int n1 = rs.getInt(1);
			int n2 = rs.getInt(2);

			if(n1 < conversion.length && n2 < conversion.length){
				int cn1 = conversion[n1];
				int cn2 = conversion[n2];
				if(cn1 > cn2){
					int t = cn1;
					cn1 = cn2;
					cn2 = t;
				}
				
				if(cn1 > -1 && cn2 > -1 && cn1 != cn2){
					rv.add(cn1+"_"+cn2);
				}
			}
		}
		
		rs.close();
		ps.close();
		
		return rv;
	}
	
	
	private class InteractionData {
		public LinkedList<Anchor> anchors;
		public LinkedList<Interaction> interactions;
		public TreeMap<Integer, LinkedList<Anchor>> nodetoanchors;
	}
}
