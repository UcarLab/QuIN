package upkeep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import db.SQLConnectionFactory;


public class OldDataRemover implements Runnable{
	
	
	public void run(){
		Connection conn = SQLConnectionFactory.getConnection();
		try {
			setOldUsers(conn);
			Long[] networkfids = getNetworkFids(conn);
			Long[] chiapetfids = getChIAPETFids(conn);
			Long[] traitfids = getTraitFids(conn);
			Long[] genefids = getGeneFids(conn);
			Long[] regionfids = getRegionFids(conn);

			dropNetworkData(conn, networkfids);
			dropChIAPETData(conn, chiapetfids);
			dropTraitData(conn, traitfids);
			dropGeneData(conn, genefids);
			dropRegionData(conn, regionfids);
			
			removeNetworkFids(conn);
			removeChIAPETFids(conn);
			removeTraitFids(conn);
			removeGeneFids(conn);
			removeRegionFids(conn);
			
			deleteOldUsers(conn);
			dropTMPTable(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private Long[] getNetworkFids(Connection conn) throws SQLException{
		return getFids(conn, "usersessions.Networks");
	}
	
	private Long[] getChIAPETFids(Connection conn) throws SQLException{
		return getFids(conn, "usersessions.ChiapetData");
	}
	
	private Long[] getTraitFids(Connection conn) throws SQLException{
		return getFids(conn, "usersessions.DiseaseLists");
	}
	
	private Long[] getGeneFids(Connection conn) throws SQLException{
		return getFids(conn, "usersessions.GeneLists");
	}
	
	private Long[] getRegionFids(Connection conn) throws SQLException{
		return getFids(conn, "usersessions.RegionLists");
	}
	
	private Long[] getFids(Connection conn, String table) throws SQLException{
		String sql = "SELECT t.fid FROM "+table+" AS t, TMP_oldusers AS o WHERE o.uid=t.uid";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		LinkedList<Long> l = new LinkedList<Long>();
		
		while(rs.next()){
			l.add(rs.getLong(1));
		}
		
		rs.close();
		ps.close();
		return l.toArray(new Long[0]);
	}
	
	
	private void removeNetworkFids(Connection conn) {
		removeFids(conn, "usersessions.Networks");
	}
	
	private void removeChIAPETFids(Connection conn) {
		removeFids(conn, "usersessions.ChiapetData");
	}
	
	private void removeTraitFids(Connection conn) {
		removeFids(conn, "usersessions.DiseaseLists");
	}
	
	private void removeGeneFids(Connection conn) {
		removeFids(conn, "usersessions.GeneLists");
	}
	
	private void removeRegionFids(Connection conn) {
		removeFids(conn, "usersessions.RegionLists");
	}
	
	private void removeFids(Connection conn, String table) {
		String sql = "DELETE FROM "+table+" WHERE uid IN (SELECT * FROM TMP_oldusers)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void setOldUsers(Connection conn) throws SQLException{
		String sql = "CREATE TEMPORARY TABLE TMP_oldusers AS (SELECT uid FROM usersessions.Sessions WHERE LASTUSED < DATE_SUB(NOW(), INTERVAL 30 DAY))";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void deleteOldUsers(Connection conn) throws SQLException{
		String sql = "DELETE FROM usersessions.Sessions WHERE uid IN (SELECT * FROM TMP_oldusers)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void dropTMPTable(Connection conn) throws SQLException{
		String sql = "DROP TABLE TMP_oldusers";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void dropNetworkData(Connection conn, Long[] fids){
		for(int i = 0; i < fids.length; i++){
			Long fid = fids[i];
			dropData(conn, fids, "ConnectedComponents_");
			dropData(conn, fid, "Edges_");
			dropData(conn, fid, "Interactions_");
			dropData(conn, fid, "Network_");
			dropData(conn, fid, "Nodes_");
			dropData(conn, fid, "SIIndex_");
			dropData(conn, fid, "SIIndexList_");
		}
	}
	
	private void dropChIAPETData(Connection conn, Long[] fids){
		dropData(conn, fids, "chiapetdata.d_");
	}
	
	private void dropTraitData(Connection conn, Long[] fids){
		dropData(conn, fids, "diseaselists.d_");
	}
	
	private void dropGeneData(Connection conn, Long[] fids){
		dropData(conn, fids, "genelists.d_");
	}
	
	private void dropRegionData(Connection conn, Long[] fids){
		dropData(conn, fids, "regionlists.d_");
	}
	
	private void dropData(Connection conn, Long[] fids, String prefix){
		for(int i = 0; i < fids.length; i++){
			dropData(conn, fids[i], prefix);
		}
	}
	
	private void dropData(Connection conn, Long fid, String prefix){
		try {
			String sql = "DROP TABLE "+prefix+fid;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	}
	
}
