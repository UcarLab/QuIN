package quin.web.servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class ServletUtil {
	
	public String[] getFiles(HttpServletRequest req, String add, String prefix) throws IOException, IllegalStateException, ServletException{
		Collection<Part> parts = req.getParts();
		String path = System.getProperty("java.io.tmpdir");
		LinkedList<String> files = new LinkedList<String>();
		for(Iterator<Part> it = parts.iterator(); it.hasNext();){
			Part next = it.next();
			
			if(!next.getName().startsWith(prefix)){
				continue;
			}
			
			String file = path + File.separator + add+ getFileName(next);
			files.add(file);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			BufferedReader br = new BufferedReader(new InputStreamReader(next.getInputStream()));
				while(br.ready()){
					bw.write((br.readLine()));
					bw.newLine();
				}
			br.close();
			bw.flush();
			bw.close();
		}
		return files.toArray(new String[0]);
	}
	
	public String getFileName(final Part part) {
	    for (String content : part.getHeader("content-disposition").split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}
	
	public void setResponse(HttpServletResponse resp, String response) throws IOException{
		PrintWriter out = resp.getWriter();
		out.print(response);
		out.flush();
	}
}