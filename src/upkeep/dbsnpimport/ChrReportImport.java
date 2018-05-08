package upkeep.dbsnpimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import db.SQLConnectionFactory;

public class ChrReportImport {
	
	private TreeSet<String> _chrs;
	
	public void importChrReport(String tablename, String dir, boolean override) throws Exception{
		File f = new File(dir);
		if(!f.isDirectory()){
			throw new Exception("Not a directory");
		}
		
		Connection conn = SQLConnectionFactory.getConnection();
		try{
			_chrs = new TreeSet<String>();
			
			PreparedStatement ps = conn.prepareStatement("USE dbSNP");
			ps.execute();
			ps.close();
			
			TableCreator tc = new TableCreator();
			tc.createTable(conn, tablename, override);
			
			ps = conn.prepareStatement("LOCK TABLES "+tablename+" WRITE");
			ps.execute();
			ps.close();
			
	
			
			File[] files = f.listFiles();
			for(int i = 0; i < files.length; i++){
				if(files[i].isFile() && files[i].getPath().endsWith("gz")){
					String filename = files[i].getName();			
					System.out.println("Importing "+filename+".");
					importFile(conn, tablename, files[i]);
					System.out.println("Finished importing "+filename+".");
				}
			}		
			
			ps = conn.prepareStatement("UNLOCK TABLES");
			ps.execute();
			ps.close();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void importFile(Connection conn, String tablename, File file) throws SQLException, IOException{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tablename+" VALUES(?, ?, ?, ?)");
		boolean ac = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		FileInputStream fis = new FileInputStream(file);
		GZIPInputStream gis = new GZIPInputStream(fis);
		InputStreamReader ir = new InputStreamReader(gis);
		BufferedReader br = new BufferedReader(ir);
		
		String chr = null;
		
		int i = 0;
		while(br.ready()){
			String line = br.readLine();
			String[] split = line.split("\t");
			String location;
			if(split.length < 11 || (location = split[11].trim()).isEmpty() || location.contains("^")){
				continue;
			}
			
			int rs;
			int start;
			int end;
			try{
				rs = Integer.parseInt(split[0]);	
				if(location.contains("..")){
					String[] split2 = location.split("..");
					start = Integer.parseInt(split2[0]);
					end = Integer.parseInt(split2[1]);
				}
				else{
					start = Integer.parseInt(location);
					end = start;
				}
			}
			catch(NumberFormatException e){
				System.out.println(line);
				e.printStackTrace();
				continue;
			}
			
			chr = "chr"+split[6];
			
			ps.setInt(1, rs);
			ps.setString(2, chr);
			ps.setInt(3, start);
			ps.setInt(4, end);

			ps.addBatch();
			
			if(i++ % 1000 == 0){
				ps.executeBatch();
				ps.clearBatch();
			}
		}
		br.close();
		ir.close();
		gis.close();
		fis.close();
		
		_chrs.add(chr);
		
		ps.executeBatch();
		
		conn.commit();
		ps.close();
		conn.setAutoCommit(ac);
	}
	
}
