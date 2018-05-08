package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.db.query.CCIdQuery;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetCCIdsServlet extends HttpServlet{
	
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
		try{
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
		
			String fminsize = req.getParameter("minsize");
			String fmaxsize = req.getParameter("maxsize");
	
			int minsize = 1;
			int maxsize = Integer.MAX_VALUE;
			try {
				minsize = Integer.parseInt(fminsize);
				maxsize = Integer.parseInt(fmaxsize);
			}
			catch(NumberFormatException e){ }
	
			//int ts = Integer.parseInt(traitsrc);
			int ts = 2; //Just GWAS for now.
			
			//boolean promoter = req.getParameter("promoter").equals("true");
			
			Integer[] ccids = new Integer[0];
			
			SIIndexUtil siu = new SIIndexUtil();
			Integer[] sids = new Integer[0];
			sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
	
			CCIdQuery ccidq = new CCIdQuery();
			int sortby = 0;
			try{
				sortby = Integer.parseInt(req.getParameter("sortby"));
			}
			catch (NumberFormatException e){
				
			}
			try {
				ccids = ccidq.getCCIds(conn, "chiapet", fid, sids, sortby, maxsize, minsize, !req.getParameter("annotatedonly").equalsIgnoreCase("true"));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			
			Gson gson = new Gson();
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(gson.toJson(ccids));
			out.flush();
		
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
