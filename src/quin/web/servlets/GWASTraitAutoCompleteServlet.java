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

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GWASTraitAutoCompleteServlet extends HttpServlet{

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
		String term = req.getParameter("term");

		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		try {
			out.print(gson.toJson(getTop20MatchingTerms(term)));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.flush();
		
		
	}
	
	private String[] getTop20MatchingTerms(String term) throws SQLException{
		if(term == null){
			return new String[0];
		}
		
		Connection conn = SQLConnectionFactory.getConnection();
		String sql = "SELECT trait FROM gwas.gwas_traits WHERE trait LIKE ? LIMIT 0,20";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, '%'+term+'%');
		
		ResultSet rs = ps.executeQuery();
		LinkedList<String> l  = new LinkedList<String>();
		
		while(rs.next()){
			l.add(rs.getString(1));
		}
		
		rs.close();
		ps.close();
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return l.toArray(new String[0]);
	}

}
