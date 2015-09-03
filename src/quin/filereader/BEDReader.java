package quin.filereader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BEDReader implements ChIAPETFileReader{
	
	private BufferedReader _br;
	
	public BEDReader(String file, int skip) throws IOException{
		_br = new BufferedReader(new FileReader(file));
		for(int i = 0; i < skip && _br.ready(); i++){
			_br.readLine();
		}
	}
	
	public BEDReader(InputStream istream) throws IOException{
		_br = new BufferedReader(new InputStreamReader(istream));
	}

	@Override
	public ChIAPETRead readLine() throws Exception {
		if(_br.ready()){
			String line = _br.readLine();
			String[] split = line.split("\\s");
			
			String name = split[3];
			
			int start, end;
			
			start = 0;
			end = name.indexOf(":", start);
			String lchr = name.substring(start, end);
			
			start = end+1;
			end = name.indexOf("..", start);
			int lstart = Integer.parseInt(name.substring(start, end));
			
			start = end+2;
			end = name.indexOf("-", start);
			int lend = Integer.parseInt(name.substring(start, end));
			
			start = end+1;
			end = name.indexOf(":", start);
			String rchr = name.substring(start, end);

			start = end+1;
			end = name.indexOf("..", start);
			int rstart = Integer.parseInt(name.substring(start, end));
			
			start = end+2;
			end = name.indexOf(",", start);
			int rend = Integer.parseInt(name.substring(start, end));
			
			
			start = end+1;
			end = name.length();
			int count = Integer.parseInt(name.substring(start, end));
			
			
			//Make sure that the starts are always less than the ends
			if(lstart > lend){
				int t = lstart;
				lstart = lend;
				lend = t;
			}
			
			if(rstart > rend){
				int t = rstart;
				rstart = rend;
				rend = t;
			}
			
			if(!lchr.equals(rchr)){
				_br.readLine(); //For interchromosome interactions, BED files have duplicate entries. inflating the PET Count
			}
			
			return new ChIAPETRead(lchr, lstart, lend, rchr, rstart, rend, count);			
		}
		else{
			_br.close();
			return null;
		}
	}

	@Override
	public boolean ready() throws IOException {
		return _br.ready();
	}

	@Override
	public void close() throws IOException {
		_br.close();
	}

}
