package apogen;

import apogen.Settings.CrawlingMode;

public class ApogenMain {

	/**
	 * Runs the Automatic Page Object Generator
	 * 
	 * @throws Exception
	 * 
	 * @throws JSONException
	 */
	public static void main(String args[]) throws Exception {
		displaySettings();
		run();
	}

	private static void displaySettings() {
		System.out.println("[LOG]\tINITIALIZING APOGEN (Automatic Page Object Generator)");
		System.out.println("[LOG]\tUrl: " + Settings.URL);
		System.out.println("[LOG]\tCrawling: " + Settings.CRAWLING);
		System.out.println("[LOG]\tClustering: " + Settings.CLUSTERING);
	}

	public static void run() throws Exception {

//		if (Settings.CRAWLING == CrawlingMode.SINGLE)
//			Crawler.runFirstCrawling();
//		else if (Settings.CRAWLING == CrawlingMode.DOUBLE)
//			Crawler.runDoubleCrawling();

		if (Settings.REPEAT_STATIC_ANALYSIS)
			StaticAnalyzer.start();
//
		if (Settings.GENERATE_CODE)
			CodeGenerator.run();

		System.exit(0);
	}

}
