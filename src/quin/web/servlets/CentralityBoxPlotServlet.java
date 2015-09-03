package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.analysis.CentralityComparison;
import quin.network.analysis.json.CentralityComparisonJson;
import quin.web.UserSession;
import quin.web.networkjson.Edge;
import quin.web.networkjson.Network;
import quin.web.networkjson.Node;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class CentralityBoxPlotServlet extends HttpServlet{
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
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
		
		String[] labels = req.getParameterValues("labels[]");
		if(labels != null){
			for(int i = 0; i < labels.length; i++){
				labels[i] = labels[i].replace("\"", "'");
			}
		}
		String smeasure = req.getParameter("measure");
		int measure = Integer.parseInt(smeasure);
		
		String[] genelists = req.getParameterValues("genes[]");
		String[] diseaselists = req.getParameterValues("diseases[]");
		String[] regionlists = req.getParameterValues("regions[]");
		String[] snplists = req.getParameterValues("snps[]");
		String[] colorindex = req.getParameterValues("colorindex[]");

		//String traitsrc = req.getParameter("traitsrc");
		//int ts = Integer.parseInt(traitsrc);
		int ts = 2; //Just GWAS for now
		
		Network njson = new Network();
		njson.setNodes(new Node[0]);
		njson.setEdges(new Edge[0]);

		Integer[] sids = new Integer[0];
		SIIndexUtil siu = new SIIndexUtil();
		sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
		
		CentralityComparison rboxplot = new CentralityComparison();
		
		String title = "";
		if(measure == 1){
			title = "Degree Centrality";
		}
		else if (measure == 2){
			title = "Closeness Centrality";
		}
		else if (measure == 3){
			title = "Harmonic Centrality";
		}
		else if (measure == 4){
			title = "Betweenness Centrality";
		}
		
		String fminsize = req.getParameter("minsize");
		String fmaxsize = req.getParameter("maxsize");

		int minsize = 1;
		int maxsize = Integer.MAX_VALUE;
		try {
			minsize = Integer.parseInt(fminsize);
			maxsize = Integer.parseInt(fmaxsize);
		}
		catch(NumberFormatException e){ }
		
		CentralityComparisonJson json = rboxplot.getJson(conn, fid, sids, measure, title, labels, minsize, maxsize, colorindex);

           
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		Gson gson = new Gson();
		
		out.print(gson.toJson(json, CentralityComparisonJson.class));
		out.flush();
	}
	
}
