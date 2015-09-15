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
import quin.network.db.query.NetworkQuery;
import quin.network.db.query.SuperImposeIndex;
import shortestpath.ShortestPath;
import shortestpath.ShortestPathAnalysis;

public class ShortestPathHeatmap {

	public byte[] generateHeatmap(Connection conn, long fid, Integer[] indices, int min, int max, int[] hops) throws SQLException, REngineException, REXPMismatchException{
		indices = filterduplicates(indices);
		
		int[] numnodesedges = getNumNodesEdges(conn, fid, min, max);
		int numnodes = numnodesedges[0];
		int numedges = numnodesedges[1];
		int[] numannotatednodes = annotationNodeCounts(conn, fid, indices, min, max);
		int[][] matrix = getMatrix(conn, fid, indices, min, max, hops);
		double[][] ratio = getRatioMatrix(matrix, numannotatednodes, numnodes, numedges);
		String[] labels = getLabels(conn, fid, indices);

		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=1200, height=600)");
		
		int l = ratio.length;
		rconn.parseAndEval("ratio = matrix(nrow="+l+", ncol="+l+")");
		for(int i = 0; i < ratio.length; i++){
			rconn.assign("x", ratio[i]);
			rconn.parseAndEval("ratio["+(i+1)+",] = x");
		}
		double[][] test = rconn.parseAndEval("ratio").asDoubleMatrix();
		rconn.parseAndEval("logratio = log(ratio, 2);");
		rconn.assign("labels", labels);
		rconn.parseAndEval("library(pheatmap);");
		rconn.parseAndEval("pheatmap(logratio, labels_row=labels, labels_col=labels, display_numbers=TRUE, fontsize_number=16, fontsize=26);");

		rconn.parseAndEval("dev.off()");
		rexp = rconn.parseAndEval("r=readBin('plot.png','raw',1024*1024); unlink('plot.png'); r");   
		
		byte[] b = rexp.asBytes();

		rconn.close();
		
        return Base64.encodeBase64(b);    
	}
	
	private String[] getLabels(Connection conn, long fid, Integer[] indices) throws SQLException{
		Util u = new Util();
		String[] rv = new String[indices.length];
		for(int i = 0; i < indices.length; i++){
			rv[i] = u.getDataset(conn, fid, indices[i]);
		}
		return rv;
	}
	
	private double[][] getRatioMatrix(int[][] observed, int[] numannotations, int numnodes, int numedges){
		int l = observed.length;
		double[][] rv = new double[l][l];
		
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				int adjust = 0;
				if(i == j){
					adjust = -1;
				}
				rv[i][j] = (double)observed[i][j]/(numedges*((double)numannotations[i]/numnodes)*((double)(numannotations[j]+adjust)/(numnodes-1)));
			}
		}
		return rv;
	}
	
	private int[] getNumNodesEdges(Connection conn, long fid, int min, int max) throws SQLException{
		NetworkQuery nq = new NetworkQuery();
		double[] info = nq.getNetworkInfo(conn, fid, min, max);
		int[] rv = new int[2];
		rv[0] = (int) info[1];
		rv[1] = (int) info[2];
		return rv;
	}
	
	private int[] annotationNodeCounts(Connection conn, long fid, Integer[] indices, int min, int max) throws SQLException{
		SuperImposeIndex si = new SuperImposeIndex();
		int[] rv = new int[indices.length];
		for(int i = 0; i < indices.length; i++){
			rv[i] = si.getAnnotationNodeCount(conn, fid, indices[i], min, max);
		}
		return rv;
	}
	
	private Integer[] filterduplicates(Integer[] array){
		TreeSet<Integer> ts = new TreeSet<Integer>();
		for(int i = 0; i < array.length; i++){
			ts.add(array[i]);
		}
		return ts.toArray(new Integer[0]);
	}
	
	private int[][] getMatrix(Connection conn, long fid, Integer[] indices, int min, int max, int[] hops) throws SQLException{
		
		TreeSet<Integer> hopcheck = new TreeSet<Integer>();
		for(int i = 0; i < hops.length; i++){
			hopcheck.add(hops[i]);
		}
		
		Util u = new Util();
		TreeMap<String, Integer> datasetindex = new TreeMap<String, Integer>();
		for(int i = 0; i < indices.length; i++){
			datasetindex.put(u.getDataset(conn, fid, indices[i]), i);
		}
		
		int l = indices.length;
		int[][] matrix = new int[l][l];
		ShortestPathAnalysis spa = new ShortestPathAnalysis(conn, fid);

		for(int i = 0; i < l; i++){
			ShortestPath[] paths = spa.getShortestPaths(conn, fid, indices, indices[i], min, max);
			for(int j = 0; j < paths.length; j++){
				ShortestPath path = paths[j];
				int term = datasetindex.get(path.getDataset());
				if(hopcheck.contains(path.getMinimumEdgesToTarget())){
					matrix[i][term]++;
				}
			}
		}
		
		//Need to divide the diagonal by two since both directions are counted
		for(int i = 0; i < l; i++){
			matrix[i][i] = matrix[i][i]/2;
		}
		return matrix;
	}
	
}
