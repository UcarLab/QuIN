package nearestgene;

public class TSS {
	public String genename;
	public String chr;
	public int tss;
	public String strand;
	
	public TSS(String g, String c, int t, String str){
		chr = c;
		genename = g;
		tss = t;
		strand = str;
	}
}