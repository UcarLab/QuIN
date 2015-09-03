package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import quin.network.Location;

public class LocationQuery {
	
	
	public Location[] getLocations(Connection conn, int dtype, long did, int genome, int upstream, int downstream, int traitsrc) throws SQLException{
		if(dtype == 1){
			//Region List
			return getLocations(conn, did);
		}
		else if(dtype == 2){
			//Gene List
			return getGenePromoterLocations(conn, did, genome, upstream, downstream);
		}
		else if(dtype == 3){
			//Trait/Disease List
			return getTraitLocations(conn, did, traitsrc);
		}
		else if(dtype == 4){
			//SNP List
			return getSNPLocations(conn, did);
		}
		return null;
	}
	
	private Location[] getLocations(Connection conn, long did) throws SQLException{
		String regiontable = "regionlists.d_"+did;
		String sql = "SELECT DISTINCT r.chr AS chr, r.start AS start, r.end AS end FROM "+regiontable+" AS r";

		return getLocations(conn, sql);
	}
	
	private Location[] getGenePromoterLocations(Connection conn, long did, int genome, int upstream, int downstream) throws SQLException{
		String ucscgenetable = "ucsc.hg19";
		String genetable = "genelists.d_"+did;
		
		String sql;
		if(upstream > 0 && downstream > 0){
			sql = "(SELECT DISTINCT ug.chrom AS chr, ug.txstart-"+upstream+" AS start, ug.txstart+"+downstream+" AS end FROM "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND ug.strand='+') UNION"
					+ "(SELECT DISTINCT ug.chrom AS chr, ug.txend-"+downstream+" AS start, ug.txend+"+upstream+" AS end FROM "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND ug.strand='-')";
		}
		else{
			sql = "SELECT DISTINCT ug.chrom AS chr, ug.txstart AS start, ug.txend AS end FROM "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene";
		}
		
		return getLocations(conn, sql);
	}

	
	private Location[] getTraitLocations(Connection conn, long did, int traitsrc) throws SQLException{
		String dbsnp = "dbsnp.grch37p13"; //TODO
		
		String cv = "clinvar.clinvar"; //TODO
		String cvt = "clinvar.clinvar_traits"; //TODO
		
		if(traitsrc == 2){
			cv = "gwas.gwas"; //TODO
			cvt = "gwas.gwas_traits"; //TODO
		}
		
		String traittable = "diseaselists.d_"+did;
		String sql = "SELECT DISTINCT dbsnp.chr AS chr, dbsnp.start AS start, dbsnp.end AS end FROM "+dbsnp+" AS dbsnp, "
				+ "(SELECT c.rsid, td.disease AS disease FROM "+cv+" AS c, "+cvt+" AS ct, "+traittable+" AS td WHERE ct.trait=td.disease AND c.traitid=ct.id) AS td"
				+" WHERE dbsnp.rsid=td.rsid";
		return getLocations(conn, sql);
	}
	
	
	private Location[] getSNPLocations(Connection conn, long did) throws SQLException{
		String dbsnp = "dbsnp.grch37p13"; //TODO
		
		String snptable = "snplists.d_"+did;
		String sql = "SELECT DISTINCT d.chr AS chr, d.start AS start, d.end AS end FROM "+dbsnp+" AS d, "+snptable+" AS sl WHERE sl.rsid=d.rsid";
		return getLocations(conn, sql);
	}
	
	private Location[] getLocations(Connection conn, String sql) throws SQLException{
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		LinkedList<Location> l = new LinkedList<Location>();
		int id = 0;
		while(rs.next()){
			String chr = rs.getString(1);
			int start = rs.getInt(2);
			int end = rs.getInt(3);
			l.add(new Location(id++, chr, start, end));
		}
		rs.close();
		ps.close();
		return l.toArray(new Location[0]);
	}
}
