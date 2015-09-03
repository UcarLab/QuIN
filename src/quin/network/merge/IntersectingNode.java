package quin.network.merge;

import java.util.List;

import quin.network.Node;

public class IntersectingNode {

	private List<Node> _nodes;
	
	public IntersectingNode(List<Node> nodes) {
		_nodes = nodes;
	}

	public List<Node> getNodes(){
		return _nodes;
	}
	
}
