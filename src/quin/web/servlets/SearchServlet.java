package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.network.db.query.SearchQuery;
import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class SearchServlet extends HttpServlet{
	
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
		
		String searchstring = req.getParameter("search").trim().toLowerCase();

		
		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		try {
			out.print(gson.toJson(getResults(conn, fid, searchstring)));
		} catch (SQLException e1) {
			e1.printStackTrace();
			util.setResponse(resp, "[\"Error: SQL Error.\"]");
			try {
				conn.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			return;
		}
		out.flush();
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int[][] getResults(Connection conn, long fid, String searchstring) throws SQLException{
		SearchQuery sq = new SearchQuery();
		String schema = "chiapet";
		String genedb = "ucsc.hg19";
		String dbsnp = "dbsnp.grch37p13";
		
		if(searchstring.contains(":")){
			String[] chrsplit = searchstring.split(":");
			String[] possplit = chrsplit[1].split("-");
			if(possplit.length < 2){
				possplit = chrsplit[1].split("\\s");
			}
			if(possplit.length < 2){
				return sq.searchByGeneName(conn, schema, fid, genedb, searchstring, 2000, 2000);
			}
			String chr = chrsplit[0];
			try {
				int start = Integer.parseInt(possplit[0].trim());
				int end = Integer.parseInt(possplit[1].trim());
				return sq.searchByPosition(conn, schema, fid, chr, start, end);
			} catch(NumberFormatException e){
				return sq.searchByGeneName(conn, schema, fid, genedb, searchstring, 2000, 2000);
			}
		}
		else if(searchstring.split("\\s").length > 2){
			String[] split = searchstring.split("\\s");
			String chr = split[0];
			try {
				int start = Integer.parseInt(split[1].trim());
				int end = Integer.parseInt(split[2].trim());
				return sq.searchByPosition(conn, schema, fid, chr, start, end);
			} catch(NumberFormatException e){
				return sq.searchByGeneName(conn, schema, fid, genedb, searchstring, 2000, 2000);
			}		
		}
		else if(searchstring.startsWith("rs")){
			try {
				int id = Integer.parseInt(searchstring.substring(2).trim());
				return sq.searchByRSID(conn, schema, fid, dbsnp, id);
			} catch(NumberFormatException e){
				return sq.searchByGeneName(conn, schema, fid, genedb, searchstring, 2000, 2000);
			}
		}
		else{
			return sq.searchByGeneName(conn, schema, fid, genedb, searchstring, 2000, 2000);
		}
	}
	
}
