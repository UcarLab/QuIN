package quin.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import quin.network.db.build.NetworkDBInsert;

public class ImportNetwork {

	private final int BATCHSIZE = 1000;
	private final int BSIZEM1 = BATCHSIZE-1;
	
	private ExportNetworkInfo _networkinfo;
	private LinkedList<ExportNode> _nodes;
	private LinkedList<ExportEdge> _edges;
	private ExportComponent[] _components;
	private ExportInteraction[] _interactions;
	private BufferedReader _br;
	private String[] _currentline;
	private int _index;
	
	public void fromGML(Connection conn, long fid, InputStream gmlfile) throws IOException, SQLException{
		
		_networkinfo = null;
		_nodes = new LinkedList<ExportNode>();
		_edges = new LinkedList<ExportEdge>();
		_components = null;
		_interactions = null;
		
		_br = new BufferedReader(new InputStreamReader(gmlfile));
		_currentline = _br.readLine().split("\\s+");
		_currentline = splitBrackets(_currentline);
		_currentline = mergeQuotes(_currentline);
		_index = 0;
		
		readGraph();
		_br.close();
		
		createTables(conn, fid);
		importInteractions(conn, fid, _interactions);
		importNodes(conn, fid, _nodes.toArray(new ExportNode[0]));
		importEdges(conn, fid, _edges.toArray(new ExportEdge[0]));
		importComponents(conn, fid, _components);
		importNetworkInfo(conn, fid, _networkinfo);
	}
	
	private void readGraph(){
		int cccount = -1;
		int nodecount = -1;
		int edgecount = -1;
		double avgn = -1;
		double avge = -1;
		int extend = -1;
		int sl = -1;
		int minpet = -1;
		int maxdist = -1;
		int minpetmax = -1;
		int minpetinterchrom = -1;
		int minsize = -1;
		int minsizepet = -1;
		
		try {
			String nexttoken = getNextToken();
			while(nexttoken != null && !nexttoken.equals("]")){
				String valtoken = getNextToken();
				if(valtoken == null || valtoken.equals("]")){
					System.out.println("Error reading network.");
					return;
				}
				if(nexttoken.equalsIgnoreCase("cccount")){
					cccount = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("nodecount")){
					nodecount = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("edgecount")){
					edgecount = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("avgcountnode")){
					avgn = Double.parseDouble(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("avgcountedge")){
					avge = Double.parseDouble(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("extend")){
					extend = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("selfligation")){
					sl = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("minpet")){
					minpet = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("maxdist")){
					maxdist = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("minpetmax")){
					minpetmax = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("minpetinterchrom")){
					minpetinterchrom = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("minsize")){
					minsize = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("minsizepet")){
					minsizepet = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("annotations")){
					skipAnnotation();
				}
				else if(nexttoken.equalsIgnoreCase("node")){
					_nodes.add(readNode());
				}
				else if(nexttoken.equalsIgnoreCase("edge")){
					_edges.add(readEdge());
				}
				else if(nexttoken.equalsIgnoreCase("interactions")){
					_interactions = readInteractions();
				}
				else if(nexttoken.equalsIgnoreCase("components")){
					_components = readComponents();
				}

				nexttoken = getNextToken();
			}
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println("Error reading network.");
		}
		
		_networkinfo = new ExportNetworkInfo(cccount, nodecount, edgecount, avgn, avge, extend, sl, minpet, maxdist, minpetmax, minpetinterchrom, minsize, minsizepet);
		
	}
	
	private ExportInteraction[] readInteractions() throws IOException{
		LinkedList<ExportInteraction> rv = new LinkedList<ExportInteraction>();
		String nexttoken = getNextToken();
		while(nexttoken != null && !nexttoken.equals("]")){
			if(nexttoken.equalsIgnoreCase("interaction_anchor")){
				String next = getNextToken();
				if(next.equals("[")){
					ExportInteraction interaction = readInteraction();
					if(interaction != null){
						rv.add(interaction);
					}
				}
			}
			nexttoken = getNextToken();
		}
		return rv.toArray(new ExportInteraction[0]);
	}
	
	private ExportComponent[] readComponents() throws IOException{
		LinkedList<ExportComponent> rv = new LinkedList<ExportComponent>();
		String nexttoken = getNextToken();
		while(nexttoken != null && !nexttoken.equals("]")){
			if(nexttoken.equalsIgnoreCase("component")){
				String next = getNextToken();
				if(next.equals("[")){
					ExportComponent cc = readComponent();
					if(cc != null){
						rv.add(cc);
					}
				}
			}
			nexttoken = getNextToken();
		}
		return rv.toArray(new ExportComponent[0]);
	}
	
	private void skipAnnotation() throws IOException{
		while(!getNextToken().equals("]"));
	}
	
	private ExportNode readNode() {
		int id = -1;
		int ccid = -1;
		String chr = "";
		int start = -1;
		int end = -1;
		int count = -1;
		int icount = -1;
		int degree = -1;
		double closeness = -1;
		double harmonic = -1;
		double betweenness = -1;
		
		
		try {
			String nexttoken = getNextToken();
			while(nexttoken != null && !nexttoken.equals("]")){
				String valtoken = getNextToken();
				if(valtoken == null || valtoken.equals("]")){
					System.out.println("Error reading node.");
					return null;
				}
				if(nexttoken.equalsIgnoreCase("id")){
					id = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("ccid")){
					ccid = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("chr")){
					chr = valtoken.replace("\"", "");
				}
				else if(nexttoken.equalsIgnoreCase("start")){
					start = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("end")){
					end = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("count")){
					count = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("interactioncount")){
					icount = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("degree")){
					degree = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("closeness")){
					closeness = Double.parseDouble(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("harmonic")){
					harmonic = Double.parseDouble(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("betweenness")){
					betweenness = Double.parseDouble(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("annotations")){
					skipAnnotation();
				}

				nexttoken = getNextToken();
			}
		}
		catch (Exception e){
			System.out.println("Error reading node.");
			return null;
		}
		return new ExportNode(id, ccid, chr, start, end, count, icount, degree, closeness, harmonic, betweenness);
	}
	
	private ExportEdge readEdge() {
		int id = -1;
		int ccid = -1;
		int source = -1;
		int target = -1;
		int count = -1;
		int icount = -1;
		
		try {
			String nexttoken = getNextToken();
			while(nexttoken != null && !nexttoken.equals("]")){
				String valtoken = getNextToken();
				if(valtoken == null || valtoken.equals("]")){
					System.out.println("Error reading edge.");
					return null;
				}
				if(nexttoken.equalsIgnoreCase("id")){
					id = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("ccid")){
					ccid = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("source")){
					source = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("target")){
					target = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("count")){
					count = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("interactioncount")){
					icount = Integer.parseInt(valtoken);
				}
				nexttoken = getNextToken();
			}
		}
		catch (Exception e){
			System.out.println("Error reading edge.");
			return null;
		}
		return new ExportEdge(id, ccid, source, target, count, icount);
	}
	
	
	private ExportComponent readComponent() {
		int id = -1;
		int nodecount = -1;
		int edgecount = -1;
		
		try {
			String nexttoken = getNextToken();
			while(nexttoken != null && !nexttoken.equals("]")){
				String valtoken = getNextToken();
				if(valtoken == null || valtoken.equals("]")){
					System.out.println("Error reading component.");
					return null;
				}
				if(nexttoken.equalsIgnoreCase("id")){
					id = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("nodecount")){
					nodecount = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("edgecount")){
					edgecount = Integer.parseInt(valtoken);
				}
				nexttoken = getNextToken();
			}
		}
		catch (Exception e){
			System.out.println("Error reading component.");
			return null;
		}
		return new ExportComponent(id, nodecount, edgecount);
	}
	
	
	private ExportInteraction readInteraction() {
		int id = -1;
		int iid = -1;
		String chr = "";
		int start = -1;
		int end = -1;
		int count = -1;
		String nodeid = null;
		String edgeid = null;
		int reason = -1;
		int distancetonode = -1;
		
		try {
			String nexttoken = getNextToken();
			while(nexttoken != null && !nexttoken.equals("]")){
				String valtoken = getNextToken();
				if(valtoken == null || valtoken.equals("]")){
					System.out.println("Error reading interaction.");
					return null;
				}
				if(nexttoken.equalsIgnoreCase("id")){
					id = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("iid")){
					iid = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("chr")){
					chr = valtoken.replace("\"", "");
				}
				else if(nexttoken.equalsIgnoreCase("start")){
					start = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("end")){
					end = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("count")){
					count = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("nodeid")){
					nodeid = valtoken.replace("\"", "");
				}
				else if(nexttoken.equalsIgnoreCase("edgeid")){
					edgeid = valtoken.replace("\"", "");
				}
				else if(nexttoken.equalsIgnoreCase("reason")){
					reason = Integer.parseInt(valtoken);
				}
				else if(nexttoken.equalsIgnoreCase("distancetonode")){
					distancetonode = Integer.parseInt(valtoken);
				}
				nexttoken = getNextToken();
			}
		}
		catch (Exception e){
			System.out.println("Error reading interaction.");
			return null;
		}
		return new ExportInteraction(id, iid, chr, start, end, count, nodeid, edgeid, reason, distancetonode);
	}
	
	private void createTables(Connection conn, long fid) throws SQLException{
		NetworkDBInsert dbinsert = new NetworkDBInsert();
		dbinsert.createTables(conn, fid);
	}
	
	private void importInteractions(Connection conn, long fid, ExportInteraction[] interactions) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Interactions_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?)");

		for(int i = 0; i < interactions.length; i++){
			ExportInteraction ci = interactions[i];
			//PE1
			ps.setInt(1, ci.getId());		//PET ID
			ps.setInt(2, ci.getIId());		//Interaction ID
			ps.setString(3, ci.getChr());		//Chr
			ps.setInt(4, ci.getStart());		//Start
			ps.setInt(5, ci.getEnd());		//End
			ps.setInt(6, ci.getCount());		//PET Count
			
			int nodeid = ci.getNodeId();
			if(nodeid < 0){
				ps.setNull(7, java.sql.Types.INTEGER);		//Node ID
			}
			else{
				ps.setInt(7, nodeid);		//Node ID
			}
			
			int edgeid = ci.getEdgeId();
			if(edgeid < 0){
				ps.setNull(8, java.sql.Types.INTEGER);
			}
			else{
				ps.setInt(8, edgeid); 		//Edge ID
			}
			ps.setInt(9, ci.getReason());
			ps.setInt(10, ci.getDistanceToNode());
			ps.addBatch();
			
			if((i % BATCHSIZE) == BSIZEM1){
				ps.executeBatch();
			}
		}
		ps.executeBatch();
		ps.close();
	}
	
	private void importNodes(Connection conn, long fid, ExportNode[] nodes) throws SQLException{
		PreparedStatement nodeps = conn.prepareStatement("INSERT INTO Nodes_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?,?)");

		for(int j = 0; j < nodes.length; j++){
			ExportNode n = nodes[j];
			nodeps.setInt(1, n.getNodeId());		//Node ID
			nodeps.setInt(2, n.getCCId());				//CCID
			nodeps.setString(3, n.getChr());	//Chr
			nodeps.setInt(4, n.getStart());		//Start
			nodeps.setInt(5, n.getEnd());		//End
			nodeps.setInt(6, n.getRCount()); //PET Count
			nodeps.setInt(7, n.getICount()); //Interaction Count
			
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
		nodeps.close();
	}
	
	private void importEdges(Connection conn, long fid, ExportEdge[] edges) throws SQLException{
		PreparedStatement edgeps = conn.prepareStatement("INSERT INTO Edges_"+fid+" VALUES(?,?,?,?,?,?)");

		for(int j = 0; j < edges.length; j++){
			ExportEdge e = edges[j];
			edgeps.setInt(1, e.getId());		//Edge ID
			edgeps.setInt(2, e.getCCId());				//CCID
			edgeps.setInt(3, e.getSource());	//N1 ID
			edgeps.setInt(4, e.getDest());		//N2 ID		
			edgeps.setInt(5, e.getPETCount());	//PET Count
			edgeps.setInt(6, e.getInteractionCount());	//PET Count
			edgeps.addBatch();
			if((j % BATCHSIZE) == BSIZEM1){
				edgeps.executeBatch();
			}
		}
		edgeps.executeBatch();
		
		edgeps.close();
	}
	
	private void importComponents(Connection conn, long fid, ExportComponent[] components) throws SQLException{
		PreparedStatement ccps = conn.prepareStatement("INSERT INTO ConnectedComponents_"+fid+" VALUES(?,?,?)");

		for(int i = 0; i < components.length; i++){
			ExportComponent cc = components[i];
			
			ccps.setInt(1, cc.getId());		//CCID
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
	
	private void importNetworkInfo(Connection conn, long fid, ExportNetworkInfo network) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Network_"+fid+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
		
		ps.setInt(1, network.getCCCount());
		ps.setInt(2, network.getNodeCount());
		ps.setInt(3, network.getEdgeCount());
		ps.setDouble(4, network.getAvgCountN());
		ps.setDouble(5, network.getAvgCountE());
		
		ps.setInt(6, network.getExtend());
		ps.setInt(7, network.getSL());
		ps.setInt(8, network.getMinPET());
		ps.setInt(9, network.getMaxDist());
		ps.setInt(10, network.getMinPETMax());
		ps.setInt(11, network.getMinPETInterchrom());
		ps.setInt(12, network.getMinSize());
		ps.setInt(13, network.getMinSizePET());
		
		ps.execute();
		ps.close();
	}
	
	private String getNextToken() throws IOException{
		if(_index >= _currentline.length){
			if(_br.ready()){
				_currentline = _br.readLine().split("\\s+");
				_currentline = splitBrackets(_currentline);
				_currentline = mergeQuotes(_currentline);
				_index = 0;
			}
			else{
				return null;
			}
		}
		
		return _currentline[_index++].replace("\"", "");
	}
	
	private String[] splitBrackets(String[] strings){
		LinkedList<String> rv = new LinkedList<String>();
		for(int i = 0; i < strings.length; i++){
			if(strings[i].contains("[") || strings[i].contains("]")){
				char[] characters = strings[i].toCharArray();
				LinkedList<Character> currentstring = new LinkedList<Character>();
				for(int j = 0; j < characters.length; j++){
					if(characters[j] == '[' || characters[j] == ']'){
						if(currentstring.size() > 0){
							char[] chars = new char[currentstring.size()];
							int index = 0;
							for(Iterator<Character> it = currentstring.iterator(); it.hasNext();){
								chars[index++] = it.next();
							}
							rv.add(new String(chars));
						}
						rv.add(new String(new char[] {characters[j]}));
						currentstring = new LinkedList<Character>();
					}
				}
			}
			else{
				rv.add(strings[i]);
			}
		}
		return rv.toArray(new String[0]);
	}
	
	private String[] mergeQuotes(String[] strings){
		LinkedList<String> rv = new LinkedList<String>();
		for(int i = 0; i < strings.length; i++){
			if(strings[i].startsWith("\"")){
				int j;
				for(j = i; j < strings.length; j++){
					if(strings[j].endsWith("\"")){
						break;
					}
				}
				String mergedstring = "";
				for(int k = i; k <= j; k++){
					mergedstring += strings[k];
				}
				rv.add(mergedstring);
				i = j;
			}
			else{
				rv.add(strings[i]);
			}
		}
		return rv.toArray(new String[0]);
	}
	
}
