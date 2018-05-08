package quin.network;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import quin.filereader.BEDReader;
import quin.filereader.ChIAPETFileReader;
import quin.filereader.ChIAPETRead;
import quin.filereader.TextReader;

public class NetworkBuilder {

	private RawInteractions _interactions;

	public NetworkBuilder(String chiapet) throws Exception {
		_interactions = getInteractions(chiapet);
	}
	
	public NetworkBuilder(Connection conn, String dbtable, boolean built) throws SQLException{
		if(built){
			_interactions = getBuiltInteractions(conn, dbtable);
		}
		else{
			_interactions = getInteractions(conn, dbtable);
		}
	}
	
	public Interaction[] getInteractions(){
		return _interactions.interactions;
	}

	public ChIAPETNetwork buildNetwork(int ext, int sldist, int minpet, int maxdist, int minmaxpet, int mininterpet, int minsize, int mintwopet) throws Exception{
		NodeExtraction ne = new NodeExtraction();
		Node[] nodes = ne.getNodes(_interactions.pairedends, 2*ext, sldist);
		
		EdgeExtraction ee = new EdgeExtraction();
		ee.getEdges(_interactions.interactions, minpet, maxdist, minmaxpet, mininterpet);
		
		Util u = new Util();
		nodes = u.filterNodes(nodes);
		
		ConnectedComponentExtraction cce = new ConnectedComponentExtraction();
		ConnectedComponent[] ccs = cce.getCCs(nodes, _interactions.interactions.length, minsize, mintwopet);
		
		NetworkParameters params = new NetworkParameters(ext, sldist, minpet, maxdist, minmaxpet, mininterpet, minsize, mintwopet);
		
		return new ChIAPETNetwork(_interactions.pairedends, _interactions.interactions, getFinalNodes(ccs), getFinalEdges(ccs), ccs, params);
	}
	
	//Assumes locations are not overlapping
	public ChIAPETNetwork buildNetwork(Location[] locations, int minpet, int maxdist, int minmaxpet, int mininterpet, int minsize, int mintwopet, int ext, boolean merge) throws Exception{
		Util u = new Util();
		if(merge){
			locations = u.getNonOverlappingLocations(locations,ext);
			System.out.println("merging");
		}
		else{
			locations = u.getNonOverlappingLocations(locations,0);
		}
		NodeExtraction ne = new NodeExtraction();
		Node[] nodes = ne.getNodes(_interactions.pairedends, locations, ext);
		
		EdgeExtraction ee = new EdgeExtraction();
		ee.getEdges(_interactions.interactions, minpet, maxdist, minmaxpet, mininterpet);
		
		//nodes = u.filterNodes(nodes);
		int icount = _interactions.interactions.length;
		
		ConnectedComponentExtraction cce = new ConnectedComponentExtraction();
		ConnectedComponent[] ccs = cce.getCCs(nodes, icount, minsize, mintwopet);
		
		//CommunityStructure communities = new CommunityStructure();
		//ccs = communities.partitionNetwork(ccs, icount, minsize, mintwopet);
		
		NetworkParameters params = new NetworkParameters(ext, -1, minpet, maxdist, minmaxpet, mininterpet, minsize, mintwopet);
		
		return new ChIAPETNetwork(_interactions.pairedends, _interactions.interactions, getFinalNodes(ccs), getFinalEdges(ccs), ccs, params);
	}
	
	
	private Edge[] getFinalEdges(ConnectedComponent[] ccs){
		LinkedList<Edge> l = new LinkedList<Edge>();
		for(int i = 0; i < ccs.length; i++){
			Edge[] ccedges = ccs[i].getEdges();
			for(int j = 0; j < ccedges.length; j++){
				l.add(ccedges[j]);
			}
		}
		return l.toArray(new Edge[0]);
	}
	
	private Node[] getFinalNodes(ConnectedComponent[] ccs){
		LinkedList<Node> l = new LinkedList<Node>();
		for(int i = 0; i < ccs.length; i++){
			Node[] ccnodes = ccs[i].getNodes();
			for(int j = 0; j < ccnodes.length; j++){
				l.add(ccnodes[j]);
			}
		}
		return l.toArray(new Node[0]);
	}
	
	

	private ChIAPETFileReader getReader(String file) throws IOException {
		if (file.endsWith(".bed")) {
			return new BEDReader(file, 0);
		} else if (file.endsWith(".txt")) {
			return new TextReader(file, 0);
		} else {
			return null;
		}
	}

	// O(n) in the number of reads
	private RawInteractions getInteractions(String file) throws Exception {
		ChIAPETFileReader reader = getReader(file);

		LinkedList<Anchor> pairedends = new LinkedList<Anchor>();
		LinkedList<Interaction> interactions = new LinkedList<Interaction>();

		int pcount = 0;
		int icount = 0;
		while (reader.ready()) {
			ChIAPETRead read = reader.readLine();

			Anchor p1 = new Anchor(pcount++, read.getLChr(),
					read.getLStart(), read.getLEnd());
			pairedends.add(p1);

			Anchor p2 = new Anchor(pcount++, read.getRChr(),
					read.getRStart(), read.getREnd());
			pairedends.add(p2);

			Interaction i = new Interaction(icount++, p1, p2, read.getCount());
			interactions.add(i);
		}

		RawInteractions rv = new RawInteractions();
		rv.pairedends = pairedends.toArray(new Anchor[0]);
		rv.interactions = interactions.toArray(new Interaction[0]);

		return rv;
	}
	
	private RawInteractions getInteractions(Connection conn, String dbtable) throws SQLException{
		LinkedList<Anchor> pairedends = new LinkedList<Anchor>();
		LinkedList<Interaction> interactions = new LinkedList<Interaction>();

		int pcount = 0;
		int icount = 0;
		
		String sql = "SELECT chr1, start1, end1, chr2, start2, end2, petcount FROM "+dbtable;
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			ChIAPETRead read = getRead(rs);

			Anchor p1 = new Anchor(pcount++, read.getLChr(),
					read.getLStart(), read.getLEnd());
			pairedends.add(p1);

			Anchor p2 = new Anchor(pcount++, read.getRChr(),
					read.getRStart(), read.getREnd());
			pairedends.add(p2);

			Interaction i = new Interaction(icount++, p1, p2, read.getCount());
			interactions.add(i);
		}

		RawInteractions rv = new RawInteractions();
		rv.pairedends = pairedends.toArray(new Anchor[0]);
		rv.interactions = interactions.toArray(new Interaction[0]);

		rs.close();
		ps.close();
		
		return rv;
	}
	
	private RawInteractions getBuiltInteractions(Connection conn, String dbtable) throws SQLException{
		LinkedList<Anchor> pairedends = new LinkedList<Anchor>();
		LinkedList<Interaction> interactions = new LinkedList<Interaction>();

		String sql = "SELECT id, chr start, end, petcount, iid FROM "+dbtable+" ORDER BY iid";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			int id1 = rs.getInt(1);
			String chr1 = rs.getString(2);
			int start1 = rs.getInt(3);
			int end1 = rs.getInt(4);
			int petcount = rs.getInt(5);
			int iid = rs.getInt(6);
			rs.next();
			int id2 = rs.getInt(1);
			String chr2 = rs.getString(2);
			int start2 = rs.getInt(3);
			int end2 = rs.getInt(4);
			
			Anchor p1 = new Anchor(id1, chr1,
					start1, end1);
			pairedends.add(p1);

			Anchor p2 = new Anchor(id2, chr2,
					start2, end2);
			pairedends.add(p2);

			Interaction i = new Interaction(iid, p1, p2, petcount);
			interactions.add(i);
		}

		RawInteractions rv = new RawInteractions();
		rv.pairedends = pairedends.toArray(new Anchor[0]);
		rv.interactions = interactions.toArray(new Interaction[0]);

		rs.close();
		ps.close();
		
		return rv;
	}
	
	private ChIAPETRead getRead(ResultSet rs) throws SQLException{
		String chr1 = rs.getString(1);
		int start1 = rs.getInt(2);
		int end1 = rs.getInt(3);
		String chr2 = rs.getString(4);
		int start2 = rs.getInt(5);
		int end2 = rs.getInt(6);
		int petcount = rs.getInt(7);
		return new ChIAPETRead(chr1, start1, end1, chr2, start2, end2, petcount);
	}
	

	private class RawInteractions {
		public Anchor[] pairedends;
		public Interaction[] interactions;
	}

}
