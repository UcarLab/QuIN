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

public class GetSNPTraitServlet extends HttpServlet{
	
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

		ServletUtil util = new ServletUtil();

		String chr = req.getParameter("chr");
		String sstart = req.getParameter("start");
		String send = req.getParameter("end");

		if(chr == null){
			util.setResponse(resp, "[\"Error: Missing chromosome.\"]");
			return;
		}
		
		int start;
		int end;
		try{
			start = Integer.parseInt(sstart);
			end = Integer.parseInt(send);
		}
		catch(NumberFormatException e){
			util.setResponse(resp, "[\"Error: Start or end not an integer.\"]");
			return;
		}
		
		Connection conn = SQLConnectionFactory.getConnection();
		
		SNPTrait[] snptraits;
		try {
			snptraits = getSNPTraits(conn, chr, start, end);
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
		
		Gson gson = new Gson();
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(gson.toJson(snptraits, SNPTrait[].class));
		out.flush();
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private SNPTrait[] getSNPTraits(Connection conn, String chr, int start, int end) throws SQLException{
		
		String sql = "(SELECT DISTINCT dbsnp.rsid AS RefSNPId, cv.cvid AS cvid, dbsnp.chr AS chr, dbsnp.start AS start, dbsnp.end AS end, cv.trait AS Trait  "
				+ "FROM dbsnp.GRCh37p13 AS dbsnp "
				+ "LEFT OUTER JOIN (SELECT cv.rsid AS rsid, cv.id AS cvid, cvt.trait AS trait FROM clinvar.clinvar AS cv, clinvar.clinvar_traits AS cvt WHERE cv.traitid=cvt.id) AS cv ON dbsnp.rsid=cv.rsid "
				+ "WHERE dbsnp.chr = ? AND dbsnp.start <= ? AND dbsnp.end >= ? ORDER BY (cv.trait IS NULL), start)";
		
		String sql2 = "SELECT DISTINCT sq.RefSNPId AS RefSNPId, sq.cvid AS cvid, sq.chr AS chr, sq.start AS start, sq.end AS end, sq.trait AS Trait, cv.cvid AS gwasid, cv.trait AS gwastrait FROM "
				+ sql + " AS sq "
				+ "LEFT OUTER JOIN (SELECT cv.rsid AS rsid, cv.id AS cvid, cvt.trait AS trait FROM gwas.gwas AS cv, gwas.gwas_traits AS cvt WHERE cv.traitid=cvt.id) AS cv ON sq.RefSNPId=cv.rsid "
				+ "ORDER BY (cv.trait IS NULL), (sq.trait IS NULL), start";
		
		PreparedStatement ps = conn.prepareStatement(sql2);
		ps.setString(1, chr);
		ps.setInt(2, end);
		ps.setInt(3, start);
		
		ResultSet rs = ps.executeQuery();
		LinkedList<SNPTrait> l = new LinkedList<SNPTrait>();
		while(rs.next()){
			int rsid = rs.getInt(1);
			Integer cvid = rs.getInt(2);
			String schr = rs.getString(3);
			int sstart = rs.getInt(4);
			int send = rs.getInt(5);
			String trait = rs.getString(6);
			Integer pmid = rs.getInt(7);
			String gtrait = rs.getString(8);
			l.add(new SNPTrait(rsid, cvid, pmid, schr, sstart, send, trait, gtrait));
		}
		
		return l.toArray(new SNPTrait[0]);
	}
	
	@SuppressWarnings("unused")
	private class SNPTrait {
		private int rsid;
		private Integer cvid;
		private Integer pmid;
		private String chr;
		private int start;
		private int end;
		private String trait;
		private String gwastrait;
		public SNPTrait(int rsidi, Integer cvidi, Integer pmidi, String chri, int starti, int endi, String traiti, String gwastraiti){
			rsid = rsidi;
			cvid = cvidi;
			pmid = pmidi;
			chr = chri;
			start = starti;
			end = endi;
			trait = traiti;
			gwastrait = gwastraiti;
		}
		
	}
	
}
