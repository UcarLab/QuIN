package upkeep.ucscimport;
import java.io.IOException;
import java.sql.SQLException;


public class RefFlatImportMain {

	public static void main(String[] args){
		
		if(args.length == 2){
			RefFlatImport rfi = new RefFlatImport();
			try {
				rfi.importFile(args[0], args[1]);
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
