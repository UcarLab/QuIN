package quin.web.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import quin.export.ExportMinHopFile;
import quin.web.UserSession;
import db.SQLConnectionFactory;

public class ExportMinHopServlet extends HttpServlet{
	
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
		
		Gson gson = new Gson();
		String[] s_genelists = gson.fromJson(req.getParameter("s_genes"), String[].class);
		String[] s_diseaselists = gson.fromJson(req.getParameter("s_diseases"), String[].class);
		String[] s_regionlists = gson.fromJson(req.getParameter("s_regions"), String[].class);
		String[] s_snplists = gson.fromJson(req.getParameter("s_snps"), String[].class);
		
		String[] t_genelists = gson.fromJson(req.getParameter("t_genes"), String[].class);
		String[] t_diseaselists = gson.fromJson(req.getParameter("t_diseases"), String[].class);
		String[] t_regionlists = gson.fromJson(req.getParameter("t_regions"), String[].class);
		String[] t_snplists = gson.fromJson(req.getParameter("t_snps"), String[].class);

		//int ts = Integer.parseInt(traitsrc);
		int ts = 2; //Just GWAS for now.
		
		//boolean promoter = req.getParameter("promoter").equals("true");
		
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f1 = File.createTempFile("minhopexport", "_"+uid+"_"+fid, tmpdir);
		File f2 = File.createTempFile("minhopexportn", "_"+uid+"_"+fid, tmpdir);
		File zf = File.createTempFile("minhopexportz", "_"+uid+"_"+fid, tmpdir);

		SIIndexUtil siu = new SIIndexUtil();
		Integer[] sids = siu.getIndices(conn, uid, fid, ts, s_genelists, s_diseaselists, s_regionlists, s_snplists);
		Integer[] tids = siu.getIndices(conn, uid, fid, ts, t_genelists, t_diseaselists, t_regionlists, t_snplists);

		String fminsize = req.getParameter("minsize");
		String fmaxsize = req.getParameter("maxsize");
		
		String ssp = req.getParameter("sp");
		String stp = req.getParameter("tp");

		boolean sp = (ssp != null && ssp.equalsIgnoreCase("true"));
		boolean tp = (stp != null && stp.equalsIgnoreCase("true"));


		int minsize = 1;
		int maxsize = Integer.MAX_VALUE;
		try {
			minsize = Integer.parseInt(fminsize);
			maxsize = Integer.parseInt(fmaxsize);
		}
		catch(NumberFormatException e){ }
		
		ExportMinHopFile export = new ExportMinHopFile();
		try {
			export.createMinHopFile(conn, fid, sids, tids, f1.getAbsolutePath(), f2.getAbsolutePath(), zf.getAbsolutePath(), minsize, maxsize, sp, tp, "hg19", 2000, 2000);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		Util u = new Util();
		String networkname = network+"_"+fid;
		try {
			networkname = u.getNetworkName(conn, fid);
			networkname = networkname.replaceAll("[^a-zA-Z0-9]", "");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		
		resp.setContentType("text/plain");
		resp.setHeader("Content-Disposition", "attachment;filename="+networkname+"_ShortestPaths.zip");
		ServletOutputStream os = resp.getOutputStream();
		byte[] buffer = new byte[1024];
		
		FileInputStream fr = new FileInputStream(zf);
		int amt = 0;
		while((amt = fr.read(buffer)) != -1){
			os.write(buffer, 0, amt);
		}
		os.flush();
		os.close();
		fr.close();
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		zf.delete();
	}
	
	
}
