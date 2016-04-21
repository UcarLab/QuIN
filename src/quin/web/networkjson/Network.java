package quin.web.networkjson;

public class Network {

	private Node[] nodes;
	private Edge[] edges;
	private SupportingEdge[][] supportingedges;
	
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

	public void setSupportingEdges(SupportingEdge[][] newedges){
		supportingedges = newedges;
	}
	
	public SupportingEdge[][] getSupportingEdges(){
		return supportingedges;
	}
	
}
