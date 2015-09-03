package quin.web.networkjson;

public class Gene extends Location{
	
	private String _symbol;
	
	public Gene(int id, String symbol, String chr, int start, int end){
		super(id, chr, start, end);
		_symbol = symbol;
	}

	public Gene(int id, String symbol){
		super(id, "", 0, 0);
		_symbol = symbol;
	}
	

	public String getSymbol(){
		return _symbol;
	}
	
}
