package quin.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import quin.filereader.ChIAPETRead;
import quin.network.Location;
import quin.network.Node;
import shortestpath.ShortestPath;
import shortestpath.ShortestPathAnalysis;

public class ExportMinHopFile {
	
	
	public void createMinHopFile(Connection conn, long fid, Integer[] indices, Integer[] tindex, String f1, String f2, String zf, int min, int max, boolean sp, boolean tp, String genome, int upstream, int downstream, ChIAPETRead[][] sedges, String[] sefiles) throws SQLException{
		ShortestPathAnalysis spa  = new ShortestPathAnalysis(conn, fid);
		
		TreeMap<Integer, Node> nodes = spa.getNodes();

		TreeMap<String, Location[]>[] m = getSortedSupportingEdges(sedges);
		
		File f = new File(f1);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			
			bw.write("Source Dataset\tSource Term\tSource Chr\tSource Start\tSource End\tSource Nearest TSS\tSource Nearest TSS Distance\tHop Count\tDistance\tTarget Dataset\tTarget Term\tTarget Chr\tTarget Start\tTarget End\tTarget Nearest TSS\tTarget Nearest TSS Distance\tAVG PET/Read Count\tMin PET/Read Count\tMax PET/Read Count\tAVG Interactions\tMin Interactions\tMax Interactions\tTotal Nodes In Component\tTotal Edges In Component\tPath\tComponent Id\tSupporting Interaction\n");
			
			ShortestPath[] paths = spa.getShortestPaths(conn, fid, indices, tindex, min, max, sp, tp, genome, upstream, downstream);
			for(int i = 0; i < paths.length; i++){
				ShortestPath path = paths[i];
				
				String si = getSupportingInteraction(nodes, path.getPath(), m, sefiles);
				bw.write(path.getDataset()+"\t"+path.getTerm()+"\t"+path.getTermChr()+"\t"+path.getTermStart()+"\t"+path.getTermEnd()+"\t"+path.getTermNearestTSS()+"\t"+path.getTermTSSDistance()+"\t"+path.getMinimumEdgesToTarget()+"\t"+path.getGenomicDistance()+"\t"+path.getTargetDataset()+"\t"+path.getTargetTerm()+"\t"+path.getTargetChr()+"\t"+path.getTargetStart()+"\t"+path.getTargetEnd()+"\t"+path.getTargetNearestTSS()+"\t"+path.getTargetTSSDistance()+"\t"+path.getAVGScore()+"\t"+path.getMinScore()+"\t"+path.getMaxScore()+"\t"+path.getAVGInteractions()+"\t"+path.getMinInteractions()+"\t"+path.getMaxInteractions()+"\t"+path.getComponentNodeCount()+"\t"+path.getComponentEdgeCount()+"\t"+path.getPath()+"\t"+path.getCCId()+"\t"+si+"\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Write the node file
		File nf = new File(f2);
		try {
			bw = new BufferedWriter(new FileWriter(nf));
			bw.write("Node Id\tChr\tStart\tEnd\n");

			for(Iterator<Node> it = nodes.values().iterator(); it.hasNext();){
				Node n = it.next();
				int id = n.getId();
				String chr = n.getChr();
				int start = n.getStart();
				int end = n.getEnd();
				bw.write(id+"\t"+chr+"\t"+start+"\t"+end+"\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zf));
			zipFile(zos, f1, "shortestpaths.txt");
			zipFile(zos, f2, "nodeidmap.txt");
			zos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		f.delete();
		nf.delete();
	}
	


	private void zipFile(ZipOutputStream zos, String f, String name) throws IOException{
		byte[] buf = new byte[1024];
		ZipEntry ze = new ZipEntry(name);
		zos.putNextEntry(ze);
		FileInputStream fin = new FileInputStream(f);
		int l;
		while((l = fin.read(buf)) > 0){
			zos.write(buf, 0, l);
		}
		fin.close();
		zos.closeEntry();
	}
	
	private TreeMap<String, Location[]>[] getSortedSupportingEdges(ChIAPETRead[][] sedges){
		@SuppressWarnings("unchecked")
		TreeMap<String, Location[]>[] rv = new TreeMap[sedges.length];
		for(int i = 0; i < sedges.length; i++){
			rv[i] = getSortedSupportingEdges(sedges[i]);
		}
		return rv;
	}
	
	private boolean isInteracting( TreeMap<String, Location[]> sedges, String schr, int ss, int se, String tchr, int ts, int te){
		//Need to check both here since we only have the left end of the supporting edge sorted
		SELocation[] sselocations = findSELocation(sedges, schr, ss, se);
		SELocation[] tselocations = findSELocation(sedges, tchr, ts, te);

		for(int i = 0; i < sselocations.length; i++){
			ChIAPETRead r = sselocations[i].getSE();
			if(tchr.equals(r.getRChr()) && te >= r.getRStart() && ts <= r.getREnd()){
				return true;
			}
		}
		
		for(int i = 0; i < tselocations.length; i++){
			ChIAPETRead r = tselocations[i].getSE();
			if(schr.equals(r.getRChr()) && se >= r.getRStart() && ss <= r.getREnd()){
				return true;
			}
		}
		
		return false;
	}
	
	private SELocation[] findSELocation(TreeMap<String, Location[]> sedges, String chr, int start, int end){
		Location[] chrloc = sedges.get(chr);
		LinkedList<SELocation> rv = new LinkedList<SELocation>();

		if(chrloc != null){
			int mid = chrloc.length/2;
			int s = 0;
			int e = chrloc.length-1;
			
			while(e-s > 2){
				if(chrloc[mid].getStart() > start){
					e = mid-1;
					mid = s+(e-s)/2;
				}
				else if(chrloc[mid].getStart() < start){
					s = mid+1;
					mid = s+(e-s)/2;
				}
				else{
					s = mid;
					break;
				}
			}
			
			for(int i = s; i < chrloc.length; i++){
				if(chrloc[i].getStart() <= end && chrloc[i].getEnd() >= start){
					rv.add((SELocation) chrloc[i]);
				}
				
				if(chrloc[i].getStart() > end){
					break;
				}
			}
			
			for(int i = s-1; i > -1; i--){
				if(chrloc[i].getStart() <= end && chrloc[i].getEnd() >= start){
					rv.add((SELocation) chrloc[i]);
				}
				
				if(start > chrloc[i].getEnd()){
					break;
				}
			}
		}
		
		return rv.toArray(new SELocation[0]);
	}
	
	private TreeMap<String, Location[]> getSortedSupportingEdges(ChIAPETRead[] sedges){
		quin.network.Util u = new quin.network.Util();
		SELocation[] selocations = new SELocation[sedges.length];
		for(int i = 0; i < sedges.length; i++){
			selocations[i] = new SELocation(i, sedges[i].getLChr(), sedges[i].getLStart(), sedges[i].getLEnd(), sedges[i]);	//Don't need to sort both ends since we only need to match to one endge initially, then check the other
		}
		return u.getChrStartSorted(selocations);
	}
	
	private class SELocation extends Location {

		private ChIAPETRead _se;
		
		public SELocation(int id, String chr, int start, int end, ChIAPETRead se) {
			super(id, chr, start, end);
			_se = se;
		}
		
		public ChIAPETRead getSE(){
			return _se;
		}
		
	}
	
	private String getSupportingInteraction(TreeMap<Integer, Node> nodes, String path, TreeMap<String, Location[]>[] m, String[] sefilenames) {
		String[] spath = path.split("\\|");
		if(spath.length > 1){
			int snodeid = Integer.parseInt(spath[0]);
			int tnodeid = Integer.parseInt(spath[spath.length-1]);
			
			Node snode = nodes.get(snodeid);
			Node tnode = nodes.get(tnodeid);
			
			String rv = "";

			for(int i = 0; i < m.length; i++){
				if(isInteracting(m[i], snode.getChr(), snode.getStart(), snode.getEnd(), tnode.getChr(), tnode.getStart(), tnode.getEnd())){
					rv += sefilenames[i]+"|";
				}
			}
			
			return rv;
		}
		
		return "";
	}
	
	
}
