package upkeep.ncbigeneimport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import db.SQLConnectionFactory;

public class GeneGoImportMain {

	public static void main(String[] args) {
		String gene2go = args[0];
		String gene2refseq = args[1];

		Connection conn = SQLConnectionFactory.getConnection();

		try {
			PreparedStatement ps = conn.prepareStatement("USE NCBI");
			ps.execute();
			ps.close();
			
			TableCreator tc = new TableCreator();
			tc.createTables(conn, "gene2go", "gene2refseq");
			
			Gene2RefSeqInsert g2refseqi = new Gene2RefSeqInsert();
			g2refseqi.insert(conn, gene2refseq, "gene2refseq");
			
			Gene2GoInsert g2goi = new Gene2GoInsert();
			g2goi.insert(conn, gene2go, "gene2go");
			
			ps = conn.prepareStatement("UNLOCK TABLES");
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
