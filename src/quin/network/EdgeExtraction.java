package quin.network;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;


public class EdgeExtraction {
	
	public Edge[] getEdges(Interaction[] interactions, int minpet, int maxdist, int minmaxpet, int mininterchrom){
		TreeMap<TwoIntKey, LinkedList<Interaction>> tm = new TreeMap<TwoIntKey, LinkedList<Interaction>>();

		for(int i = 0; i < interactions.length; i++){
			Interaction ci = interactions[i];
			if(ci.getReason() == Interaction.AMBIGUOUS){
				continue;
			}
			Anchor[] pe = ci.getAnchors();

			Node n1 = pe[0].getNode();
			Node n2 = pe[1].getNode();
			if(n1 == null || n2 == null || n1.getIntegerId() == n2.getIntegerId()){
				if(n1 == n2){
					if(n1 == null){
						ci.setReason(Interaction.NOANCHOR);
					}
					else{
						ci.setReason(Interaction.SAMENODE);	
					}
				}
				else{
					ci.setReason(Interaction.ONEANCHOR);
				}
				
				continue;
			}
			
			TwoIntKey key = new TwoIntKey(n1, n2);	
			if(!tm.containsKey(key)){
				tm.put(key, new LinkedList<Interaction>());
			}
			tm.get(key).add(ci);
		}
		LinkedList<Edge> el = new LinkedList<Edge>();
		int eid = 0;

		int amininterchrom = Math.abs(mininterchrom);
		while(!tm.isEmpty()){
			Entry<TwoIntKey, LinkedList<Interaction>> entry = tm.pollFirstEntry();
			TwoIntKey key = entry.getKey();
			Node n1 = key.getN1();
			Node n2 = key.getN2();
			
			Interaction[] intera = entry.getValue().toArray(new Interaction[0]);

			Edge e = new Edge(eid++, n1, n2, intera);
			Integer ed = e.getDistance();
			int petcount = e.getPETCount();
			
			if(e.getPETCount() >= minpet){
				if(ed != null && mininterchrom < 0){
					for(int i = 0; i < intera.length; i++){
						Interaction cur = intera[i];
						cur.setReason(Interaction.INTRACHROM);
					}
				}
				else if(ed != null && ed > maxdist && petcount < minmaxpet){
					for(int i = 0; i < intera.length; i++){
						Interaction cur = intera[i];
						cur.setReason(Interaction.DISTANCE);
					}
				}
				else if(ed == null && petcount < amininterchrom){
					for(int i = 0; i < intera.length; i++){
						Interaction cur = intera[i];
						cur.setReason(Interaction.INTERCHROM);
					}					
				}
				else{
					el.add(e);
					n1.addEdge(e);
					n2.addEdge(e);
					for(int i = 0; i < intera.length; i++){
						intera[i].setEdge(e);
					}
				}
			}

		}
		return el.toArray(new Edge[0]);
	}
	
	private class TwoIntKey implements Comparable<TwoIntKey>{
		private int _i1;
		private int _i2;
		private Node _n1;
		private Node _n2;
		
		public TwoIntKey(Node n1, Node n2){
			_i1 = n1.getIntegerId();
			_i2 = n2.getIntegerId();
			_n1 = n1;
			_n2 = n2;
			
			if(_i1 > _i2){
				int t = _i1;
				_i1 = _i2;
				_i2 = t;
				_n1 = n2;
				_n2 = n1;
			}
		}
		
		public Node getN1(){
			return _n1;
		}
		
		public Node getN2(){
			return _n2;
		}

		@Override
		public int compareTo(TwoIntKey o) {
			if(_i1 < o._i1 ){
				return -1;
			}
			else if(_i1 > o._i1){
				return 1;
			}
			else{
				if(_i2 < o._i2){
					return -1;
				}
				else if(_i2 > o._i2){
					return 1;
				}
				else{
					return 0;
				}
			}
		}
	}
	
}
