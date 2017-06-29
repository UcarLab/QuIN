package db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class SQLConnectionFactory {

	// TODO make data source xml file
	private static DataSource _ds;
	public static Connection getConnection() {
		try {
			DataSource ds = getDataSource();
			Connection conn = ds.getConnection();
			if(conn.isClosed()){
				throw new SQLException();
			}
			return conn;
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Resetting.");
			_ds = null;
			try {
				DataSource ds = getDataSource();
				return ds.getConnection();
			} catch (NamingException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return null;
		}
		return null;
	}
	
	private static DataSource getDataSource() throws NamingException{
		if(_ds == null){
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			_ds = (DataSource) envContext.lookup("jdbc/chinqtdb");
		}
		return _ds;
	}

}
