package quin.web.servlets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import quin.network.db.query.SuperImposeIndex;

public class SIIndexUtil {

	public Integer[] getIndices(Connection conn, long uid, long fid, int ts, String[] genelists, String[] diseaselists, String[] regionlists, String[] snplists){
		LinkedList<Integer> sidsl = new LinkedList<Integer>();

		try {
			long[] gids = getIds(conn, genelists, uid, "usersessions.GeneLists");
			long[] dids = getIds(conn, diseaselists, uid, "usersessions.DiseaseLists");
			long[] rids = getIds(conn, regionlists, uid, "usersessions.RegionLists");
			long[] snpids = getIds(conn, snplists, uid, "usersessions.SNPLists");

			SuperImposeIndex si = new SuperImposeIndex();

			for(int i = 0; i < rids.length; i++){
				sidsl.add(si.getIndexId(conn, fid, 1, rids[i], 0, 1, -1, -1, -1));
			}
			for(int i = 0; i < gids.length; i++){
				sidsl.add(si.getIndexId(conn, fid, 2, gids[i], -1, 1, 2000, 2000, -1));
			}
			for(int i = 0; i < dids.length; i++){
				sidsl.add(si.getIndexId(conn, fid, 3, dids[i], -1, 1, -1, -1, ts));
			}
			for(int i = 0; i < snpids.length; i++){
				sidsl.add(si.getIndexId(conn, fid, 4, snpids[i], -1, 1, -1, -1, -1));
			}
			
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return sidsl.toArray(new Integer[0]);
	}
	
	//TODO make superclass/function class
	private long[] getIds(Connection conn, String[] params, long uid, String table) throws SQLException{
		if(params == null){
			return new long[0];
		}
		Util u = new Util();
		LinkedList<Long> l = new LinkedList<Long>();
		for(int i = 0; i < params.length; i++){
			try{
				long fid = Long.parseLong(params[i]);
				if(u.dataexists(conn, table, uid, fid)){
					l.add(fid);
				}

			}
			catch(NumberFormatException e){}
		}
		
		long[] rv = new long[l.size()];
		int index = 0;
		for(Iterator<Long> it = l.iterator(); it.hasNext();){
			rv[index++] = it.next();
		}
		
		return rv;
	}
}
