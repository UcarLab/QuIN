package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.web.UserSession;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class FirstVisitServlet extends HttpServlet{
	
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
			
			Cookie[] cookies = req.getCookies();
			Boolean fv = (cookies == null);
			
			UserSession us = new UserSession();
	
			try {
				us.getUserId(req, resp, conn);
			} catch (Exception e) {
				e.printStackTrace();
				util.setResponse(resp, "[\"Error: Error loading session data.\"]");
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			Gson gson = new Gson();
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(gson.toJson(fv));
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
