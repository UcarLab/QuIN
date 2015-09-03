package quin.network.analysis;

import java.util.Iterator;
import java.util.LinkedList;

import quin.network.ChIAPETNetwork;
import quin.network.ConnectedComponent;
import quin.network.Edge;
import quin.network.Node;

public class Centrality {
	
	public void computeCentrality(ChIAPETNetwork n){
		computeCloseness(n);
		computeHarmonic(n);
		computeBetweenness(n);
	}

	public void computeCloseness(ChIAPETNetwork n){
		ConnectedComponent[] ccs = n.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			ConnectedComponent curcc = ccs[i];
			Node[] nodes = curcc.getNodes();
			int ccsize = nodes.length;
			for(int j = 0; j < nodes.length; j++){
				Node curnode = nodes[j];				
				double closeness = 1/(double)getCloseness(curnode, nodes);
				double linsindex = closeness*ccsize*ccsize;
				curnode.setCloseness(closeness);
				curnode.setLinsIndex(linsindex);
			}
		}
		
	}
	
	public void computeHarmonic(ChIAPETNetwork n){
		ConnectedComponent[] ccs = n.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			ConnectedComponent curcc = ccs[i];
			Node[] nodes = curcc.getNodes();
			for(int j = 0; j < nodes.length; j++){
				nodes[j].setHarmonic(getHarmonic(nodes[j], nodes));
			}
		}
	}
	
	public void computeBetweenness(ChIAPETNetwork n){
		ConnectedComponent[] ccs = n.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			ConnectedComponent curcc = ccs[i];
			Node[] nodes = curcc.getNodes();
			double[] bc = getBetweenness(nodes);
			
			for(int j = 0; j < nodes.length; j++){
				nodes[j].setBetweenness(bc[j]);
			}
		}
	}
	
	//"A Faster Algorithm For Betweenness Centrality" - Ulrik Brandes (2001)
	private double[] getBetweenness(Node[] nodes){
		double[] betweenness = new double[nodes.length]; //holds currently known scores
		
		for(int i = 0; i < nodes.length; i++){
			Node s = nodes[i];
			int scid = s.getCCIndex();
			LinkedList<Node> S = new LinkedList<Node>();
			@SuppressWarnings("unchecked")
			LinkedList<Node>[] P = new LinkedList[nodes.length];
			int[] o = new int[nodes.length];
			o[scid] = 1;
			int[] d = new int[nodes.length];
			for(int j = 0; j < nodes.length; j++){
				P[j] = new LinkedList<Node>();
				d[j] = -1;
			}
			d[scid] = 0;
			LinkedList<Node> Q = new LinkedList<Node>();
			Q.add(s);
			while(!Q.isEmpty()){
				Node v = Q.removeFirst();
				int vcid = v.getCCIndex();
				S.push(v);
				
				Edge[] edges = v.getEdges();
				for(int j = 0; j < edges.length; j++){
					Node w = edges[j].getAdjacentNode(v);
					int wcid = w.getCCIndex();
					if(d[wcid] < 0){
						Q.add(w);
						d[wcid] = d[vcid]+1;
					}
					
					if(d[wcid] == (d[vcid]+1)){
						o[wcid] += o[vcid];
						P[wcid].add(v);
					}
				}
			}
			
			double[] g = new double[nodes.length];
			
			while(!S.isEmpty()){
				Node w = S.pop();
				int wcid = w.getCCIndex();
				for(Iterator<Node> it = P[wcid].iterator(); it.hasNext();){
					Node v = it.next();
					int vcid = v.getCCIndex();
					g[vcid] += (double)o[vcid]/o[wcid]*(1+g[wcid]);
				}
				if(wcid != scid){
					betweenness[wcid] += g[wcid];
				}
			}
			
			
		}
		
		return betweenness;
	}
	
	//BFS
	private double getHarmonic(Node source, Node[] nodes){		
		boolean[] visited = new boolean[nodes.length];
		visited[source.getCCIndex()] = true;
		
		LinkedList<Node> todo = new LinkedList<Node>();
		LinkedList<Integer> pathcount = new LinkedList<Integer>();
		Edge[] edges = source.getEdges();
		for(int j = 0; j < edges.length; j++){
			Node an = edges[j].getAdjacentNode(source);
			int anid = an.getCCIndex();
			if(!visited[anid]){
				visited[anid] = true;
				todo.add(an);
				pathcount.add(1);
			}
		}

		
		double sp = 0;
		while(!todo.isEmpty()){
			Node cn = todo.removeFirst();
			int iteration = pathcount.removeFirst();
			sp += 1/(double)iteration++;
			edges = cn.getEdges();
			for(int j = 0; j < edges.length; j++){
				Node an = edges[j].getAdjacentNode(cn);
				int anid = an.getCCIndex();
				if(!visited[anid]){
					visited[anid] = true;
					todo.add(an);
					pathcount.add(iteration);
				}
			}
		}

		return sp;
	}
	
	
	//BFS
	private int getCloseness(Node source, Node[] nodes){				
		LinkedList<Node> todo = new LinkedList<Node>();
		LinkedList<Integer> pathcount = new LinkedList<Integer>();
		todo.add(source);
		pathcount.add(0);
		
		boolean[] visited = new boolean[nodes.length];
		visited[source.getCCIndex()] = true;
		
		int sp = 0;
		while(!todo.isEmpty()){
			Node cn = todo.removeFirst();
			int iteration = pathcount.removeFirst();;
			sp += iteration++;
			Edge[] edges = cn.getEdges();
			for(int j = 0; j < edges.length; j++){
				Node an = edges[j].getAdjacentNode(cn);
				int anid = an.getCCIndex();
				if(!visited[anid]){
					visited[anid] = true;
					todo.add(an);
					pathcount.add(iteration);
				}
			}
		}

		return sp;
	}
	
}
