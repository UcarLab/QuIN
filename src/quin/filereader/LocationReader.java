package quin.filereader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import quin.network.Location;

public class LocationReader{

	private BufferedReader _br;
	private int _id;
	
	public LocationReader(String file, int skip) throws IOException{
		_br = new BufferedReader(new FileReader(file));
		for(int i = 0; i < skip && _br.ready(); i++){
			_br.readLine();
		}
		_id = 0;
	}
	
	public LocationReader(InputStream istream) throws IOException{
		_br = new BufferedReader(new InputStreamReader(istream));
		_id = 0;
	}
	
	public Location[] getLocations() throws Exception{
		LinkedList<Location> l = new LinkedList<Location>();
		while(ready()){
			Location cl = readLine();
			if(cl != null){
				l.add(cl);
			}
		}
		close();
		return l.toArray(new Location[0]);
	}
	
	public Location readLine() throws Exception {
		if(_br.ready()){
			String line = _br.readLine();
			String[] split = line.split("\\s");
			
			String chr = split[0];
			int start = Integer.parseInt(split[1]);
			int end = Integer.parseInt(split[2]);
			
			//Make sure start < end
			if(start > end){
				int t = start;
				start = end;
				end = t;
			}
			
			if(start > end){
				int t = start;
				start = end;
				end = t;
			}
			
			return new Location(_id++, chr,start,end);
		}
		else{
			_br.close();
			return null;
		}
	}

	public boolean ready() throws IOException {
		return _br.ready();
	}

	public void close() throws IOException {
		_br.close();
	}

}
