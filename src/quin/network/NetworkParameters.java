package quin.network;

public class NetworkParameters {
	
	private int _ext;
	private int _sldist;
	private int _minpet;
	private int _maxdist;
	private int _minmaxpet;
	private int _mininterpet;
	private int _minsize;
	private int _mintwopet;
	
	public NetworkParameters(int ext, int sldist, int minpet, int maxdist, int minmaxpet, int mininterpet, int minsize, int mintwopet){
		_ext = ext;
		_sldist = sldist;
		_minpet = minpet;
		_maxdist = maxdist;
		_minmaxpet = minmaxpet;
		_mininterpet = mininterpet;
		_minsize = minsize;
		_mintwopet = mintwopet;
	}
	
	
	public int getExt(){
		return _ext;
	}
	
	public int getSLDist(){
		return _sldist;
	}
	
	public int getMinPET(){
		return _minpet;
	}
	
	public int getMaxDist(){
		return _maxdist;
	}
	
	public int getMinMaxPET(){
		return _minmaxpet;
	}
	
	public int getMinInterPET(){
		return _mininterpet;
	}
	
	public int getMinSize(){
		return _minsize;
	}
	
	public int getMinTwoPET(){
		return _mintwopet;
	}
	
}
