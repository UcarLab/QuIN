package quin.web.networkjson;

public class Edge {

	private Integer id;
	private Integer node1;
	private Integer node2;
	private Integer petcount;
	private Integer interactioncount;
	
	public void setId(int newid){
		id = newid;
	}
	
	public Integer getId(){
		return id;
	}
	
	public void setNode1(int n1id){
		node1 = n1id;
	}
	
	public Integer getNode1(){
		return node1;
	}
	
	public void setNode2(Integer n2id){
		node2 = n2id;
	}
	
	public Integer getNode2(){
		return node2;
	}
	
	
	public void setPETCount(Integer newpetcount){
		petcount = newpetcount;
	}
	
	public Integer getPETCount(){
		return petcount;
	}
	
	public void setInteractionCount(Integer newinter){
		interactioncount = newinter;
	}
	
	public Integer getInteractionCount(){
		return interactioncount;
	}
	
}
