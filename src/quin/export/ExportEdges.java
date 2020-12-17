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

import nearestgene.NearestTSSUtil;
import nearestgene.NearestTSSUtil.FlankingTSS;

public class ExportEdges {
	

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
		
		
		/*for(int i = 0; i < nodes.length; i++){
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
		}*/
		bw.write("Chr\tStart\tEnd\tAnnotation\tUpstream TSS\tUpstream TSS Distance\tDownstream TSS\tDownstream TSS Distance\tChr\tStart\tEnd\tAnnotation\tUpstream TSS\tUpstream TSS Distance\tDownstream TSS\tDownstream TSS Distance\tInteraction Count\tPET Count\tCCID\n");

		NearestTSSUtil ntss = new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");

		for(int i = 0; i < edges.length; i++){
			ExportEdge ce = edges[i];
			ExportNode source = nodemap.get(ce.getSource());
			ExportNode dest = nodemap.get(ce.getDest());
			
			
			FlankingTSS stss = ntss.getFlankingTSS(source.getChr(), source.getStart(), source.getEnd());
			FlankingTSS dtss = ntss.getFlankingTSS(dest.getChr(), dest.getStart(), dest.getEnd());

			/*bw.write("\t"+ftss.getUpStreamTSS());
			bw.write("\t"+ftss.getUpstreamDistance());
			bw.write("\t"+ftss.getUpstreamStrand());
			bw.write("\t"+ftss.getDownStreamTSS());
			bw.write("\t"+ftss.getDownstreamDistance());
			bw.write("\t"+ftss.getDownstreamStrand());*/
			
			String sourcea = "";
			String desta = "";
			for(int j = 0; j < annotations.length; j++){
				if(annotations[j].containsKey(ce.getSource())){
					sourcea += datasets[j]+",";
				}
				if(annotations[j].containsKey(ce.getDest())){
					desta += datasets[j]+",";
				}
			}
			
			bw.write(source.getChr()+"\t"+source.getStart()+"\t"+source.getEnd()+"\t"+sourcea);
			bw.write("\t"+stss.getUpStreamTSS());
			bw.write("\t"+stss.getUpstreamDistance());
			bw.write("\t"+stss.getDownStreamTSS());
			bw.write("\t"+stss.getDownstreamDistance());
			bw.write("\t"+dest.getChr()+"\t"+dest.getStart()+"\t"+dest.getEnd()+"\t"+desta);
			bw.write("\t"+dtss.getUpStreamTSS());
			bw.write("\t"+dtss.getUpstreamDistance());
			bw.write("\t"+dtss.getDownStreamTSS());
			bw.write("\t"+dtss.getDownstreamDistance());
			bw.write("\t"+ce.getInteractionCount()+"\t"+ce.getPETCount()+"\t"+ce.getCCId()+"\n");

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
