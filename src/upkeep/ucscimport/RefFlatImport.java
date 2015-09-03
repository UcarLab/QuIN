package upkeep.ucscimport;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

import db.SQLConnectionFactory;


public class RefFlatImport {
	
	public void importFile(String table, String file) throws IOException, SQLException{
		FileInputStream fis = new FileInputStream(file);
		GZIPInputStream gzis = new GZIPInputStream(fis);
		InputStreamReader isr = new InputStreamReader(gzis);
		BufferedReader br = new BufferedReader(isr);
		
		Connection conn = SQLConnectionFactory.getConnection();
		
		//PreparedStatement dps = conn.prepareStatement("DELETE FROM table");
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+table+" VALUES(?,?,?,?,?,?,?,?,?,?,?)");
		
		while(br.ready()){
			String line = br.readLine();
			String[] cols = line.split("\t");
			
			ps.setString(1, cols[0]);
			ps.setString(2, cols[1]);
			ps.setString(3, cols[2]);
			ps.setString(4, cols[3]);
			ps.setInt(5, Integer.parseInt(cols[4]));
			ps.setInt(6, Integer.parseInt(cols[5]));
			ps.setInt(7, Integer.parseInt(cols[6]));
			ps.setInt(8, Integer.parseInt(cols[7]));
			ps.setInt(9, Integer.parseInt(cols[8]));
						
			ps.setBlob(10, getBlob(conn, cols[9]));
			ps.setBlob(11, getBlob(conn, cols[10]));
			
			ps.execute();
		}
		
		br.close();
		isr.close();
		gzis.close();
		fis.close();
		conn.close();
	}
	
	private Blob getBlob(Connection conn, String value) throws IOException, SQLException{
		Blob b = conn.createBlob();
		OutputStream bw = b.setBinaryStream(1);
		bw.write(value.getBytes());
		bw.flush();
		bw.close();
		return b;
	}
	
}
