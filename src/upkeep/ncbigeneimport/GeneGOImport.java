package upkeep.ncbigeneimport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import db.SQLConnectionFactory;

public class GeneGOImport {

	public void importNCBIGene(String gene2go, String t1, String gene2refseq, String t2){
		Connection conn = SQLConnectionFactory.getConnection();

		try {
			PreparedStatement ps = conn.prepareStatement("USE NCBI");
			ps.execute();
			ps.close();
			
			TableCreator tc = new TableCreator();
			tc.createTables(conn, t1, t2);
			
			Gene2RefSeqInsert g2refseqi = new Gene2RefSeqInsert();
			g2refseqi.insert(conn, gene2refseq, t2);
			
			Gene2GoInsert g2goi = new Gene2GoInsert();
			g2goi.insert(conn, gene2go, t1);
			
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				PreparedStatement ps = conn.prepareStatement("UNLOCK TABLES");
				ps.execute();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
