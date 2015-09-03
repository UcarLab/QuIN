package quin.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Dendrogram {

	
	private Node[] _nodes;
	private int _iteration;
	private int _maxiter;
	private double _maxq;
	//private double _lastq;
	
	//Not exactly a dendrogram in some sense, but helps with dividing the network into communities
	//specifically for the CommunityStructure class, may be hard to use elsewhere
	public Dendrogram(int size, double q0){
		_nodes = new Node[size];
		for(int i = 0; i < size; i++){
			_nodes[i] = new Leaf(i);
		}
		_maxiter = 0;
		_maxq = q0;
		_iteration = 1;
	}
	
	public int[][] getCommunities(){
		Node root = getRoot();
		//System.out.println(_lastq);
		/*if(_lastq < 0){
			_maxiter = _iteration-1;
		}*/
		Node[] communities = getCommunityRoots(root, _maxiter);
		int[][] rv = new int[_nodes.length-_maxiter][];
		for(int i = 0; i < communities.length; i++){
			rv[i] = getIndices(communities[i]);
		}
		return rv;
	}
	
	private Node getRoot(){
		int l = _nodes.length;
		for(int i = 0; i < l; i++){
			if(_nodes[i].getIteration() == l-1){
				return _nodes[i];
			}
		}
		return null;
	}
	
	private Node[] getCommunityRoots(Node root, int maxiter){
		List<Node> l = new LinkedList<Node>();
		LinkedList<Node> todo = new LinkedList<Node>();
		todo.add(root);
		while(!todo.isEmpty()){
			Node next = todo.removeLast();
			if(next.getIteration() <= maxiter){
				l.add(next);
			}
			else{
				ClusterNode cn = (ClusterNode) next;
				Node[] children = cn.getNodes();
				todo.add(children[0]);
				todo.add(children[1]);
			}
		}
		return l.toArray(new Node[0]);
	}
	
	private int[] getIndices(Node root){
		List<Integer> l = new LinkedList<Integer>();
		LinkedList<Node> todo = new LinkedList<Node>();
		todo.add(root);
		while(!todo.isEmpty()){
			Node next = todo.removeLast();
			if(next instanceof Leaf){
				Leaf ln = (Leaf) next;
				l.add(ln.getIndex());
			}
			else if(next instanceof ClusterNode){
				ClusterNode cn = (ClusterNode) next;
				Node[] nodes = cn.getNodes();
				todo.add(nodes[0]);
				todo.add(nodes[1]);
			}
		}
		
		int[] rv = new int[l.size()];
		int i = 0;
		for(Iterator<Integer> it = l.iterator(); it.hasNext();){
			rv[i++] = it.next();
		}
		return rv;
	}
	
	
	public void merge(int i1, int i2, double q) throws Exception{
		//_lastq = q;
		if(_iteration >= _nodes.length){
			throw new Exception("Merge called too many times.  Should only be called (n-1) times!");
		}
		ClusterNode cn = new ClusterNode(_nodes[i1], _nodes[i2], _iteration);
		_nodes[i1] = cn;
		_nodes[i2] = cn;
		if(q >= _maxq){
			_maxq = q;
			_maxiter = _iteration;
		}
		_iteration++;
	}
	
	
	private abstract class Node {
		private int _iteration;
		
		public void setIteration(int i){
			_iteration = i;
		}
		
		public int getIteration(){
			return _iteration;
		}
	}
	
	private class Leaf extends Node {
		private int _index;

		public Leaf(int index){
			setIteration(0);
			_index = index;
		}
		
		public int getIndex(){
			return _index;
		}
	}
	
	private class ClusterNode extends Node {
		private Node _n1;
		private Node _n2;
		
		public ClusterNode(Node n1, Node n2, int iteration){
			_n1 = n1;
			_n2 = n2;
			setIteration(iteration);
		}
		
		public Node[] getNodes(){
			return new Node[] {_n1, _n2};
		}
	}
	
}
