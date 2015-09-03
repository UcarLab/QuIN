package upkeep.dbsnpimport;

public class Main {

	public static void main(String[] args){
		if(args.length == 3){
			ChrReportImport cri = new ChrReportImport();
			try {
				cri.importChrReport(args[0], args[1], args[2].equals("true"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
