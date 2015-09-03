package goanalysis;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import db.SQLConnectionFactory;

public class TopGO {
	
	public GOJson topGOAnalysis(int taxid, String[] igenes, int top) throws REngineException, REXPMismatchException, SQLException, IOException{
		File g2go = Gene2GO.getGene2GOFile(taxid);
		//File igf = writeGeneFile(igenes);
		top = Math.min(Math.max(1, top), 100);
		RConnection rconn = new RConnection();
		rconn.parseAndEval("library(topGO)");
		rconn.parseAndEval("geneID2GO <- readMappings(\""+g2go.getAbsolutePath()+"\")");
		rconn.parseAndEval("geneNames <- names(geneID2GO)");
		//rconn.parseAndEval("igenes <- read.table(\""+igf.getAbsolutePath()+"\")$V1");
		rconn.assign("igenes", igenes);
		rconn.parseAndEval("geneList <- factor(as.integer(geneNames %in% igenes))");
		rconn.parseAndEval("names(geneList) <- geneNames");
		rconn.parseAndEval("GOdata <- new(\"topGOdata\", ontology=\"BP\", allGenes=geneList, annot = annFUN.gene2GO, gene2GO = geneID2GO)");
		rconn.parseAndEval("test.stat <- new(\"classicCount\", testStatistic = GOFisherTest, name = \"Fisher test\")");
		rconn.parseAndEval("resultFisher <- getSigGroups(GOdata, test.stat)");
		REXP rexp = rconn.parseAndEval("GenTable(GOdata, classic=resultFisher, topNodes="+top+")");
		
		RList rl = rexp.asList();
		String[] goids = rl.at(0).asStrings();
		String[] terms = rl.at(1).asStrings();
		int[] genesinterm = rl.at(2).asIntegers();
		int[] genesincomponent = rl.at(3).asIntegers();
		String[] expected = rl.at(4).asStrings();
		String[] fisher = rl.at(5).asStrings();
		GOJson rv = new GOJson(goids, terms, genesinterm, genesincomponent, expected, fisher);

		rconn.parseAndEval("allGO = genesInTerm(GOdata)");
		rconn.parseAndEval("getgenes = lapply(allGO,function(x) x[x %in% igenes])");

		int l = top+1;
		for(int i = 1; i < l; i++){
			rconn.parseAndEval("whichGO <- names(sort(score(resultFisher)))["+i+"]");
			REXP exp = rconn.parseAndEval("getgenes[[whichGO]]");
			rv.addGO2Gene((i-1), exp.asStrings());
		}
		
		rconn.close();
		return rv;
	}
	
	
	public GOJson topGOAnalysis(long fid, int taxid, int downstream, int upstream, int top) throws REngineException, REXPMismatchException, SQLException, IOException{
		File g2go = Gene2GO.getGene2GOFile(taxid);
		TreeMap<Integer, LinkedList<String>> cc2genes = getCCToGenes(fid, taxid, downstream, upstream);
		
		LinkedList<String> allgenes = new LinkedList<String>();
		for(Iterator<LinkedList<String>> it = cc2genes.values().iterator(); it.hasNext();){
			allgenes.addAll(it.next());
		}
		
		RConnection rconn = new RConnection();
		rconn.parseAndEval("library(topGO)");
		rconn.parseAndEval("geneID2GO <- readMappings(\""+g2go.getAbsolutePath()+"\")");
		rconn.parseAndEval("geneNames <- names(geneID2GO)");
		rconn.assign("igenes", allgenes.toArray(new String[0]));
		rconn.parseAndEval("geneList <- factor(as.integer(geneNames %in% igenes))");
		rconn.parseAndEval("names(geneList) <- geneNames");
		rconn.parseAndEval("GOdata <- new(\"topGOdata\", ontology=\"BP\", allGenes=geneList, annot = annFUN.gene2GO, gene2GO = geneID2GO)");
		rconn.parseAndEval("test.stat <- new(\"classicCount\", testStatistic = GOFisherTest, name = \"Fisher test\")");
		
//		LinkedList<Integer> ccids = new LinkedList<Integer>();
//		LinkedList<String> goids = new LinkedList<String>();
//		LinkedList<String> terms = new LinkedList<String>();
//		LinkedList<Integer> genesinterm = new LinkedList<Integer>();
//		LinkedList<Integer> genesincomponent = new LinkedList<Integer>();
//		LinkedList<String> expected = new LinkedList<String>();
//		LinkedList<String> fisher = new LinkedList<String>();

		for(Iterator<Entry<Integer, LinkedList<String>>> it = cc2genes.entrySet().iterator(); it.hasNext();){
			Entry<Integer, LinkedList<String>> next = it.next();
			//int ccid = next.getKey();
			String[] genes = next.getValue().toArray(new String[0]);
			rconn.assign("igenes", genes);
			rconn.parseAndEval("geneNames <- names(geneID2GO)");
			rconn.parseAndEval("GOdata@allScores=factor(as.integer(geneNames %in% igenes))");
			REXP rexp = rconn.parseAndEval("GenTable(GOdata, classic=resultFisher, topNodes=1)");		
			RList rl = rexp.asList();

			String[] goids = rl.at(0).asStrings();
			/*String[] terms = rl.at(1).asStrings();
			int[] genesinterm = rl.at(2).asIntegers();
			int[] genesincomponent = rl.at(3).asIntegers();
			String[] expected = rl.at(4).asStrings();
			String[] fisher = rl.at(5).asStrings();*/
			
			if(goids.length > 0){
				
				
			}
		}
		
		rconn.close();
		return null;
	}
	
	private TreeMap<Integer, LinkedList<String>> getCCToGenes(long fid, int taxid, int downstream, int upstream) throws SQLException{
		String sql = "SELECT DISTINCT n.ccid, r.gene_id, FROM chiapet.nodes_"+fid+" AS n, ucsc.hg19 AS g, ncbi.gene2refseq as r WHERE r.tax_id=? AND r.accession=g.name AND "
				+ "g.chrom=n.chr AND "
				+"((n.start <= g.txstart+"+upstream+" AND g.txstart-"+downstream+" <= n.end AND g.strand='+')"
				+"|| (n.start <= g.txend+"+downstream+" AND g.txend-"+upstream+" <= n.end AND g.strand='-'))s";
		Connection conn = SQLConnectionFactory.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, taxid);
		ResultSet rs = ps.executeQuery();
		
		TreeMap<Integer, LinkedList<String>> rv = new TreeMap<Integer, LinkedList<String>>();
		while(rs.next()){
			int ccid = rs.getInt(1);
			if(!rv.containsKey(ccid)){
				rv.put(ccid, new LinkedList<String>());
			}
			rv.get(ccid).add(rs.getString(2));
		}
		
		rs.close();
		ps.close();
		conn.close();
		return rv;
	}
	
	/*private File writeGeneFile(String[] genes) throws IOException{
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile("rgenes", ".txt", tmpdir);
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(int i = 0; i < genes.length; i++){
			bw.write(genes[i]+"\n");
		}
		bw.flush();
		bw.close();
		return f;
	}*/
	
}
