package quin.network.analysis.json;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import quin.network.analysis.InteractionSeparationBarplot;
import quin.network.analysis.NodeSpanHistogram;

public class NetworkStatisticsJson {
	
	
	private String nodespan;
	private String interactionsep;

	public NetworkStatisticsJson(Connection conn, String title, long fid, int min, int max) throws REngineException, REXPMismatchException, IOException, SQLException{
		int binsize = 500;
		boolean density = false;
		NodeSpanHistogram nsh = new NodeSpanHistogram();
		nodespan = new String(nsh.getRHistogram(conn, "Node Span Distribution", fid, binsize, density, min, max));
		
		InteractionSeparationBarplot isb = new InteractionSeparationBarplot();
		interactionsep = new String(isb.getRBarplot(conn, title, fid));
		
	}
	
	public String getNodespan(){
		return nodespan;
	}
	
	
	public String getInteractionsep(){
		return interactionsep;
	}
	
}
