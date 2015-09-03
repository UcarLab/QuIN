package quin.network.merge;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import quin.network.Anchor;
import quin.network.Node;

public class NodeIntersection {

	private TreeMap<Integer, Integer> _n1;
	private TreeMap<Integer, Integer> _n2;
	private TreeMap<Integer, TreeSet<Integer>[]> _groups;
	private int _count;
	
	public NodeIntersection(){
		_n1 = new TreeMap<Integer, Integer>();
		_n2 = new TreeMap<Integer, Integer>();
		_groups = new TreeMap<Integer, TreeSet<Integer>[]>();
		_count = 0;
	}
	
	
	@SuppressWarnings("unchecked")
	public void addIntersection(int nid1, int nid2){
		
		boolean cn1 = _n1.containsKey(nid1);
		boolean cn2 = _n2.containsKey(nid2);
		
		if(cn1 && cn2){
			int id1 = _n1.get(nid1);
			int id2 = _n2.get(nid2);
			if(id1 != id2){
				//Have to merge the groups
				TreeSet<Integer>[] tsets1 = _groups.get(id1);
				TreeSet<Integer>[] tsets2 = _groups.get(id2);
				
				tsets1[0].addAll(tsets2[0]);
				tsets1[1].addAll(tsets2[1]);
				
				//remap the ids
				for(Iterator<Integer> it = tsets2[0].iterator(); it.hasNext();){
					_n1.put(it.next(), id1);
				}
				
				for(Iterator<Integer> it = tsets2[1].iterator(); it.hasNext();){
					_n2.put(it.next(), id1);
				}
				
				//remove the group since it was merged with the first one.
				_groups.remove(id2);
			}
			
		}
		else if(!cn1 && !cn2){
			//new mapping
			int id = _count++;
			TreeSet<Integer> ts1 =  new TreeSet<Integer>();
			TreeSet<Integer> ts2 =  new TreeSet<Integer>();
			ts1.add(nid1);
			ts2.add(nid2);
			_groups.put(id, new TreeSet[]{ ts1, ts2 });
			_n1.put(nid1, id);
			_n2.put(nid2, id);
		}
		else{
			//existing mapping
			if(cn1){
				int id = _n1.get(nid1);
				_n2.put(nid2, id);
				_groups.get(id)[1].add(nid2);
			}
			else{
				int id = _n2.get(nid2);
				_n1.put(nid1, id);
				_groups.get(id)[0].add(nid1);
			}
		}
	}
	
	public int[] getTotalIntersectingNodes(){
		return new int[] { _n1.size(), _n2.size() };
	}
	
	
	public TreeMap<Integer, Integer> getN1EdgeMapping(){
		return _n1;
	}
	
	public TreeMap<Integer, Integer> getN2EdgeMapping(){
		return _n2;
	}
	
	public TreeMap<Integer, Node> getOuterNodes(TreeMap<Integer, LinkedList<Anchor>> a1, TreeMap<Integer, LinkedList<Anchor>> a2){
		TreeMap<Integer, Node> m = new TreeMap<Integer, Node>();
		for(Iterator<Entry<Integer, TreeSet<Integer>[]>> it = _groups.entrySet().iterator(); it.hasNext();){
			Entry<Integer, TreeSet<Integer>[]> next = it.next();
			int id = next.getKey();
			TreeSet<Integer>[] sets = next.getValue();
			
			LinkedList<Anchor> anchors = new LinkedList<Anchor>();
			
			for(Iterator<Integer> it2 = sets[0].iterator(); it2.hasNext();){
				anchors.addAll(a1.get(it2.next()));
			}
			
			for(Iterator<Integer> it2 = sets[1].iterator(); it2.hasNext();){
				anchors.addAll(a2.get(it2.next()));
			}
			
			String chr = anchors.getFirst().getChr();
			int start = Integer.MAX_VALUE;
			int end = Integer.MIN_VALUE;
			Anchor[] aa = new Anchor[anchors.size()];
			int c = 0;
			for(Iterator<Anchor> it2 = anchors.iterator(); it2.hasNext();){
				Anchor a = it2.next();
				start = Math.min(a.getStart(), start);
				end = Math.max(a.getEnd(), end);
				aa[c++] = a;
			}
			
			Node n = new Node(id, chr, start, end, aa);
			m.put(id, n);
			
			for(Iterator<Anchor> it2 = anchors.iterator(); it2.hasNext();){
				it2.next().setNode(n);
			}
		}
		
		return m;
	}
	
	public TreeMap<Integer, Node> getInnerNodes(int start, int end, TreeMap<Integer, LinkedList<Anchor>> a1, TreeMap<Integer, LinkedList<Anchor>> a2){
		TreeMap<Integer, Node> m = new TreeMap<Integer, Node>();
		for(Iterator<Entry<Integer, TreeSet<Integer>[]>> it = _groups.entrySet().iterator(); it.hasNext();){
			Entry<Integer, TreeSet<Integer>[]> next = it.next();
			int id = next.getKey();
			TreeSet<Integer>[] sets = next.getValue();
			
			LinkedList<Anchor> anchors = new LinkedList<Anchor>();
			
			for(Iterator<Integer> it2 = sets[0].iterator(); it2.hasNext();){
				anchors.addAll(a1.get(it2.next()));
			}
			
			for(Iterator<Integer> it2 = sets[1].iterator(); it2.hasNext();){
				anchors.addAll(a2.get(it2.next()));
			}
			
			String chr = anchors.getFirst().getChr();
//			int start = Integer.MAX_VALUE;
//			int end = Integer.MIN_VALUE;
			Anchor[] aa = new Anchor[anchors.size()];
			int c = 0;
			for(Iterator<Anchor> it2 = anchors.iterator(); it2.hasNext();){
				Anchor a = it2.next();
				start = Math.min(a.getStart(), start);
				end = Math.max(a.getEnd(), end);
				aa[c++] = a;
			}
			
			Node n = new Node(id, chr, start, end, aa);
			m.put(id, n);
			
			for(Iterator<Anchor> it2 = anchors.iterator(); it2.hasNext();){
				it2.next().setNode(n);
			}
		}
		
		return m;
	}
}
