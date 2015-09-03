package goanalysis.gohierarchy;

import java.util.LinkedList;
import java.util.List;

public class Node<T> {

	private T _data;
	private List<Node<T>> _parents;
	private List<Node<T>> _children;
	
	public Node(T data){
		_data = data;
		_parents = new LinkedList<Node<T>>();
		_children = new LinkedList<Node<T>>();
	}
	
	public void addParent(Node<T> parent){
		_parents.add(parent);
	}
	
	public void addChild(Node<T> child){
		_children.add(child);
	}
	
	@SuppressWarnings("unchecked")
	public Node<T>[] getChildren(){
		return _children.toArray(new Node[0]);
	}
	
	@SuppressWarnings("unchecked")
	public Node<T>[] getParents(){
		return _parents.toArray(new Node[0]);
	}
	
	public T getData(){
		return _data;
	}
	
}
