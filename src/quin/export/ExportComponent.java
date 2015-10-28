package quin.export;

public class ExportComponent {
	
	private int _id;
	private int _nodecount;
	private int _edgecount;
	
	public ExportComponent(int id, int nodecount, int edgecount){
		_id = id;
		_nodecount = nodecount;
		_edgecount = edgecount;
	}
	
	public int getId(){
		return _id;
	}
	
	public int getNodeCount(){
		return _nodecount;
	}
	
	public int getEdgeCount(){
		return _edgecount;
	}
	
}