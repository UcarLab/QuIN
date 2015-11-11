package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.db.query.CCIdQuery;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetTDCCIdsServlet extends HttpServlet{
	
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
		
		String[] s_genelists = req.getParameterValues("s_genes[]");
		String[] s_diseaselists = req.getParameterValues("s_diseases[]");
		String[] s_regionlists = req.getParameterValues("s_regions[]");
		String[] s_snplists = req.getParameterValues("s_snps[]");
		
		String[] t_genelists = req.getParameterValues("t_genes[]");
		String[] t_diseaselists = req.getParameterValues("t_diseases[]");
		String[] t_regionlists = req.getParameterValues("t_regions[]");
		String[] t_snplists = req.getParameterValues("t_snps[]");
	
		String fminsize = req.getParameter("minsize");
		String fmaxsize = req.getParameter("maxsize");

		String ssp = req.getParameter("sp");
		String stp = req.getParameter("tp");

		boolean sp = (ssp != null && ssp.equalsIgnoreCase("true"));
		boolean tp = (stp != null && stp.equalsIgnoreCase("true"));

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
		Integer[] s_sids = new Integer[0];
		s_sids = siu.getIndices(conn, uid, fid, ts, s_genelists, s_diseaselists, s_regionlists, s_snplists);
		
		Integer[] t_sids = new Integer[0];
		t_sids = siu.getIndices(conn, uid, fid, ts, t_genelists, t_diseaselists, t_regionlists, t_snplists);

		CCIdQuery ccidq = new CCIdQuery();
//		int sortby = 0;
//		try{
//			sortby = Integer.parseInt(req.getParameter("sortby"));
//		}
//		catch (NumberFormatException e){
//		}

		try {
			
			Integer[] p_ccids = new Integer[0];
			if(sp || tp){
				p_ccids = ccidq.getPromoterCCIds(conn, "chiapet", fid, 0, maxsize, minsize, false, 2000, 2000);
			}
			Integer[] s_ccids = ccidq.getCCIds(conn, "chiapet", fid, s_sids, 0, maxsize, minsize, false);
			Integer[] t_ccids = ccidq.getCCIds(conn, "chiapet", fid, t_sids, 0, maxsize, minsize, false);

			TreeSet<Integer> sourceset = new TreeSet<Integer>();
			for(int i = 0; i < s_ccids.length; i++){
				sourceset.add(s_ccids[i]);
			}
			if(sp){
				for(int i = 0; i < p_ccids.length; i++){
					sourceset.add(p_ccids[i]);
				}
			}
			
			LinkedList<Integer> finalset = new LinkedList<Integer>();
			for(int i = 0; i < t_ccids.length; i++){
				if(sourceset.contains(t_ccids[i])){
					finalset.add(t_ccids[i]);
				}
			}
			if(tp){
				for(int i = 0; i < p_ccids.length; i++){
					if(sourceset.contains(p_ccids[i])){
						finalset.add(p_ccids[i]);
					}
				}
			}
			
			ccids = finalset.toArray(new Integer[0]);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(gson.toJson(ccids));
		out.flush();
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
