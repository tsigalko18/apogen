package apogen;

public class ApogenMain {

	/**
	 * Runs the Automatic Page Object Generator
	 * @throws Exception 
	 * 
	 * @throws JSONException 
	 */
	 public static void main(String args[]) throws Exception {
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
    
}
