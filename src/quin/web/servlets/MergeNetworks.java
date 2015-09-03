package quin.web.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.ChIAPETNetwork;
import quin.network.analysis.Centrality;
import quin.network.db.build.NetworkDBInsert;
import quin.network.merge.NetworkIntersection;
import quin.web.UserSession;
import db.SQLConnectionFactory;

public class MergeNetworks extends HttpServlet {

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

		String sfid1 = req.getParameter("fid1");
		Long fid1 = null;
		if(sfid1 != null){
			fid1 = Long.parseLong(sfid1);
		}
		
		String sfid2 = req.getParameter("fid2");
		Long fid2 = null;
		if(sfid2 != null){
			fid2 = Long.parseLong(sfid2);
		}
		
		
		//make sure the data exists for this user
		try {
			if(fid1 == null || !u.dataexists(conn, "usersessions.Networks", uid, fid1) || fid2 == null || !u.dataexists(conn, "usersessions.Networks", uid, fid2)){
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
			name = "N" + fid1+" I N"+fid2;
		}

		
		//build the network
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
			NetworkIntersection ni = new NetworkIntersection();
			ChIAPETNetwork network = ni.getIntersection(conn, fid1, fid2);
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
