package quin.web.networkjson;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Node {

	private Set<String> geneids;
	private Set<String> genenames;
	private Set<String> genesymbols;
	private Integer id;
	private String chr;
	private Integer start;
	private Integer end;
	private Integer petcount;
	private Integer interactioncount;
	
	private Integer degree;
	private Double closeness;
	private Double linsindex;
	private Double harmonic;
	private Double betweenness;
	
	private TreeMap<String, Integer> annotations;
	private String[][] finalannotations;
	
	public Node(){
		geneids = new TreeSet<String>();
		genenames = new TreeSet<String>();
		genesymbols = new TreeSet<String>();
		annotations = new TreeMap<String, Integer>();
	}
	
	public Set<String> getGeneIds(){
		return geneids;
	}
	
	public void addGeneId(String gn){
		geneids.add(gn);
	}
	
	public Set<String> getGeneNames(){
		return genenames;
	}
	
	public void addGeneName(String gn){
		genenames.add(gn);
	}
	
	public Set<String> getGeneSymbols(){
		return genesymbols;
	}
	
	public void addGeneSymbol(String gs){
		genesymbols.add(gs);
	}
	
	public Integer getId(){
		return id;
	}
	
	public void setId(Integer newid){
		id = newid;
	}
	
	public String getChr(){
		return chr;
	}
	
	public void setChr(String newchr){
		chr = newchr;
	}
	
	public Integer getStart(){
		return start;
	}
	
	public void setStart(Integer newstart){
		start = newstart;
	}
	
	public Integer getEnd(){
		return end;
	}
	
	public void setEnd(int newend){
		end = newend;
	}
	
	public Integer getPETCount(){
		return petcount;
	}
	
	public void setPETCount(Integer newpetcount){
		petcount = newpetcount;
	}
	
	public void setInteractionCount(Integer newinter){
		interactioncount = newinter;
	}
	
	public Integer getInteractionCount(){
		return interactioncount;
	}
	
	public Map<String, Integer> getAnnotations(){
		return annotations;
	}
	
	public void setAnnotations(TreeMap<String, Integer> annot){
		annotations = annot;
	}
	
	public void setDegree(Integer dc){
		degree = dc;
	}
	
	public Integer getDegree(){
		return degree;
	}
	
	public void setCloseness(Double cc){
		closeness = cc;
	}

	public Double getCloseness(){
		return closeness;
	}
	
	public void setLinsIndex(Double li){
		linsindex = li;
	}

	public Double getLinsIndex(){
		return linsindex;
	}
	
	public void setHarmonic(Double hc){
		harmonic = hc;
	}

	public Double getHarmonic(){
		return harmonic;
	}
	
	public void setBetweenness(Double bc){
		betweenness = bc;
	}

	public Double getBetweenness(){
		return betweenness;
	}
	
	public void addAnnotation(String id){
		Integer count = annotations.get(id);
		if(count == null){
			annotations.put(id, 1);
		}
		else{
			annotations.put(id, count+1);
		}

	}
	
	public void finalizeAnnotations(){
		Set<Entry<String, Integer>> s = annotations.entrySet();
		
		String[][] rv = new String[s.size()][2];
		Iterator<Entry<String,Integer>> it = s.iterator();
		for(int i = 0; i < rv.length; i++){
			Entry<String, Integer> e = it.next();
			rv[i][0] = e.getKey();
			rv[i][1] = Integer.toString(e.getValue());
		}
		
		finalannotations = rv;
	}
	
	public String[][] getFinalAnnotations(){
		return finalannotations;
	}
	
}
