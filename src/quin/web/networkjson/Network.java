package quin.web.networkjson;

public class Network {

	private Node[] nodes;
	private Edge[] edges;
	
	public void setNodes(Node[] newnodes){
		nodes = newnodes;
	}
	
	public Node[] getNodes(){
		return nodes;
	}
	
	public void setEdges(Edge[] newedges){
		edges = newedges;
	}
	
	public Edge[] getEdges(){
		return edges;
	}

}
