package upkeep.clinvarimport;
import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class ClinvarTraitImportMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImportTraitSNP its = new ImportTraitSNP();
		try {
			String tablename = "clinvar.clinvar";
			its.run(args[0], tablename);
		} catch (ParserConfigurationException | SAXException | IOException | SQLException e) {
			e.printStackTrace();
		}
	}

}
