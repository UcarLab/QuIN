package quin.network.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tomcat.util.codec.binary.Base64;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import quin.export.Util;
import quin.network.Location;
import quin.web.networkjson.Edge;

public class AnnotationInteractionEnrichment {

	public AIEJson generateHeatmap(Connection conn, long fid, Integer[] indices, int min, int max, String testtype, int npermutes) throws SQLException, REngineException, REXPMismatchException{
		String[] labels = getLabels(conn, fid, indices);
		InteractionMatrix im = getMatrix(conn, fid, indices, min, max, testtype, npermutes);

		int[][] cm = im.getCountMatrix();
		int numedges = im.getEdgeCount();
		
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=1200, height=600)");
		
		int l = cm.length;
		rconn.parseAndEval("heatmapmat = matrix(nrow="+l+", ncol="+l+")");

		String[][] significance = new String[l][l];
		double[][] em = im.getTheoreticalMatrix();
		if(testtype.equals("p")){
			em = im.getExpectedMatrix();
		}
		
		//Reachable r = im.getReachable();
		//int totalpossiblesum = r.getTotalEdgesSum();
		//int[][] aedges = r.getAnnotatedMatrixSum();
		
		double maxv = 0;
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				int count = cm[i][j];
				double expected = em[i][j];
					double ratio = 0;
					if(count != 0 && expected != 0){
						ratio = Math.log(count/expected)/Math.log(2);
					}
					rconn.parseAndEval("heatmapmat["+(i+1)+","+(j+1)+"] = "+ratio);
					maxv = Math.max(Math.abs(Math.ceil(ratio)), maxv);
					double less = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+em[i][j]/numedges+", alternative=\"less\")$p.value").asDouble();
					double greater = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+em[i][j]/numedges+", alternative=\"greater\")$p.value").asDouble();

					//less = rconn.parseAndEval("phyper("+cm[i][j]+", "+aedges[i][j]+", "+(totalpossiblesum-aedges[i][j])+", "+numedges+", lower.tail=TRUE)").asDouble();
					//greater = rconn.parseAndEval("phyper("+cm[i][j]+", "+(aedges[i][j]-1)+", "+(totalpossiblesum-aedges[i][j]+1)+", "+numedges+", lower.tail=FALSE)").asDouble();

					if(less < greater){
						significance[i][j] = Double.toString(-less);
					}
					else{
						significance[i][j] = Double.toString(greater);
					}
				//}
			}
		}
		
		/*int[][][] amat = r.getAnnotatedMatrix();
		int[] totals = r.getTotalEdges();
		int[] obsedges = r.getObservedEdges();
		int[][][] aobs = r.getAObs();
		String[][][] chrsignificance = new String[l][l][totals.length];
		TreeMap<String, Integer> chrkey = r.getChrKey();
		for(int k = 0; k < totals.length; k++){
			for(int i = 0; i < l; i++){
				for(int j = 0; j < l; j++){
					double less = 1;
					double greater = 1;
					less = rconn.parseAndEval("phyper("+aobs[i][j][k]+", "+amat[i][j][k]+", "+(totals[k]-amat[i][j][k])+", "+obsedges[k]+", lower.tail=TRUE)").asDouble();
					greater = rconn.parseAndEval("phyper("+aobs[i][j][k]+", "+(amat[i][j][k]-1)+", "+(totals[k]-amat[i][j][k]+1)+", "+obsedges[k]+", lower.tail=FALSE)").asDouble();
	
					if(less < greater){
						chrsignificance[i][j][k] = Double.toString(-less);
					}
					else{
						chrsignificance[i][j][k] = Double.toString(greater);
					}
				}
			}
		}
		/*for(Iterator<String> it = chrkey.keySet().iterator(); it.hasNext();){
			System.out.print(it.next()+",");
		}
		System.out.print("\n");
		for(int i = 0; i < l; i++){
			//System.out.print("A"+i+": ");
			for(int j = 0; j < l; j++){
				System.out.print("A:"+i+"|"+j+",");
				for(Iterator<Integer> it = chrkey.values().iterator(); it.hasNext();){
					System.out.print(chrsignificance[i][j][it.next()]+",");
				}
				System.out.print("\n");
			}
			System.out.print("\n");
		}*/
		
		double increment =2*maxv/100;
		int toz = (int) Math.ceil(maxv/increment);
		
		//rconn.parseAndEval("logratio = ratio;");
		rconn.assign("labels", labels);
		rconn.parseAndEval("library(pheatmap);");
		rconn.parseAndEval("pheatmap(heatmapmat, labels_row=labels, labels_col=labels, display_numbers=TRUE, fontsize_number=14, fontsize=24, cluster_rows=FALSE, cluster_cols=FALSE, breaks=seq("+-maxv+", "+maxv+", "+increment+"), color=c(colorRampPalette(c(\"blue\",\"white\"))("+toz+"),colorRampPalette(c(\"white\",\"red\"))("+toz+")));");

		rconn.parseAndEval("dev.off()");
		rexp = rconn.parseAndEval("r=readBin('plot.png','raw',1024*1024); unlink('plot.png'); r");   
		
		byte[] b = rexp.asBytes();

		rconn.close();
		
        return new AIEJson(new String(Base64.encodeBase64(b)), significance, cm, im.getExpectedMatrix(), im.getTheoreticalMatrix(), im.getPValMatrix(), im.getAnnotationInteractionCounts(), im.getAnnotationInteractionExpectedFrequencies(), im.getAnnotationTotals(), numedges, labels);    
	}
	
	private String[] getLabels(Connection conn, long fid, Integer[] indices) throws SQLException{
		Util u = new Util();
		String[] rv = new String[indices.length];
		for(int i = 0; i < indices.length; i++){
			rv[i] = u.getDataset(conn, fid, indices[i]);
		}
		return rv;
	}
	
	private void writeEdges(Connection conn, long fid, String file, Edge[] edges, Integer[] indices, Map<Integer, List<Location>>[] annotations) throws SQLException, IOException{
		AnnotatedEdge[] ae = new AnnotatedEdge[edges.length];
		String[] labels = getLabels(conn, fid, indices);
		for(int i = 0; i < edges.length; i++){
			ae[i] = new AnnotatedEdge(edges[i]);
			ae[i].setAnnotation(annotations, indices);
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("Edge Id");
		bw.write("\tNode1 ID");
		for(int i = 0; i < labels.length; i++){
			bw.write("\tNode1 "+labels[i]);
		}
		bw.write("\tNode2 ID");
		for(int i = 0; i < labels.length; i++){
			bw.write("\tNode2 "+labels[i]);
		}
		bw.write("\n");
		
		for(int i = 0; i < ae.length; i++){
			bw.write(ae[i].getId().toString());
			bw.write(ae[i].getAnnotations()+"\n");
		}
		
		bw.flush();
		bw.close();
		
		
	}
	
	private double[][] getTheoreticalExpected(Reachable r, Map<Integer, List<Location>>[] nodea, int totaledges){
		
		double[][] rv = new double[nodea.length][nodea.length];
		
		int totalpossiblesum = r.getTotalEdgesSum();
		int[] totalpossible = r.getTotalEdges();
		int[][][] amat = r.getAnnotatedMatrix();
		int[] observededges = r.getObservedEdges();
		for(int i = 0; i < nodea.length; i++){
			for(int j = 0; j < nodea.length; j++){
				double totale = 0;
				for(int k = 0; k < totalpossible.length; k++){
					totale += observededges[k]*(double)amat[i][j][k]/totalpossible[k];
				}
				rv[i][j] = totale;
			}
		}
		
		//System.out.println(r);
		
		return rv;
	}
	
	
	private class Reachable {
		private int _totaledgessum;
		private int _totaledges[];
		private int[][][] _annotatedmatrix;
		private int[][] _annotatedmatrixsum;
		private int[] _observededges;
		private int[][][] _aobs;
		private TreeMap<String, Integer> _chrkey;
		public Reachable(int[] total, int[][][] amat, int[] observededges, int[][][] aobs, TreeMap<String, Integer> chrkey){
			_observededges = observededges;
			_totaledges = total;
			_totaledgessum = 0;
			_annotatedmatrixsum = new int[amat.length][amat[0].length];
			for(int i = 0; i < _totaledges.length; i++){
				_totaledgessum += _totaledges[i];
				for(int j = 0; j < amat.length; j++){
					for(int k = 0; k < amat[j].length; k++){
						_annotatedmatrixsum[j][k] += amat[j][k][i];
					}
				}
			}
			_annotatedmatrix = amat;
			_chrkey = chrkey;
			_aobs = aobs;
		}
		
		public TreeMap<String, Integer> getChrKey(){
			return _chrkey;
		}
		
		public int[][][] getAObs(){
			return _aobs;
		}
		
		public int[] getObservedEdges(){
			return _observededges;
		}
		
		public int getTotalEdgesSum(){
			return _totaledgessum;
		}
		
		public int[] getTotalEdges(){
			return _totaledges;
		}
		
		public int[][][] getAnnotatedMatrix(){
			return _annotatedmatrix;
		}
		
		public int[][] getAnnotatedMatrixSum(){
			return _annotatedmatrixsum;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			for(Iterator<Entry<String, Integer>> it = _chrkey.entrySet().iterator(); it.hasNext();){
				Entry<String, Integer> next = it.next();
				String chr = next.getKey();
				int index = next.getValue();
				sb.append(chr+"\n");
				sb.append(_observededges[index]+":"+_totaledges[index]+"\n");
				for(int i = 0; i < _annotatedmatrix.length; i++){
					for(int j = 0; j < _annotatedmatrix.length; j++){
						sb.append(_annotatedmatrix[i][j][index]+",");
					}
					sb.append("\n");
				}
				sb.append("\n");
			}
			return sb.toString();
		}
		
	}
	
	private Reachable reachable(Map<Integer, Location> allnodes, Map<Integer, List<Location>>[] nodea, int maxdist, Edge[] edges){
		Set<Integer> seen = new TreeSet<Integer>();
		
		quin.network.Util u = new quin.network.Util();
		Map<String, Location[]> chrstartsorted = u.getChrSorted(allnodes.values().toArray(new Location[0]));
		
		TreeMap<String, Integer> chrkey = new TreeMap<String, Integer>();
		int index = 0;
		
		for(Iterator<String> it = chrstartsorted.keySet().iterator(); it.hasNext();){
			String chr = it.next();
			chrkey.put(chr.toLowerCase(),index++);
		}
		
		int numchr = chrkey.size();
		int[] totaledges = new int[numchr];
		int[][][] amatrix = new int[nodea.length][nodea.length][numchr];
		

		for(Iterator<Location> it = allnodes.values().iterator(); it.hasNext();){
			Location n1l = it.next();
			int n1 = n1l.getIntegerId();
			LinkedList<Integer> n1a = new LinkedList<Integer>();
			seen.add(n1);
			String n1chr = n1l.getChr();
			int chrindex = chrkey.get(n1chr.toLowerCase());
			for(int i = 0; i < nodea.length; i++){
				if(nodea[i].containsKey(n1)){
					n1a.add(i);
				}
			}
			
			Location[] listlookup = chrstartsorted.get(n1chr.toLowerCase());
			
			for(int m = 0; m < listlookup.length; m++){
				Location n2l = listlookup[m];
				int n2 = n2l.getIntegerId();
				
				int start = n1l.getEnd();
				int end = n2l.getStart();
				
				if(start > end){
					start = n1l.getStart();
					end = n2l.getEnd();
				}
				
				int diff = end-start;
				if(!seen.contains(n2) && diff <= maxdist){
					totaledges[chrindex]++;

					LinkedList<Integer> n2a = new LinkedList<Integer>();
					for(int i = 0; i < nodea.length; i++){
						if(nodea[i].containsKey(n2)){
							n2a.add(i);
						}
					}
					
					for(Iterator<Integer> ait = n1a.iterator(); ait.hasNext();){
						int a1 = ait.next();
						for(Iterator<Integer> ait2 = n2a.iterator(); ait2.hasNext();){
							int a2 = ait2.next();
							amatrix[a1][a2][chrindex]++;
							if(a2 != a1){
								amatrix[a2][a1][chrindex]++;
							}
						}
					}
				}
				
				
			}
		}
		
		int[] obsedges = new int[numchr];
		int[][][] aobs = new int[nodea.length][nodea.length][numchr];
		for(int j = 0; j < edges.length; j++){
			Location n1l = allnodes.get(edges[j].getNode1());
			Location n2l = allnodes.get(edges[j].getNode2());
			int start = n1l.getEnd();
			int end = n2l.getStart();
			
			if(start > end){
				start = n1l.getStart();
				end = n2l.getEnd();
			}
			int diff = end-start;
			String n1chr = n1l.getChr();
			int chri = chrkey.get(n1chr.toLowerCase());
			if(n1l.getChr().equals(n2l.getChr()) && diff <= maxdist){
				obsedges[chri]++;
				
				int chrindex = chrkey.get(n1chr.toLowerCase());
				LinkedList<Integer> n1a = new LinkedList<Integer>();
				for(int i = 0; i < nodea.length; i++){
					if(nodea[i].containsKey(edges[j].getNode1())){
						n1a.add(i);
					}
				}
				
				LinkedList<Integer> n2a = new LinkedList<Integer>();
				for(int i = 0; i < nodea.length; i++){
					if(nodea[i].containsKey(edges[j].getNode2())){
						n2a.add(i);
					}
				}
				
				for(Iterator<Integer> ait = n1a.iterator(); ait.hasNext();){
					int a1 = ait.next();
					for(Iterator<Integer> ait2 = n2a.iterator(); ait2.hasNext();){
						int a2 = ait2.next();
						aobs[a1][a2][chrindex]++;
						if(a2 != a1){
							aobs[a2][a1][chrindex]++;
						}
					}
				}
			}
			
		}
		
		return new Reachable(totaledges, amatrix, obsedges, aobs, chrkey);
	}
	
	/*
	//TODO
	private Location[] getMergeLocations(int numnodes, String file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		LinkedList<Location> l = new LinkedList<Location>();
		int id = numnodes*10;
		while(br.ready()){
			String line = br.readLine();
			String[] split = line.split("\t");
			String chr = split[0];
			int start = Integer.parseInt(split[1]);
			int end = Integer.parseInt(split[2]);
			l.add(new Location(id++, chr, start, end));
		}
		br.close();
		return l.toArray(new Location[0]);
	}
	
	private TreeMap<Integer, Integer> getMapping(Map<Integer, List<Location>> nodeids, Map<Integer, Location> nodes, Location[] senodeids){
		
		TreeMap<Integer, Integer> rv = new TreeMap<Integer, Integer>();
		
		for(Iterator<Integer> it = nodeids.keySet().iterator(); it.hasNext();){
			int nodeid = it.next();
			Location nl = nodes.get(nodeid);
			int seid = -1;
			for(int i = 0; i < senodeids.length; i++){
				Location sel = senodeids[i];
				if(nl.getChr().equals(sel.getChr()) && nl.getStart() <= sel.getEnd() && nl.getEnd() >= sel.getStart()){
					seid = sel.getIntegerId();
					break;
				}
			}
			if(seid == -1){
				System.out.println("Error");
			}
			rv.put(nodeid, seid);
		}
		
		return rv;
	}
	
	
	private Map<Integer, List<Location>> updateNodeids(Map<Integer, List<Location>> nodeids, TreeMap<Integer, Integer> mapping){
		Map<Integer, List<Location>> rv = new TreeMap<Integer, List<Location>>();
		for(Iterator<Integer> it = nodeids.keySet().iterator(); it.hasNext();){
			rv.put(mapping.get(it.next()), new LinkedList<Location>());
		}
		return rv;
	}
	
	private Edge[] updateEdges(Edge[] edges, TreeMap<Integer, Integer> mapping){
		LinkedList<Edge> newedges = new LinkedList<Edge>();
		TreeSet<String> seen = new TreeSet<String>();
		for(int i = 0; i < edges.length; i++){
			Edge ce = edges[i];
			int eid = ce.getId();
			int n1 = ce.getNode1();
			int n2 = ce.getNode2();
			
			boolean mn1 = mapping.containsKey(n1);
			boolean mn2 = mapping.containsKey(n2);
			if(mn1 || mn2){
				if(mn1){
					n1 = mapping.get(n1);
				}
				if(mn2){
					n2 = mapping.get(n2);
				}
				if(n1 != n2){
					
					if(n1 > n2){
						int temp = n2;
						n2 = n1;
						n1 = temp;
					}
					
					String key = n1+":"+n2;
					if(!seen.contains(key)){
						Edge newedge = new Edge();
						newedge.setId(eid);
						newedge.setNode1(n1);
						newedge.setNode2(n2);
						newedges.add(newedge);
						seen.add(key);
					}
					
				}
			}
			else{
				newedges.add(ce);
			}
		}
		return newedges.toArray(new Edge[0]);
	}
	
	private Map<Integer, Location> updateNodes(Map<Integer, Location> nodes, TreeMap<Integer, Integer> mapping){
		Map<Integer, Location> rv = new TreeMap<Integer, Location>();
		
		TreeMap<Integer, LinkedList<Location>> senodeclusters = new TreeMap<Integer, LinkedList<Location>>();
		
		for(Iterator<Entry<Integer, Location>> it = nodes.entrySet().iterator(); it.hasNext();){
			Entry<Integer, Location> next = it.next();
			int nid = next.getKey();
			if(mapping.containsKey(nid)){
				int senodeid = mapping.get(nid);
				if(!senodeclusters.containsKey(senodeid)){
					senodeclusters.put(senodeid, new LinkedList<Location>());
				}
				senodeclusters.get(senodeid).add(next.getValue());
			}
			else{
				rv.put(nid, next.getValue());
			}
		}
		
		
		//Merging nodes and add them to the list
		for(Iterator<Entry<Integer, LinkedList<Location>>> it = senodeclusters.entrySet().iterator(); it.hasNext();){
			Entry<Integer, LinkedList<Location>> next = it.next();
			
			String chr = "";
			int minstart = Integer.MAX_VALUE;
			int maxend = -1;
			for(Iterator<Location> it2 = next.getValue().iterator(); it2.hasNext();){
				Location next2 = it2.next();
				chr = next2.getChr();
				minstart = Math.min(minstart, next2.getStart());
				maxend = Math.max(maxend, next2.getEnd());
			}
			rv.put(next.getKey(), new Location(next.getKey(), chr, minstart, maxend));
		}
		
		return rv;
	}*/
	
	private InteractionMatrix getMatrix(Connection conn, long fid, Integer[] indices, int min, int max, String testtype, int npermutes) throws SQLException{
		AIEQuery aieq = new AIEQuery();
		
		int nodecount = aieq.getNodeCount(conn, fid, max, min);
		
		
		
		Map<Integer, List<Location>>[] nodeids = aieq.getNodeIds(conn, "chiapet", fid, indices, max, min);
		Edge[] edges = aieq.getEdges(conn, fid, max, min);
		Map<Integer, Location> nodes = aieq.getNodes(conn, fid, max, min);

		
		/*#MERGING Nodes
		//TODO ##MERGING - Read SE node locations
		Location[] senodes = null;
		try {
			 senodes = getMergeLocations(nodecount, "/Users/athib/Desktop/10-20-2015/CTS Interaction Comparison & Motifs/Data/Super Enhancers/MCF-7.bed");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//TODO ##MERGING - Replace nodeids with SE node ids, returning a mapping between
		TreeMap<Integer, Integer> mapping = getMapping(nodeids[3], nodes, senodes);
		nodeids[3] = updateNodeids(nodeids[3], mapping);
		
		//TODO ##MERGING - Replace nodeids with SE node ids in each edge
		edges = updateEdges(edges, mapping);
		
		//TODO ##MERGING - Node id locations with min/max node locations
		nodes = updateNodes(nodes, mapping);
		
		nodecount = nodes.size();
		*/
		//Set the sets to easily check if a node has an annotation;
		
		
		int il = indices.length;
		int[] totals = new int[il];
		for(int i = 0; i < il; i++){
			totals[i] = nodeids[i].size();
		}
		
		
		int[][] cm = new int[il][il];
		/*int[][] acm = getACM(edges, nodeids, nodecount);	//Count of annotations that interact with another at least once
		double[][] acmp;
		if(testtype.equals("p")){
			acmp = getPermutationACM(edges, nodeids, nodecount, npermutes, acm);	//Count of the expected number of an annotation interacting P(A1 interacts with A2 at least once) = SUM( 1-(N-A2 choose degree)/(N choose degree) )

		}
		else{
			acmp = getExpectedFrequencyAtLeastOne(edges, nodeids);	//Count of the expected number of an annotation interacting P(A1 interacts with A2 at least once) = SUM( 1-(N-A2 choose degree)/(N choose degree) )
		}*/

		double[][] tem = new double[il][il];
		double[][] em;
		double[][] pvm;

		
		int numedges = edges.length;
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				//Compute the observed count
				int ecount = 0;
				for(int k = 0; k < edges.length; k++){
					Edge ce = edges[k];
					int n1 = ce.getNode1();
					int n2 = ce.getNode2();
					boolean i1 = nodeids[i].containsKey(n1);
					boolean i2 = nodeids[i].containsKey(n2);
					boolean j1 = nodeids[j].containsKey(n1);
					boolean j2 = nodeids[j].containsKey(n2);
					
					
					if(i1 && j2){
						ecount++;
					}
					else if(i2 && j1){
						ecount++;
					}

				}
				cm[i][j] = ecount;

				int ni = nodeids[i].size();
				int nj = nodeids[j].size();
				double ep;
				if(i == j){
					ep = (ni/(double)nodecount)*((nj-1)/(double)(nodecount-1));
				}
				else {
					ep = (ni/(double)nodecount)*((nj)/(double)(nodecount-1))+(nj/(double)nodecount)*((ni)/(double)(nodecount-1));
				}
				
				tem[i][j] = ep*numedges;

			}
		}
		
		//int[][] intervals = getRange(20000, 1000000);
		Reachable r = null;
		if(testtype.equals("it")){
			r = reachable(nodes, nodeids, npermutes, edges);
			tem = getTheoreticalExpected(r, nodeids, numedges);
		}
		else if(testtype.equals("p")){
				int[][][] permutes = getExpected(edges, nodecount, totals, npermutes);

				em = getAverage(permutes);
				pvm = getPValue(permutes, cm);
				return new InteractionMatrix(cm, em, tem, pvm, null, null, totals, numedges, r);
		}


		return new InteractionMatrix(cm, null, tem, null, null, null, totals, numedges,r);
	}
	
	private int[][] getRange(int binsize, int limit){
		int l = limit/binsize;
		int[] mindist = new int[l];
		int [] maxdist = new int[l];
		for(int i = 0; i < l; i++){
			mindist[i] = i*binsize;
			maxdist[i] = (i+1)*binsize;
		}
		return new int[][]{mindist,maxdist};
	}
	
	private double[][] getExpectedFrequencyAtLeastOne(Edge[] edges, Map<Integer, List<Location>>[] annotations){
		TreeMap<Integer, Integer> nodeedgecount = new TreeMap<Integer, Integer>();
		
		for(int i = 0; i < edges.length; i++){
			int n1id = edges[i].getNode1();
			int n2id = edges[i].getNode2();
			if(!nodeedgecount.containsKey(n1id)){
				nodeedgecount.put(n1id, 0);
			}
			if(!nodeedgecount.containsKey(n2id)){
				nodeedgecount.put(n2id, 0);
			}
			nodeedgecount.put(n1id, nodeedgecount.get(n1id)+1);
			nodeedgecount.put(n2id, nodeedgecount.get(n2id)+1);
		}
		
		int al = annotations.length;
		double[][] rv = new double[al][al];
		int N = nodeedgecount.size();
		
		for(Iterator<Entry<Integer,Integer>> it = nodeedgecount.entrySet().iterator(); it.hasNext();){
			Entry<Integer,Integer> entry = it.next();
			int nid = entry.getKey();
			int ec = entry.getValue();
			for(int i = 0; i < annotations.length; i++){
				if(annotations[i].containsKey(nid)){
					for(int j = 0; j < annotations.length; j++){
						rv[i][j] += 1-getProbabilityInteraction(N, ec, annotations[j].size());
					}
				}
			}
		}
		
//		for(int i = 0; i < rv.length; i++){
//			for(int j = 0; j < rv[i].length; j++){
//				rv[i][j] = rv[i][j]/annotations[i].size();
//			}
//		}
		
		return rv;
	}
	
	private Map<Integer, List<Location>> getPermutedSet(List<Integer> nids, int size, Random r){
		Collections.shuffle(nids, r);
		Map<Integer, List<Location>> rv = new TreeMap<Integer, List<Location>>();
		for(int i = 0; i < size; i++){
			rv.put(nids.get(i), new LinkedList<Location>());
		}
		return rv;
	}
	
	private double[][] getPermutationACM(Edge[] edges, Map<Integer, List<Location>>[] sets, int nodecount, int p, int[][] acm){
		int il = sets.length;
		List<Integer> nids = getNodeIndices(nodecount);
		int[][][] counts = new int[p][il][il];
		Random seed = new Random(929);
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				for(int k = 0; k < p; k++){
					//Permute the current annotation
					Map<Integer, List<Location>> pset = getPermutedSet(nids, sets[j].size(), seed);
					Map<Integer, List<Location>>[] reusable = new Map[2];
					reusable[0] = sets[i];
					reusable[1] = pset;
					int[][] count = getACM(edges, reusable, nodecount);
					counts[k][i][j] = count[0][1];	//record the count for this permutation
				}
			}
		}
		
		//Get the P-Value
		double[][] rv = new double[il][il];
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				int lsum = 0;
				int gsum = 0;
				for(int k = 0; k < p; k++){
					if(acm[i][j] >= counts[k][i][j]){
						gsum++;
					}
					if(acm[i][j] <= counts[k][i][j]){
						lsum++;
					}
				}
				double lp =	(double)(lsum+1)/(p+1);
				double gp = (double)(gsum+1)/(p+1);
				if(lp < gp){
					rv[i][j] = -lp;
				}
				else{
					rv[i][j] = gp;
				}
			}
		}
		
		return rv;
	}
	
	private int[][] getACM(Edge[] edges, Map<Integer, List<Location>>[] sets, int nodecount){
		int il = sets.length;
		int[][] acm = new int[il][il];
		int numedges = edges.length;
		//Matrix of treemaps containing boolean values of whether a node with annotation i interacts with annotation j
		TreeSet[][] acmtrees = new TreeSet[il][il];
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				acmtrees[i][j] = new TreeSet<Integer>();
			}
		}
		
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				//Compute the observed count
				int ecount = 0;
				for(int k = 0; k < edges.length; k++){
					Edge ce = edges[k];
					int n1 = ce.getNode1();
					int n2 = ce.getNode2();
					boolean i1 = sets[i].containsKey(n1);
					boolean i2 = sets[i].containsKey(n2);
					boolean j1 = sets[j].containsKey(n1);
					boolean j2 = sets[j].containsKey(n2);
					
					if(i1 && j2){
						acmtrees[i][j].add(n1);
						ecount++;
					}
					else if(i2 && j1){
						acmtrees[i][j].add(n2);
						ecount++;
					}

				}
			}
		}
		
		//generate count matrix of nodes with annotations a1 interacting with a2
		for(int i = 0; i < il; i++){
			for(int j = 0; j < il; j++){
				acm[i][j] = acmtrees[i][j].size();
			}
		}
		
		return acm;
	}
	
	
	private double getProbabilityInteraction(int N, int D, int A){
		if(A < D){
			return 0;
		}
		
		int nd = N-D;
		double rv = 1;
		for(int i = 0; i < A; i++){
			rv = rv*((double)(nd-i)) / ((double)(N-i));
		}
		
		return rv;
	}
	
	private double[][] getAverage(int[][][] p){
		if(p == null || p.length == 0){
			return null;
		}
		
		double[][] summat = new double[p[0].length][p[0][0].length];
		for(int i = 0; i < p.length; i++){
			for(int j = 0; j < p[0].length; j++){
				for(int k = 0; k < p[i][j].length; k++){
					summat[j][k] += p[i][j][k];
				}
			}
		}
		for(int i = 0; i < p[0].length; i++){
			for(int j = 0; j < p[0][0].length; j++){
				summat[i][j] = summat[i][j]/p.length;
			}
		}
		return summat;
	}
	
	private double[][] getPValue(int[][][] p, int[][] o){
		if(o == null || p == null || o.length == 0){
			return null;
		}
		
		double[][] avg = getAverage(p);

		int[][] sum = new int[o.length][o[0].length];
		for(int i = 0; i < o.length; i++){
			for(int j = 0; j < o[i].length; j++){
				for(int k = 0; k < p.length; k++){
					double nulldval = (double)p[k][i][j] - avg[i][j];
					double cpval = Math.abs((double)o[i][j] - avg[i][j]);
					if(cpval <= nulldval || -cpval >= nulldval){
						sum[i][j]++;
					}
				}
			}
		}
		
		double[][] pval = new double[o.length][o[0].length];
		
		for(int i = 0; i < o.length; i++){
			for(int j = 0; j < o[i].length; j++){
				pval[i][j] = (double)(sum[i][j]+1)/(p.length+1);
			}
		}
		return pval;
	}
	
	private List<Integer> getNodeIndices(int n){
		List<Integer> rv = new ArrayList<Integer>(n);
		for(int i = 0; i < n; i++){
			rv.add(i);
		}
		return rv;
	}
	
	private int[][][] getExpected(Edge[] edges, int n, int[] a, int p){
		AIEQuery aieq = new AIEQuery();
		Edge[] ae = aieq.getAdjustedNodeIdEdges(edges);
		List<Integer> ni = getNodeIndices(n);
		
		int al = a.length;
		int[][][] rv = new int[p][al][al];
		
		for(int i = 0; i < p; i++){
			//set up the boolean arrays to know where the annotations are in this round
			boolean[][] na = new boolean[al][n];
			for(int j = 0; j < al; j++){
				Collections.shuffle(ni);
				for(int k = 0; k < a[j]; k++){
					na[j][ni.get(k)] = true;
				}
			}

			//determine counts for each annotation.  Notice that each annotation is shuffled exactly p times and compared exactly p times
			for(int x = 0; x < al; x++){
				for(int y = x; y < al; y++){
					for(int j = 0; j < ae.length; j++){
						if((na[x][ae[j].getNode1()] && na[y][ae[j].getNode2()]) || (na[y][ae[j].getNode1()] && na[x][ae[j].getNode2()])){
							rv[i][x][y]++;
							rv[i][y][x] = rv[i][x][y];
						}
					}
				}
			}

		}
		
		return rv;
	}
	
	
	private class InteractionMatrix {
		
		private int[][] _cm;
		private double[][] _em;
		private double[][] _tem;
		private double[][] _pm;
		private int _nedges;
		private int[] _atotals;
		private int[][] _acm;
		private double[][] _acmp;
		private Reachable _r;
		public InteractionMatrix(int[][] cm, double[][] em, double[][] tem, double[][] pm, int[][] acm, double[][] acmp, int[] atotals, int nedges, Reachable r){
			_cm = cm;
			_em = em;
			_pm = pm;
			_tem = tem;
			_nedges = nedges;
			_acm = acm;
			_acmp = acmp;
			_atotals = atotals;
			_r = r;
		}
		
		public int[][] getCountMatrix(){
			return _cm;
		}
		
		public double[][] getExpectedMatrix(){
			return _em;
		}
		
		public double[][] getTheoreticalMatrix(){
			return _tem;
		}
		
		public double[][] getPValMatrix(){
			return _pm;
		}
		
		public int getEdgeCount(){
			return _nedges;
		}
		
		public int[] getAnnotationTotals(){
			return _atotals;
		}
		
		public int[][] getAnnotationInteractionCounts(){
			return _acm;
		}
		
		
		public double[][] getAnnotationInteractionExpectedFrequencies(){
			return _acmp;
		}
		
		public Reachable getReachable(){
			return _r;
		}
		
	}
	
}
