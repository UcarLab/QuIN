package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import quin.filereader.ChIAPETRead;

public class InteractionQuery {

	
	public ChIAPETRead[] getInteractions(Connection conn, long fid) throws SQLException{
		String table = "chiapetdata.d_"+fid;
		String sql = "SELECT i.chr1, i.start1, i.end1, i.chr2, i.start2, i.end2, i.petcount  FROM "+table+" AS i";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		LinkedList<ChIAPETRead> rv = new LinkedList<ChIAPETRead>();
		while(rs.next()){
			rv.add(new ChIAPETRead(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
		}
		rs.close();
		ps.close();
		return rv.toArray(new ChIAPETRead[0]);
	}
	
}
