package goanalysis;

import goanalysis.gohierarchy.GOHierarchyHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;

public class GO2Gene {
	
	public TreeMap<String, String[]> getGO2Gene(Connection conn, int taxid, String[] genes) throws SQLException{
		createTMPGeneTable(conn, genes);
		String sql = "SELECT DISTINCT g2go.goid, GROUP_CONCAT(DISTINCT g2go.gene_id SEPARATOR \",\") AS gene_ids FROM ncbi.gene2go AS g2go, TMP_GO2GENE AS g WHERE g2go.tax_id=? AND g2go.gene_id=g.gene_id GROUP BY g2go.goid";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, taxid);
		ResultSet rs = ps.executeQuery();
		TreeMap<String, String[]> rv = new TreeMap<String, String[]>();
		while(rs.next()){
			rv.put(rs.getString(1), rs.getString(2).split(","));
		}
		rs.close();
		ps.close();
		dropTMPGeneTable(conn);
		return rv;
	}
	
	public String[] getGO2Gene(Connection conn, int taxid, String goid, String[] genes) throws SQLException{
		createTMPGOIdTable(conn, goid);
		String sql = "SELECT g2go.gene_id AS gene_id FROM ncbi.gene2go AS g2go, TMP_GOIDS AS goids WHERE g2go.goid=goids.goid AND g2go.tax_id=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, taxid);
		ResultSet rs = ps.executeQuery();
		LinkedList<String> rv = new LinkedList<String>();
		while(rs.next()){
			rv.add(rs.getString(1));
		}
		rs.close();
		ps.close();
		dropTMPGOIdTable(conn);
		return rv.toArray(new String[0]);
	}
	
	private void createTMPGeneTable(Connection conn, String[] genes) throws SQLException{
		String sql = "CREATE TEMPORARY TABLE TMP_GO2GENE (gene_id INT NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
		insertGenes(conn, genes);
	}
	
	private void insertGenes(Connection conn, String[] genes) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO TMP_GO2GENE VALUES(?)");
		for(int i = 0; i < genes.length; i++){
			try{
				ps.setInt(1, Integer.parseInt(genes[i]));
				ps.addBatch();
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
			ps.addBatch();
		}
		ps.close();
	}	
	
	private void dropTMPGeneTable(Connection conn) throws SQLException{
		String sql = "DROP TABLE TMP_GO2GENE";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	
	private void createTMPGOIdTable(Connection conn, String goid) throws SQLException{
		String sql = "CREATE TEMPORARY TABLE TMP_GOIDS (goid varchar(255) NOT NULL)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
		insertGOIds(conn, goid);
	}
	
	private void insertGOIds(Connection conn, String goid) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO TMP_GOIDS VALUES(?)");
		String[] goids = GOHierarchyHelper.getParentGOIds(goid);
		for(int i = 0; i < goids.length; i++){
			try{
				ps.setString(1, goids[i]);
				ps.addBatch();
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
		}
		ps.executeBatch();
		ps.close();
	}	
	
	private void dropTMPGOIdTable(Connection conn) throws SQLException{
		String sql = "DROP TABLE TMP_GOIDS";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}

}
