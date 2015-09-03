package quin.network;

import java.util.Map.Entry;
import java.util.TreeMap;

public class ParameterAnalyzer {
	
	public Integer[] getSL(NetworkBuilder nb){
		int interval = 1000;
		Interaction[] interactions = nb.getInteractions();
		TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
		for(int i = 0; i < interactions.length; i++){
			Integer distance = interactions[i].getDistance();
			int count = interactions[i].getPETCount();
			if(distance != null){
				int rdistance = Math.max(distance,0)/interval;
				Integer curcount = tm.get(rdistance);
				if(curcount == null){
					tm.put(rdistance, count);
				}
				else{
					tm.put(rdistance, curcount+count);
				}
			}
		}
		Integer[] counts = new Integer[tm.lastKey()+1];
		
		for(int i = 0; i < counts.length; i++){
			counts[i] = 0;
		}
		while(!tm.isEmpty()){
			Entry<Integer, Integer> e = tm.pollFirstEntry();
			counts[e.getKey()] = e.getValue();
		}
		
		return counts;
	}
	
	public Integer[] getMaxDist(NetworkBuilder nb, int interval, int sl, int ext){
		Edge[] edges = null;
		try {
			edges = nb.buildNetwork(ext, sl, 0, 0, 0, 0, 0, 0).getEdges();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
		for(int i = 0; i < edges.length; i++){
			Integer distance = edges[i].getDistance();
			if(distance != null){
				int rdistance = (distance/interval);
				Integer curcount = tm.get(rdistance);
				if(curcount == null){
					tm.put(rdistance, 1);
				}
				else{
					tm.put(rdistance, curcount+1);
				}
			}
		}
		Integer[] counts = new Integer[tm.lastKey()+1];
		
		for(int i = 0; i < counts.length; i++){
			counts[i] = 0;
		}
		while(!tm.isEmpty()){
			Entry<Integer, Integer> e = tm.pollFirstEntry();
			counts[e.getKey()] = e.getValue();
		}
		
		return counts;
	}
	
	public Integer[] getExt(NetworkBuilder nb, int interval, int max, int sl){
		Integer[] rv = new Integer[max];
		for(int i = 0; i < max; i ++){
			int ext = i*interval;
			try {
				ChIAPETNetwork network = nb.buildNetwork(ext, sl, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
				rv[i] = network.getNodeCount();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rv;
	}

	public void getExtGraph(NetworkBuilder nb, int sl){
		
		
		for(int i = 0; i < 25000; i += 500){
			
			//nb.buildNetwork(i, sl, minpet, maxdist, minmaxpet, mininterpet, mintwopet)
			
		}
		
	}
	
}
