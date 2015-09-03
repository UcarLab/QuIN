package goanalysis.gohierarchy;

import java.io.IOException;

public class GOHierarchyHelper {

	private static volatile GOHierarchy _hierarchy = null;
	
	public static GOHierarchy getInstance(){
		if(_hierarchy == null){
			buildHierarchy();
		}
		return _hierarchy;
	}
	
	public static String[] getParentGOIds(String goid){
		return getInstance().getParentGOIds(goid);
	}
	
	private static void buildHierarchy(){
		try {
			_hierarchy = new GOHierarchy("go-basic.obo");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
