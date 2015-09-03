package quin.network;

public class Interaction {
	
	public static int SAMENODE = 1;
	public static int NOANCHOR = 2;
	public static int ONEANCHOR = 3;
	public static int DISTANCE = 4;
	public static int INTERCHROM = 5;
	public static int AMBIGUOUS = 6;
	public static int COMPONENTSIZE = 7;
	public static int COMMUNITY = 8;
	public static int INTRACHROM = 9;

	
	private int _id;
	private Edge _edge;
	private Anchor _n1;
	private Anchor _n2;
	private int _petcount;
	private Integer _distance;
	private int _removereason;
	
	public Interaction(int id, Anchor n1, Anchor n2, int rc){
		_id = id;
		_n1 = n1;
		_n2 = n2;
		_n1.setInteraction(this);
		_n1.setPairedEnd(_n2);
		_n2.setInteraction(this);
		_n2.setPairedEnd(_n1);
		_petcount = rc;
		if(_n2.getChr().equals(_n1.getChr())){
			_distance = _n2.getStart()-_n1.getEnd()+1;
		}
		else {
			_distance = null;
		}
		_removereason = 0;
	}
	
	public int getId(){
		return _id;
	}

	public Anchor[] getAnchors(){
		return new Anchor[]{_n1, _n2};
	}
	
	public Anchor getAnchor(Anchor n){
		if(n.equals(_n1)){
			return _n2;
		}
		else if(n.equals(_n2)){
			return _n1;
		}
		return null;
	}
	
	public int getPETCount(){
		return _petcount;
	}
	
	public void setEdge(Edge e){
		_edge = e;
	}
	
	public Edge getEdge(){
		return _edge;
	}
	
	public Integer getDistance(){
		return _distance;
	}
	
	public int getReason(){
		return _removereason;
	}
	
	public void setReason(int reasoncode){
		_removereason = reasoncode;
	}
	
	
}
