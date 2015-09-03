package upkeep.clinvarimport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


public class TraitSNPParser extends DefaultHandler {
	
	private Map<String, Integer> _traits;
	private Map<Integer, List<Integer[]>> _snptraitmap;
	private ClinVar _current;
	private boolean _traitstart;
	private boolean _namestart;
	private String _content;
	private String _type;
	
	public void printTraits(){
		for(Iterator<String> it = _traits.keySet().iterator(); it.hasNext();){
			System.out.println(it.next());
		}
		System.out.println(_snptraitmap.size());
	}
	
	public void importTraits(Connection conn, String tablename) throws SQLException{
		boolean ac = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tablename+"_traits VALUES(?,?)");
		
		for(Iterator<Entry<String, Integer>> it = _traits.entrySet().iterator(); it.hasNext();){
			Entry<String, Integer> next = it.next();
			ps.setInt(1, next.getValue());
			ps.setString(2, next.getKey());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		
		conn.commit();
		conn.setAutoCommit(ac);
	}
	
	public void importMapping(Connection conn, String tablename) throws SQLException{
		boolean ac = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tablename+" VALUES(?,?,?)");
		
		for(Iterator<Entry<Integer, List<Integer[]>>> it = _snptraitmap.entrySet().iterator(); it.hasNext();){
			Entry<Integer, List<Integer[]>> next = it.next();
			Integer rsid = next.getKey();
			List<Integer[]> traits = next.getValue();

			TreeMap<Integer, TreeSet<Integer>> idused = new TreeMap<Integer, TreeSet<Integer>>();
			
			for(Iterator<Integer[]> it2 = traits.iterator(); it2.hasNext();){
				Integer[] traitid = it2.next();
				int id = traitid[0];
				int tid = traitid[1];
				
				if(!idused.containsKey(id) || !idused.get(id).contains(tid)){
					ps.setInt(1, id);
					ps.setInt(2, rsid);
					ps.setInt(3, tid);
					ps.addBatch();
					
					if(!idused.containsKey(id)){
						idused.put(id, new TreeSet<Integer>());
					}
					
					idused.get(id).add(tid);
				}
			}
		}
		ps.executeBatch();
		ps.close();
		
		conn.commit();
		conn.setAutoCommit(ac);
	}
	
	@Override
	public void startDocument(){
		_traits = new TreeMap<String, Integer>();
		_snptraitmap = new TreeMap<Integer, List<Integer[]>>();
		_traitstart = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		if(localName.equals("ClinVarSet")){
			startClinVarSet(attributes);
		}
		else if(localName.equals("XRef")){
			parseXRef(attributes);
		}
		else if(localName.equals("ElementValue")){
			startElementValue(attributes);
		}
		else if(localName.equals("TraitSet")){
			startTraitSet();
		}
		else if(localName.equals("Name")){
			startName();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		if(localName.equals("ClinVarSet")){
			endClinVarSet();
		}
		else if(localName.equals("ElementValue")){
			finishElementValue(_type);
		}
		else if(localName.equals("TraitSet")){
			endTraitSet();
		}
		else if(localName.equals("Name")){
			endName();
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		_content = new String(ch, start, length);
	}
	
	private void parseXRef(Attributes attr){
		String type = attr.getValue("Type");
		if(type != null &&type.equals("rs")){
			Integer rsid = Integer.parseInt(attr.getValue("ID").trim());
			_current.snps.add(rsid);
		}
	}
	
	private void startElementValue(Attributes attr){
		_type = attr.getValue("Type");
	}
	
	private void finishElementValue(String type){
		if(_traitstart && _namestart && type != null && type.equals("Preferred")){
			_current.traits.add(_content);
		}
	}
	
	private void startTraitSet(){
		_traitstart = true;
	}
	
	private void endTraitSet(){
		_traitstart = false;
	}
	
	private void startName(){
		_namestart = true;
	}
	
	private void endName(){
		_namestart = false;
	}
	
	private void startClinVarSet(Attributes attr){
		_current = new ClinVar();
		_current.id = Integer.parseInt(attr.getValue("ID").trim());
	}
	
	private void endClinVarSet(){
		TreeSet<String> traits = _current.traits;
		TreeSet<Integer> snps = _current.snps;
		Integer id = _current.id;
		
		if(id != null && traits.size() > 0 && snps.size() > 0){
			for(Iterator<String> it = traits.iterator(); it.hasNext();){
				String trait = it.next();
				if(!_traits.containsKey(trait)){
					_traits.put(trait, _traits.size()+1);
				}
				
				int traitid = _traits.get(trait);
				
				for(Iterator<Integer> it2 = snps.iterator(); it2.hasNext();){
					Integer snp = it2.next();
					
					if(!_snptraitmap.containsKey(snp)){
						_snptraitmap.put(snp, new LinkedList<Integer[]>());
					}
					
					_snptraitmap.get(snp).add(new Integer[]{id, traitid});
				}
			}
		}
	}

	private class ClinVar {
		public Integer id = null;
		public TreeSet<String> traits = new TreeSet<String>();
		public TreeSet<Integer> snps = new TreeSet<Integer>();
	}
	
}
