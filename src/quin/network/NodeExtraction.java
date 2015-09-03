package quin.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

public class NodeExtraction {
	
	public Node[] getNodes(Anchor[] pe, int ext, int sldist){
		Util u = new Util();
		TreeMap<String, Location[]> tm = u.getChrStartSorted(pe);
		int nid = 0;
		LinkedList<Node> nl = new LinkedList<Node>();
		while(!tm.isEmpty()){			
			Entry<String, Location[]> e = tm.pollFirstEntry();
			LinkedList<Anchor> cl = new LinkedList<Anchor>();

			Location[] sortedpe = e.getValue();
			String chr = e.getKey();
			int end = sortedpe[0].getEnd();
			int start = Math.max(sortedpe[0].getStart(), 0);
			int eend = end+ext;
			
			cl.add((Anchor)sortedpe[0]);
			
			for(int i = 1; i < sortedpe.length; i++){
				Anchor cpe = (Anchor)sortedpe[i];
				int cpestart = cpe.getStart();
				int cpeend = cpe.getEnd();
				Interaction cpei = cpe.getInteraction();
				Integer cpeid = cpei.getDistance();
				if(cpeid != null && cpei.getDistance() < sldist){
					cpeend = Math.max(cpeend, cpei.getAnchor(cpe).getEnd());
				}

				if(cpestart <= eend){
					end = Math.max(end, cpeend);
					eend = end+ext;
				}
				else{
					//Create a new node
					Anchor[] pea = cl.toArray(new Anchor[0]);
					Node n = new Node(nid++, chr, start, end, pea);
					nl.add(n);
					for(int j = 0; j < pea.length; j++){
						pea[j].setNode(n);
					}
					nl.add(n);
					cl = new LinkedList<Anchor>();
					start = cpestart;
					end = cpeend;
					eend = end+ext;
				}
				cl.add(cpe);
			}
			Anchor[] pea = cl.toArray(new Anchor[0]);
			Node n = new Node(nid++, chr, start, end, pea);
			nl.add(n);
			for(int i = 0; i < pea.length; i++){
				pea[i].setNode(n);
			}
		}
		return nl.toArray(new Node[0]);
	}
	
	
	//peak files by definition should not be overlapping with each other
	public Node[] getNodes(Anchor[] anchors, Location[] peaks, int ext){
		Util u = new Util();

		TreeMap<String, Location[]> sortedpeaks = u.getChrStartSorted(peaks);
		TreeMap<String, Location[]> sortedanchors = u.getChrStartSorted(anchors);

		LinkedList<Node> nodes = new LinkedList<Node>();
		int nid = 0;
		while(!sortedpeaks.isEmpty()){
			Entry<String, Location[]> e = sortedpeaks.pollFirstEntry();
			String chr = e.getKey();
			Location[] chrpeaks = e.getValue();
			Location[] chranchors = sortedanchors.remove(chr);
			if(chranchors == null){
				continue;
			}
			int ai = 0;
			int pi = 0;
			@SuppressWarnings("unchecked")
			LinkedList<Anchor>[] potentialnodes = new LinkedList[chrpeaks.length];

			for(int i = 0; i < chrpeaks.length; i++){
				Location curpeak = chrpeaks[i];
				potentialnodes[i] = new LinkedList<Anchor>();
				Location prevpeak = curpeak;
				Location nextpeak = curpeak;
				if(i > 0){
					prevpeak = chrpeaks[i-1];
				}
				if(i+1 < chrpeaks.length){
					nextpeak = chrpeaks[i+1];
				}

				//Step 1
				int li = i-1;
				for(int i2 = 0; i2 < pi; i2++){
					//Remove if the anchor is not overlapping with just one without extending
					Anchor a = potentialnodes[li].removeLast();
					LinkedList<Anchor> putback = new LinkedList<Anchor>();

					if(!(a.getStart() <= curpeak.getEnd()+ext && curpeak.getStart() <= a.getEnd()+ext)){ //if doesn't overlap with the current peak, add it back
						putback.add(a);
					}
					else{	//Otherwise, do a tiebreaker where the anchor is put in the single node that overlaps without extension, ambiguous if both
						int tb = tiebreak(chrpeaks[li], a, curpeak);
						if(tb < 0){
							putback.add(a);
						}
						else if(tb > 0){
							potentialnodes[i].add(a);
						}
						else {
							a.getInteraction().setReason(Interaction.AMBIGUOUS);
						}
					}
					
					potentialnodes[li].addAll(putback);
					ai++;
				}
				
				//Step 2
				while(ai < chranchors.length && chranchors[ai].getEnd() <= curpeak.getEnd()-ext){
					Anchor anchor  = (Anchor)chranchors[ai];
					if(curpeak.getStart() <= chranchors[ai].getEnd()+ext){
						potentialnodes[i].add(anchor);
					}
					setMinDistance(prevpeak, anchor, curpeak);
					ai++;
				}
				
				//Step 3
				pi = 0;
				while((ai+pi) < chranchors.length && chranchors[ai+pi].getStart() <= curpeak.getEnd()+ext){
					Anchor a = (Anchor)chranchors[ai+pi];
					potentialnodes[i].add(a);
					setMinDistance(curpeak, a, nextpeak);
					pi++;
				}
			}
			Location lastpeak = chrpeaks[chrpeaks.length-1];
			for(int i = ai; i < chranchors.length; i++){
				setMinDistance(lastpeak, (Anchor) chranchors[i], lastpeak);
			}
			
			//create nodes from nonempty anchor lists
			for(int i = 0; i < potentialnodes.length; i++){
				LinkedList<Anchor> al = potentialnodes[i];
				Location peak = chrpeaks[i];
				if(al.size() > 0){
					Node newnode = new Node(nid++, peak.getChr(), peak.getStart(), peak.getEnd(), al.toArray(new Anchor[0]));
					for(Iterator<Anchor> it = al.iterator(); it.hasNext();){
						it.next().setNode(newnode);
					}
					nodes.add(newnode);
				}
			}
		}
		
		return nodes.toArray(new Node[0]);
	}
	
	private void setMinDistance(Location pn, Anchor a, Location nn){
		if(pn.equals(nn)){
			a.setDistanceToNode(Math.max(0,Math.max(a.getStart()-pn.getEnd(), nn.getStart()-a.getEnd())));
		}
		else{
			a.setDistanceToNode(Math.max(0,Math.min(a.getStart()-pn.getEnd(), nn.getStart()-a.getEnd())));
		}
	}
	
	private int tiebreak(Location pn, Anchor a, Location sn){
		boolean ipn = pn.getEnd() <= a.getStart() && pn.getStart() <= a.getEnd();
		boolean isn = sn.getEnd() <= a.getStart() && sn.getStart() <= a.getEnd();
		
		if(ipn && !isn){
			return -1;
		}
		
		if(isn && !ipn){
			return 1;
		}
		
		return 0;
	}
	
	
}
