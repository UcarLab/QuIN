package quin.export;

public class ExportInteraction {
	
	private int _id;
	private int _iid;
	private String _chr;
	private int _start;
	private int _end;
	private int _count;
	private int _nodeid;
	private int _edgeid;
	private int _reason;
	private int _dtonode;
	
	public ExportInteraction(int id, int iid, String chr, int start, int end, int count, String nodeid, String edgeid, int reason, int dtonode){
		_id = id;
		_iid = iid;
		_chr = chr;
		_start = start;
		_end = end;
		_count = count;
		
		try {
			_nodeid = Integer.parseInt(nodeid);
		}
		catch(NumberFormatException e){
			_nodeid = -1;
		}
		
		try {
			_edgeid = Integer.parseInt(edgeid);
		}
		catch(NumberFormatException e){
			_edgeid = -1;
		}
		_reason = reason;
		_dtonode = dtonode;
	}
	
	public int getId(){
		return _id;
	}
	
	public int getIId(){
		return _iid;
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
	
	public int getCount(){
		return _count;
	}

	public int getNodeId(){
		return _nodeid;
	}
	
	public int getEdgeId(){
		return _edgeid;
	}
	
	public int getReason(){
		return _reason;
	}
	
	public int getDistanceToNode(){
		return _dtonode;
	}
}