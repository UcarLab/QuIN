package quin.filereader;

import java.io.IOException;

public interface ChIAPETFileReader {

	public ChIAPETRead readLine() throws Exception;
	
	public boolean ready() throws IOException;
	
	public void close() throws IOException;
	
}
