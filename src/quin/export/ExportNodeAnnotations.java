package quin.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import nearestgene.NearestTSSUtil;
import nearestgene.NearestTSSUtil.FlankingTSS;
import edu.ohsu.graphlet.core.BaseGraphletSignature;
import quin.network.Node;
import quin.network.analysis.GraphletCounterHelper;
import shortestpath.ShortestPathAnalysis;
import shortestpath.ShortestPathAnalysis.ComponentData;

public class ExportNodeAnnotations {

	
	//Exports a file where each row is a node and each column is 0 or 1 value for the annotations on that node
	
	public void writeFile(Connection conn, long fid, Integer[] sids, String sf, boolean graphletscores, boolean relative) throws SQLException, IOException{
		String nodetable = "chiapet.Nodes_"+fid;
		String cctable = "chiapet.ConnectedComponents_"+fid;

		ShortestPathAnalysis spa = new ShortestPathAnalysis(conn, fid);
		
		//String genedbgid = "ucsc."+genedb;
		
		String sql = "SELECT DISTINCT n.id, n.chr, n.start, n.end, n.degree, n.closeness, n.harmonic, n.betweenness, CAST(n.degree AS decimal(64,10))/(cc.nodecount-1), n.closeness*(cc.nodecount-1), n.harmonic/(cc.nodecount-1), (CASE WHEN cc.nodecount < 3 THEN 0 ELSE (n.betweenness/((cc.nodecount-1)*(cc.nodecount-2))) END), cc.nodecount  FROM "+nodetable+" AS n, "+cctable+" AS cc WHERE n.ccid = cc.id";
		
		String[] datasets = new String[sids.length];
		@SuppressWarnings("unchecked")
		TreeMap<Integer, Integer>[] na = new TreeMap[sids.length];
		Util u = new Util();
		for(int i = 0; i < sids.length; i++){
			datasets[i] = u.getDataset(conn, fid, sids[i]);
			na[i] = u.getAnnotationNodeIds(conn, fid, sids[i]);
		}
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();

		TreeMap<Integer, String[]> nodeinformation = new TreeMap<Integer, String[]>();
		
		while(rs.next()){
			int nid = rs.getInt(1);
			String chr = rs.getString(2);
			int start = rs.getInt(3);
			int end = rs.getInt(4);
			int degree = rs.getInt(5);
			double closeness = rs.getDouble(6);
			double harmonic = rs.getDouble(7);
			double betweenness = rs.getDouble(8);
			double ndegree = rs.getDouble(9);
			double ncloseness = rs.getDouble(10);
			double nharmonic = rs.getDouble(11);
			double nbetweenness = rs.getDouble(12);
			int ccnodecount = rs.getInt(13);

			if(!nodeinformation.containsKey(nid)){
				nodeinformation.put(nid, new String[] {Integer.toString(nid), chr, Integer.toString(start), Integer.toString(end), Integer.toString(degree), Double.toString(closeness), Double.toString(harmonic), Double.toString(betweenness), Double.toString(ndegree), Double.toString(ncloseness), Double.toString(nharmonic), Double.toString(nbetweenness), Integer.toString(ccnodecount), Integer.toString(end-start)});
			}
		}
		
		rs.close();
		ps.close();
		
		File f = new File(sf);
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("Node ID");
		bw.write("\tChr");
		bw.write("\tStart");
		bw.write("\tEnd");
		bw.write("\tDegree");
		bw.write("\tCloseness");
		bw.write("\tHarmonic");
		bw.write("\tBetweenness");
		bw.write("\tNormalized Degree");
		bw.write("\tNormalized Closeness");
		bw.write("\tNormalized Harmonic");
		bw.write("\tNomralized Betweenness");
		bw.write("\tComponent Size");
		bw.write("\tNode Size");
		bw.write("\tNode Size Relative To Neighbors");
		bw.write("\tAVG PET");
		bw.write("\tUpstream TSS");
		bw.write("\tUpstream TSS Distance");
		bw.write("\tUpstream TSS Strand");
		bw.write("\tDownstream TSS");
		bw.write("\tDownstream TSS Distance");
		bw.write("\tDownstream TSS Strand");
		
		if(graphletscores){
			for(int i = 0; i < 73; i++){
				bw.write("\tOrbit "+i);
			}
		}

		for(int i = 0; i < datasets.length; i++){
			bw.write("\t"+datasets[i]);
		}
		
		//DirectionInteraction count info
		//bw.write("\tComponent Id");
		for(int i = 0; i < datasets.length; i++){
			bw.write("\t"+datasets[i]+" Interactions");
		}
		
		bw.write("\n");
		
		GraphletCounterHelper gch = new GraphletCounterHelper();
		TreeMap<Integer, Node> nodes = spa.getNodes();
		
		//ComponentData cd = spa.getComponents();
		//TreeMap<Integer,Integer> nodetoccid = cd.nodetoccid;
		//int[][] componentannotations = getAnnotationCount(na, cd);
		
		NearestTSSUtil ntss = new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
		
		while(!nodeinformation.isEmpty()){
			Entry<Integer, String[]> entry = nodeinformation.pollFirstEntry();
			int nid = entry.getKey();
			String[] ninfo = entry.getValue();

			bw.write(ninfo[0]);
			bw.write("\t"+ninfo[1]);
			bw.write("\t"+ninfo[2]);
			bw.write("\t"+ninfo[3]);
			bw.write("\t"+ninfo[4]);
			bw.write("\t"+ninfo[5]);
			bw.write("\t"+ninfo[6]);
			bw.write("\t"+ninfo[7]);
			bw.write("\t"+ninfo[8]);
			bw.write("\t"+ninfo[9]);
			bw.write("\t"+ninfo[10]);
			bw.write("\t"+ninfo[11]);
			bw.write("\t"+ninfo[12]);
			bw.write("\t"+ninfo[13]);
			bw.write("\t"+spa.getRelativeNodeSize(nid));
			bw.write("\t"+spa.getAVGPET(nid));
			
			
			Node curnode = nodes.get(nid);
			
			FlankingTSS ftss = ntss.getFlankingTSS(curnode.getChr(), curnode.getStart(), curnode.getEnd());
			bw.write("\t"+ftss.getUpStreamTSS());
			bw.write("\t"+ftss.getUpstreamDistance());
			bw.write("\t"+ftss.getUpstreamStrand());
			bw.write("\t"+ftss.getDownStreamTSS());
			bw.write("\t"+ftss.getDownstreamDistance());
			bw.write("\t"+ftss.getDownstreamStrand());
			
			if(graphletscores){
				BaseGraphletSignature sig = gch.getGraphletSignature(curnode);
				double[] wc = sig.getWeightedCounts();
				double sum = 1;
				if(relative){
					sum = 0;
					for(int i = 0; i < 73; i++){
						sum += wc[i];
					}
					sum = sum == 0 ? 1 : sum;
				}
				
				for(int i = 0; i < 73; i++){
					bw.write("\t"+Double.toString(wc[i]/sum));
				}
			}
			
			for(int i = 0; i < datasets.length; i++){
				bw.write("\t"+(na[i].containsKey(nid) ? na[i].get(nid) : 0));
			}
			
			//Write annotation interaction data
			//bw.write("\t"+ccid);
			int[] counts = new int[datasets.length];
			Integer[] de = spa.getConnectedNodes(nid);//Direct Edges
			for(int i = 0; i < de.length; i++){
				for(int j = 0; j < datasets.length; j++){
					if(na[j].containsKey(de[i])){
						counts[j]++;
					}
				}
			}
			for(int i = 0; i < datasets.length; i++){
				bw.write("\t"+(counts[i]));
			}
			bw.write("\n");
		}
		
		bw.flush();
		bw.close();
	}
	
	private int[][] getAnnotationCount(TreeMap<Integer, Integer>[] na, ComponentData cd){
		//Generate CCXA array of annotation counts
		TreeMap<Integer, Node[]> cc = cd.components;
		int ccl = cc.size();
		int[][] rv = new int[ccl][na.length];
		
		for(Iterator<Entry<Integer, Node[]>> it = cc.entrySet().iterator(); it.hasNext();){
			Entry<Integer, Node[]> cur = it.next();
			int ccid = cur.getKey();
			Node[] ccnl = cur.getValue();
			for(int j = 0; j < ccnl.length; j++){
				Node curnode = ccnl[j];
				int nid = curnode.getIntegerId();
				for(int k = 0; k < na.length; k++){
					if(na[k].containsKey(nid)){
						rv[ccid][k]++;
					}
					
				}
			}
		}
		
		return rv;
	}
	
}
