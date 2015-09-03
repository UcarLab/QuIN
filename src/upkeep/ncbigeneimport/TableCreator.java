package upkeep.ncbigeneimport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TableCreator {
	
	public void createTables(Connection conn, String t1, String t2) throws SQLException{
		createGene2GoTable(conn, t1);
		createGene2RefSeqTable(conn, t2);
	}
	
	private void dropTable(Connection conn, String tablename) throws SQLException{
			PreparedStatement ps;
			ps = conn.prepareStatement("DROP TABLE IF EXISTS "+tablename);
			ps.execute();
			ps.close();	
	}
	
	private void createGene2GoTable(Connection conn, String table) throws SQLException{
		String tablename = table.trim().replace("\\s", "");
		dropTable(conn, tablename);
		PreparedStatement ps = conn.prepareStatement("CREATE TABLE "+tablename+" (tax_id INT NOT NULL, gene_id INT NOT NULL, goid VARCHAR(15) NOT NULL, evidence VARCHAR(7) NOT NULL, qualifier VARCHAR(511) NOT NULL, goterm VARCHAR(511) NOT NULL, pubmed VARCHAR(511) NOT NULL, category VARCHAR(511) NOT NULL, INDEX(goid, tax_id) USING HASH,  INDEX(gene_id) USING HASH)");
		ps.execute();
		ps.close();
	}
	
	private void createGene2RefSeqTable(Connection conn, String table) throws SQLException{
		String tablename = table.trim().replace("\\s", "");
		dropTable(conn, tablename);
		PreparedStatement ps = conn.prepareStatement("CREATE TABLE "+tablename+" (tax_id INT NOT NULL, gene_id INT NOT NULL, accession VARCHAR(31) NOT NULL, INDEX(tax_id, gene_id) USING HASH,  INDEX(accession) USING HASH)");
		ps.execute();
		ps.close();
	}
	
}
