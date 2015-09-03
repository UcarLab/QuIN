package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetUploadedDataIds extends HttpServlet{
	
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
		
		// Get the user information
		UserSession us = new UserSession();
		long uid = -1;
		try {
			uid = us.getUserId(req, resp, conn);
		} catch (Exception e) {
			e.printStackTrace();
			util.setResponse(resp, "Error: Error loading session data.");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}
		
		Gson gson = new Gson();
		OutputJson output = new OutputJson();
		
		
		try {
			//Get ChIAPET Data
			output.chiapetdata = getFileInfo(conn, "usersessions.ChiapetData", uid);
			
			//Get Network Data
			output.networks = getFileInfo(conn, "usersessions.Networks", uid);

			//Get Gene Data
			output.genelists = getFileInfo(conn, "usersessions.GeneLists", uid);

			//Get Disease Data
			output.diseaselists = getFileInfo(conn, "usersessions.DiseaseLists", uid);
			
			//Get SNP Data
			output.snplists = getFileInfo(conn, "usersessions.SNPLists", uid);

			//Get Region Data
			output.regionlists = getFileInfo(conn, "usersessions.RegionLists", uid);
			
			//Get Public ChIAPET Data
			output.pchiapetdata = getFileInfo(conn, "usersessions.ChiapetData", -1);
			
			//Get Public Network Data
			output.pnetworks = getFileInfo(conn, "usersessions.Networks", -1);

			//Get Public Gene Data
			output.pgenelists = getFileInfo(conn, "usersessions.GeneLists", -1);

			//Get Public Disease Data
			output.pdiseaselists = getFileInfo(conn, "usersessions.DiseaseLists", -1);

			//Get Public Region Data
			output.pregionlists = getFileInfo(conn, "usersessions.RegionLists", -1);

			//Get SNP Data
			output.psnplists = getFileInfo(conn, "usersessions.SNPLists", -1);

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(gson.toJson(output, OutputJson.class));
		out.flush();
	}
	
	
	private String[][] getFileInfo(Connection conn, String sessiontable, long uid) throws SQLException{
		String sql = "SELECT fid, name FROM "+sessiontable+" WHERE uid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, uid);
		ResultSet rs = ps.executeQuery();
		LinkedList<String[]> l = new LinkedList<String[]>();
		while(rs.next()){
			l.add(new String[] {Integer.toString(rs.getInt(1)), rs.getString(2)});
		}
		rs.close();
		ps.close();
		return l.toArray(new String[0][]);
	}
	
	@SuppressWarnings("unused")
	private class OutputJson {
		
		private String[][] chiapetdata;
		
		private String[][] networks;
		
		private String[][] genelists;
		
		private String[][] diseaselists;
		
		private String[][] snplists;
		
		private String[][] regionlists;
		
		private String[][] pchiapetdata;
		
		private String[][] pnetworks;
		
		private String[][] pgenelists;
		
		private String[][] pdiseaselists;
		
		private String[][] pregionlists;
		
		private String[][] psnplists;

		
	}

}
