package upkeep.ncbigeneimport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class Gene2GoInsert {

	public void insert(Connection conn, String file, String table) throws IOException, SQLException{
		PreparedStatement lockps = conn.prepareStatement("LOCK TABLES "+table+" WRITE");
		lockps.execute();
		lockps.close();
		
		String sql = "INSERT INTO "+table+" (tax_id, gene_id, goid, evidence, qualifier, goterm, pubmed, category) VALUES (?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		FileInputStream fis = new FileInputStream(file);
		GZIPInputStream gis = new GZIPInputStream(fis);
		InputStreamReader ir = new InputStreamReader(gis);
		BufferedReader br = new BufferedReader(ir);
		
		int batchcount = 0;
		if(br.ready()){
			br.readLine();
			while(br.ready()){
				batchcount++;
				String line = br.readLine();
				Scanner sc = new Scanner(line);
				sc.useDelimiter("\t");
				
				int taxid = sc.nextInt();
				int geneid = sc.nextInt();
				String goid = sc.next();
				String evidence = sc.next();
				String qualifier = sc.next();
				String goterm = sc.next();
				String pubmed = sc.next();
				String category = sc.next();
				
				ps.setInt(1, taxid);
				ps.setInt(2, geneid);
				ps.setString(3, goid);
				ps.setString(4, evidence);
				ps.setString(5, qualifier);
				ps.setString(6, goterm);
				ps.setString(7, pubmed);
				ps.setString(8, category);
				sc.close();

				ps.addBatch();
				if(batchcount % 1000 == 0){
					ps.executeBatch();
				}
			}
			ps.executeBatch();
		}
		
		br.close();
		ir.close();
		gis.close();
		fis.close();
		
		ps.close();
	}
	
}
