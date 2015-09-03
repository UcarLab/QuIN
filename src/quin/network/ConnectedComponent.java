package quin.network;

public class ConnectedComponent {

	private Node[] _nodes;
	private Edge[] _edges;
	
	public ConnectedComponent(Node[] nodes, Edge[] edges){
		_nodes = nodes;
		_edges = edges;
	}
	
	public Node[] getNodes(){
		return _nodes;
	}
	
	public Edge[] getEdges(){
		return _edges;
	}
	
	public int getNodeCount(){
		return _nodes.length;
	}
	
	public int getEdgeCount(){
		return _edges.length;
	}
	
}
