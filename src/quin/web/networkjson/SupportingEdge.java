package quin.web.networkjson;

public class SupportingEdge {

	private Integer id;
	private Integer node1;
	private Integer node2;
	private long dataid;
	
	public SupportingEdge(Integer iid, Integer inode1, Integer inode2, long did){
		id = iid;
		node1 = inode1;
		node2 = inode2;
		dataid = did;
	}
	
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
	
	public void setDataId(int ndataid){
		dataid = ndataid;
	}
	
	public long getDataId(){
		return dataid;
	}
}
