package nearestgene;

public class TSSGene {
	private String _gene;
	private int _distance;
	
	public TSSGene(String g, int d){
		_gene = g;
		_distance = d;
	}
	
	public String getGene(){
		return _gene;
	}
	
	public int getDistance(){
		return _distance;
	}
}
