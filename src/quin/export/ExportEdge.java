package quin.export;

public class ExportEdge {
	private int _id;
	private int _ccid;
	private int _src;
	private int _dest;
	private int _petcount;
	private int _intcount;
	
	public ExportEdge(int id, int ccid, int src, int dest, int petcount, int intcount){
		_id = id;
		_ccid = ccid;
		_src = src;
		_dest = dest;
		_petcount = petcount;
		_intcount = intcount;
	}
	
	public int getId(){
		return _id;
	}
	
	public int getCCId(){
		return _ccid;
	}
	
	public int getSource(){
		return _src;
	}
	
	public int getDest(){
		return _dest;
	}
	
	public int getPETCount(){
		return _petcount;
	}
	
	public int getInteractionCount(){
		return _intcount;
	}
	
	public String getLabel(){
		return "PET: "+_petcount+", Interactions: "+_intcount;
	}
}
