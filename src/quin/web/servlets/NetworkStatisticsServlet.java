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

import quin.network.analysis.json.NetworkStatisticsJson;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class NetworkStatisticsServlet extends HttpServlet{
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
		try{
			Util u = new Util();
	
			UserSession us = new UserSession();
			long uid = -1;
			try {
				uid = us.getUserId(req, resp, conn);
			} catch (Exception e) {
				closeConnection(conn);
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			String network = req.getParameter("network");
			long fid = -1;
			try {
				fid = Long.parseLong(network);
				if(!u.dataexists(conn, "usersessions.Networks", uid, fid)){
					closeConnection(conn);
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}
			catch(Exception e){
				e.printStackTrace();
				closeConnection(conn);
				return;
			}
			
	
			/*String sbinsize = req.getParameter("binsize");
			int binsize = Integer.parseInt(sbinsize);
			String sdensity = req.getParameter("density");
			boolean density = (sdensity != null && sdensity.equals("true"));
			*/
			String title = "Network";
			try {
				title = u.getNetworkName(conn, fid);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			//String fminsize = req.getParameter("minsize");
			//String fmaxsize = req.getParameter("maxsize");
	
			int minsize = 1;
			int maxsize = Integer.MAX_VALUE;
			try {
				//minsize = Integer.parseInt(fminsize);
				//maxsize = Integer.parseInt(fmaxsize);
			}
			catch(NumberFormatException e){
				
			}
			
			NetworkStatisticsJson json = null;
			try {
				json = new NetworkStatisticsJson(conn, title, fid, minsize, maxsize);
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
					
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			Gson gson = new Gson();
			
			out.print(gson.toJson(json, NetworkStatisticsJson.class));
			out.flush();
		}
		finally{
			closeConnection(conn);
		}
	}
	
	private void closeConnection(Connection conn){
		try {
			conn.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
