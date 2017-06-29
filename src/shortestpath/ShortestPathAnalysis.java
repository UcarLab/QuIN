package shortestpath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import quin.export.Util;
import quin.network.Anchor;
import quin.network.ConnectedComponent;
import quin.network.ConnectedComponentExtraction;
import quin.network.Edge;
import quin.network.Interaction;
import quin.network.Node;
import nearestgene.NearestTSSUtil;
import nearestgene.TSSGene;

public class ShortestPathAnalysis {
	
	private TreeMap<Integer, Node> _nodes;
	
	public ShortestPathAnalysis(Connection conn, long fid){
		_nodes = setNetwork(conn, fid);
	}
	
	public double getAVGPET(int nid){
		Node n = _nodes.get(nid);
		Edge[] edges = n.getEdges();
		int petsum = 0;
		for(int i = 0; i < edges.length; i++){
			petsum += edges[i].getPETCount();
		}
		return (double)petsum/edges.length;
	}
	
	//log2 Target Node Size Over Average Surrounding Node Size
	public double getRelativeNodeSize(int nid){
		Node n = _nodes.get(nid);
		Edge[] edges = n.getEdges();
		int runningsum = 0;
		for(int i = 0; i < edges.length; i++){
			Node an = edges[i].getAdjacentNode(n);
			runningsum += an.getEnd()- an.getStart();
		}
		
		return Math.log((n.getEnd()-n.getStart())/((double)runningsum/edges.length))/Math.log(2);
	}
	
	public Integer[] getConnectedNodes(int nid){
		Node n = _nodes.get(nid);
		Edge[] edges = n.getEdges();
		Set<Integer> rv = new TreeSet<Integer>();
		for(int i = 0; i < edges.length; i++){
			Node an = edges[i].getAdjacentNode(n);
			rv.add(an.getIntegerId());
		}
		
		return rv.toArray(new Integer[0]);
	}
	
	public ComponentData getComponents(){
		ConnectedComponentExtraction cc = new ConnectedComponentExtraction();
		Node[] nodes = _nodes.values().toArray(new Node[0]);
		int maxedgeid = 0;
		for(int i = 0; i < nodes.length; i++){
			 Edge[] edges = nodes[i].getEdges();
			 for(int j = 0; j < edges.length; j++){
				 maxedgeid = Math.max(maxedgeid, edges[j].getId());
			 }
		}
		TreeMap<Integer, Node[]> components = new TreeMap<Integer,Node[]>();
		TreeMap<Integer, Integer> nodeindex = new TreeMap<Integer,Integer>();
		ConnectedComponent[] ccs = cc.getCCs(nodes, maxedgeid+1, 0, 0);
		
		for(int i = 0; i < ccs.length; i++){
			Node[] cnodes = ccs[i].getNodes();
			for(int j = 0; j < cnodes.length; j++){
				nodeindex.put(cnodes[j].getIntegerId(), i);
			}
			components.put(i, cnodes);
		}
		
		ComponentData rv = new ComponentData();
		rv.components = components;
		rv.nodetoccid = nodeindex;
		
		return rv;
	}
	
	public class ComponentData {
		public TreeMap<Integer, Node[]> components;
		public TreeMap<Integer, Integer> nodetoccid;
	}
	
	public TreeMap<Integer, Node> getNodes(){
		return _nodes;
	}
	
	public ShortestPath[] getShortestPaths(Connection conn, long fid, Integer[] indices, Integer[] tindex, int min, int max, boolean sp, boolean tp, String pgenome, int upstream, int downstream) throws SQLException{
		TreeMap<Integer, Node> nodes = getNodes();
		NARegion[][] nids = getNodeIds(conn, fid, indices, min, max, (sp ? pgenome : null), upstream, downstream);
		ARegion[][] ngm = getNodeTargetMapping(conn, fid, tindex, (tp ? pgenome : null), upstream, downstream);
		
		NodeToTarget[] data = doBFS(nodes, nids, ngm);

		NearestTSSUtil ntss = new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
		
		LinkedList<ShortestPath> shortestpaths = new LinkedList<ShortestPath>();
		
		for(int i = 0; i < data.length; i++){
			NARegion ni = data[i].getNodeInfo();
			ARegion gi = data[i].getGeneInfo();
			String path = data[i].getPath();
			int hc = data[i].getHopCount();
			double avgpet = data[i]._avgpet;
			int minpet = data[i]._minpet;
			int maxpet = data[i]._maxpet;
			double avginter = data[i]._avginter;
			int mininter = data[i]._mininter;
			int maxinter = data[i]._maxinter;
			int nc = ni.getNodeCount();
			int ec = ni.getEdgeCount();
			int ccid = ni.getCCId();
			
			String dataset = ni.getDataset();
			String term = ni.getTerm();
			String nchr = ni.getChr();
			int ns = ni.getStart();
			int ne = ni.getEnd();
			
			String tdataset = gi.getDataset();
			String targetterm = gi.getTerm();
			String targetchr = gi.getChr();
			int targetstart = gi.getStart();
			int targetend = gi.getEnd();

			int distance = -1;
			if(nchr.equals(targetchr)){
				if(ns <= targetend && targetstart <= ne){
					distance = 0;
				}
				else{
					distance = Math.max(targetstart-ne, ns-targetend);
				}
			}
			
			//Get term nearest TSS
			TSSGene[] termtssgenes = ntss.getNearestGene(nchr, ns, ne);
			String termtss = termtssgenes[0].getGene();
			for(int ii = 1; ii < termtssgenes.length; ii++){
				termtss += ","+termtssgenes[ii].getGene();
			}
			int termtssd = termtssgenes[0].getDistance();
			
			//Get target nearest TSS
			TSSGene[] targettssgenes = ntss.getNearestGene(targetchr, targetstart, targetend);
			String targettss = targettssgenes[0].getGene();
			for(int ii = 1; ii < targettssgenes.length; ii++){
				targettss += ","+targettssgenes[ii].getGene();
			}
			int targettssd = targettssgenes[0].getDistance();
			
			shortestpaths.add(new ShortestPath(dataset,term,nchr,ns,ne,termtss,termtssd,hc,distance, tdataset, targetterm,targetchr,targetstart,targetend,targettss,targettssd,avgpet,minpet,maxpet,avginter,mininter,maxinter,nc,ec,path, ccid));
		}
			
		
		return shortestpaths.toArray(new ShortestPath[0]);
	}
	
	private NodeToTarget[] doBFS(TreeMap<Integer,Node> nodes, NARegion[][] nids, ARegion[][] genes){
		LinkedList<NodeToTarget> rv = new LinkedList<NodeToTarget>();
		for(int i = 0; i < nids.length; i++){
			if(nids[i] != null){
				for(int j = 0; j < nids[i].length; j++){
					NodeToTarget[] results = doBFS(Integer.toString(i), nodes.get(nids[i][j].getNID()), nids[i][j], genes, nodes.lastKey());
					for(int k = 0; k < results.length; k++){
						rv.add(results[k]);
					}
				}
			}
		}
		return rv.toArray(new NodeToTarget[0]);
	}
	
	private NodeToTarget[] doBFS(String index, Node n, NARegion r, ARegion[][] gm, int maxid){
		boolean[] visited = new boolean[maxid+1];

		LinkedList<Node> todo = new LinkedList<Node>();
		LinkedList<Integer> hopcount = new LinkedList<Integer>();
		LinkedList<String> path = new LinkedList<String>();
		LinkedList<Integer> petmin = new LinkedList<Integer>();
		LinkedList<Integer> petmax = new LinkedList<Integer>();
		LinkedList<Integer> petsum = new LinkedList<Integer>();
		LinkedList<Integer> intermin = new LinkedList<Integer>();
		LinkedList<Integer> intermax = new LinkedList<Integer>();
		LinkedList<Integer> intersum = new LinkedList<Integer>();
		
		todo.add(n);
		hopcount.add(0);
		path.add(""+n.getIntegerId());
		int npetcount = 0;
		petmin.add(npetcount);
		petmax.add(npetcount);
		petsum.add(npetcount);
		
		int nintercount = 0;
		intermin.add(nintercount);
		intermax.add(nintercount);
		intersum.add(nintercount);

		visited[n.getIntegerId()] = true;
		
		TreeMap<Integer, LinkedList<double[]>> mhmap = new TreeMap<Integer, LinkedList<double[]>>();
		TreeMap<Integer, LinkedList<String>> mhpath = new TreeMap<Integer, LinkedList<String>>();

		while(!todo.isEmpty()){
			Node cn = todo.removeFirst();
			int chc = hopcount.removeFirst();
			String cp = path.removeFirst();
			int cpetsum = petsum.removeFirst();
			int cpetmin = petmin.removeFirst();
			int cpetmax = petmax.removeFirst();
			int cintersum = intersum.removeFirst();
			int cintermin = intermin.removeFirst();
			int cintermax = intermax.removeFirst();
			int id = cn.getIntegerId();
			
			ARegion[] g = null;
			if(id < gm.length){
				g = gm[id];
			}
			

			
			if(g != null){
				if(!mhmap.containsKey(id)){
					mhmap.put(id, new LinkedList<double[]>());
					mhpath.put(id, new LinkedList<String>());
				}
				
				LinkedList<double[]> vlist = mhmap.get(id);
				double[] oldvals = vlist.isEmpty() ? null : vlist.getLast();
				int lasthc;
				if(oldvals == null){
					lasthc = Integer.MAX_VALUE;
				}
				else{
					lasthc = (int) oldvals[0];
				}
				
				int div = Math.max(chc, 1);
				double[] vals = new double[] { chc, (double)cpetsum/div, cpetmin, cpetmax, (double)cintersum/div, cintermin, cintermax};
				
				if(chc < lasthc){
					mhmap.put(id, new LinkedList<double[]>());
					mhpath.put(id, new LinkedList<String>());
				}
				
				if(chc <= lasthc){
					mhmap.get(id).add(vals);
					mhpath.get(id).add(cp);
				}
			}
			
			Edge[] edges = cn.getEdges();
			for(int i = 0; i < edges.length; i++){
				Node adjn = edges[i].getAdjacentNode(cn);
				int adjid = adjn.getIntegerId();
				int adjpetcount = edges[i].getPETCount();
				int adjintercount = edges[i].getInteractionCount();

				if(!visited[adjid]){
					todo.add(adjn);
					hopcount.add(chc+1);
					path.add(cp+"|"+adjn.getIntegerId());
					petsum.add(cpetsum+adjpetcount);
					if(chc > 0){
						petmin.add(Math.min(cpetmin, adjpetcount));
						petmax.add(Math.max(cpetmax, adjpetcount));
						intermin.add(Math.min(cintermin, adjintercount));
						intermax.add(Math.max(cintermax, adjintercount));
					}
					else{
						petmin.add(adjpetcount);
						petmax.add(adjpetcount);
						intermin.add(adjintercount);
						intermax.add(adjintercount);
					}
					
					intersum.add(cintersum+adjintercount);
					visited[adjid] = true;
				}
			}
		}
		
		LinkedList<NodeToTarget> rv = new LinkedList<NodeToTarget>();
		while(!mhmap.isEmpty()){
			Entry<Integer, LinkedList<double[]>> e = mhmap.pollFirstEntry();
			LinkedList<double[]> vallists = e.getValue();
			LinkedList<String> paths = mhpath.get(e.getKey());
			ARegion[] gl = gm[e.getKey()];
			
			Iterator<double[]> valit = vallists.iterator();
			Iterator<String> pathit = paths.iterator();
			
			while(valit.hasNext()){
				double[] vals = valit.next();
				int hc = (int) vals[0];
				double avgp = vals[1];
				int minp = (int) vals[2];
				int maxp = (int) vals[3];
				double avgi = vals[4];
				int mini = (int) vals[5];
				int maxi = (int) vals[6];
				String fpath = pathit.next();
				for(int i = 0; i < gl.length; i++){
					rv.add(new NodeToTarget(r, gl[i], hc, avgp, minp, maxp, avgi, mini, maxi, fpath));
				}
			}

		}
		return rv.toArray(new NodeToTarget[0]);
	}
	
	private TreeMap<Integer, Node> setNetwork(Connection conn, long fid){
		TreeMap<Integer, Node> nodes = null;
		try {
			nodes = getNodes(conn, fid);
			setEdges(conn, fid, nodes);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return nodes;
	}
	
	private void setEdges(Connection conn, long fid, TreeMap<Integer, Node> nodes) throws SQLException{
		String sql = "SELECT id, n1, n2, petcount, interactioncount FROM chiapet.Edges_"+fid;
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			int id = rs.getInt(1);
			Node n1 = nodes.get(rs.getInt(2));
			Node n2 = nodes.get(rs.getInt(3));
			int petcount = rs.getInt(4);
			int interactioncount = rs.getInt(5);
			
			Interaction[] interactions = new Interaction[interactioncount];
			Anchor a = new Anchor(0, "", 0, 0);
			interactions[0] = new Interaction(0, a, a, petcount-interactions.length+1);
			for(int i = 1; i < interactions.length; i++){
				interactions[i] = new Interaction(0, a, a, 1);
			}
			
			Edge e = new Edge(id, n1, n2, interactions);
			n1.addEdge(e);
			n2.addEdge(e);
		}
		
	}
	
	private TreeMap<Integer, Node> getNodes(Connection conn, long fid) throws SQLException{
		String sql = "SELECT id, chr, start, end FROM chiapet.Nodes_"+fid;
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		TreeMap<Integer, Node> rv = new TreeMap<Integer, Node>();
		while(rs.next()){
			int id = rs.getInt(1);
			String chr = rs.getString(2);
			int start  = rs.getInt(3);
			int end = rs.getInt(4);
			
			
			Node n = new Node(id, chr, start, end, new Anchor[0]);
			rv.put(id,  n);
		}
		
		rs.close();
		ps.close();
		return rv;
	}
	
	private NARegion[][] getNodeIds(Connection conn, long fid, Integer[] indices, int min, int max, String genome, int upstream, int downstream) throws SQLException{
		String schema = "chiapet";
		String indextable = schema+".SIIndex_"+fid;
		String nodetable = schema+".Nodes_"+fid;
		String cctable = schema+".ConnectedComponents_"+fid;

		int s = indices.length;
		if(genome != null){
			s++;
		}
		NARegion[][] rv = new NARegion[s+1][];
		String sql = "SELECT i.nid, i.term, i.chr, i.start, i.end, c.nodecount, c.edgecount, c.id FROM "+indextable+" AS i, "+nodetable+" AS n, "+cctable+" AS c WHERE i.iid = ? AND n.id=i.nid AND n.ccid=c.id AND c.nodecount <= ? AND c.nodecount >= ?";

		PreparedStatement ps = conn.prepareStatement(sql);
		Util u = new Util();
		for(int i = 0; i < indices.length; i++){
			String dataset = u.getDataset(conn, fid, indices[i]);
			ps.setInt(1, indices[i]);
			ps.setInt(2, max);
			ps.setInt(3, min);
			ResultSet rs = ps.executeQuery();
			LinkedList<NARegion> l = new LinkedList<NARegion>();
			while(rs.next()){
				l.add(new NARegion(dataset, rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8)));
			}
			rv[i] = l.toArray(new NARegion[0]);
			rs.close();
		}
		ps.close();
		
		if(genome != null){
			LinkedList<NARegion> l = new LinkedList<NARegion>();
			NARegion[][] pr = getNodeGeneMapping(conn, fid, genome, upstream, downstream);
			for(int i = 0; i < pr.length; i++){
				if(pr[i] != null){
					for(int j = 0; j < pr[i].length; j++){
						l.add(pr[i][j]);
					}
				}
			}
			rv[indices.length] = l.toArray(new NARegion[0]);
		}
		
		return rv;
	}

	private NARegion[][] getNodeGeneMapping(Connection conn, long fid, String genome, int upstream, int downstream){
		try {
			return getNodeGeneMapping(getChrStartSortedNodeList(conn, fid), getChrPromoterStartSortedGeneList(conn, genome, upstream, downstream));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ARegion[][] getNodeTargetMapping(Connection conn, long fid, Integer[] index, String genome, int upstream, int downstream) throws SQLException{
		String schema = "chiapet";
		String indextable = schema+".SIIndex_"+fid;
		String in = "";
		TreeMap<Integer, String> datasetmap = new TreeMap<Integer, String>();
		Util u = new Util();
		for(int i = 0; i < index.length; i++){
			in += ",?";
			datasetmap.put(index[i], u.getDataset(conn, fid, index[i]));
		}
		String sql = "SELECT nid, term, chr, start, end, iid FROM "+indextable+" WHERE iid IN(-1"+in+")";
		PreparedStatement ps = conn.prepareStatement(sql);
		for(int i = 0; i < index.length; i++){
			ps.setInt((i+1), index[i]);
		}
		ResultSet rs = ps.executeQuery();
		TreeMap<Integer, LinkedList<ARegion>> map = new TreeMap<Integer, LinkedList<ARegion>>();
		while(rs.next()){
			int nid = rs.getInt(1);
			String term = rs.getString(2);
			String chr = rs.getString(3);
			int start = rs.getInt(4);
			int end = rs.getInt(5);
			int diid = rs.getInt(6);
			
			if(!map.containsKey(nid)){
				map.put(nid, new LinkedList<ARegion>());
			}
			
			map.get(nid).add(new ARegion(datasetmap.get(diid), term, chr, start, end));
		}
		rs.close();
		ps.close();
		
		if(genome != null){
			ARegion[][] pr = getNodeGeneMapping(conn, fid, genome, upstream, downstream);
			
			for(int i = 0; i < pr.length; i++){
				if(pr[i] != null){
					for(int j = 0; j < pr[i].length; j++){
						if(!map.containsKey(i)){
							map.put(i, new LinkedList<ARegion>());
						}
						map.get(i).add(pr[i][j]);
					}
				}
			}
		}
		
		int l = 0;
		if(map.size() > 0){
			l = map.lastKey()+1;
		}
		ARegion[][] rv = new ARegion[l][];
		while(!map.isEmpty()){
			Entry<Integer, LinkedList<ARegion>> e = map.pollFirstEntry();
			rv[e.getKey()] = e.getValue().toArray(new ARegion[0]);
		}
		return rv;
	}
	
	private NARegion[][] getNodeGeneMapping(TreeMap<String, LinkedList<CCInfoNode>> nodes, TreeMap<String, LinkedList<ARegion>> genes){
		TreeMap<Integer, LinkedList<NARegion>> map = new TreeMap<Integer, LinkedList<NARegion>>();
		while(!nodes.isEmpty()){
			Entry<String, LinkedList<CCInfoNode>> ne = nodes.pollFirstEntry();
			CCInfoNode[] nl = ne.getValue().toArray(new CCInfoNode[0]);
			LinkedList<ARegion> l = genes.get(ne.getKey());
			
			if(l == null){
				l = new LinkedList<ARegion>();
			}
			ARegion[] gl = l.toArray(new ARegion[0]);
			
			for(int i = 0; i < nl.length; i++){
				CCInfoNode cn = nl[i];
				for(int j = 0; j < gl.length; j++){
					ARegion cg = gl[j];
					if(cg.getStart() <= cn.getEnd() && cn.getStart() <= cg.getEnd()){
						int cid = cn.getIntegerId();
						if(!map.containsKey(cid)){
							map.put(cid, new LinkedList<NARegion>());
						}
						map.get(cid).add(new NARegion(cg.getDataset(), cid, cg.getTerm(), cg.getChr(), cg.getStart(), cg.getEnd(), cn.getNodeCount(), cn.getEdgeCount(), cn.getCCId()));
					}
					else if(cg.getStart() > cn.getEnd()){
						break;
					}
				}
			}
			
		}
		
		
		NARegion[][] rv = new NARegion[map.lastKey()+1][];
		
		while(!map.isEmpty()){
			Entry<Integer, LinkedList<NARegion>> e = map.pollFirstEntry();
			rv[e.getKey()] = e.getValue().toArray(new NARegion[0]);
		}
		
		return rv;
	}
	
	private TreeMap<String, LinkedList<CCInfoNode>> getChrStartSortedNodeList(Connection conn, long fid) throws SQLException{
		String cctable = "chiapet.ConnectedComponents_"+fid;

		String sql = "SELECT n.id, n.chr, n.start, n.end, cc.nodecount, cc.edgecount, cc.id FROM chiapet.Nodes_"+fid+" AS n, "+cctable+" AS cc WHERE n.ccid = cc.id ORDER BY start ASC";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		TreeMap<String, LinkedList<CCInfoNode>> sortednodelist = new TreeMap<String, LinkedList<CCInfoNode>>();
		while(rs.next()){
			int id = rs.getInt(1);
			String chr = rs.getString(2);
			int start  = rs.getInt(3);
			int end = rs.getInt(4);
			int nc = rs.getInt(5);
			int ec = rs.getInt(6);
			int ccid = rs.getInt(7);
			
			if(!sortednodelist.containsKey(chr)){
				sortednodelist.put(chr, new LinkedList<CCInfoNode>());
			}
			
			CCInfoNode n = new CCInfoNode(id, chr, start, end, new Anchor[0], nc, ec);
			n.setCCId(ccid);
			sortednodelist.get(chr).add(n);
		}
		
		rs.close();
		ps.close();
		return sortednodelist;
	}
		
		
	private TreeMap<String, LinkedList<ARegion>> getChrPromoterStartSortedGeneList(Connection conn, String genome, int upstream, int downstream) throws SQLException{
		if(!genome.equals("hg19") && !genome.equals("hg38")){
			genome = "hg19";
		}
		String sql = "SELECT * FROM ((SELECT genename AS gene, chrom AS chr, txstart-"+upstream+" AS start, txstart+"+downstream+" AS end FROM ucsc."+genome+" WHERE strand='+') UNION"
				+ "(SELECT genename AS gene, chrom AS chr, txend-"+downstream+" AS start, txend+"+upstream+" AS end FROM ucsc."+genome+" WHERE strand='-')) AS p ORDER BY start ASC";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		TreeMap<String, LinkedList<ARegion>> sortedgenelist = new TreeMap<String, LinkedList<ARegion>>();
		while(rs.next()){
			String gene = rs.getString(1);
			String chr = rs.getString(2);
			int start  = rs.getInt(3);
			int end = rs.getInt(4);
			
			if(!sortedgenelist.containsKey(chr)){
				sortedgenelist.put(chr, new LinkedList<ARegion>());
			}
			
			ARegion g = new ARegion("Promoter "+upstream+"/"+downstream, gene, chr, start, end);
			sortedgenelist.get(chr).add(g);
		}
		
		rs.close();
		ps.close();
		return sortedgenelist;
	}
		
	
	private class ARegion {
		
		private String _dataset;
		private String _term;
		private String _chr;
		private int _start;
		private int _end;
		
		public ARegion(String dataset, String term, String chr, int start, int end){
			_dataset = dataset;
			_term = term;
			_chr = chr;
			_start = start;
			_end = end;
		}
		
		public String getDataset(){
			return _dataset;
		}
		
		public String getTerm(){
			return _term;
		}
		
		public String getChr(){
			return _chr;
		}
		
		public int getStart(){
			return _start;
		}
		
		public int getEnd(){
			return _end;
		}
		
	}
	
	private class NARegion extends ARegion {
		private int _nid;
		private int _numnodes;
		private int _numedges;
		private int _ccid;
		public NARegion(String dataset, int nid, String term, String chr, int start, int end, int numnodes, int numedges, int ccid) {
			super(dataset, term, chr, start, end);
			_nid = nid;
			_numnodes = numnodes;
			_numedges = numedges;
			_ccid = ccid;
		}

		public int getNID(){
			return _nid;
		}
		
		public int getNodeCount(){
			return _numnodes;
		}
		
		public int getEdgeCount(){
			return _numedges;
		}
		
		public int getCCId(){
			return _ccid;
		}
	}
	
	private class NodeToTarget {
		private NARegion _node;
		private ARegion _gene;
		private int _hopcount;
		private int _minpet, _maxpet;
		private int _mininter, _maxinter;
		private double _avgpet, _avginter;
		private String _path;
		
		public NodeToTarget(NARegion node, ARegion gene, int hopcount, double avgpet, int minpet, int maxpet, double avginter, int mininter, int maxinter, String path){
			_node = node;
			_gene = gene;
			_hopcount = hopcount;
			_avgpet = avgpet;
			_minpet = minpet;
			_maxpet = maxpet;
			_avginter = avginter;
			_mininter = mininter;
			_maxinter = maxinter;
			_path = path;
		}
		
		public NARegion getNodeInfo(){
			return _node;
		}
		
		public ARegion getGeneInfo(){
			return _gene;
		}
		
		public int getHopCount(){
			return _hopcount;
		}
		
		public String getPath(){
			return _path;
		}
	}
	
	private class CCInfoNode extends Node {
		private int _numnodes;
		private int _numedges;
		
		public CCInfoNode(int id, String chr, int start, int end, Anchor[] pes, int numnodes, int numedges) {
			super(id, chr, start, end, pes);
			_numnodes = numnodes;
			_numedges = numedges;
		}
		
		public int getNodeCount(){
			return _numnodes;
		}
		
		public int getEdgeCount(){
			return _numedges;
		}
		
	}
	
	
	
	private Integer[] getRandomIds(TreeMap<Integer, Node> nodeids, int num){
		
		Integer[] anid = nodeids.keySet().toArray(new Integer[0]);
		
		List<Boolean> shufflelist = new LinkedList<Boolean>();
		int index = 0;
		for(int i = 0; i < num; i++){
			shufflelist.add(true);
			index++;
		}
		for(int i = index; i < nodeids.size(); i++){
			shufflelist.add(false);
		}
		
		Collections.shuffle(shufflelist);
		
		LinkedList<Integer> rv = new LinkedList<Integer>();
		int i2 = 0;
		for(Iterator<Boolean> it = shufflelist.iterator(); it.hasNext();){
			if(it.next()){
				rv.add(anid[i2]);
			}
			i2++;
		}
		
		return rv.toArray(new Integer[0]);
	}
	
	
	public ShortestPath[] getShortestPathsRandom(Connection conn, long fid, Integer[] indices, Integer[] tindex, int min, int max, boolean sp, boolean tp, String pgenome, int upstream, int downstream) throws SQLException{
		TreeMap<Integer, Node> nodes = getNodes();
		
		
		Integer[] rnids = getRandomIds(nodes, 35);
		
		NARegion[][] nids = new NARegion[1][35]; 
		for(int i = 0; i < rnids.length; i++){
			nids[0][i] = new NARegion(null, rnids[i], "", "", 0, 0, 0, 0, 0);
		}
		
		ARegion[][] ngm = getNodeTargetMapping(conn, fid, tindex, (tp ? pgenome : null), upstream, downstream);
		
		NodeToTarget[] data = doBFS(nodes, nids, ngm);

		NearestTSSUtil ntss = new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
		
		LinkedList<ShortestPath> shortestpaths = new LinkedList<ShortestPath>();
		
		for(int i = 0; i < data.length; i++){
			NARegion ni = data[i].getNodeInfo();
			ARegion gi = data[i].getGeneInfo();
			String path = data[i].getPath();
			int hc = data[i].getHopCount();
			double avgpet = data[i]._avgpet;
			int minpet = data[i]._minpet;
			int maxpet = data[i]._maxpet;
			double avginter = data[i]._avginter;
			int mininter = data[i]._mininter;
			int maxinter = data[i]._maxinter;
			int nc = ni.getNodeCount();
			int ec = ni.getEdgeCount();
			int ccid = ni.getCCId();
			
			String dataset = ni.getDataset();
			String term = ni.getTerm();
			String nchr = ni.getChr();
			int ns = ni.getStart();
			int ne = ni.getEnd();
			
			String tdataset = gi.getDataset();
			String targetterm = gi.getTerm();
			String targetchr = gi.getChr();
			int targetstart = gi.getStart();
			int targetend = gi.getEnd();

			int distance = -1;
			if(nchr.equals(targetchr)){
				if(ns <= targetend && targetstart <= ne){
					distance = 0;
				}
				else{
					distance = Math.max(targetstart-ne, ns-targetend);
				}
			}
			
			//Get term nearest TSS
			TSSGene[] termtssgenes = ntss.getNearestGene(nchr, ns, ne);
			String termtss = termtssgenes[0].getGene();
			for(int ii = 1; ii < termtssgenes.length; ii++){
				termtss += ","+termtssgenes[ii].getGene();
			}
			int termtssd = termtssgenes[0].getDistance();
			
			//Get target nearest TSS
			TSSGene[] targettssgenes = ntss.getNearestGene(targetchr, targetstart, targetend);
			String targettss = targettssgenes[0].getGene();
			for(int ii = 1; ii < targettssgenes.length; ii++){
				targettss += ","+targettssgenes[ii].getGene();
			}
			int targettssd = targettssgenes[0].getDistance();
			
			shortestpaths.add(new ShortestPath(dataset,term,nchr,ns,ne,termtss,termtssd,hc,distance, tdataset, targetterm,targetchr,targetstart,targetend,targettss,targettssd,avgpet,minpet,maxpet,avginter,mininter,maxinter,nc,ec,path, ccid));
		}
			
		
		return shortestpaths.toArray(new ShortestPath[0]);
	}
}
