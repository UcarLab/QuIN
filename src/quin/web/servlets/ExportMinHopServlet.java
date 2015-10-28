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

import quin.export.ExportMinHopFile;
import quin.network.db.query.SuperImposeIndex;
import quin.web.UserSession;

import com.google.gson.Gson;

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
		
		
		String starget = req.getParameter("target");
		Integer target = null;
		try {
			target = getTargetId(conn, fid, starget);
		}
		catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}
		
		Gson gson = new Gson();
		String[] genelists = gson.fromJson(req.getParameter("genes"), String[].class);
		String[] diseaselists = gson.fromJson(req.getParameter("diseases"), String[].class);
		String[] regionlists = gson.fromJson(req.getParameter("regions"), String[].class);
		String[] snplists = gson.fromJson(req.getParameter("snps"), String[].class);

		//int ts = Integer.parseInt(traitsrc);
		int ts = 2; //Just GWAS for now.
		
		//boolean promoter = req.getParameter("promoter").equals("true");
		
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f1 = File.createTempFile("minhopexport", "_"+uid+"_"+fid, tmpdir);
		File f2 = File.createTempFile("minhopexportn", "_"+uid+"_"+fid, tmpdir);
		File zf = File.createTempFile("minhopexportz", "_"+uid+"_"+fid, tmpdir);

		SIIndexUtil siu = new SIIndexUtil();
		Integer[] sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
			
		String fminsize = req.getParameter("minsize");
		String fmaxsize = req.getParameter("maxsize");

		int minsize = 1;
		int maxsize = Integer.MAX_VALUE;
		try {
			minsize = Integer.parseInt(fminsize);
			maxsize = Integer.parseInt(fmaxsize);
		}
		catch(NumberFormatException e){ }
		
		ExportMinHopFile export = new ExportMinHopFile();
		try {
			export.createMinHopFile(conn, fid, sids, target, f1.getAbsolutePath(), f2.getAbsolutePath(), zf.getAbsolutePath(), minsize, maxsize);
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
	
	private Integer getTargetId(Connection conn, long fid, String target) throws SQLException{
		String[] split = target.split("_");
		if(split.length == 2){
			try{
				int dtype = Integer.parseInt(split[0]);
				int id = Integer.parseInt(split[1]);
				SuperImposeIndex si = new SuperImposeIndex();

				if(dtype == 1){
					return si.getIndexId(conn, fid, 1, id, 0, 1, -1, -1, -1);

				}
				else if(dtype == 2){
					return si.getIndexId(conn, fid, 2, id, -1, 1, 2000, 2000, -1);

				}
				else if(dtype == 3){
					return si.getIndexId(conn, fid, 3, id, -1, 1, -1, -1, 2);
				}
				else if(dtype == 4){
					return si.getIndexId(conn, fid, 4, id, -1, 1, -1, -1, 2);
				}

			}catch(NumberFormatException e){
				
			}
		}
		return null;
	}
	
}
