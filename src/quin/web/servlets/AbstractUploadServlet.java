package quin.web.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import quin.web.UserSession;
import db.SQLConnectionFactory;

public abstract class AbstractUploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String _dataschema;
	protected String _sessionschema;
	protected String _sessiontable;
	protected String[] _validextensions;
	protected boolean _allowlist = true;
	protected HttpServletRequest _req;

	@Override 
	protected void doGet (HttpServletRequest req, HttpServletResponse resp){
		//Should only process post data.
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		_req = req;
		
		UserSession us = new UserSession();
		ServletUtil util = new ServletUtil();
		Util uu = new Util();

		// Prepare the connection
		Connection conn = SQLConnectionFactory.getConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e2) {
			e2.printStackTrace();
			util.setResponse(resp, "Error: SQL error.");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}

		// Set up the upload information
		Part filepart = req.getPart("file");
		if (filepart == null) {
			// no file error
			util.setResponse(resp, "Error: Missing File.");
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}
		String filename = util.getFileName(filepart);
		InputStream is = filepart.getInputStream();
		String[] listitems = getListItems(req.getParameter("list"));

		String name = req.getParameter("name");
		if (name == null || name.equals("")) {
			if (filename != null) {
				name = filename.substring(filename.lastIndexOf("//") + 1);
			} else {
				name = "List: "+listitems[0]+"...";
			}
		}

		

		// Get the user information
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
		
		//Check the file:
		boolean hasfile = checkFile(filename);
		if(!(_allowlist && listitems.length > 0) && !hasfile){
			util.setResponse(resp, "Error: Invalid file/no data submitted.");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}

		// Get the database file id
		// TODO check total size should be less than 10gb per session. Also
		// need to track IP numbers. Maybe limit the number of UID too to
		// shut down the site if someone decides to attack it.
		long fid = -1;
		try {
			fid = uu.getFid(conn, name, _sessionschema, _sessiontable);
		} catch (Exception e) {
			util.setResponse(resp, "Error: Error processing the file.");
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}

		// Process the data
		try {
			createTable(conn, fid);
			if(hasfile){
				insertValues(conn, fid, is, filename);
			}
			if(_allowlist){
				insertValues(conn, fid, listitems);
			}
			uu.updateFid(conn, uid, fid, _sessionschema, _sessiontable);
			conn.commit();
			util.setResponse(resp, "1");
		} catch (SQLException e) {
			e.printStackTrace();
			util.setResponse(resp, "Error: SQL Exception");
			try {
				uu.dropAndRemove(conn, uid, fid, _dataschema,
						_sessionschema, _sessiontable);
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			util.setResponse(resp, "Error: An unexpected error occurred.");
			try {
				uu.dropAndRemove(conn, uid, fid, _dataschema,
						_sessionschema, _sessiontable);
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean checkFile(String filename) {
		if(filename == null){
			return false;
		}
		
		for(int i = 0; i < _validextensions.length; i++){
			if(filename.endsWith(_validextensions[i])){
				return true;
			}
		}
		
		return false;
	}
	
	private String[] getListItems(String l){
		String[] listitems = new String[0];
		if(l != null){
			String[] li = l.trim().split("\n");
			LinkedList<String> lilist = new LinkedList<String>();
			for(int i = 0; i < li.length; i++){
				String cur = li[i].trim();
				if(cur.length() > 0){
					lilist.add(cur);
				}
			}
			listitems = lilist.toArray(new String[0]);
		}
		return listitems;
	}

	protected abstract void insertValues(Connection conn, long fid,
			InputStream is, String filename) throws Exception;

	protected abstract void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException;
	
	protected abstract void createTable(Connection conn, long fid)
			throws SQLException;
	
}
