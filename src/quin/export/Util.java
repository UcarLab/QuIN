package quin.export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

public class Util {

	
	public String[] getLabels(Connection conn, long fid, Integer[] ids) throws SQLException{
		Util u = new Util();
		String[] rv = new String[ids.length];
		for(int i = 0; i < ids.length; i++){
			rv[i] = u.getDataset(conn, fid, ids[i]);
		}
		return rv;
	}
	
	public String getDataset(Connection conn, long fid, int index) throws SQLException{
		String schema = "chiapet";
		String indextable = schema+".SIIndexList_"+fid;
		String sql = "SELECT dtype, did FROM "+indextable+" WHERE id = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, index);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int dtype = rs.getInt(1);
		long did = rs.getLong(2);
		rs.close();
		ps.close();
		if(dtype == 1){
			return getRegionListDataset(conn, did);
		}
		else if(dtype == 2){
			return getGeneListDataset(conn, did);
		}
		else if(dtype == 3){
			return getTraitListDataset(conn, did);
		}
		else if(dtype == 4){
			return getSNPListDataset(conn, did);
		}
		return null;
	}
	
	public TreeSet<Integer> getAnnotationNodeIds(Connection conn, long fid, int ssid) throws SQLException{
		String table = "chiapet.SIIndex_"+fid;
		String sql = "SELECT DISTINCT NID FROM "+table+" AS si WHERE iid=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, ssid);
		ResultSet rs = ps.executeQuery();
		TreeSet<Integer> rv = new TreeSet<Integer>();
		while(rs.next()){
			rv.add(rs.getInt(1));
		}
		rs.close();
		ps.close();
		return rv;

	}
	
	private String getDataset(Connection conn, long did, String table) throws SQLException{
		String sql = "SELECT name FROM "+table+" WHERE fid = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, did);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String rv = rs.getString(1);
		rs.close();
		ps.close();
		return rv;
	}

	private String getRegionListDataset(Connection conn, long did) throws SQLException{
		return getDataset(conn, did, "usersessions.RegionLists");
	}
	private String getTraitListDataset(Connection conn, long did) throws SQLException{
		return getDataset(conn, did, "usersessions.DiseaseLists");
	}
	private String getGeneListDataset(Connection conn, long did) throws SQLException{
		return getDataset(conn, did, "usersessions.GeneLists");
	}
	private String getSNPListDataset(Connection conn, long did) throws SQLException{
		return getDataset(conn, did, "usersessions.SNPLists");
	}
	
}
