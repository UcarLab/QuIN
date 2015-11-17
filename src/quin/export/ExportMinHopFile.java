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
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import quin.network.Node;
import shortestpath.ShortestPath;
import shortestpath.ShortestPathAnalysis;

public class ExportMinHopFile {
	
	
	public void createMinHopFile(Connection conn, long fid, Integer[] indices, Integer[] tindex, String f1, String f2, String zf, int min, int max, boolean sp, boolean tp, String genome, int upstream, int downstream) throws SQLException{
		ShortestPathAnalysis spa  = new ShortestPathAnalysis(conn, fid);
		
		TreeMap<Integer, Node> nodes = spa.getNodes();

		File f = new File(f1);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write("Source Dataset\tSource Term\tSource Chr\tSource Start\tSource End\tSource Nearest TSS\tSource Nearest TSS Distance\tHop Count\tDistance\tTarget Dataset\tTarget Term\tTarget Chr\tTarget Start\tTarget End\tTarget Nearest TSS\tTarget Nearest TSS Distance\tAVG PET/Read Count\tMin PET/Read Count\tMax PET/Read Count\tAVG Interactions\tMin Interactions\tMax Interactions\tTotal Nodes In Component\tTotal Edges In Component\tPath\n");
			
			ShortestPath[] paths = spa.getShortestPaths(conn, fid, indices, tindex, min, max, sp, tp, genome, upstream, downstream);
			for(int i = 0; i < paths.length; i++){
				ShortestPath path = paths[i];
				bw.write(path.getDataset()+"\t"+path.getTerm()+"\t"+path.getTermChr()+"\t"+path.getTermStart()+"\t"+path.getTermEnd()+"\t"+path.getTermNearestTSS()+"\t"+path.getTermTSSDistance()+"\t"+path.getMinimumEdgesToTarget()+"\t"+path.getGenomicDistance()+"\t"+path.getTargetDataset()+"\t"+path.getTargetTerm()+"\t"+path.getTargetChr()+"\t"+path.getTargetStart()+"\t"+path.getTargetEnd()+"\t"+path.getTargetNearestTSS()+"\t"+path.getTargetTSSDistance()+"\t"+path.getAVGScore()+"\t"+path.getMinScore()+"\t"+path.getMaxScore()+"\t"+path.getAVGInteractions()+"\t"+path.getMinInteractions()+"\t"+path.getMaxInteractions()+"\t"+path.getComponentNodeCount()+"\t"+path.getComponentEdgeCount()+"\t"+path.getPath()+"\n");
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
	
	
}
