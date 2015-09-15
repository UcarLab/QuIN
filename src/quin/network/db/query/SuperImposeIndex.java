package quin.network.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SuperImposeIndex {
	
	
	//TODO SNP list
	public Integer getIndexId(Connection conn, long fid, int dtype, long did, int ext, int genome, int upstream, int downstream, int traitsrc) throws SQLException{
		String sql = "SELECT id FROM SIIndexList_"+fid+" WHERE dtype=? AND did=? AND ext=? AND genome=? AND upstream=? AND downstream=? AND traitsrc=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ps.setInt(1, dtype);
		ps.setLong(2, did);
		ps.setInt(3, ext);
		ps.setInt(4, genome);
		ps.setInt(5, upstream);
		ps.setInt(6, downstream);
		ps.setInt(7, traitsrc);
		
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			return rs.getInt(1);
		}
		else{
			return insertIndex(conn, fid, dtype, did, ext, genome, upstream, downstream, traitsrc);
		}
	}
	
	public Integer getAnnotationNodeCount(Connection conn, long fid, Integer index, int min, int max) throws SQLException{
		String sql = "SELECT count(DISTINCT s.nid) FROM chiapet.SIIndex_"+fid+" AS s, chiapet.Nodes_"+fid+" AS n, chiapet.ConnectedComponents_"+fid+" AS c WHERE s.iid=? AND s.nid=n.id AND n.ccid=c.id AND c.nodecount >= ? AND c.nodecount <= ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, index);
		ps.setInt(2, min);
		ps.setInt(3, max);
		
		ResultSet rs = ps.executeQuery();
		int rv = -1;
		if(rs.next()){
			rv = rs.getInt(1);
		}
		rs.close();
		ps.close();
		
		return rv;
	}
	
	private Integer insertIndex(Connection conn, long fid, int dtype, long did, int ext, int genome, int upstream, int downstream, int traitsrc) throws SQLException{
		String sql = "INSERT INTO SIIndexList_"+fid+" (dtype, did, ext, genome, upstream, downstream, traitsrc) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setInt(1, dtype);
		ps.setLong(2, did);
		ps.setInt(3, ext);
		ps.setInt(4, genome);
		ps.setInt(5, upstream);
		ps.setInt(6, downstream);
		ps.setInt(7, traitsrc);
		ps.execute();
		
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		int id = rs.getInt(1);
		rs.close();
		
		ps.close();
		insertIndexData(conn, id, fid, dtype, did, ext, genome, upstream, downstream, traitsrc);
		return id;
	}
	
	private void insertIndexData(Connection conn, int iid, long fid, int dtype, long did, int ext, int genome, int upstream, int downstream, int traitsrc) throws SQLException{
		if(iid > 1000){
			String sql = "ALTER TABLE SIIndex_"+fid+" DROP PARTITION p"+(iid-1000);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.execute();
			ps.close();
			
			sql = "DELETE FROM chiapet.SIIndexlist_"+fid+" WHERE id=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, iid);
			ps.execute();
			ps.close();
		}
		String sql = "ALTER TABLE SIIndex_"+fid+" ADD PARTITION (PARTITION p"+iid+" VALUES IN(?))";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, iid);
		ps.execute();
		ps.close();
		if(dtype == 1){
			//Region List
			insertRegionAnnotations(conn, iid, fid, did, ext);
		}
		else if(dtype == 2){
			//Gene List
			insertGeneAnnotations(conn, iid, fid, did, genome, upstream, downstream);
		}
		else if(dtype == 3){
			//Trait/Disease List
			insertTraitAnnotations(conn, iid, fid, did, traitsrc);
		}
		else if(dtype == 4){
			//SNP List
			insertSNPAnnotations(conn, iid, fid, did, traitsrc);
		}
	}
	
	private void insertRegionAnnotations(Connection conn, int iid, long fid, long did, int ext) throws SQLException{
		String regiontable = "regionlists.d_"+did;
		String sql = "INSERT INTO SIIndex_"+fid+" (iid,nid,term,chr,start,end) ";
		sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid, 'region' AS term, r.chr AS chr, r.start AS start, r.end AS end FROM chiapet.Nodes_"+fid+" AS n, "+regiontable+" AS r WHERE n.chr=r.chr AND r.start <= n.end+"+ext+" AND n.start <= r.end+"+ext+")";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	private void insertGeneAnnotations(Connection conn, int iid, long fid, long did, int genome, int upstream, int downstream) throws SQLException{
		String ucscgenetable = "ucsc.hg19";
		String genetable = "genelists.d_"+did;
		
		String sql;
		if(upstream > 0 && downstream > 0){
			sql = "INSERT INTO SIIndex_"+fid+" (iid,nid,term,chr,start,end) ";
			sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid, g.gene AS term, ug.chrom AS chr, ug.txstart AS start, ug.txend AS end FROM chiapet.Nodes_"+fid+" AS n, "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND ug.chrom=n.chr AND ((n.start <= ug.txstart+"+downstream+" AND ug.txstart-"+upstream+" <= n.end AND ug.strand='+') OR (n.start <= ug.txend+"+upstream+" AND ug.txend-"+downstream+" <= n.end AND ug.strand='-')))";
		}
		else{
			sql = "INSERT INTO SIIndex_"+fid+" (iid,nid,term,chr,start,end) ";
			sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid, g.name AS term, g.chrom AS chr, g.txstart AS start, g.txend AS end FROM chiapet.Nodes_"+fid+" AS n, "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND g.chrom=n.chr AND n.start <= g.txend AND g.txstart <= n.end)";
		}
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	/* TODO
	 * private void insertPromoterAnnotations(Connection conn, int iid, int fid, int did, int genome, int upstream, int downstream) throws SQLException{
		String ucscgenetable = "ucsc.hg19";
		
		String sql;
		if(upstream > 0 && downstream > 0){
			sql = "INSERT INTO SIIndex_"+fid+" (iid,nid) ";
			sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid FROM chiapet.Nodes_"+fid+" AS n, "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND ug.chrom=n.chr AND ((n.start <= ug.txstart+"+downstream+" AND ug.txstart-"+upstream+" <= n.end AND ug.strand='+') OR (n.start <= ug.txend+"+upstream+" AND ug.txend-"+downstream+" <= n.end AND ug.strand='-')))";
		}
		else{
			sql = "INSERT INTO SIIndex_"+fid+" (iid,nid)";
			sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid FROM chiapet.Nodes_"+fid+" AS n, "+genetable+" AS g, "+ucscgenetable+" AS ug WHERE ug.genename=g.gene AND g.chrom=n.chr AND n.start <= g.txend AND g.txstart <= n.end)";
		}
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}*/
	
	private void insertTraitAnnotations(Connection conn, int iid, long fid, long did, int traitsrc) throws SQLException{
		String dbsnp = "dbsnp.grch37p13"; //TODO
		
		String cv = "clinvar.clinvar"; //TODO
		String cvt = "clinvar.clinvar_traits"; //TODO
		
		if(traitsrc == 2){
			cv = "gwas.gwas"; //TODO
			cvt = "gwas.gwas_traits"; //TODO
		}
		
		String traittable = "diseaselists.d_"+did;
		String sql = "INSERT INTO SIIndex_"+fid+" (iid,nid,term,chr,start,end) ";
		sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid, r.disease AS term, r.chr AS chr, r.start AS start, r.end AS end FROM chiapet.Nodes_"+fid+" AS n, (SELECT * FROM (SELECT dbsnp.chr AS chr, dbsnp.start AS start, dbsnp.end AS end, td.disease AS disease FROM "+dbsnp+" AS dbsnp, "
				+ "(SELECT c.rsid, td.disease AS disease FROM "+cv+" AS c, "+cvt+" AS ct, "+traittable+" AS td WHERE ct.trait=td.disease AND c.traitid=ct.id) AS td"
				+" WHERE dbsnp.rsid=td.rsid) AS r) AS r WHERE n.chr=r.chr AND r.start <= n.end AND n.start <= r.end)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
	
	
	private void insertSNPAnnotations(Connection conn, int iid, long fid, long did, int traitsrc) throws SQLException{
		String dbsnp = "dbsnp.grch37p13"; //TODO
		
		String snptable = "snplists.d_"+did;
		String sql = "INSERT INTO SIIndex_"+fid+" (iid,nid,term,chr,start,end) ";
		sql += "(SELECT DISTINCT "+iid+" AS iid, n.id AS nid, r.snpid AS term, r.chr AS chr, r.start AS start, r.end AS end FROM chiapet.Nodes_"+fid+" AS n, "
				+ "(SELECT sl.rsid AS snpid, d.chr AS chr, d.start AS start, d.end AS end FROM "+dbsnp+" AS d, "+snptable+" AS sl WHERE sl.rsid=d.rsid) AS r WHERE n.chr=r.chr AND r.start <= n.end AND n.start <= r.end)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
	}
}
