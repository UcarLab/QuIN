package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.db.query.CCQuery;
import quin.web.UserSession;
import quin.web.networkjson.Edge;
import quin.web.networkjson.Network;
import quin.web.networkjson.Node;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetCCServlet extends HttpServlet{
	
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
		
		String[] genelists = req.getParameterValues("genes[]");
		String[] diseaselists = req.getParameterValues("diseases[]");
		String[] regionlists = req.getParameterValues("regions[]");
		String[] snplists = req.getParameterValues("snps[]");

		//String traitsrc = req.getParameter("traitsrc");
		//int ts = Integer.parseInt(traitsrc);
		int ts = 2; //Just GWAS for now
		String sccid = req.getParameter("ccid");
		int ccid = Integer.parseInt(sccid);
		
		//boolean promoter = req.getParameter("promoter").equals("true");
		
		Network njson = new Network();
		njson.setNodes(new Node[0]);
		njson.setEdges(new Edge[0]);

		SIIndexUtil siu = new SIIndexUtil();
		Integer[] sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
		CCQuery ccq = new CCQuery();
		try {
			njson = ccq.getCC(conn, "chiapet", fid, ccid, 2000, 2000, sids);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(gson.toJson(njson, Network.class));
		out.flush();
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
