package quin.network.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import quin.network.Location;
import quin.web.networkjson.Edge;

public class AnnotatedEdge extends Edge{

	private List<Location>[] _n1annotations;
	private List<Location>[] _n2annotations;
	
	public AnnotatedEdge(Edge e){
		this.setId(e.getId());
		this.setInteractionCount(e.getInteractionCount());
		this.setNode1(e.getNode1());
		this.setNode2(e.getNode2());
		this.setPETCount(e.getPETCount());
	}
	
	
	@SuppressWarnings("unchecked")
	public void setAnnotation(Map<Integer, List<Location>>[] annotations, Integer[] indices){
		_n1annotations = new List[indices.length];
		_n2annotations = new List[indices.length];
		
		for(int i = 0; i < indices.length; i++){
			_n1annotations[i] = annotations[i].get(this.getNode1());
			_n2annotations[i] = annotations[i].get(this.getNode2());
		}
	}
	
	
	public String getAnnotations(){
		StringBuilder sb = new StringBuilder();
		sb.append("\t"+this.getNode1());
		for(int i = 0; i < _n1annotations.length; i++){
			sb.append("\t");
			if(_n1annotations[i] != null){
				for(Iterator<Location> it = _n1annotations[i].iterator(); it.hasNext();){
					sb.append(it.next().toString()+"|");
				}
			}
		}
		sb.append("\t"+this.getNode2());
		for(int i = 0; i < _n2annotations.length; i++){
			sb.append("\t");
			if(_n2annotations[i] != null){
				for(Iterator<Location> it = _n2annotations[i].iterator(); it.hasNext();){
					sb.append(it.next().toString()+"|");
				}
			}
		}
		
		return sb.toString();
	}
}
