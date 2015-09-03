package quin.web.servlets;

import goanalysis.GO2Gene;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetGene2Go extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String goid = req.getParameter("goid");
		String[] geneids = req.getParameterValues("geneids[]");
		
		GO2Gene go2g = new GO2Gene();
		Connection conn = SQLConnectionFactory.getConnection();
		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		try {
			out.print(gson.toJson(go2g.getGO2Gene(conn, 9606, goid, geneids)));
		} catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
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
	
}
