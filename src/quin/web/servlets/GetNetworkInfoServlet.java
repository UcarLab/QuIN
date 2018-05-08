package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.db.query.NetworkQuery;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetNetworkInfoServlet extends HttpServlet{
	
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
			
			String fminsize = req.getParameter("minsize");
			String fmaxsize = req.getParameter("maxsize");
	
			int minsize = 1;
			int maxsize = Integer.MAX_VALUE;
			try {
				minsize = Integer.parseInt(fminsize);
				maxsize = Integer.parseInt(fmaxsize);
			}
			catch(NumberFormatException e){ }
			
	//		String[] genelists = req.getParameterValues("genes[]");
	//		String[] diseaselists = req.getParameterValues("diseases[]");
	//		String[] regionlists = req.getParameterValues("regions[]");
			
			//boolean promoter = req.getParameter("promoter").equals("true");
			
			double[] networkinfo = new double[0];
			try {
	//			int[] gids = getIds(conn, genelists, uid, "usersessions.genelists");
	//			int[] dids = getIds(conn, diseaselists, uid, "usersessions.diseaselists");
	//			int[] rids = getIds(conn, regionlists, uid, "usersessions.regionlists");
				NetworkQuery ccidq = new NetworkQuery();
				networkinfo = ccidq.getNetworkInfo(conn, fid, minsize, maxsize);
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			
			Gson gson = new Gson();
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(gson.toJson(networkinfo));
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
	
//	private int[] getIds(Connection conn, String[] params, int uid, String table) throws SQLException{
//		if(params == null){
//			return new int[0];
//		}
//		Util u = new Util();
//		LinkedList<Integer> l = new LinkedList<Integer>();
//		for(int i = 0; i < params.length; i++){
//			try{
//				int fid = Integer.parseInt(params[i]);
//				if(u.dataexists(conn, table, uid, fid)){
//					l.add(fid);
//				}
//
//			}
//			catch(NumberFormatException e){}
//		}
//		
//		int[] rv = new int[l.size()];
//		int index = 0;
//		for(Iterator<Integer> it = l.iterator(); it.hasNext();){
//			rv[index++] = it.next();
//		}
//		
//		return rv;
//	}
	
}
