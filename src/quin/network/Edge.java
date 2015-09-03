package quin.network;

public class Edge {

	private int _id;
	private Node _n1;
	private Node _n2;
	private Interaction[] _interactions;
	private int _count;
	private Integer _distance;
	private Double _betweenness;
	
	public Edge(int id, Node n1, Node n2, Interaction[] interactions){
		_id = id;
		_interactions = interactions;
		_n1 = n1;
		_n2 = n2;
		_count = 0;
		for(int i = 0; i < interactions.length; i++){
			Interaction ci = interactions[i];
			_count += ci.getPETCount();
		}
		
		_distance = null;
		if(_n1.getChr().equals(_n2.getChr())){
			_distance = Math.max(_n1.getEnd(), _n2.getEnd())-Math.min(_n1.getStart(), _n2.getStart());
		}
		_betweenness = null;
	}
	
	public int getId(){
		return _id;
	}
	
	public Node[] getNodes(){
		return new Node[]{_n1, _n2};
	}
	
	public Interaction[] getInteractions(){
		return _interactions;
	}
	
	public Node getAdjacentNode(Node n){
		if(n.equals(_n1)){
			return _n2;
		}
		else if(n.equals(_n2)){
			return _n1;
		}
		return null;
	}
	
	public int getInteractionCount(){
		return _interactions.length;
	}
	
	public int getPETCount(){
		return _count;
	}
	
	public Integer getDistance(){
		return _distance;
	}
	
	public void setBetweenness(Double d){
		_betweenness = d;
	}
	
	public Double getBetweenness(){
		return _betweenness;
	}

}
