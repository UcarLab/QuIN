package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetSessionURL extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException{
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {		

		Cookie[] cookies = req.getCookies();
		Boolean fv = (cookies == null);
		
		String rv = "";
		if(!fv){
			Long uid = null;
			String phrase = null;
			for(int i = 0; i < cookies.length; i++){
				Cookie cc = cookies[i];
				String name = cc.getName();
				String value = cc.getValue();
				if(name.equals("uid")){
					try {
						uid = Long.parseLong(value);
					}
					catch(NumberFormatException e){ }
					cc.setMaxAge(0);
				}
				else if(name.equals("phrase")){
					phrase = value;
					cc.setMaxAge(0);
				}
			}
			if(uid != null && phrase != null){
				rv="/setSession.html?uid="+uid+"&phrase="+phrase;
			}
		}
		
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		out.print(rv);
		out.flush();
	}
	
}
