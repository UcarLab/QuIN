package quin.web.servlets;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.annotation.MultipartConfig;

import quin.export.ImportNetwork;

@MultipartConfig
public class ImportNetworkServlet extends AbstractUploadServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImportNetworkServlet(){
		_dataschema = "chiapet";
		_sessionschema = "usersessions";
		_sessiontable = "Networks";
		_validextensions = new String[]{".gml"};
		_allowlist = false;
	}
	

	@Override
	protected void insertValues(Connection conn, long fid, InputStream is,
			String filename) throws Exception {
		ImportNetwork in = new ImportNetwork();
		in.fromGML(conn, fid, is);
	}

	@Override
	protected void insertValues(Connection conn, long fid, String[] lines)
			throws SQLException {
	}

	@Override
	protected void createTable(Connection conn, long fid) throws SQLException {
	}

	
}
