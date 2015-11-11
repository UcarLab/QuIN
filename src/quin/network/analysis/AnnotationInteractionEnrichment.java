package quin.network.analysis;

import java.sql.Connection;
import java.sql.SQLException;
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

	public AIEJson generateHeatmap(Connection conn, long fid, Integer[] indices, int min, int max) throws SQLException, REngineException, REXPMismatchException{
		String[] labels = getLabels(conn, fid, indices);
		InteractionMatrix im = getMatrix(conn, fid, indices, min, max);

		int[][] cm = im.getCountMatrix();
		double[][] epm = im.getExpectedProbabilityMatrix();
		int numedges = im.getEdgeCount();
		
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=1200, height=600)");
		
		int l = cm.length;
		rconn.parseAndEval("heatmapmat = matrix(nrow="+l+", ncol="+l+")");

		String[][] significance = new String[l][l];
		double[][] em = new double[l][l];
		double maxv = 0;
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				int count = cm[i][j];
				double expected = epm[i][j]*numedges;
				em[i][j] = expected;
				if(count != 0 && expected != 0){
					double ratio = Math.log(count/expected)/Math.log(2);
					rconn.parseAndEval("heatmapmat["+(i+1)+","+(j+1)+"] = "+ratio);
					maxv = Math.max(Math.abs(Math.ceil(ratio)), maxv);
					double less = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+epm[i][j]+", alternative=\"less\")$p.value").asDouble();
					double greater = rconn.parseAndEval("binom.test("+cm[i][j]+", "+numedges+", p="+epm[i][j]+", alternative=\"greater\")$p.value").asDouble();

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
		
        return new AIEJson(new String(Base64.encodeBase64(b)), significance, cm, em, numedges, labels);    
	}
	
	private String[] getLabels(Connection conn, long fid, Integer[] indices) throws SQLException{
		Util u = new Util();
		String[] rv = new String[indices.length];
		for(int i = 0; i < indices.length; i++){
			rv[i] = u.getDataset(conn, fid, indices[i]);
		}
		return rv;
	}
	
	
	private InteractionMatrix getMatrix(Connection conn, long fid, Integer[] indices, int min, int max) throws SQLException{
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
		for(int i = 0; i < nodeids.length; i++){
			Integer[] c = nodeids[i];
			sets[indexmap.get(c[0])].add(c[1]);
		}
		
		int il = indices.length;
		int[][] cm = new int[il][il];
		double[][] em = new double[il][il];
		
		int numedges = edges.length;
		for(int i = 0; i < indices.length; i++){
			for(int j = 0; j < indices.length; j++){
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
				
				int ni = sets[i].size();
				int nj = sets[j].size();
				double ep;
				if(i == j){
					ep = (ni/(double)nodecount)*((nj-1)/(double)(nodecount-1));
				}
				else {
					ep = (ni/(double)nodecount)*((nj)/(double)(nodecount-1))+(nj/(double)nodecount)*((ni)/(double)(nodecount-1));
				}
				
				em[i][j] = ep;
				cm[i][j] = ecount;
			}
		}

		return new InteractionMatrix(cm, em, numedges);
	}
	
	private class InteractionMatrix {
		
		int[][] _cm;
		double[][] _em;
		int _nedges;
		
		public InteractionMatrix(int[][] cm, double[][] em, int nedges){
			_cm = cm;
			_em = em;
			_nedges = nedges;
		}
		
		public int[][] getCountMatrix(){
			return _cm;
		}
		
		public double[][] getExpectedProbabilityMatrix(){
			return _em;
		}
		
		public int getEdgeCount(){
			return _nedges;
		}
		
	}
	
}
