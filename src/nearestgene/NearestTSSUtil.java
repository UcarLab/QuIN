package nearestgene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class NearestTSSUtil {
	private TreeMap<String, TSS[]> _tsssorted;
	private TreeMap<String, LinkedList<TSS>> _genetss;

	public NearestTSSUtil(Connection conn, String database, String genecol, String chrcol, String startcol, String endcol, String strandcol) throws SQLException{
		String startsortedsql = "SELECT g."+genecol+", g."+chrcol+", CASE g."+strandcol+" WHEN '-' THEN g."+endcol+" ELSE g."+startcol+" END AS TSS, g."+strandcol+" FROM "+database+" AS g ORDER BY tss ASC";
		PreparedStatement ps = conn.prepareStatement(startsortedsql);
		ResultSet rs = ps.executeQuery();
		
		TreeMap<String, LinkedList<TSS>> map = new TreeMap<String, LinkedList<TSS>>();
		_genetss = new TreeMap<String, LinkedList<TSS>>();
		while(rs.next()){
			String chr = rs.getString(2);
			if(!map.containsKey(chr)){
				map.put(chr, new LinkedList<TSS>());
			}
			map.get(chr).add(new TSS(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
			if(!_genetss.containsKey(rs.getString(1).toLowerCase())){
				_genetss.put(rs.getString(1).toLowerCase(), new LinkedList<TSS>());
			}
			_genetss.get(rs.getString(1).toLowerCase()).add(new TSS(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
		}
		rs.close();
		ps.close();
		_tsssorted = new TreeMap<String, TSS[]>();
		for(Iterator<Entry<String, LinkedList<TSS>>> it = map.entrySet().iterator(); it.hasNext();){
			Entry<String, LinkedList<TSS>> e = it.next();
			_tsssorted.put(e.getKey(), e.getValue().toArray(new TSS[0]));
		}
		
	}
	
	public TSS[] getTSS(String gene){
		if(!_genetss.containsKey(gene.toLowerCase())){
			return new TSS[0];
		}
		return _genetss.get(gene.toLowerCase()).toArray(new TSS[0]);
	}
	
	public TSSGene[] getNearestGene(String chr, int start, int end){
		TreeSet<String> nearesttss = new TreeSet<String>();
		int nearesttssd = Integer.MAX_VALUE;


		TSS[] slist = _tsssorted.get(chr);
		if(slist == null){
			return new TSSGene[]{ new TSSGene("null", -1)};
		}
		int min = 0;
		int max = slist.length;
		int mid = max/2;
		while(true){
			mid = min+((max-min)/2);
			TSS midtss = slist[mid];
			int tss = midtss.tss;
			if(tss < start){
				mid = min;
				min = mid+1;
			}
			else if(tss > end){
				mid = max;
				max = mid-1;
			}
			else{
				//Found a match, now scan in both directions until the start position is out of range
				nearesttss.add(midtss.genename);
				nearesttssd = 0;

				for(int i = mid-1; i > -1; i--){
					TSS g = slist[i];
					if(g.tss <= end && g.tss >= start){
						nearesttss.add(g.genename);
					}
					else{
						break;
					}
				}
				for(int i = mid+1; i < slist.length; i++){
					TSS g = slist[i];
					if(g.tss <= end && g.tss >= start){
						nearesttss.add(g.genename);
					}
					else{
						break;
					}
				}
				break;
			}
			
			if(min >= max){
				break;
			}
		}
		
		if(nearesttss.isEmpty()){
			//if no match, look at the nearest tss and add the closest one
			TSS midtss = slist[mid];
			int tss = midtss.tss;
			if(tss < start){ //look right
				if(mid == slist.length-1){
					nearesttss.add(midtss.genename);
					nearesttssd = start-tss;
				}
				else{
					TSS rtss = slist[mid+1];
					int ld = start-tss;
					int rd = rtss.tss-end;
					if(ld < rd){
						nearesttss.add(midtss.genename);
						addSameTSSLeft(nearesttss, slist, mid);
					}
					else if(ld > rd){
						nearesttss.add(rtss.genename);
						addSameTSSRight(nearesttss, slist, mid+1);
					}
					else{
						nearesttss.add(midtss.genename);
						addSameTSSLeft(nearesttss, slist, mid);
						nearesttss.add(rtss.genename);
						addSameTSSRight(nearesttss, slist, mid+1);
					}
					nearesttssd = Math.min(ld, rd);
				}
			}
			else { //look left
				if(mid == 0){
					nearesttss.add(midtss.genename);
					nearesttssd = tss-end;
				}
				else{
					TSS ltss = slist[mid-1];
					int rd = tss-end;
					int ld = start-ltss.tss;
					if(ld < rd){
						nearesttss.add(ltss.genename);
						addSameTSSLeft(nearesttss, slist, mid-1);
					}
					else if(ld > rd){
						nearesttss.add(midtss.genename);
						addSameTSSRight(nearesttss, slist, mid);
					}
					else{
						nearesttss.add(ltss.genename);
						addSameTSSLeft(nearesttss, slist, mid-1);
						nearesttss.add(midtss.genename);
						addSameTSSRight(nearesttss, slist, mid);
					}
					nearesttssd = Math.min(ld, rd);
				}
			}
		}
		
		String[] genes = nearesttss.toArray(new String[0]);
		
		TSSGene[] rv = new TSSGene[genes.length];
		for(int i = 0; i < genes.length; i++){
			rv[i] = new TSSGene(genes[i], nearesttssd);
		}
		
		return rv;
	}
	
	
	//Gets the nearest TSS upstream and downstream of the current position
	//TODO Should use a shared function since this is copied code.  Copied because the method is a bit integrated and the goal isn't to clean up the code right now
	public FlankingTSS getFlankingTSS(String chr, int start, int end){
		TreeSet<String> upstreamnearesttss = new TreeSet<String>();
		LinkedList<String> upstreamneareststrand = new LinkedList<String>();
		int utssd = -1;
		
		TreeSet<String> downstreamnearesttss = new TreeSet<String>();
		LinkedList<String> downstreamneareststrand = new LinkedList<String>();
		int dtssd = -1;

		TSS[] slist = _tsssorted.get(chr);
		if(slist == null){
			return new FlankingTSS(upstreamnearesttss, utssd, upstreamneareststrand, downstreamnearesttss, dtssd, downstreamneareststrand);
		}
		int min = 0;
		int max = slist.length;
		int mid = max/2;
		while(true){
			mid = min+((max-min)/2);
			TSS midtss = slist[mid];
			int tss = midtss.tss;
			if(tss < start){
				mid = min;
				min = mid+1;
			}
			else if(tss > end){
				mid = max;
				max = mid-1;
			}
			else{
				upstreamnearesttss.add(midtss.genename);
				upstreamneareststrand.add(midtss.strand);
				utssd = 0;
				dtssd = 0;


				for(int i = mid-1; i > -1; i--){
					TSS g = slist[i];
					if(g.tss <= end && g.tss >= start){
						upstreamnearesttss.add(g.genename);
						upstreamneareststrand.add(g.strand);
					}
					else{
						break;
					}
				}
				for(int i = mid+1; i < slist.length; i++){
					TSS g = slist[i];
					if(g.tss <= end && g.tss >= start){
						upstreamnearesttss.add(g.genename);
						upstreamneareststrand.add(g.strand);
					}
					else{
						break;
					}
				}
				
				downstreamnearesttss = upstreamnearesttss;
				downstreamneareststrand = upstreamneareststrand;
				break;
			}
			
			if(min >= max){
				break;
			}
		}
		
		if(upstreamnearesttss.isEmpty()){
			TSS midtss = slist[mid];
			int tss = midtss.tss;
			
			int ui, di;
			if(tss < start){
				ui = mid;
				di = mid+1;
			}
			else{
				ui = mid-1;
				di = mid;
			}
			
			//look downstream
			if(di < slist.length){
				TSS rtss = slist[di];
				downstreamnearesttss.add(rtss.genename);
				downstreamneareststrand.add(rtss.strand);
				addSameTSSLeft(downstreamnearesttss, slist, di);
				addSameTSSRight(downstreamnearesttss, slist, di);
				addSameTSSStrandLeft(downstreamneareststrand, slist, di);
				addSameTSSStrandRight(downstreamneareststrand, slist, di);
				dtssd = rtss.tss - end;
			}
			
			
			//look upstream
			if(ui > -1){
				TSS ltss = slist[ui];
				upstreamnearesttss.add(ltss.genename);
				upstreamneareststrand.add(ltss.strand);
				addSameTSSLeft(upstreamnearesttss, slist, ui);
				addSameTSSRight(upstreamnearesttss, slist, ui);
				addSameTSSStrandLeft(upstreamneareststrand, slist, ui);
				addSameTSSStrandRight(upstreamneareststrand, slist, ui);
				utssd = start - ltss.tss;
			}
		
		}
		
		return new FlankingTSS(upstreamnearesttss, utssd, upstreamneareststrand, downstreamnearesttss, dtssd, downstreamneareststrand);
	}
	
	private void addSameTSSLeft(TreeSet<String> set, TSS[] alltss, int position){
		int itss = alltss[position].tss;
		for(int i = position; i > -1; i--){
			int ntss = alltss[i].tss;
			if(itss == ntss){
				set.add(alltss[i].genename);
			}
			else {
				break;
			}
		}
	}
	
	private void addSameTSSStrandLeft(LinkedList<String> set, TSS[] alltss, int position){
		int itss = alltss[position].tss;
		for(int i = position-1; i > -1; i--){
			int ntss = alltss[i].tss;
			if(itss == ntss){
				set.add(alltss[i].strand);
			}
			else {
				break;
			}
		}
	}
	
	private void addSameTSSRight(TreeSet<String> set, TSS[] alltss, int position){
		int itss = alltss[position].tss;
		for(int i = position; i < alltss.length; i++){
			int ntss = alltss[i].tss;
			if(itss == ntss){
				set.add(alltss[i].genename);
			}
			else{
				break;
			}
		}
	}
	
	private void addSameTSSStrandRight(LinkedList<String> set, TSS[] alltss, int position){
		int itss = alltss[position].tss;
		for(int i = position+1; i < alltss.length; i++){
			int ntss = alltss[i].tss;
			if(itss == ntss){
				set.add(alltss[i].strand);
			}
			else{
				break;
			}
		}
	}
	
	public class FlankingTSS {
		
		private String _ugenes;
		private String _dgenes;
		
		private String _ud;
		private String _dd;
		
		private String _us;
		private String _ds;
		
		public String getUpStreamTSS(){
			return _ugenes;
		}
		
		public String getDownStreamTSS(){
			return _dgenes;
		}
		
		public String getUpstreamDistance(){
			return _ud;
		}
		
		public String getDownstreamDistance(){
			return _dd;
		}
		
		public String getUpstreamStrand(){
			return _us;
		}
		
		public String getDownstreamStrand(){
			return _ds;
		}
		
		public FlankingTSS(TreeSet<String> ug, int ud, LinkedList<String> us, TreeSet<String> dg, int dd, LinkedList<String> ds){
			_ugenes = getGenes(ug);
			_dgenes = getGenes(dg);
			if(ud > -1){
				_ud = Integer.toString(ud);
			}
			else{
				_ud = "";
			}
			
			if(dd > -1){
				_dd = Integer.toString(dd);
			}
			else{
				_dd = "";
			}
			
			_us = getStrand(us);
			_ds = getStrand(ds);
			
		}
		
		private String getStrand(LinkedList<String> ds){
			int rsum = 0;
			int total = 0;
			for(Iterator<String> it = ds.iterator(); it.hasNext();){
				if(it.next().equals("+")){
					rsum++;
				}
				total++;
			}
			
			if(total == 0){
				return "0.5";
			}
			else{
				return Double.toString((double)rsum/total);
			}
		}
		
		private String getGenes(TreeSet<String> g){
			StringBuilder sb = new StringBuilder();
			Iterator<String> it = g.iterator();
			if(it.hasNext()){
				sb.append(it.next());
			}
			while(it.hasNext()){
				sb.append(","+it.next());
			}
			return sb.toString();
		}
	}
	
}
