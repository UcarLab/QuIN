package quin.filereader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextReader implements ChIAPETFileReader{

	private BufferedReader _br;
	
	public TextReader(String file, int skip) throws IOException{
		_br = new BufferedReader(new FileReader(file));
		for(int i = 0; i < skip && _br.ready(); i++){
			_br.readLine();
		}
	}
	
	public TextReader(InputStream istream) throws IOException{
		_br = new BufferedReader(new InputStreamReader(istream));
	}
	
	@Override
	public ChIAPETRead readLine() throws Exception {
		if(_br.ready()){
			String line = _br.readLine();
			String[] split = line.split("\\s");
			
			String lchr = split[0];
			int lstart = Integer.parseInt(split[1]);
			int lend = Integer.parseInt(split[2]);
			String rchr = split[3];
			int rstart = Integer.parseInt(split[4]);
			int rend = Integer.parseInt(split[5]);
			int count = Integer.parseInt(split[6]);
			
			//Make sure start < end
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
			
			if(rstart < lstart){
				return new ChIAPETRead(rchr, rstart, rend, lchr, lstart, lend, count);
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
