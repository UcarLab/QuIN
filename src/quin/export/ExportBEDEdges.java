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


public class ExportBEDEdges {
	

	public void exportEdges(Connection conn, long fid, String file, int min, int max, Integer[] indices) throws SQLException, IOException{
		ExportNode[] nodes = getNodes(conn, fid, min, max);
		ExportEdge[] edges = getEdges(conn, fid, min, max);
		
		TreeMap<Integer, ExportNode> nodemap = new TreeMap<Integer, ExportNode>();
		
		for(int i = 0; i < nodes.length; i++){
			nodemap.put(nodes[i].getNodeId(), nodes[i]);
		}
		
		
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
		
		

		for(int i = 0; i < edges.length; i++){
			ExportEdge ce = edges[i];
			ExportNode source = nodemap.get(ce.getSource());
			ExportNode dest = nodemap.get(ce.getDest());
			
			
			bw.write(source.getChr()+"\t"+source.getStart()+"\t"+dest.getEnd());
			bw.write("\t"+source.getChr()+":"+source.getStart()+".."+source.getEnd()+"-"+dest.getChr()+":"+dest.getStart()+".."+dest.getEnd()+","+ce.getPETCount());
			bw.write("\t"+ce.getPETCount()*100);
			bw.write("\t.");
			bw.write("\t"+source.getStart()+"\t"+dest.getEnd());
			bw.write("\t255,0,0\t2");
			bw.write("\t"+(source.getEnd()-source.getStart())+","+(dest.getEnd()-dest.getStart())+"\t0,"+(dest.getStart()-source.getStart())+"\n");

		}
		bw.flush();
		bw.close();
		
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
	
}
