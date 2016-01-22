package quin.web.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.ChIAPETNetwork;
import quin.network.Location;
import quin.network.NetworkBuilder;
import quin.network.analysis.Centrality;
import quin.network.db.build.NetworkDBInsert;
import quin.network.db.query.LocationQuery;
import quin.web.UserSession;
import db.SQLConnectionFactory;

public class BuildNetwork extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Connection conn = SQLConnectionFactory.getConnection();
		ServletUtil util = new ServletUtil();
		Util u = new Util();
		
		// Get the user information
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

		String sfid = req.getParameter("fid");
		
		Long fid = null;
		
		if(sfid != null){
			fid = Long.parseLong(sfid);
		}
		
		//make sure the data exists for this user
		try {
			if(fid == null || !u.dataexists(conn, "usersessions.ChiapetData", uid, fid)){
				util.setResponse(resp, "[\"Error: Data not found.\"]");
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				return;
			}
		} catch (SQLException e1) {
			util.setResponse(resp, "[\"Error: SQL Error.\"]");
			try {
				conn.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			return;
		}
		
		String name = req.getParameter("name");
		if (name == null || name.trim().equals("")) {
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH)+1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minutes = cal.get(Calendar.MINUTE);
			int seconds = cal.get(Calendar.SECOND);
			name = "Network "+month+"/"+day+"/"+year+" "+hour+":"+minutes+":"+seconds;
		}

		// set build parameters:
		int dt = -1;
		int did = -1;
		int ext = 0;
		int sldist = 0;
		int minpet = 0;
		int maxdist = 1000000;
		int maxdistmin = Integer.MAX_VALUE;
		int intermin = Integer.MAX_VALUE;
		int minsize = 2;
		int twomin = Integer.MAX_VALUE;

		try {
			String rf = req.getParameter("rf");
			String[] split = rf.split("_");
			if(split.length == 2){
				dt = Integer.parseInt(split[0]);
				did = Integer.parseInt(split[1]);
			}
		} catch (Exception e) { }
		
		try {
			ext = Integer.parseInt(req.getParameter("ext"));
		} catch (Exception e) {
		}
		/*try {
			sldist = Integer.parseInt(req.getParameter("sl"));
		} catch (Exception e) {
		}*/
		try {
			minpet = Integer.parseInt(req.getParameter("minpet"));
		} catch (Exception e) {
		}
		try {
			maxdist = Integer.parseInt(req.getParameter("maxdist"));
		} catch (Exception e) {
		}
		/*try {
			maxdistmin = Integer.parseInt(req.getParameter("maxdistmin"));
		} catch (Exception e) {
		}*/
		try {
			int interminf = Integer.parseInt(req.getParameter("intermin"));
			if(interminf == 0){
				intermin = 0;
			}
			else if(interminf < 0){
				intermin = -1;
			}
		} catch (Exception e) {
		}
		/*try {
			minsize = Integer.parseInt(req.getParameter("minsize"));
		} catch (Exception e) {
		}*/
		/*try {
			twomin = Integer.parseInt(req.getParameter("twomin"));
		} catch (Exception e) {
		}*/
		
		//build the network
		final String DBTABLE = "chiapetdata.d_"+fid;
		final String NETWORKSCHEMA = "chiapet";
		final String SESSIONSCHEMA = "usersessions";
		final String SESSIONTABLE = "Networks";
		
		Util uu = new Util();
		
		long nfid;
		try {
			nfid = uu.getFid(conn, name, SESSIONSCHEMA, SESSIONTABLE);
		} catch (SQLException e1) {
			util.setResponse(resp, "[\"Error: SQL Error.\"]");
			e1.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			return;
		}
		
		try {
			NetworkBuilder nb = new NetworkBuilder(conn, DBTABLE, false);
			ChIAPETNetwork network;
			LocationQuery lq = new LocationQuery();
			if(dt > -1 && did > -1){
				Location[] locations = lq.getLocations(conn, dt, did, -1, 2000, 2000, 1);
				network = nb.buildNetwork(locations, minpet, maxdist, maxdistmin, intermin, minsize, twomin, ext);
			}
			else{
				 network = nb.buildNetwork(ext, sldist, minpet, maxdist, maxdistmin, intermin, minsize, twomin);
			}
			Centrality centrality = new Centrality();
			centrality.computeCentrality(network);
			NetworkDBInsert dbi = new NetworkDBInsert();
			dbi.insertData(nfid, network, NETWORKSCHEMA);
			uu.updateFid(conn, uid, nfid, SESSIONSCHEMA, SESSIONTABLE);
			util.setResponse(resp, "[1]");
		} catch (SQLException e) {
			e.printStackTrace();
			util.setResponse(resp, "[\"Error: SQL Error.\"]");
		} catch (Exception e) {
			e.printStackTrace();
			util.setResponse(resp, "[\"Error: Unexpected Error.\"]");
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
