package upkeep.gwasimport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import db.SQLConnectionFactory;

public class ImportGWAS {

	public void importGWAS(String file, String tablename) throws IOException, SQLException{
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		TreeMap<String, Integer> traits = new TreeMap<String, Integer>();
		TreeMap<Integer, List<Integer[]>> snpmap = new TreeMap<Integer, List<Integer[]>>();
		
		if(br.ready()){
			br.readLine();
		}
		while(br.ready()){
			String[] cols = br.readLine().split("\t");
			if(cols.length < 21){
				continue;
			}
			try{
				int pmedid = Integer.parseInt(cols[1].trim());
				String trait = cols[7].trim();
				String[] snps = cols[21].split(",|:|x");
				if(!snps[0].startsWith("rs")){
					continue;
				}
				
				if(!traits.containsKey(trait)){
					traits.put(trait, traits.size()+1);
				}
				
				int traitid = traits.get(trait);
				

				
				for(int i = 0; i < snps.length; i++){
					try{
						int rsid = Integer.parseInt(snps[i].trim().replace("rs", ""));
						if(!snpmap.containsKey(rsid)){
							snpmap.put(rsid, new LinkedList<Integer[]>());
						}
						
						snpmap.get(rsid).add(new Integer[]{pmedid, traitid});
					}
					catch(NumberFormatException e){
						
					}
				}
			}
			catch(NumberFormatException e){
				
			}
			
		}
		br.close();

		
		Connection conn = SQLConnectionFactory.getConnection();
		
		try{
			TableCreator tc = new TableCreator();
			tc.createTables(conn, tablename, true);
			
			importTraits(conn, tablename, traits);
			importMapping(conn, tablename, snpmap);
		
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void importTraits(Connection conn, String tablename, TreeMap<String, Integer> traits) throws SQLException{
		boolean ac = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tablename+"_traits VALUES(?,?)");
		
		for(Iterator<Entry<String, Integer>> it = traits.entrySet().iterator(); it.hasNext();){
			Entry<String, Integer> next = it.next();
			ps.setInt(1, next.getValue());
			ps.setString(2, next.getKey());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		
		conn.commit();
		conn.setAutoCommit(ac);
	}
	
	private void importMapping(Connection conn, String tablename, TreeMap<Integer, List<Integer[]>> snptraitmap) throws SQLException{
		boolean ac = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tablename+" VALUES(?,?,?)");
		
		for(Iterator<Entry<Integer, List<Integer[]>>> it = snptraitmap.entrySet().iterator(); it.hasNext();){
			Entry<Integer, List<Integer[]>> next = it.next();
			Integer rsid = next.getKey();
			List<Integer[]> traits = next.getValue();

			TreeMap<Integer, TreeSet<Integer>> idused = new TreeMap<Integer, TreeSet<Integer>>();
			
			for(Iterator<Integer[]> it2 = traits.iterator(); it2.hasNext();){
				Integer[] traitid = it2.next();
				int id = traitid[0];
				int tid = traitid[1];
				
				if(!idused.containsKey(id) || !idused.get(id).contains(tid)){
					ps.setInt(1, id);
					ps.setInt(2, rsid);
					ps.setInt(3, tid);
					ps.addBatch();
					
					if(!idused.containsKey(id)){
						idused.put(id, new TreeSet<Integer>());
					}
					idused.get(id).add(tid);
				}
			}
		}
		ps.executeBatch();
		ps.close();
		
		conn.commit();
		conn.setAutoCommit(ac);
	}

}
