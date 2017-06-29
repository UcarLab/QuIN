package quin.network.analysis;

import quin.network.Node;
import edu.ohsu.graphlet.core.BaseGraphletSignature;
import edu.ohsu.graphlet.core.GraphletCounter;

public class GraphletCounterHelper {

	GraphletCounter _gs;
	
	public GraphletCounterHelper(){
		_gs = new GraphletCounter();
	}
	
	public BaseGraphletSignature getGraphletSignature(Node n){
		return new BaseGraphletImpl(_gs.getGraphletSignature(n));
	}
	
	public BaseGraphletSignature[] getGraphletSignatures(Node[] n){
		BaseGraphletSignature[] rv = new BaseGraphletImpl[n.length];
		for(int i = 0; i < n.length; i++){
			rv[i] = getGraphletSignature(n[i]);
		}
		return rv;
	}
	
	private class BaseGraphletImpl extends BaseGraphletSignature {

		public BaseGraphletImpl(int[] counts){
			setCounts(counts);
		}
		
		@Override
		public String getId() {
			return null;
		}
		
	}
	
}
