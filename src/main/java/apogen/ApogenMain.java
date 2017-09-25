package apogen;

public class ApogenMain {

	/**
	 * Runs the Automatic Page Object Generator
	 * @throws Exception 
	 * 
	 * @throws JSONException 
	 */
	 public static void main(String args[]) throws Exception {
		 displaySettings();
		 new ApogenMain().run();
	 }
	
	public void run() throws Exception {
		
		if(Settings.CRAWLING)
			Crawler.crawl();
		
		if(Settings.REPEAT_STATIC_ANALYSIS)
			StaticAnalyzer.start();
	
		if(Settings.GENERATE_CODE)
			CodeGenerator.run();
	
		System.exit(0);
	}
	
	private static void displaySettings() {
		System.out.println("[LOG]\tINITIALIZING APOGEN (Automatic Page Object Generator)");
		System.out.println("[LOG]\tUrl: " + Settings.URL);
		System.out.println("[LOG]\tCrawling: " + Settings.CRAWLING);
		System.out.println("[LOG]\tClustering: " + Settings.CLUSTERING);
	}
    
}
