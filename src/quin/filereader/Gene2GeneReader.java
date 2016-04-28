package quin.filereader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeSet;

import nearestgene.NearestTSSUtil;
import nearestgene.TSS;

public class Gene2GeneReader {

	private BufferedReader _br;
	private NearestTSSUtil _nutil;
	
	public Gene2GeneReader(String file, int skip, Connection conn) throws IOException, SQLException{
		_br = new BufferedReader(new FileReader(file));
		_nutil =  new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
		for(int i = 0; i < skip && _br.ready(); i++){
			_br.readLine();
		}
	}
	
	public Gene2GeneReader(InputStream istream, Connection conn) throws IOException, SQLException{
		_br = new BufferedReader(new InputStreamReader(istream));
		_nutil =  new NearestTSSUtil(conn, "ucsc.hg19", "geneName", "chrom", "txStart", "txEnd", "strand");
	}
	
	public LinkedList<ChIAPETRead> readLine() throws Exception {
		if(_br.ready()){
			String line = _br.readLine();
			String[] split = line.split("\\s");
			
			//TODO get locations by adding 2kb for each TSS;
			
			if(split.length < 2){
				return new LinkedList<ChIAPETRead>();
			}
			
			TSS[] g1 = _nutil.getTSS(split[0]);
			TSS[] g2 = _nutil.getTSS(split[1]);
			LinkedList<ChIAPETRead> rv = new LinkedList<ChIAPETRead>();
			TreeSet<String> duplicatefilter = new TreeSet<String>();
			for(int i = 0; i < g1.length; i++){
				for(int j = 0; j < g2.length; j++){
					String lchr = g1[i].chr;
					int lstart = g1[i].tss-2000;
					int lend = g1[i].tss+2000;
					String rchr = g2[j].chr;
					int rstart = g2[j].tss-2000;
					int rend = g2[j].tss+2000;
					int count = 0;
					
					//Make sure start < end
					if(lstart > lend){
						int t = lstart;
						lstart = lend;
						lend = t;
					}
					
					if(rstart > rend){
						int t = rstart;
						rstart = rend;
						rend = t;
					}
					
					if(rstart < lstart){
						String teststring = rchr+"|"+rstart+"|"+rend+"|"+lchr+"|"+lstart+"|"+lend+"|"+count;
						if(!duplicatefilter.contains(teststring)){
							rv.add(new ChIAPETRead(rchr, rstart, rend, lchr, lstart, lend, count));
						}
						duplicatefilter.add(teststring);
					}
					else{
						String teststring = rchr+"|"+rstart+"|"+rend+"|"+lchr+"|"+lstart+"|"+lend+"|"+count;
						if(!duplicatefilter.contains(teststring)){
							rv.add(new ChIAPETRead(lchr, lstart, lend, rchr, rstart, rend, count));
						}
						duplicatefilter.add(teststring);
					}
				}
			}
			return rv;
		}
		else{
			_br.close();
			return null;
		}
	}

	public boolean ready() throws IOException {
		return _br.ready();
	}

	public void close() throws IOException {
		_br.close();
	}

}
