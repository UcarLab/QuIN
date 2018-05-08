package upkeep.clinvarimport;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import db.SQLConnectionFactory;

public class ImportTraitSNP {

	
	public void run(String file, String tablename) throws ParserConfigurationException, SAXException, IOException, SQLException{
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    
		SAXParser saxp = spf.newSAXParser();
		XMLReader xmlReader = saxp.getXMLReader();
		TraitSNPParser snpparser = new TraitSNPParser();
		xmlReader.setContentHandler(snpparser);
		xmlReader.parse(convertToFileURL(file));
		
		Connection conn = SQLConnectionFactory.getConnection();
		try{
		TableCreator tc = new TableCreator();
		
		tc.createTables(conn, tablename, true);
	
		snpparser.importTraits(conn, tablename);
		snpparser.importMapping(conn, tablename);
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
	
}
