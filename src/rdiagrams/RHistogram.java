package rdiagrams;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

public class RHistogram {

	
	public byte[] getHistogram(File f, String title, String xlab, int binsize, boolean density) throws REngineException, REXPMismatchException, IOException, SQLException{
		RConnection rconn = new RConnection();
		REXP rexp = rconn.parseAndEval("png('plot.png', width=800, height=600)");
		

		rconn.parseAndEval("h = read.csv(\""+f.getAbsolutePath()+"\", header = TRUE);");
		rconn.parseAndEval("hv=as.vector(as.matrix(h));");
		
		if(density){
			rconn.parseAndEval("hist(hv, main='"+title+"', xlab='"+xlab+"', col='#99CCFF', breaks=seq(0, max(hv)+"+binsize+", "+binsize+"), prob=TRUE)");	
			rconn.parseAndEval("hd = density(hv)");		
			rconn.parseAndEval("lines(hd, xlab='Node Span', col=\"red\", lwd=3);  dev.off()");		
		}
		else{
			rconn.parseAndEval("hist(hv, main='"+title+"', xlab='"+xlab+"', col='#99CCFF', breaks=seq(0, max(hv)+"+binsize+", "+binsize+")); dev.off()");		
		}
		
		rexp = rconn.parseAndEval("r=readBin('plot.png','raw',1024*1024); unlink('plot.png'); r");   

		
		byte[] b = rexp.asBytes();
		
		rconn.close();
		
        return Base64.encodeBase64(b);    
	}
}
