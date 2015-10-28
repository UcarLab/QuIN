package quin.export;

public class ExportNetworkInfo {
	private int _cccount;
	private int _nodecount;
	private int _edgecount;
	private double _avgcountn;
	private double _avgcounte;
	private int _extend;
	private int _sl;
	private int _minpet;
	private int _maxdist;
	private int _minpetmax;
	private int _minpetinterchrom;
	private int _minsize;
	private int _minsizepet;
	
	public ExportNetworkInfo(int cccount, int nodecount, int edgecount, double avgcountn, double avgcounte, int extend, int sl, int minpet, int maxdist, int minpetmax, int minpetinterchrom, int minsize, int minsizepet){
		_cccount = cccount;
		_nodecount = nodecount;
		_edgecount = edgecount;
		_avgcountn = avgcountn;
		_avgcounte = avgcounte;
		_extend = extend;
		_sl = sl;
		_minpet = minpet;
		_maxdist = maxdist;
		_minpetmax = minpetmax;
		_minpetinterchrom = minpetinterchrom;
		_minsize = minsize;
		_minsizepet = minsizepet;
	}
	
	public int getCCCount(){
		return _cccount;
	}
	
	public int getNodeCount(){
		return _nodecount;
	}
	
	public int getEdgeCount(){
		return _edgecount;
	}
	
	public double getAvgCountN(){
		return _avgcountn;
	}
	
	public double getAvgCountE(){
		return _avgcounte;
	}
	
	public int getExtend(){
		return _extend;
	}
	
	public int getSL(){
		return _sl;
	}
	
	public int getMaxDist(){
		return _maxdist;
	}
	
	public int getMinPET(){
		return _minpet;
	}
	
	public int getMinPETMax(){
		return _minpetmax;
	}
	
	public int getMinPETInterchrom(){
		return _minpetinterchrom;
	}
	
	public int getMinSize(){
		return _minsize;
	}
	
	public int getMinSizePET(){
		return _minsizepet;
	}
}