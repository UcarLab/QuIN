package quin.network;

import java.util.LinkedList;

public class Node extends Location{

	private Anchor[] _pairedends;
	private LinkedList<Edge> _edges;
	private Double _closeness;
	private Double _betweenness;
	public Double _harmonic;
	public Double _lin;
	public Double _gdistcloseness;
	public Integer _ccindex;
	public int _ccid;
	public Integer _petcount;
	
	public Node(int id, String chr, int start, int end, Anchor[] pes){
		super(id, chr, start, end);
		setPairedEnds(pes);
		_edges = new LinkedList<Edge>();
		_closeness = -1d;
		_betweenness = -1d;
		_harmonic = -1d;
		_lin = -1d;
		_gdistcloseness = -1d;
	}
	
	private void setPairedEnds(Anchor[] pes){
		_pairedends = pes;
		_petcount = 0;
		for(int i = 0; i < pes.length; i++){
			_petcount += pes[i].getInteraction().getPETCount();
		}
	}
	
	public void addEdge(Edge edge){
		_edges.add(edge);
	}
	
	public void removeEdge(Edge edge){
		_edges.remove(edge);
	}
	
	public Anchor[] getAnchors(){
		return _pairedends;
	}
	
	public Edge[] getEdges(){
		return _edges.toArray(new Edge[0]);
	}
	
	public int getInteractionCount(){
		return _pairedends.length;
	}
	
	public int getPETCount(){
		return _petcount;
	}
	
	public int getDegree(){
		return _edges.size();
	}
	
	public void setCloseness(Double c){
		_closeness = c;
	}
	
	public Double getCloseness(){
		return fixDouble(_closeness);
	}
	
	public void setBetweenness(Double b){
		_betweenness = b;
	}
	
	public Double getBetweenness(){
		return _betweenness;
	}
	
	
	public void setHarmonic(Double h){
		_harmonic = h;
	}
	
	public Double getHarmonic(){
		return _harmonic;
	}
	
	public void setLinsIndex(Double li){
		_lin = li;
	}
	
	public Double getLinsIndex(){
		return _lin;
	}

	public void setGDistCloseness(Double c){
		_gdistcloseness = c;
	}
	
	public Double getGDistCloseness(){
		return _gdistcloseness;
	}
	
	public void setCCIndex(Integer ccindex){
		_ccindex = ccindex;
	}
	
	public Integer getCCIndex(){
		return _ccindex;
	}
	
	public void setCCId(int ccid){
		_ccid = ccid;
	}
	
	public int getCCId(){
		return _ccid;
	}
	
	
	
	private Double fixDouble(Double d){
		if(d.isInfinite()){
			return 1d;
		}
		return d;
	}
	
}
