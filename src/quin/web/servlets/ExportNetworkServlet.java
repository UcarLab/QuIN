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

import quin.export.ExportBEDEdges;
import quin.export.ExportEdges;
import quin.export.ExportNetwork;
import quin.web.UserSession;
import db.SQLConnectionFactory;

public class ExportNetworkServlet extends HttpServlet{
	
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
			
			File tmpdir = new File(System.getProperty("java.io.tmpdir"));
			File f = File.createTempFile("networkexport", "_"+uid+"_"+fid, tmpdir);
			
			//String fminsize = req.getParameter("minsize");
			//String fmaxsize = req.getParameter("maxsize");
	
			int minsize = 1;
			int maxsize = Integer.MAX_VALUE;
			/*try {
				minsize = Integer.parseInt(fminsize);
				maxsize = Integer.parseInt(fmaxsize);
			}
			catch(NumberFormatException e){ }
			*/
			
			Gson gson = new Gson();
			String[] genelists = gson.fromJson(req.getParameter("genes"), String[].class);
			String[] diseaselists = gson.fromJson(req.getParameter("diseases"), String[].class);
			String[] regionlists = gson.fromJson(req.getParameter("regions"), String[].class);
			String[] snplists = gson.fromJson(req.getParameter("snps"), String[].class);
			int ts = 2; //Just GWAS for now.
	
			SIIndexUtil siu = new SIIndexUtil();
			Integer[] sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
				
			
			//ExportNetwork export = new ExportNetwork();
			ExportBEDEdges export = new ExportBEDEdges();
			try {
				export.exportEdges(conn, fid, f.getAbsolutePath(), minsize, maxsize, sids);
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
			resp.setHeader("Content-Disposition", "attachment;filename="+networkname+"_edges.bed");
			ServletOutputStream os = resp.getOutputStream();
			byte[] buffer = new byte[1024];
			
			FileInputStream fr = new FileInputStream(f);
			int amt = 0;
			while((amt = fr.read(buffer)) != -1){
				os.write(buffer, 0, amt);
			}
			os.flush();
			os.close();
			fr.close();
			
			f.delete();
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
