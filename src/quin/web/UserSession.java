package quin.web;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserSession {

	public Long getUserId(HttpServletRequest req, HttpServletResponse resp, Connection conn) throws SQLException{
		
		Cookie[] cookies = req.getCookies();
		
		if(cookies== null){
			cookies = new Cookie[0];
		}
		
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
			Long iuid = uid;
			if(!updateLastUsed(conn, iuid, phrase)){
				String[] uinfo =  setAndGetUID(conn);
				uid = Long.parseLong(uinfo[0]);
				phrase = uinfo[1];
			}
		}
		else{
			String[] uinfo =  setAndGetUID(conn);
			uid = Long.parseLong(uinfo[0]);
			phrase = uinfo[1];
		}

		final int EXP = 604800;
		
		Cookie uidc = new Cookie("uid", uid.toString());
		uidc.setMaxAge(EXP);

		Cookie pc = new Cookie("phrase", phrase);
		pc.setMaxAge(EXP);
		
		resp.addCookie(uidc);
		resp.addCookie(pc);

		return uid;
	}
	
	private String[] setAndGetUID(Connection conn) throws SQLException{
		String sql = "INSERT INTO usersessions.Sessions (phrase,lastused) VALUES (?,?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		String phrase = getRandomString();
		ps.setString(1, phrase);
		ps.setDate(2, new Date(System.currentTimeMillis()));
		ps.execute();
		
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		long uid = rs.getInt(1);
		rs.close();
		
		ps.close();
		return new String[]{Long.toString(uid), phrase};
	}
	
	private boolean updateLastUsed(Connection conn, Long uid, String phrase) throws SQLException{
		String sql = "UPDATE usersessions.Sessions SET LASTUSED=? WHERE UID=? AND phrase=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setDate(1, new Date(System.currentTimeMillis()));
		ps.setLong(2, uid);
		ps.setString(3, phrase);
		int nupdates = ps.executeUpdate();
		ps.close();
		return (nupdates > 0);
	}
	
	private String getRandomString(){
		SecureRandom sr = new SecureRandom();
		return new BigInteger(130, sr).toString(32);
	}
	
	public boolean sessionExists(Connection conn, Long uid, String phrase) throws SQLException{
		String sql = "SELECT count(*) FROM usersessions.Sessions WHERE UID=? AND phrase=?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, uid);
		ps.setString(2, phrase);
		ResultSet rs = ps.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		rs.close();
		ps.close();
		return (count > 0);
	}
	
}
