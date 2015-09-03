package quin.filereader;

public class ChIAPETRead {

	private String _lchr, _rchr;
	private int _lstart, _lend, _rstart, _rend, _count;
	
	public ChIAPETRead(String lchr, int lstart, int lend, String rchr, int rstart, int rend, int count){
		_lchr = lchr;
		_lstart = lstart;
		_lend = lend;
		_rchr = rchr;
		_rstart = rstart;
		_rend = rend;
		_count = count;
	}
	
	public String getLChr(){
		return _lchr;
	}
	
	public int getLStart(){
		return _lstart;
	}
	
	public int getLEnd(){
		return _lend;
	}
	
	public String getRChr(){
		return _rchr;
	}
	
	public int getRStart(){
		return _rstart;
	}
	
	public int getREnd(){
		return _rend;
	}
	
	public int getCount(){
		return _count;
	}
	
	
}
