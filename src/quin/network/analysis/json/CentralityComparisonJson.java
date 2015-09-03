package quin.network.analysis.json;

public class CentralityComparisonJson {

	private String rBoxPlot;
	private String[] labels;
	private String[][] mannWhitneyTable;
	
	public CentralityComparisonJson(String[] l, byte[] r, String[][] mwt){
		rBoxPlot = new String(r);
		labels = l;
		mannWhitneyTable = mwt;
	}
	
	public String getRBoxPlot(){
		return rBoxPlot;
	}
	
	public String[] getLabels(){
		return labels;
	}
	
	public String[][] getMannWhitneyTable(){
		return mannWhitneyTable;
	}
}
