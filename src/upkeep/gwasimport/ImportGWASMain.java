package upkeep.gwasimport;

import java.io.IOException;
import java.sql.SQLException;

public class ImportGWASMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImportGWAS igwas = new ImportGWAS();
		try {
			String tablename = "gwas.gwas";

			igwas.importGWAS(args[0], tablename);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

}
