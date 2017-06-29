package quin.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;


public class CommunityStructure {

	private int _maxccid;
	//"Fast algorithm for detecting community structure" - Newman (2004)
	public ConnectedComponent[] partitionNetwork(ConnectedComponent[] components, int icount, int minsize, int mintwopet){
		LinkedList<ConnectedComponent> rv = new LinkedList<ConnectedComponent>();
		for(int i = 0; i < components.length; i++){
			ConnectedComponent[] communities = getCommunities(components[i], getMaxNid(components[i]), icount, minsize, mintwopet);
			int[] csizes = new int[communities.length];
			int p = 0;
			for(int j = 0; j < communities.length; j++){
				csizes[j] = communities[j].getNodeCount();
				p += communities[j].getEdgeCount();
			}
			//System.out.println(components[i].getNodeCount()+"\t"+csizes.length+"\t"+components[i].getEdgeCount()+"\t"+p+"\t"+getSurpriseScore(components[i].getNodeCount(), csizes, components[i].getEdgeCount(), p));
		
			if(getSurpriseScore(components[i].getNodeCount(), csizes, components[i].getEdgeCount(), p) <= 0.0000001){
				for(int j = 0; j < communities.length; j++){
					rv.add(communities[j]);
				}
			}
			else{
				rv.add(components[i]);
			}
		}
		return rv.toArray(new ConnectedComponent[0]);
	}
	
	private int getMaxNid(ConnectedComponent c){
		Node[] nodes = c.getNodes();
		int rv = 0;
		for(int i = 0; i < nodes.length; i++){
			rv = Math.max(nodes[i].getIntegerId(), rv);
		}
		return rv;
	}
	
	private ConnectedComponent[] getCommunities(ConnectedComponent c, int maxnid, int icount, int minsize, int mintwopet){
		int[] nodeindex = getNodeIndex(maxnid, c.getNodes());
		Dendrogram d = getDendrogram(c, nodeindex);
		setCCIds(d, c.getNodes());
		removeEdges(c.getEdges());
		ConnectedComponentExtraction cce = new ConnectedComponentExtraction();
		return cce.getCCs(c.getNodes(), icount, minsize, mintwopet);
	}
	
	private void setCCIds(Dendrogram dendrogram, Node[] nodes){
		int[][] communities = dendrogram.getCommunities();
		for(int i = 0; i < communities.length; i++){
			for(int j = 0; j < communities[i].length; j++){
				nodes[communities[i][j]].setCCId(_maxccid+i+1);
			}
		}
		_maxccid =  _maxccid+communities.length+1;
	}
	
	private void removeEdges(Edge[] edges){
		for(int i = 0; i < edges.length; i++){
			removeEdge(edges[i]);
		}
	}
	
	private void removeEdge(Edge e){
		Node[] nodes = e.getNodes();
		if(nodes[0].getCCId() != nodes[1].getCCId()){
			Interaction[] interactions = e.getInteractions();
			for(int i = 0; i < interactions.length; i++){
				interactions[i].setEdge(null);
				interactions[i].setReason(Interaction.COMMUNITY);
			}
			nodes[0].removeEdge(e);
			nodes[1].removeEdge(e);
		}
	}
	
	//Node index here is nodeid->index
	private Dendrogram getDendrogram(ConnectedComponent c, int[] nodeindex){
	//Map from matrix position to nodeid
		int nodecount = c.getNodeCount();
		Edge[] edges = c.getEdges();
		int totalpet = getTotalPETCount(edges);

		double[][] ematrix =  getEMatrix(edges, nodeindex, nodecount, totalpet);
		double[] avector = getAVector(ematrix);
		double q = getQ(ematrix, avector);
		
		TreeSet<Integer> indices = new TreeSet<Integer>();
		for(int i = 0; i < nodecount; i++){
			indices.add(i);
		}
		
		Dendrogram dendrogram = new Dendrogram(nodecount, q);
		
		while(indices.size() > 1){
			double maxdq = Double.NEGATIVE_INFINITY;
			int maxi1 = -1;
			int maxi2 = -1;
			for(Iterator<Integer> it = indices.iterator(); it.hasNext();){
				int i1 = it.next();
				for(Iterator<Integer> it2 = indices.iterator(); it2.hasNext();){
					int i2 = it2.next();
					if(i1 != i2){
						double dq = getDQ(ematrix, avector, i1, i2);

						if(dq >= maxdq){
							maxi1 = i1;
							maxi2 = i2;
							maxdq = dq;
						}
					}
				}
			}

			mergeCommunities(ematrix, avector, maxi1, maxi2);
			indices.remove(Math.max(maxi1, maxi2));
			q += maxdq;
			try {
				dendrogram.merge(maxi1, maxi2, q);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return dendrogram;
	}
	
	private double getDQ(double[][] ematrix, double[] avector, int c1, int c2){
		return 2*(ematrix[c1][c2] - avector[c1]*avector[c2]);
	}
	
	private double getQ(double[][] ematrix, double[] avector){
		double rv = 0;
		for(int i = 0; i < ematrix.length; i++){
			rv += (ematrix[i][i] - (avector[i]*avector[i]));
		}
		return rv;
	}
	
	private double[] getAVector(double[][] ematrix){
		double[] rv = new double[ematrix.length];
		for(int i = 0; i < ematrix.length; i++){
			for(int j = 0; j < ematrix.length; j++){
				rv[i] += ematrix[i][j];
			}
		}
		return rv;
	}
	
	private double[][] getEMatrix(Edge[] edges, int[] nodeindex, int numnodes, int totalpet){
		double[][] rv = new double[numnodes][numnodes];
		for(int i = 0; i < edges.length; i++){
			Node[] nodes = edges[i].getNodes();
			int n1 = nodeindex[nodes[0].getIntegerId()];
			int n2 = nodeindex[nodes[1].getIntegerId()];
			int petcount = edges[i].getPETCount();
			rv[n1][n2] += petcount;
			rv[n2][n1] += petcount;
		}
		for(int i = 0; i < numnodes; i++){
			for(int j = 0; j < numnodes; j++){
				rv[i][j] = rv[i][j]/totalpet;
			}
		}
		return rv;
	}
	
	private int getTotalPETCount(Edge[] edges){
		int totalpet = 0;
		for(int i = 0 ; i < edges.length; i++){
			totalpet += edges[i].getPETCount(); 
		}
		return totalpet;
	}
	
	private void mergeCommunities(double[][] ematrix, double[] avector, int c1, int c2){
		for(int i = 0; i < ematrix.length; i++){
			ematrix[i][c1] += ematrix[i][c2];
			ematrix[c1][i] += ematrix[c2][i];
		}
		avector[c1] = 0;
		for(int i = 0; i < ematrix.length; i++){
			avector[c1] += ematrix[i][c1];
		}
	}
	
	//0 - nodeid->index
	private int[] getNodeIndex(int maxid, Node[] nodes){
		int[] rv = new int[maxid+1];
		for(int i = 0; i < nodes.length; i++){
			rv[nodes[i].getIntegerId()] = i;
		}
		return rv;
	}
	
	//"Deciphering Network Community Structure by Surprise" - Aldecoa & Marin (2011)
	/**
	 * @param F 
	 * @param csizes Array of the number of nodes in each community
	 * @param n The total number of edges before partitioning
	 * @param p The total number of intracomunity edges.
	 * @return The Surprise Score
	 */
	private double getSurpriseScore(int N, int[] csizes, int n, int p){
//		int F = (N*(N-1))/2;
//		int M = getMaxIntraCommunity(csizes);
//		int maxlinks = Math.min(M, n);

		double rv = 0;
//		for(int j = p; j <= maxlinks; j++){
//			rv += Math.exp((binomialCoefficientLog(M, j)+ binomialCoefficientLog((F-M), (n-j)))
//					- binomialCoefficientLog(F, n));
//		}
		
		return rv;
	}
	
//	private int getMaxIntraCommunity(int[] csizes){
//		int rv = 0;
//		for(int i = 0; i < csizes.length; i++){
//			rv = Math.max(rv, (csizes[i]*(csizes[i]-1))/2);
//		}
//		return rv;
//	}
	
}
