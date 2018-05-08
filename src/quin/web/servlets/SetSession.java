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

import db.SQLConnectionFactory;

public class SetSession extends HttpServlet{
	
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
			String uid = req.getParameter("uid");
			String phrase = req.getParameter("phrase");
			
			String rv = "error";
			if(uid != null && phrase != null){
				Long luid = Long.parseLong(uid);
				UserSession us = new UserSession();
				try {
					if(us.sessionExists(conn, luid, phrase)){
						final int EXP = 604800;
						
						Cookie uidc = new Cookie("uid", luid.toString());
						uidc.setMaxAge(EXP);
	
						Cookie pc = new Cookie("phrase", phrase);
						pc.setMaxAge(EXP);
						resp.addCookie(uidc);
						resp.addCookie(pc);
						rv = "set";
					}
				} catch (SQLException e) {
					e.printStackTrace();
				};
			}
			
			resp.setContentType("text/plain");
			PrintWriter out = resp.getWriter();
			out.print(rv);
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
