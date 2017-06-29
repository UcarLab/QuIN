package quin.network.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import quin.network.analysis.json.CentralityComparisonJson;

public class CentralityComparison {
	/*private String[]  COLORS = new String[]{"#0099ff", "#ff0000", "#00cc00", "#ffcc00", "#663399",
	      			"#ffff33", "#00ffcc", "#ff3399", "#00ff33", "#ff6600", "#cc33cc",
	      			"#ccff66", "#006699", "#990000", "#339933", "#33cc00", "#336633",
	      			"#cccc33", "#0000ff", "#ff3366", "#33ff00", "#cc9900", "#cc66ff",
	      			"#ffff99" };*/

	public CentralityComparisonJson getJson(Connection conn, long fid, Integer[] sids, int measure, String title, String[] labels, int min, int max, String[] colors){
		Dataset[] files = new Dataset[sids.length+1];
		//Dataset[] files = new Dataset[sids.length+2];
		/*try {
			files[0] = writeFile(conn, fid, labels[0], null, measure);
			files[1] = writeFile(conn, fid, labels[1], -1, measure);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		try {
			files[0] = writeFile(conn, fid, "All", null, measure, min, max);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		for(int i = 0; i < sids.length; i++){
			try {
				files[i+1] = writeFile(conn, fid, labels[i], sids[i], measure, min, max);
				//files[i+2] = writeFile(conn, fid, labels[i+2], sids[i], measure);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		CentralityComparisonJson rv = null;
		
		int size = labels != null ? labels.length+1 : 1;
		String[] nlabels = new String[size];
		String[] ncolors = new String[size];
		nlabels[0] = "All";
		ncolors[0] = "#cccccc";
		for(int i = 1; i < nlabels.length; i++){
			nlabels[i] = labels[i-1];
			ncolors[i] = colors[i-1];
		}
		
		try {
			rv = new CentralityComparisonJson(nlabels, getRBoxPlot(conn, fid, files, title, nlabels, ncolors), getMannWhitneyTable(files));
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		for(int i = 0; i < files.length; i++){
			files[i].FILE.delete();
		}
		return rv;
	}
	
	public byte[] getRBoxPlot(Connection conn, long fid, Dataset[] files, String title, String[] labels, String[] colors) throws REngineException, REXPMismatchException, IOException, SQLException{
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=1200, height=600)");
		
		
		for(int i = 0; i < files.length; i++){
			rconn.parseAndEval("d_"+i+" = read.csv(\""+files[i].FILE.getAbsolutePath()+"\", header = TRUE);");
		}
		
		String listparams = "d_0$measure";
		String names = "\""+labels[0]+" ("+files[0].COUNT+")\"";
		for(int i = 1; i < files.length; i++){
			listparams += ", d_"+i+"$measure";
			names += ", \""+labels[i]+" ("+files[i].COUNT+")\"";
		}
		rconn.parseAndEval("library(vioplot);");
		//rconn.parseAndEval("list = list("+listparams+");");
		/*
		 * ## set up frame, without axes
plot(1,1,xlim=c(0,3.5),ylim=range(c(a,b,c)),type="n",
     xlab="",ylab="",axes=FALSE)
## bottom axis, with user-specified labels
axis(side=1,at=1:3,labels=c("first","second","third"))
axis(side=2)
		 */
		
		int l = files.length;
		rconn.parseAndEval("plot(1,1,xlim=c(0.5,"+l+".5),ylim=range(c("+listparams+")),type='n', xlab='',ylab='',axes=FALSE);");
		rconn.parseAndEval("axis(side=1,at=1:"+l+",labels=c("+names+"));");
		rconn.parseAndEval("axis(side=2);");
		for(int i = 0; i < l; i++){
			int i1 = i+1;
			rconn.parseAndEval("vioplot(d_"+i+"$measure, at=c("+i1+"), col='"+colors[i]+"', add=TRUE);");
		}
		rconn.parseAndEval("dev.off()");
		rexp = rconn.parseAndEval("r=readBin('plot.png','raw',1024*1024); unlink('plot.png'); r");   

		
		byte[] b = rexp.asBytes();

		rconn.close();
		
        return Base64.encodeBase64(b);    
	}
	
	public String[][] getMannWhitneyTable(Dataset[] datasets) throws REngineException, REXPMismatchException{
		
		RConnection rconn = new RConnection();
		
		for(int i = 0; i < datasets.length; i++){
			rconn.parseAndEval("d_"+i+" = read.csv(\""+datasets[i].FILE.getAbsolutePath()+"\", header = TRUE);");
		}
		


		
		int l = datasets.length;
		String[][] rv = new String[l][l];
		for(int i = 0; i < l; i++){
			for(int j = 0; j < l; j++){
				REXP rexp1 = rconn.parseAndEval("wilcox.test(d_"+i+"$measure, d_"+j+"$measure, alternative=\"less\")$p.value;");
				REXP rexp2 = rconn.parseAndEval("wilcox.test(d_"+i+"$measure, d_"+j+"$measure, alternative=\"greater\")$p.value;");
				double cutoff = Math.pow(10,-300);
				if(rexp1.asDouble() < rexp2.asDouble()){
					if(rexp1.asDouble() < cutoff){
						rv[i][j] = "- < "+cutoff;
					}
					else{
						rv[i][j] = "-"+rexp1.asString();
					}
					
				}
				else{
					if(rexp2.asDouble() < cutoff){
						rv[i][j] = "< "+cutoff;
					}
					else{
						rv[i][j] = rexp2.asString();
					}
				}
			}
		}
		
		rconn.close();
		return rv;
	}
	
	private Dataset writeFile(Connection conn, long fid, String label, Integer sid, int measure, int min, int max) throws IOException, SQLException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rboxplot", "_"+sid, tmpdir);
		String m = getMeasure(measure);
		String sql = "SELECT DISTINCT "+m+" AS measure, n.id FROM chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" AS cc";
		PreparedStatement ps;
		if(sid != null && sid >= 0){
			sql += ", SIIndex_"+fid+" AS si WHERE si.iid = ? AND si.nid=n.id AND n.ccid = cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sid);
			ps.setInt(2, max);
			ps.setInt(3, min);
		}
		else{
			sql += " WHERE n.ccid = cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, max);
			ps.setInt(2, min);
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
			//return "CAST(n.degree AS decimal(64,10))/(cc.nodecount-1)";
			return "n.degree";
		}
		else if (measure == 2){
			//return "n.closeness*(cc.nodecount-1)";
			return "n.closeness";
		}
		else if (measure == 3){
			//return "n.harmonic/(cc.nodecount-1)";
			return "n.harmonic";
		}
		else if (measure == 4){
			return "(CASE WHEN cc.nodecount < 3 THEN 0 ELSE (n.betweenness/((cc.nodecount-1)*(cc.nodecount-2))) END)";
		}
		return "CAST(n.degree AS decimal(64,10))/(cc.nodecount-1)";
	}
	
	private class Dataset {
		public File FILE;
		public int COUNT;
		
		public Dataset(File file, int count){
			FILE = file;
			COUNT = count;
		}
	}
	
}
