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

public class ExportNetwork {
	

	public void toGML(Connection conn, long fid, String file, int min, int max) throws SQLException, IOException{
		ExportNode[] nodes = getNodes(conn, fid, min, max);
		ExportEdge[] edges = getEdges(conn, fid, min, max);
		
		File f = new File(file);
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(f));
		bw.write("graph [\ndirected 0\n");

		for(int i = 0; i < nodes.length; i++){
			ExportNode cn = nodes[i];
			bw.write("node [\n");
			bw.write("id "+cn.getNodeId()+"\n");
			bw.write("label \""+cn.getNodeId()+"\"\n");
			bw.write("position \""+cn.getLabel()+"\"\n");
			bw.write("]\n");
		}
		
		for(int i = 0; i < edges.length; i++){
			ExportEdge ce = edges[i];
			bw.write("edge [\n");
			bw.write("source "+ce.getSource()+"\n");
			bw.write("target "+ce.getDest()+"\n");
			bw.write("label \""+i+"\"\n");
			bw.write("position \""+ce.getLabel()+"\"\n");
			bw.write("]\n");
		}
		
		bw.write("]\n");
		bw.flush();
		bw.close();
		
	}
	
	private ExportNode[] getNodes(Connection conn, long fid, int min, int max) throws SQLException{
		String table = "chiapet.Nodes_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;
		String sql = "SELECT n.id, n.chr, n.start, n.end FROM "+table+" AS n, "+cctable+" AS cc WHERE n.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportNode> rv = new LinkedList<ExportNode>();
		while(rs.next()){
			int nid = rs.getInt(1);
			String chr = rs.getString(2);
			int start = rs.getInt(3);
			int end = rs.getInt(4);
			rv.add(new ExportNode(nid, chr, start, end));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportNode[0]);
	}
	
	private ExportEdge[] getEdges(Connection conn, long fid, int min, int max) throws SQLException{
		String table = "chiapet.Edges_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;

		String sql = "SELECT e.n1, e.n2, e.petcount, e.interactioncount FROM "+table+" AS e, "+cctable+" AS cc WHERE e.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, max);
		ps.setInt(2, min);
		ResultSet rs = ps.executeQuery();
		LinkedList<ExportEdge> rv = new LinkedList<ExportEdge>();
		while(rs.next()){
			int source = rs.getInt(1);
			int dest = rs.getInt(2);
			int petcount = rs.getInt(3);
			int intcount = rs.getInt(4);
			rv.add(new ExportEdge(source, dest, petcount, intcount));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ExportEdge[0]);
	}
	
	@SuppressWarnings("unused")
	private class ExportNode {
		
		private int _nodeid;
		private String _chr;
		private int _start;
		private int _end;
		
		public ExportNode(int nid, String chr, int start, int end){
			_nodeid = nid;
			_chr = chr;
			_start = start;
			_end = end;
		}
		
		public int getNodeId(){
			return _nodeid;
		}
		
		public String getChr(){
			return _chr;
		}
		
		public int getStart(){
			return _start;
		}
		
		public int getEnd(){
			return _end;
		}
		
		public String getLabel(){
			return _chr+":"+_start+"-"+_end;
		}
	}
	
	@SuppressWarnings("unused")
	private class ExportEdge {
		private int _src;
		private int _dest;
		private int _petcount;
		private int _intcount;
		
		public ExportEdge(int src, int dest, int petcount, int intcount){
			_src = src;
			_dest = dest;
			_petcount = petcount;
			_intcount = intcount;
		}
		
		public int getSource(){
			return _src;
		}
		
		public int getDest(){
			return _dest;
		}
		
		public int getPETCount(){
			return _petcount;
		}
		
		public int getInteractionCount(){
			return _intcount;
		}
		
		public String getLabel(){
			return "PET: "+_petcount+", Interactions: "+_intcount;
		}
	}
	
}
