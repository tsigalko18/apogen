package apogen;

import java.io.IOException;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

import utils.UtilsCrawler;

public class Crawler {

	protected static String diff_dir;

	/**
	 * Initialize and Run the crawler
	 * 
	 * @throws Exception
	 */
	public static void crawl() throws Exception {

		CrawljaxConfigurationBuilder b = Crawler.initCrawljax();
		Crawler.runCrawljax(b);

	}

	/**
	 * initializes the properties and the crawler
	 * 
	 * @return CrawljaxConfigurationBuilder
	 * @throws Exception
	 */
	private static CrawljaxConfigurationBuilder initCrawljax() throws Exception {

		System.out.print("[LOG]\tCRAWLER SETUP...");

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(Settings.URL);

		try {
			UtilsCrawler.myCrawlRules(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("[LOG]\tCOMPLETED SUCCESSFULLY");

		return builder;
	}

	/**
	 * Run Crawljax with the configurations in input
	 * 
	 * @param CrawljaxConfigurationBuilder
	 *            builder
	 */
	private static void runCrawljax(CrawljaxConfigurationBuilder builder) {
		System.out.println("[LOG]\tSTARTED CRAWLING OF: " + Settings.URL);

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();

		System.out.println("[LOG]\tCRAWLING ENDED WITH STATUS: " + crawljax.getReason());
		System.out.println("[LOG]\tCRAWLING OF " + Settings.URL + " FINISHED");
		System.out.println("[LOG]\tCRAWLING RESULTS SAVED IN " + Settings.OUT_DIR);
	}

}
