package goanalysis;

public class GOJson {

	private String[] goids;
	private String[] terms;
	private int[] genesinterm;
	private int[] genesincomponent;
	private String[] expected;
	private String[] fisher;
	private String[][] go2gene;
	
	public GOJson(String[] gids, String[] t, int[] git, int[] gic, String[] e, String[] f) {
		goids = gids;
		terms = t;
		genesinterm = git;
		genesincomponent = gic;
		expected = e;
		fisher = f;
		go2gene = new String[gids.length][];
	}
	
	public String[] getGOIds(){
		return goids;
	}
	
	public String[] getTerms(){
		return terms;
	}
	
	public int[] getGenesInTerm(){
		return genesinterm;
	}
	
	public int[] getGenesInComponent(){
		return genesincomponent;
	}
	
	public String[] getExpected(){
		return expected;
	}
	
	public String[] getFisher(){
		return fisher;
	}
	
	public String[][] getGO2Gene(){
		return go2gene;
	}
	
	public void addGO2Gene(int index, String[] geneids){
		go2gene[index] = geneids;
	}
}
