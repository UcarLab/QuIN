package goanalysis.gohierarchy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

public class GOHierarchy {
	
	private TreeMap<String, Node<String>> _nodes;
	private TreeMap<String, String> _altidmapping;

	public GOHierarchy(String file) throws IOException{
		setNodes(file);
		setHierarchy(file);
	}
	
	public String[] getParentGOIds(String goid){
		goid = _altidmapping.get(goid);	//Use primary GO Id
		TreeSet<String> goids = new TreeSet<String>();
		Node<String> inode = _nodes.get(goid);
		if(inode != null){
			LinkedList<Node<String>> todo = new LinkedList<Node<String>>();
			todo.addLast(inode);
			while(!todo.isEmpty()){
				Node<String> currentnode = todo.removeFirst();
				goids.add(currentnode.getData());
				Node<String>[] parents = currentnode.getParents();
				for(int i = 0; i < parents.length; i++){
					todo.addLast(parents[i]);
				}
			}			
		}
		return goids.toArray(new String[0]);
	}
	
	private void setNodes(String file) throws IOException{
		_nodes = new TreeMap<String, Node<String>>();
		_altidmapping = new TreeMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(GOHierarchyHelper.class.getResourceAsStream(file)));
		while(br.ready()){
			String line = br.readLine().trim();
			if(line.equals("[Term]") && br.ready()){
				String nline = br.readLine();
				String[] split = nline.split("\\s");
				if(split[0].equals("id:")){
					String goid = split[1];
					_nodes.put(goid, new Node<String>(goid));
					_altidmapping.put(goid, goid);
				}
			}
		}
		br.close();
	}
	
	private void setHierarchy(String file) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(GOHierarchyHelper.class.getResourceAsStream(file)));
		while(br.ready()){
			String line = br.readLine().trim();
			if(line.equals("[Term]") && br.ready()){
				String nline = br.readLine();
				String[] split = nline.split("\\s");
				if(split[0].equals("id:")){
					String goid = split[1];
					Node<String> cnode = _nodes.get(goid);
					while(br.ready()){
						String termline = br.readLine();
						if(termline.trim().equals("")){
							break;
						}
						else{
							String[] split2 = termline.split("\\s");
							if(split2[0].equals("relationship:")){
								String pgoid = split2[2];
								Node<String> pnode = _nodes.get(pgoid);
								pnode.addChild(cnode);
								cnode.addParent(pnode);
							}
							else if(split2[0].equals("is_a:")){
								String pgoid = split2[1];
								Node<String> pnode = _nodes.get(pgoid);
								pnode.addChild(cnode);
								cnode.addParent(pnode);
							}
							else if(split2[0].equals("alt_id:")){
								String agoid = split2[1];
								_altidmapping.put(agoid, goid);
							}
						}
					}
				}
			}
		}
		br.close();
	}
	
}
