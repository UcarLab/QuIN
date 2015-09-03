package quin.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Util {

	public TreeMap<String, Location[]> getChrStartSorted(Location[] pe){
		TreeMap<String, Location[]> chrsorted = getChrSorted(pe);
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!chrsorted.isEmpty()){
			Entry<String, Location[]> e = chrsorted.pollFirstEntry();
			rv.put(e.getKey(), getStartSorted(e.getValue()));
		}	
		return rv;
	}
	
	public TreeMap<String, Location[]> getChrEndSorted(Location[] pe){
		TreeMap<String, Location[]> chrsorted = getChrSorted(pe);
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!chrsorted.isEmpty()){
			Entry<String, Location[]> e = chrsorted.pollFirstEntry();
			rv.put(e.getKey(), getEndSorted(e.getValue()));
		}	
		return rv;
	}
	
	public TreeMap<String, Location[]> getChrSorted(Location[] pe){
		TreeMap<String, LinkedList<Location>> tm = new TreeMap<String, LinkedList<Location>>();
		for(int i = 0; i < pe.length; i++){
			String chr = pe[i].getChr().toLowerCase();
			if(!tm.containsKey(chr)){
				tm.put(chr, new LinkedList<Location>());
			}
			tm.get(chr).add(pe[i]);
		}
		
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!tm.isEmpty()){
			Entry<String, LinkedList<Location>> e = tm.pollFirstEntry();
			rv.put(e.getKey(), e.getValue().toArray(new Location[0]));
		}
		return rv;
	}
	
	private Location[] getStartSorted(Location[] pe){
		return getSorted(pe, new SortAdapter<Location>(){
			@Override
			public int getValue(Location e) {
				return e.getStart();
			}

			@Override
			public LinkedList<Location> getEntry(TreeMap<Integer, LinkedList<Location>> tm) {
				return tm.pollFirstEntry().getValue();
			}
		});
	}
	
	private Location[] getEndSorted(Location[] pe){
		return getSorted(pe, new SortAdapter<Location>(){
			@Override
			public int getValue(Location e) {
				return e.getEnd();
			}

			@Override
			public LinkedList<Location> getEntry(TreeMap<Integer, LinkedList<Location>> tm) {
				return tm.pollLastEntry().getValue();
			}
		});
	}
	
	
	private Location[] getSorted(Location[] pe, SortAdapter<Location> sa){
		TreeMap<Integer, LinkedList<Location>> tm = new TreeMap<Integer, LinkedList<Location>>();
		for(int i = 0; i < pe.length; i++){
			Location pei = pe[i];
			int start = sa.getValue(pei);
			if(!tm.containsKey(start)){
				tm.put(start, new LinkedList<Location>());
			}
			tm.get(start).add(pei);
		}
		Location[] rv = new Location[pe.length];
		int i = 0;
		while(!tm.isEmpty()){
			LinkedList<Location> l = sa.getEntry(tm);
			for(Iterator<Location> it = l.iterator(); it.hasNext();){
				rv[i++] = it.next();
			}
		}
		return rv;
	}
	
	public Location[] getNonOverlappingLocations(Location[] loc){
		TreeMap<String, Location[]> chrsorted = getChrStartSorted(loc);
		LinkedList<Location> rv = new LinkedList<Location>();
		
		while(!chrsorted.isEmpty()){
			Location[] sl = chrsorted.pollFirstEntry().getValue();
			
			int i = 0;
			while(i < sl.length-1){
				Location cl = sl[i];
				Location nl = sl[i+1];
				if(nl.getStart() <= cl.getEnd()){
					int si = i;
					i++;
					while(i < sl.length-1){
						cl = sl[i];
						nl = sl[i+1];
						if(nl.getStart() > cl.getEnd()){
							int end = sl[si].getEnd();
							for(int j = si+1; j < i; j++){
								end = Math.max(sl[j].getEnd(), end);
							}
							rv.add(new Location(sl[si].getId(), sl[si].getChr(), sl[si].getStart(), end));
							break;
						}
						else{
							i++;
						}
					}
				}
				else{
					rv.add(cl);
				}
				i++;
			}
			if(i < sl.length){
				rv.add(sl[i]);
			}
		}
		
		return rv.toArray(new Location[0]);
	}
	
	public Node[] filterNodes(Node[] nodes){
		LinkedList<Node> l = new LinkedList<Node>();
		for(int i = 0; i < nodes.length; i++){
			Node cur = nodes[i];
			if(cur.getDegree() > 0){
				l.add(cur);
			}
		}
		
		return l.toArray(new Node[0]);
	}
	
	
	private interface SortAdapter<T> {
		
		public int getValue(T e);
		
		public LinkedList<T> getEntry(TreeMap<Integer, LinkedList<T>> tm);
		
	}
}
