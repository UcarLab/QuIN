package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NetworkQuery {

	public double[] getNetworkInfo(Connection conn, long fid, int min, int max) throws SQLException{
		String schema = "chiapet";
		String networktable = schema+".Network_"+fid; // TODO
		String cctable = schema+".ConnectedComponents_"+fid;
		String edgetable = schema+".Edges_"+fid;
		
		String sql = "SELECT count(cc.id), sum(cc.nodecount), sum(cc.edgecount), avgpet.*, n.extend, n.minpet, n.maxdist, n.minpetinterchrom "
				+ "FROM "+cctable+" AS cc, (SELECT sum(e.petcount)/count(*) FROM "+cctable+" AS cc, "+edgetable+" AS e "
				+ "WHERE e.ccid=cc.id AND cc.nodecount <= ? AND cc.nodecount >= ?) AS  avgpet, "+networktable+" AS n WHERE cc.nodecount <= ? AND cc.nodecount >= ?";
		
		PreparedStatement countps = conn.prepareStatement(sql);
		countps.setInt(1, max);
		countps.setInt(2, min);
		countps.setInt(3, max);
		countps.setInt(4, min);

		ResultSet crs = countps.executeQuery();
		crs.next();
		int ncc = crs.getInt(1);
		int nn = crs.getInt(2);
		int ne = crs.getInt(3);
		double avgpet = crs.getDouble(4);
		
		int ext = crs.getInt(5);
		int mpet = crs.getInt(6);
		int maxdist = crs.getInt(7);
		int minpetchrom = crs.getInt(8);
		countps.close();
		
		return new double[]{ncc, nn, ne, avgpet, ext, mpet, maxdist, minpetchrom};
	}
	
}
