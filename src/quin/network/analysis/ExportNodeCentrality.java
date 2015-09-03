package quin.network.analysis;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//import org.rosuda.REngine.REXP;
//import org.rosuda.REngine.REXPMismatchException;
//import org.rosuda.REngine.REngineException;
//import org.rosuda.REngine.Rserve.RConnection;


public class ExportNodeCentrality {

	
	public void getExportFile(Connection conn, long fid, Integer[] sids, int measure, String title, String[] labels){
		
		
	}

	
	/*public double[][] getMannWhitneyTable(Dataset[] datasets) throws REngineException, REXPMismatchException{
		
		RConnection rconn = new RConnection();
		
		for(int i = 0; i < datasets.length; i++){
			rconn.parseAndEval("d_"+i+" = read.csv(\""+datasets[i].FILE.getAbsolutePath()+"\", header = TRUE);");
		}
		


		
		int l = datasets.length;
		double[][] rv = new double[l][l];
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				REXP rexp = rconn.parseAndEval("wilcox.test(d_"+i+"$measure, d_"+j+"$measure)$p.value;");
				rv[i][j] = rexp.asDouble();
			}
		}
		
		rconn.close();
		return rv;
	}
	
	/*private Dataset writeFile(Connection conn, long fid, String filename, Integer[] sids) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = new File(filename);
		String m = getMeasure(measure);
		String sql = "SELECT "+m+" AS measure FROM chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" AS cc";
		PreparedStatement ps;
		if(sid != null && sid > 0){
			sql += ", SIIndex_"+fid+" AS si WHERE si.iid = ? AND si.nid=n.id AND n.ccid = cc.id";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sid);
		}
		else if(sid != null && sid < 0){
			sql = "SELECT "+m+" AS measure FROM ((SELECT distinct n.id FROM chiapet.Nodes_"+fid+" AS n, ucsc.hg19 AS g WHERE strand='+' AND g.chrom=n.chr AND n.start <= g.txstart+2000 AND n.end >= g.txstart-2000)" 
				+ " UNION "
				+ "(SELECT distinct n.id FROM chiapet.Nodes_"+fid+" AS n, ucsc.hg19 AS g WHERE strand='-' AND g.chrom=n.chr AND n.start <= g.txend+2000 AND n.end >= g.txend-2000)) AS r, chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" AS cc WHERE n.ccid = cc.id AND n.id = r.id"; 		//TODO remove this, this is just to get POL2 sites in
			ps = conn.prepareStatement(sql);
		}
		else{
			sql += " WHERE n.ccid = cc.id";
			ps = conn.prepareStatement(sql);
		}

		ResultSet rs = ps.executeQuery();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("measure\n");
		int count = 0;
		while(rs.next()){
			bw.write(rs.getString(1)+"\n");
			count++;
		}
		
		bw.flush();
		bw.close();
		rs.close();
		ps.close();
		
		return new Dataset(f, count);
	}
	
	private String getMeasure(int measure){
		if(measure == 1){
			return "CAST(n.degree AS decimal(64,10))/(cc.nodecount-1)";
		}
		else if (measure == 2){
			return "n.closeness*(cc.nodecount-1)";
		}
		else if (measure == 3){
			return "n.harmonic/(cc.nodecount-1)";
		}
		else if (measure == 4){
			return "(CASE WHEN cc.nodecount = 2 THEN 0 ELSE (n.betweenness/((cc.nodecount-1)*(cc.nodecount-2))) END)";
		}
		return "CAST(n.degree AS decimal(64,10))/(cc.nodecount-1)";
	}*/
	
}
