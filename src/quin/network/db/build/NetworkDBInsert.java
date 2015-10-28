package quin.network.db.build;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import quin.network.Anchor;
import quin.network.ChIAPETNetwork;
import quin.network.ConnectedComponent;
import quin.network.Edge;
import quin.network.Interaction;
import quin.network.NetworkParameters;
import quin.network.Node;
import db.SQLConnectionFactory;

public class NetworkDBInsert {

	private final int BATCHSIZE = 1000;
	private final int BSIZEM1 = BATCHSIZE-1;
	
	//FID is the file id from the database to distinguish between different datasets
	public void insertData(long fid, ChIAPETNetwork network, String schema){		
		Connection conn = SQLConnectionFactory.getConnection();
	
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement("USE "+schema);
			ps.execute();
			
			createTables(conn, fid);
			
			insertNetwork(conn, network, fid);
			insertCCs(conn, network, fid);
			insertEdges(conn, network, fid);
			insertNodes(conn, network, fid);
			insertInteractions(conn, network, fid);
			
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createTables(Connection conn, long fid) throws SQLException{
		createNetworkTable(conn, fid);
		createCCTable(conn, fid);
		createNodeTable(conn, fid);
		createEdgeTable(conn, fid);
		createInteractionTable(conn, fid);
		createSIIndexListTable(conn, fid);
		createSIIndexTable(conn, fid);
	}
	
	private void createNetworkTable(Connection conn, long fid) throws SQLException{
		String sql = "CREATE TABLE Network_"+fid+" (cccount INT NOT NULL, nodecount INT NOT NULL, edgecount INT NOT NULL, avgpetnode DOUBLE NOT NULL, avgpetedge DOUBLE NOT NULL,  extend INT NOT NULL, selfligation INT NOT NULL, minpet INT NOT NULL, maxdist INT NOT NULL, minpetmax INT NOT NULL, minpetinterchrom INT NOT NULL, minsize INT NOT NULL, minsizepet INT NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createCCTable(Connection conn, long fid) throws SQLException{
		String sql = "CREATE TABLE ConnectedComponents_"+fid+" (id INT NOT NULL, nodecount INT NOT NULL, edgecount INT NOT NULL, PRIMARY KEY(id))";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createNodeTable(Connection conn, long fid) throws SQLException{
		String sql = "CREATE TABLE Nodes_"+fid+" (id INT NOT NULL, ccid INT NOT NULL, chr VARCHAR(50) NOT NULL, start INT NOT NULL, end INT NOT NULL, petcount INT NOT NULL, interactioncount INT NOT NULL, degree INT NOT NULL, closeness DOUBLE NOT NULL, harmonic DOUBLE NOT NULL, betweenness DOUBLE NOT NULL, PRIMARY KEY(id), INDEX(ccid) USING HASH, INDEX(chr, start, end) USING BTREE)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createEdgeTable(Connection conn, long fid) throws SQLException{
		String sql = "CREATE TABLE Edges_"+fid+" (id INT NOT NULL, ccid INT NOT NULL, n1 INT NOT NULL, n2 INT NOT NULL, petcount INT NOT NULL, interactioncount INT NOT NULL, PRIMARY KEY(id), INDEX(n1) USING HASH, INDEX(n2) USING HASH, INDEX(ccid) USING HASH)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createInteractionTable(Connection conn, long fid) throws SQLException{
		String sql = "CREATE TABLE Interactions_"+fid+" (id INT NOT NULL, iid INT NOT NULL, chr VARCHAR(50) NOT NULL, start INT NOT NULL, end INT NOT NULL, petcount INT NOT NULL, nodeid INT, edgeid INT, reason INT NOT NULL, dtonode INT NOT NULL, PRIMARY KEY(id), INDEX(chr, start, end) USING BTREE, INDEX(nodeid) USING HASH, INDEX(edgeid) USING HASH)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createSIIndexListTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE SIIndexList_"+fid+" (id INT NOT NULL AUTO_INCREMENT, dtype INT NOT NULL, did BIGINT NOT NULL, ext INT NOT NULL, genome INT NOT NULL, upstream INT NOT NULL, downstream INT NOT NULL, traitsrc INT NOT NULL, PRIMARY KEY(id))";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void createSIIndexTable(Connection conn, long fid) throws SQLException {
		String sql = "CREATE TABLE SIIndex_"+fid+" (iid INT NOT NULL, nid INT NOT NULL, term VARCHAR(255) NOT NULL, chr VARCHAR(50) NOT NULL, start INT NOT NULL, end INT NOT NULL) "
				+ "PARTITION BY LIST(iid) (PARTITION dummy VALUES IN (-1))";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close(); 
	}
	
	private void insertNetwork(Connection conn, ChIAPETNetwork network, long fid) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Network_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
		
		ps.setInt(1, network.getCCCount());
		ps.setInt(2, network.getNodeCount());
		ps.setInt(3, network.getEdgeCount());
		ps.setDouble(4, network.getAvgPETPerNode());
		ps.setDouble(5, network.getAvgPETPerEdge());
		
		NetworkParameters np = network.getParameters();
		
		ps.setInt(6, np.getExt());
		ps.setInt(7, np.getSLDist());
		ps.setInt(8, np.getMinPET());
		ps.setInt(9, np.getMaxDist());
		ps.setInt(10, np.getMinMaxPET());
		ps.setInt(11, np.getMinInterPET());
		ps.setInt(12, np.getMinSize());
		ps.setInt(13, np.getMinTwoPET());
		
		ps.execute();
		ps.close();
	}	
	
	private void insertCCs(Connection conn, ChIAPETNetwork network, long fid) throws SQLException{
		PreparedStatement ccps = conn.prepareStatement("INSERT INTO ConnectedComponents_"+fid+" VALUES(?,?,?)");

		ConnectedComponent[] ccs = network.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			int ccid = i+1;
			ConnectedComponent cc = ccs[i];
			
			ccps.setInt(1, ccid);		//CCID
			ccps.setInt(2, cc.getNodeCount());		//Node Count
			ccps.setInt(3, cc.getEdgeCount());	//Edge Count
			ccps.addBatch();			
			
			if((i % BATCHSIZE) == BSIZEM1){
				ccps.executeBatch();
			}
		}
		ccps.executeBatch();
		ccps.close();
	}	
	
	
	private void insertEdges(Connection conn, ChIAPETNetwork network, long fid) throws SQLException{
		PreparedStatement edgeps = conn.prepareStatement("INSERT INTO Edges_"+fid+" VALUES(?,?,?,?,?,?)");

		ConnectedComponent[] ccs = network.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			int ccid = i+1;
			Edge[] edges = ccs[i].getEdges();
			for(int j = 0; j < edges.length; j++){
				Edge e = edges[j];
				Node[] nodes = e.getNodes();
				Node n1 = nodes[0];
				Node n2 = nodes[1];
				
				edgeps.setInt(1, e.getId());		//Edge ID
				edgeps.setInt(2, ccid);				//CCID
				edgeps.setInt(3, n1.getId());	//N1 ID
				edgeps.setInt(4, n2.getId());		//N2 ID		
				edgeps.setInt(5, e.getPETCount());	//PET Count
				edgeps.setInt(6, e.getInteractionCount());	//PET Count

				edgeps.addBatch();
								
				if((j % BATCHSIZE) == BSIZEM1){
					edgeps.executeBatch();
				}
			}
			edgeps.executeBatch();
		}
		
		edgeps.close();
	}
	
	private void insertNodes(Connection conn, ChIAPETNetwork network, long fid) throws SQLException{
		PreparedStatement nodeps = conn.prepareStatement("INSERT INTO Nodes_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?,?)");

		ConnectedComponent[] ccs = network.getCCs();
		
		for(int i = 0; i < ccs.length; i++){
			int ccid = i+1;
			Node[] nodes = ccs[i].getNodes();
			for(int j = 0; j < nodes.length; j++){
				Node n = nodes[j];
				
				nodeps.setInt(1, n.getId());		//Node ID
				nodeps.setInt(2, ccid);				//CCID
				nodeps.setString(3, n.getChr());	//Chr
				nodeps.setInt(4, n.getStart());		//Start
				nodeps.setInt(5, n.getEnd());		//End
				nodeps.setInt(6, n.getPETCount()); //PET Count
				nodeps.setInt(7, n.getInteractionCount()); //Interaction Count
				
				//Following is NOT normalized, normalizing it afterwards
				nodeps.setInt(8, n.getDegree());	//Degree	
				nodeps.setDouble(9, n.getCloseness());	//Closeness
				nodeps.setDouble(10, n.getHarmonic());	//Harmonic
				nodeps.setDouble(11, n.getBetweenness());	//Betweenness
				nodeps.addBatch();
				
				
				if((j % BATCHSIZE) == BSIZEM1){
					nodeps.executeBatch();
				}
			}
			nodeps.executeBatch();
		}
		
		nodeps.close();
	}
	
	private void insertInteractions(Connection conn, ChIAPETNetwork network, long fid) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Interactions_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?)");
		

		Interaction[] interactions = network.getInteractions();
		for(int i = 0; i < interactions.length; i++){
			Interaction ci = interactions[i];
			Edge e = ci.getEdge();
			Anchor[] pes = ci.getAnchors();
			Anchor pe1 = pes[0];
			Anchor pe2 = pes[1];
			
			//PE1
			ps.setInt(1, pe1.getId());		//PET ID
			ps.setInt(2, ci.getId());		//Interaction ID
			ps.setString(3, pe1.getChr());		//Chr
			ps.setInt(4, pe1.getStart());		//Start
			ps.setInt(5, pe1.getEnd());		//End
			ps.setInt(6, ci.getPETCount());		//PET Count
			if(pe1.getNode() == null){
				ps.setNull(7, java.sql.Types.INTEGER);		//Node ID
			}
			else{
				ps.setInt(7, pe1.getNode().getId());		//Node ID
			}
			
			if(e == null){
				ps.setNull(8, java.sql.Types.INTEGER);
			}
			else{
				ps.setInt(8, e.getId()); 		//Edge ID
			}
			ps.setInt(9, ci.getReason());
			ps.setInt(10, pe1.getDistanceToNode());
			ps.addBatch();
			
			//PE2
			ps.setInt(1, pe2.getId());		//PET ID
			ps.setInt(2, ci.getId());		//Interaction ID
			ps.setString(3, pe2.getChr());		//Chr
			ps.setInt(4, pe2.getStart());		//Start
			ps.setInt(5, pe2.getEnd());		//End
			ps.setInt(6, ci.getPETCount());		//PET Count
			if(pe2.getNode() == null){
				ps.setNull(7, java.sql.Types.INTEGER);		//Node ID
			}
			else{
				ps.setInt(7, pe2.getNode().getId());		//Node ID
			}
			
			if(e == null){
				ps.setNull(8, java.sql.Types.INTEGER);
			}
			else{
				ps.setInt(8, e.getId()); 		//Edge ID
			}
			ps.setInt(9, ci.getReason());
			ps.setInt(10, pe2.getDistanceToNode());
			ps.addBatch();
			
			if((i % BATCHSIZE) == BSIZEM1){
				ps.executeBatch();
			}
		}
		ps.executeBatch();
		ps.close();
	}
	
	
}

