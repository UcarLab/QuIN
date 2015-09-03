package quin.network.analysis.json;

public class HistogramJson {

	private String histogram;
	
	public HistogramJson(byte[] r){
		histogram = new String(r);
	}
	
	public String getHistogram(){
		return histogram;
	}
	
}
