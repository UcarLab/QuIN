package upkeep;

public class DatabaseUpdater implements Runnable{

	
	//Update NCBI ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2refseq.gz, ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2go.gz.gz
	//Update UCSC ftp://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/refFlat.sql, ftp://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/refFlat.txt.gz
	//update GO obo http://purl.obolibrary.org/obo/go/go-basic.obo //TODO find  location to store this
	
	@Override
	public void run(){
		//updateGWAS(); test and this updates correctly
		//updateNCBIGene();
	}
	/*private void updateDbSNP(){
		//Update dbSNP ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b142_GRCh37p13/chr_rpts/  replace human9606_b142 with correct build
		System.out.println("Updating dbSNP.");
		File fdir = null;
		try {
			fdir = getFilesViaFTP("ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b142_GRCh37p13/chr_rpts", "dbsnp", "dir");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		String dir = fdir.getAbsolutePath();
		String tablename = "dbsnp.GRCh37p13";
		ChrReportImport dbi = new ChrReportImport();
		try {
			dbi.importChrReport(tablename, dir, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished updating dbSNP.");
	}
	
	private void updateClinvar(){
		//update clinvar ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/xml/ClinVarFullRelease_00-latest.xml.gz
		System.out.println("Updating ClinVar.");

		File f = null;
		try {
			f = getFileViaFTP("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/xml/ClinVarFullRelease_00-latest.xml.gz", "", "", "", "clinvar", ".xml");
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String file = f.getAbsolutePath();
		ImportTraitSNP ci = new ImportTraitSNP();
		try {
			String tablename = "clinvar.clinvar";
			ci.run(file, tablename);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Finished updating ClinVar.");

	}
	
	private void updateGWAS(){
		//update gwas http://www.genome.gov/admin/gwascatalog.txt
		System.out.println("Updating GWAS.");

		File f = null;
		try {
			f = getFileViaHTTP("http://www.genome.gov/admin/gwascatalog.txt", "gwas", ".txt");
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String file = f.getAbsolutePath();
		String tablename = "gwas.gwas2";
		ImportGWAS gi = new ImportGWAS();
		try {
			gi.importGWAS(file, tablename);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		f.delete();
		System.out.println("Finished updating GWAS.");

	}
	
	private void updateNCBIGene(){
		//NCBI ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2refseq.gz, ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2go.gz
		System.out.println("Updating NCBI Gene databases.");

		File f1 = null;
		File f2 = null;

		try {
			f1 = getFileViaFTP("ftp.ncbi.nlm.nih.gov", "/gene/DATA/gene2go.gz", "anonymous", "", "gene2go", ".gz");
			f2 = getFileViaFTP("ftp.ncbi.nlm.nih.gov", "gene/DATA/gene2refseq.gz", "anonymous", "", "gene2refseq", ".gz");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String file1 = f1.getAbsolutePath();
		String t1 = "ncbi.gene2go2";
		String file2 = f2.getAbsolutePath();
		String t2 = "ncbi.gene2refseq2";
		GeneGOImport ngi = new GeneGOImport();
		ngi.importNCBIGene(file1, t1, file2, t2);
		System.out.println("Finished updating NCBI gene databases.");

	}
	
	private File getFilesViaFTP(String urldirectory, String prefix, String suffix) throws IOException, URISyntaxException{
		FTPClient client = new FTPClient();
		
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File newdir = File.createTempFile(prefix, suffix, tmpdir);
		newdir.delete();
		newdir.mkdir();
		
		FTPFile[] files = client.listFiles(urldirectory);
		
		for(int i = 0; i < files.length; i++){
			//getFileViaFTP(files[i].getLink(), prefix, suffix+"_"+i, newdir.getAbsolutePath());
		}
	
		return newdir;
	}
	
	private File getFileViaFTP(String server, String file, String user, String pass, String prefix, String suffix) throws ClientProtocolException, URISyntaxException, IOException{
		return getFileViaFTP(server, file, user, pass, prefix, suffix, System.getProperty("java.io.tmpdir"));
	}
	
	private File getFileViaFTP(String server, String file, String user, String pass, String prefix, String suffix, String tmp) throws URISyntaxException, ClientProtocolException, IOException{
		FTPClient client = new FTPClient();
		
		File tmpdir = new File(tmp);
		File f = File.createTempFile(prefix, suffix, tmpdir);

		client.connect(server);
		client.login(user, pass);
    	System.out.println(client.getReplyString());

		client.setFileType(FTPClient.BINARY_FILE_TYPE);
		System.out.println("Retrieving File: "+server+file);

	    try{
	    	InputStream istream = client.retrieveFileStream(file);
	    	OutputStream os = new FileOutputStream(f);
	        IOUtils.copy(istream, os);
	    	os.flush();
	    	os.close();
	    	client.completePendingCommand();
	    	client.disconnect();
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
		System.out.println("File Saved: "+server+file);

	    return f;
	}
	
	private File getFileViaHTTP(String url, String prefix, String suffix) throws URISyntaxException, ClientProtocolException, IOException{
		CloseableHttpClient client = HttpClients.createDefault();
		URI uri = new URI(url);
		HttpGet get = new HttpGet(uri);
		CloseableHttpResponse response = client.execute(get);
		
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		File f = File.createTempFile(prefix, suffix, tmpdir);
		System.out.println("Retrieving File: "+url);
	    try{
	    	InputStream istream = response.getEntity().getContent();
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(f));
	    	BufferedReader br = new BufferedReader(new InputStreamReader(istream));
	    	while(br.ready()){
		    	bw.write(br.readLine()+"\n");
	    	}
	    	br.close();
	    	bw.flush();
	    	bw.close();
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    finally {
				response.close();
		    	client.close();
	    }
		System.out.println("File Saved: "+url);

	    
	    return f;
	}*/
	
}
