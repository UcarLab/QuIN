package goanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import db.SQLConnectionFactory;

public class Gene2GO {
	
	private static TreeMap<Integer, File> _gene2go;
	
	public static File getGene2GOFile(int taxid) throws SQLException, IOException{
		if(_gene2go == null){
			_gene2go = new TreeMap<Integer, File>();
		}
		if(!_gene2go.containsKey(taxid)){
			_gene2go.put(taxid, writeGene2GO(taxid));
		}
		return _gene2go.get(taxid);
	}

	private static File writeGene2GO(int taxid) throws SQLException, IOException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rgene2go", "_"+taxid, tmpdir);
		
		Connection conn = SQLConnectionFactory.getConnection();
		try{
			String sql = "SELECT gene_id, group_concat(goid SEPARATOR ', ') AS GOIDS FROM ncbi.gene2go WHERE tax_id=? GROUP BY gene_id";
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, taxid);
			
			ResultSet rs = ps.executeQuery();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			while(rs.next()){
				String geneid = rs.getString(1);
				String goids = rs.getString(2);
				bw.write(geneid+"\t"+goids+"\n");
			}
			bw.flush();
			bw.close();
			
			rs.close();
			ps.close();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return f;
	}
	
}
