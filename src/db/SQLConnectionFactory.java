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
			return ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
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
