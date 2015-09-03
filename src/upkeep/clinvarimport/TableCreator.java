package upkeep.clinvarimport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TableCreator {
	
	public void createTables(Connection conn, String tablename, boolean override) throws SQLException{
		tablename = tablename.replace("\\s", "");
		if(override){
			dropTables(conn, tablename);
		}
		createTraitTable(conn, tablename);
		createTraitMappingTable(conn, tablename);

	}
	
	private void dropTables(Connection conn, String tablename) throws SQLException{
			PreparedStatement ps;
			
			ps = conn.prepareStatement("DROP TABLE IF EXISTS "+tablename);
			ps.execute();
			ps.close();
			
			ps= conn.prepareStatement("DROP TABLE IF EXISTS "+tablename+"_traits");
			ps.execute();
			ps.close();
			

	}
	
	private void createTraitMappingTable(Connection conn, String tablename) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("CREATE TABLE "+tablename+" (id INT NOT NULL, rsid INT NOT NULL, traitid INT NOT NULL, FOREIGN KEY(traitid) REFERENCES "+tablename+"_traits(id), INDEX(id) USING HASH, INDEX(rsid) USING HASH, INDEX(traitid) USING HASH)");
		ps.execute();
		ps.close();
	}
	
	private void createTraitTable(Connection conn, String tablename) throws SQLException{
		String tablename2 = tablename+"_traits";
		PreparedStatement ps = conn.prepareStatement("CREATE TABLE "+tablename2+" (id INT NOT NULL, trait VARCHAR(255), PRIMARY KEY(id), INDEX(id) USING HASH, INDEX(trait) USING BTREE)");
		ps.execute();
		ps.close();
	}
	
}
