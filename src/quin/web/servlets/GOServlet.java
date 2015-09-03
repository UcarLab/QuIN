package quin.web.servlets;

import goanalysis.TopGO;
import goanalysis.GOJson;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import com.google.gson.Gson;

public class GOServlet  extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] geneids = req.getParameterValues("geneids");
		
		if(geneids == null){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		execute(req, resp, geneids);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] geneids = req.getParameterValues("geneids[]");
		
		if(geneids == null){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		execute(req, resp, geneids);
	}
	
	private void execute(HttpServletRequest req, HttpServletResponse resp, String[] geneids) throws IOException{
		Gson gson = new Gson();
		resp.setContentType("application/json");
		
		TopGO tg = new TopGO();
		
    	PrintWriter w = resp.getWriter();
    	boolean error = true;
		try {
			w.print(gson.toJson(tg.topGOAnalysis(9606, geneids, 25), GOJson.class));
	    	w.flush();
	    	error = false;
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(error){
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}
	
}
