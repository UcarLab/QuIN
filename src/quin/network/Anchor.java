package quin.network;

public class Anchor extends Location {

	private Node _node;
	private Interaction _interaction;
	private Anchor _paired;
	private int _dtonode;

	
	public Anchor(int id, String chr, int start, int end){
		super(id, chr, start, end);
		_node = null;
		_dtonode = -1;

	}
	
	public void setInteraction(Interaction i){
		_interaction = i;
	}

	public Interaction getInteraction(){
		return _interaction;
	}
	
	public void setNode(Node n){
		_node = n;
	}
	
	public Node getNode(){
		return _node;
	}
	
	public void setPairedEnd(Anchor p){
		_paired = p;
	}
	
	public Anchor getPairedEnd(){
		return _paired;
	}
	
	public int getDistanceToNode(){
		return _dtonode;
	}
	
	public void setDistanceToNode(int dtn){
		_dtonode = dtn;
	}
	
}
