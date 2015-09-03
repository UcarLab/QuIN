package rdiagrams;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

public class RBarplot {
	
	public byte[] getBarplot(File f, String title) throws REngineException, REXPMismatchException, IOException, SQLException{
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=800, height=600)");
		
		rconn.parseAndEval("bp = read.csv(\""+f.getAbsolutePath()+"\", header = TRUE, check.names=FALSE);");
		rconn.parseAndEval("bp = data.matrix(bp)");		
		rconn.parseAndEval("plot <- barplot(bp, main=\""+title+"\", ylim=c(0, 1.1*max(bp)), beside=TRUE, col='#99CCFF')");		
		rconn.parseAndEval("text(x = plot, y = bp, label = bp, pos = 3, cex = 0.8); dev.off()");

		rexp = rconn.parseAndEval("r=readBin('plot.png','raw',1024*1024); unlink('plot.png'); r");   
		
		byte[] b = rexp.asBytes();
		
		rconn.close();
		
        return Base64.encodeBase64(b);   
	}
}
