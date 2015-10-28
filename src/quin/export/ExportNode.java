package quin.export;

public class ExportNode {
	
	private int _nodeid;
	private int _ccid;
	private String _chr;
	private int _start;
	private int _end;
	private int _rcount;
	private int _icount;
	private int _degree;
	private double _closeness;
	private double _harmonic;
	private double _betweenness;
	
	public ExportNode(int nid, int ccid, String chr, int start, int end, int rcount, int icount, int degree, double closeness, double harmonic, double betweenness){
		_nodeid = nid;
		_ccid = ccid;
		_chr = chr;
		_start = start;
		_end = end;
		_rcount = rcount;
		_icount = icount;
		_degree = degree;
		_closeness = closeness;
		_harmonic = harmonic;
		_betweenness = betweenness;
	}
	
	public int getNodeId(){
		return _nodeid;
	}
	
	public int getCCId(){
		return _ccid;
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
	
	public int getRCount(){
		return _rcount;
	}
	
	public int getICount(){
		return _icount;
	}
	
	public int getDegree(){
		return _degree;
	}
	
	public double getCloseness(){
		return _closeness;
	}
	
	public double getHarmonic(){
		return _harmonic;
	}
	
	public double getBetweenness(){
		return _betweenness;
	}
}