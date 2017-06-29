package quin.network.analysis;

public class AIEJson {

	private String heatmap;
	private String[][] binomialmatrix;
	private int[][] countmatrix;
	private double[][] expectedmatrix;
	private double[][] texpectedmatrix;
	private double[][] permutationpval;
	private int[][] annotationinteractioncount;
	private double[][] annotationinteractionexpected;
	private int[] annotationtotals;
	private int edgecount;
	private String[] labels;
	
	public AIEJson(String hm, String[][] bm, int[][] cm, double[][] em, double[][] tem, double[][] ppv, int[][] aic, double[][] aie, int[] atotals, int ec, String[] l){
		heatmap = hm;
		binomialmatrix = bm;
		countmatrix = cm;
		expectedmatrix = em;
		texpectedmatrix = tem;
		permutationpval = ppv;
		edgecount = ec;
		labels = l;
		
		annotationinteractioncount = aic;
		annotationinteractionexpected = aie;
		annotationtotals = atotals;
	}
	
	public String getHeatmap(){
		return heatmap;
	}
	
	public String[][] getBinomialMatrix(){
		return binomialmatrix;
	}
	
	public int[][] getCountMatrix(){
		return countmatrix;
	}
	
	public double[][] getExpectedMatrix(){
		return expectedmatrix;
	}
	
	public double[][] getTExpectedMatrix(){
		return texpectedmatrix;
	}
	
	public double[][] getPermutationPVal(){
		return permutationpval;
	}
	
	public int[][] getAnnotationInteractionCount(){
		return annotationinteractioncount;
	}
	
	public double[][] getAnnotationInteractionExpected(){
		return annotationinteractionexpected;
	}
	
	public int[] getAnnotationTotals(){
		return annotationtotals;
	}
	
	public int getEdgeCount(){
		return edgecount;
	}
	
	public String[] getLabels(){
		return labels;
	}
	
	public void setHeatmap(String hm){
		heatmap = hm;
	}
	
	public void setBinomialMatrix(String[][] bm){
		binomialmatrix = bm;
	}
	
	public void setCountMatrix(int[][] cm){
		countmatrix = cm;
	}
	
	public void setExpectedMatrix(double[][] em){
		expectedmatrix = em;
	}
	
	public void setEdgeCount(int ec){
		edgecount = ec;
	}
	
	public void setLabels(String[] l){
		labels = l;
	}
}
