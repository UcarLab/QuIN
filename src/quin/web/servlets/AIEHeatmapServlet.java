package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import quin.network.analysis.AnnotationInteractionEnrichment;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class AIEHeatmapServlet extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException{
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {		

		Connection conn = SQLConnectionFactory.getConnection();
		ServletUtil util = new ServletUtil();
		
		UserSession us = new UserSession();
		long uid = -1;
		try {
			uid = us.getUserId(req, resp, conn);
		} catch (Exception e) {
			util.setResponse(resp, "[\"Error: Error loading session data.\"]");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}
		

		String network = req.getParameter("network");
		long fid = -1;
		try {
			fid = Long.parseLong(network);
			Util u = new Util();
			if(!u.dataexists(conn, "usersessions.Networks", uid, fid)){
				util.setResponse(resp, "[\"Error: Error loading network.\"]");
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				return;
			}
		}
		catch(Exception e){
			util.setResponse(resp, "[\"Error: Error loading network.\"]");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}
		
		Gson gson = new Gson();
		String[] genelists = req.getParameterValues("genes[]");
		String[] diseaselists = req.getParameterValues("diseases[]");
		String[] regionlists = req.getParameterValues("regions[]");
		String[] snplists = req.getParameterValues("snps[]");

		//int ts = Integer.parseInt(traitsrc);
		int ts = 2; //Just GWAS for now.
		
		//boolean promoter = req.getParameter("promoter").equals("true");
		
		SIIndexUtil siu = new SIIndexUtil();
		Integer[] sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
			
		String fminsize = req.getParameter("minsize");
		String fmaxsize = req.getParameter("maxsize");

		int minsize = 1;
		int maxsize = Integer.MAX_VALUE;
		try {
			minsize = Integer.parseInt(fminsize);
			maxsize = Integer.parseInt(fmaxsize);
		}
		catch(NumberFormatException e){ }
		
		byte[] heatmap = new byte[0];
		AnnotationInteractionEnrichment sph = new AnnotationInteractionEnrichment();
		try {
			heatmap = sph.generateHeatmap(conn, fid, sids, minsize, maxsize);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		
		out.print(gson.toJson(new String(heatmap)));
		out.flush();
	}
	
}
