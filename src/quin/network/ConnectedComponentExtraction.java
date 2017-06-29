package quin.network;

import java.util.Iterator;
import java.util.LinkedList;

public class ConnectedComponentExtraction {

	public ConnectedComponent[] getCCs(Node[] nodes, int icount, int minsize, int mintwopet){
		LinkedList<ConnectedComponent> ccl = new LinkedList<ConnectedComponent>();
		
		int maxid = 0;
		for(int i = 0; i < nodes.length; i++){
			maxid = Math.max(nodes[i].getIntegerId(), maxid);
		}
		boolean[] nvisited = new boolean[maxid+1];
		boolean[] evisited = new boolean[icount];

		for(int i = 0; i < nodes.length; i++){
			int nid = nodes[i].getIntegerId();
			LinkedList<Node> todo = new LinkedList<Node>();
			LinkedList<Node> nl = new LinkedList<Node>();
			LinkedList<Edge> el = new LinkedList<Edge>();
			int maxpet = 0;
			if(!nvisited[nid]){
				todo.add(nodes[i]);
				nvisited[nid] = true;
				while(!todo.isEmpty()){
					Node cn = todo.removeFirst();
					nl.add(cn);
					Edge[] edges = cn.getEdges();
					for(int j = 0; j < edges.length; j++){
						int eid = edges[j].getId();
						if(!evisited[eid]){
							maxpet = Math.max(maxpet, edges[j].getPETCount());
							el.add(edges[j]);
							evisited[eid] = true;
							Node an = edges[j].getAdjacentNode(cn);
							int anid = an.getIntegerId();
							if(!nvisited[anid]){
								nvisited[anid] = true;
								todo.add(an);
							}
						}
					}
				}
				if(nl.size() >= minsize || maxpet > mintwopet){
					Node[] n = nl.toArray(new Node[0]);
					for(int j = 0; j < n.length; j++){
						n[j].setCCIndex(j);
					}
					ccl.add(new ConnectedComponent(n, el.toArray(new Edge[0])));
				}
				else{
					flagInteractions(el);
				}
			}
		}
		
		LinkedList<ConnectedComponent> fcl = new LinkedList<ConnectedComponent>();
		while(!ccl.isEmpty()){
			ConnectedComponent cc = ccl.removeFirst();
			fcl.add(cc);
		}
		return fcl.toArray(new ConnectedComponent[0]);
	}
	
	
	private void flagInteractions(LinkedList<Edge> el){
		for(Iterator<Edge> it = el.iterator(); it.hasNext();){
			Interaction[] interactions = it.next().getInteractions();
			for(int i = 0; i < interactions.length; i++){
				interactions[i].setReason(Interaction.COMPONENTSIZE);
			}
		}
	}
}
