package quin.network.analysis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tomcat.util.codec.binary.Base64;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import quin.export.Util;
import quin.web.networkjson.Edge;

public class AnnotationInteractionEnrichment {

	public AIEJson generateHeatmap(Connection conn, long fid, Integer[] indices, int min, int max, int npermutes) throws SQLException, REngineException, REXPMismatchException{
		String[] labels = getLabels(conn, fid, indices);
		InteractionMatrix im = getMatrix(conn, fid, indices, min, max, npermutes);

		int[][] cm = im.getCountMatrix();
		int numedges = im.getEdgeCount();
		
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=1200, height=600)");
		
		int l = cm.length;
		rconn.parseAndEval("heatmapmat = matrix(nrow="+l+", ncol="+l+")");

		String[][] significance = new String[l][l];
		double[][] em = im.getTheoreticalMatrix();
		if(npermutes > 0){
			em = im.getExpectedMatrix();
		}
		double maxv = 0;
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				int count = cm[i][j];
				double expected = em[i][j];
				if(count != 0 && expected != 0){
					double ratio = Math.log(count/expected)/Math.log(2);
					rconn.parseAndEval("heatmapmat["+(i+1)+","+(j+1)+"] = "+ratio);
					maxv = Math.max(Math.abs(Math.ceil(ratio)), maxv);
					double less = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+em[i][j]/numedges+", alternative=\"less\")$p.value").asDouble();
					double greater = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+em[i][j]/numedges+", alternative=\"greater\")$p.value").asDouble();

					if(less < greater){
						significance[i][j] = Double.toString(-less);
					}
					else{
						significance[i][j] = Double.toString(greater);
					}
				}
			}
		}
		
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
		
        return new AIEJson(new String(Base64.encodeBase64(b)), significance, cm, im.getExpectedMatrix(), im.getTheoreticalMatrix(), im.getPValMatrix(), numedges, labels);    
	}
	
	private String[] getLabels(Connection conn, long fid, Integer[] indices) throws SQLException{
		Util u = new Util();
		String[] rv = new String[indices.length];
		for(int i = 0; i < indices.length; i++){
			rv[i] = u.getDataset(conn, fid, indices[i]);
		}
		return rv;
	}
	
	
	private InteractionMatrix getMatrix(Connection conn, long fid, Integer[] indices, int min, int max, int npermutes) throws SQLException{
		AIEQuery aieq = new AIEQuery();
		
		int nodecount = aieq.getNodeCount(conn, fid, max, min);
		Integer[][] nodeids = aieq.getNodeIds(conn, "chiapet", fid, indices, max, min);
		Edge[] edges = aieq.getEdges(conn, fid, max, min);
		
		TreeMap<Integer, Integer> indexmap = new TreeMap<Integer, Integer>();
		
		@SuppressWarnings("unchecked")
		TreeSet<Integer>[] sets = new TreeSet[indices.length];
		for(int i = 0; i < sets.length; i++){
			sets[i] = new TreeSet<Integer>();
			indexmap.put(indices[i], i);
		}
		
		//Set the sets to easily check if a node has an annotation;
		int[] al = new int[indices.length];
		for(int i = 0; i < nodeids.length; i++){
			Integer[] c = nodeids[i];
			sets[indexmap.get(c[0])].add(c[1]);
		}
		for(int i = 0; i < indices.length; i++){
			al[i] = sets[i].size();
		}
		
		int il = indices.length;
		int[][] cm = new int[il][il];
		double[][] tem = new double[il][il];
		double[][] em;
		double[][] pvm;

		
		int numedges = edges.length;
		for(int i = 0; i < indices.length; i++){
			for(int j = 0; j < indices.length; j++){
				//Compute the observed count
				int ecount = 0;
				for(int k = 0; k < edges.length; k++){
					Edge ce = edges[k];
					boolean i1 = sets[i].contains(ce.getNode1());
					boolean i2 = sets[i].contains(ce.getNode2());
					boolean j1 = sets[j].contains(ce.getNode1());
					boolean j2 = sets[j].contains(ce.getNode2());
					if((i1 && j2) || (i2 && j1)){
						ecount++;
					}
				}
				cm[i][j] = ecount;

				
				
				int ni = sets[i].size();
				int nj = sets[j].size();
				double ep;
				if(i == j){
					ep = (ni/(double)nodecount)*((nj-1)/(double)(nodecount-1));
				}
				else {
					ep = (ni/(double)nodecount)*((nj)/(double)(nodecount-1))+(nj/(double)nodecount)*((ni)/(double)(nodecount-1));
				}
				
				tem[i][j] = ep*numedges;
//
//				//Compute the expected count using permutations and calculate a P-Value
//				int p = 10000;
//				int ni = sets[i].size();
//				int nj = sets[j].size();
//				if(i == j){
//					int[] permutes = getExpected(edges, nodecount, ni, p);
//					double avg = getAverage(permutes);
//					
//					em[i][j] = avg;
//				}
//				else {
//					ep = (ni/(double)nodecount)*((nj)/(double)(nodecount-1))+(nj/(double)nodecount)*((ni)/(double)(nodecount-1));
//				}
//				
//				em[i][j] = ep;
			}
		}
		
		if(npermutes > 0){
			int[][][] permutes = getExpected(edges, nodecount, al, npermutes);

			em = getAverage(permutes);
			pvm = getPValue(permutes, cm);
			return new InteractionMatrix(cm, em, tem, pvm, numedges);
		}

		return new InteractionMatrix(cm, null, tem, null, numedges);
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
		
		int[][] _cm;
		double[][] _em;
		double[][] _tem;
		double[][] _pm;
		int _nedges;
		
		public InteractionMatrix(int[][] cm, double[][] em, double[][] tem, double[][] pm, int nedges){
			_cm = cm;
			_em = em;
			_pm = pm;
			_tem = tem;
			_nedges = nedges;
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
		
	}
	
}
