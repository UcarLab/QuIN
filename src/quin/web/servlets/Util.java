package quin.web.servlets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Util {
	
	public boolean dataexists(Connection conn, String table, long uid, long fid) throws SQLException{
		String sql = "SELECT fid FROM "+table+" WHERE (uid=? OR uid=?) AND fid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, uid);
		ps.setLong(2, -1);
		ps.setLong(3, fid);
		ResultSet rs = ps.executeQuery();
		boolean result = rs.next();
		rs.close();
		ps.close();
		return result;
	}

	public long getFid(Connection conn, String name, String schema, String tablename)
			throws SQLException {
		
		String sql = "INSERT INTO "+schema+"."+tablename+" (uid, name) VALUES (?,?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setLong(1, -2);
		ps.setString(2, name);
		ps.execute();

		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		long fid = rs.getLong(1);
		rs.close();

		ps.close();
		return fid;
	}
	
	public void updateFid(Connection conn, long uid, long fid, String schema, String tablename)
			throws SQLException {
		String sql = "UPDATE "+schema+"."+tablename+" SET uid=? WHERE fid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, uid);
		ps.setLong(2, fid);
		ps.executeUpdate();
		ps.close();
	}
	
	public void dropAndRemove(Connection conn, long uid, long fid, String dataschema, String sessionschema, String sessiontable) throws SQLException{
		drop(conn, uid, fid, dataschema);
		remove(conn, uid, fid, sessionschema, sessiontable);
	}
	
	private void remove(Connection conn, long uid, long fid, String sessionschema, String sessiontable) throws SQLException{
		String sql = "REMOVE FROM "+sessionschema+"."+sessiontable+" WHERE uid=? AND fid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, uid);
		ps.setLong(2, fid);
		ps.execute();
		ps.close();
	}
	
	private void drop(Connection conn, long uid, long fid, String schema) throws SQLException{
		String sql = "DROP TABLE IF EXISTS "+schema+".d_"+fid;
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}

	public String getNetworkName(Connection conn, long fid) throws SQLException{
		String sql = "SELECT name FROM usersessions.Networks WHERE fid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, fid);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String rv = rs.getString(1);
		rs.close();
		ps.close();
		return rv;
	}
}
