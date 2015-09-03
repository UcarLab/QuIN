package quin.network;

public class ChIAPETNetwork {

	private Node[] _nodes;
	private Edge[] _edges;
	private ConnectedComponent[] _ccs;
	private double _avgnr;
	private double _avger;
	private Anchor[] _pets;
	private Interaction[] _interactions;
	private NetworkParameters _params;

	public ChIAPETNetwork(Anchor[] pets, Interaction[] inter, Node[] nodes, Edge[] edges, ConnectedComponent[] ccs, NetworkParameters params){
		_pets = pets;
		_interactions = inter;
		_nodes = nodes;
		_edges = edges;
		_ccs = ccs;
		_params = params;
		
		int sum = 0;
		for(int i = 0; i < _nodes.length; i++){
			sum += _nodes[i].getPETCount();
		}
		_avgnr = (double)sum/_nodes.length;
		
		sum = 0;
		for(int i = 0; i < _edges.length; i++){
			sum += _edges[i].getPETCount();
		}
		_avger = (double)sum/_edges.length;

	}
	
	public Anchor[] getPairedEnds(){
		return _pets;
	}
	
	public Interaction[] getInteractions(){
		return _interactions;
	}
	
	public int getCCCount(){
		return _ccs.length;
	}
	
	public double getAvgPETPerEdge(){
		return _avger;
	}
	
	public double getAvgPETPerNode(){
		return _avgnr;
	}
	
	public int getNodeCount(){
		return _nodes.length;
	}
	
	public int getEdgeCount(){
		return _edges.length;
	}
	
	public Node[] getNodes(){
		return _nodes;
	}
	
	public Edge[] getEdges(){
		return _edges;
	}
	
	public ConnectedComponent[] getCCs(){
		return _ccs;
	}
	
	public NetworkParameters getParameters(){
		return _params;
	}

}
