package apogen;

import java.io.IOException;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

import utils.FirstCrawlConfigurationAndSpecification;
import utils.SecondCrawlConfigurationAndSpecification;

public class Crawler {

	protected static String diff_dir;

	/**
	 * perform one crawling only
	 * 
	 * @throws Exception
	 */
	public static void runFirstCrawling() throws Exception {
		firstCrawl();
	}

	/**
	 * perform two crawlings in series
	 * 
	 * @throws Exception
	 */
	public static void runDoubleCrawling() throws Exception {
		firstCrawl();
		secondCrawl();
	}

	/**
	 * init and run the first crawl
	 * 
	 * @throws Exception
	 */
	private static void firstCrawl() throws Exception {
		CrawljaxConfigurationBuilder b = Crawler.initFirstCrawl();
		Crawler.runCrawljax(b);
	}

	/**
	 * init and run the second crawl
	 * 
	 * @throws Exception
	 */
	private static void secondCrawl() throws Exception {
		CrawljaxConfigurationBuilder b = Crawler.initSecondCrawl();
		Crawler.runCrawljax(b);
	}

	/**
	 * initializes the properties and the crawler
	 * 
	 * @return CrawljaxConfigurationBuilder
	 * @throws Exception
	 */
	private static CrawljaxConfigurationBuilder initFirstCrawl() throws Exception {

		System.out.println("[LOG]\tFIRST CRAWL SETUP");

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(Settings.URL);

		try {
			FirstCrawlConfigurationAndSpecification.myCrawlRules(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("[LOG]\tFIRST CRAWL SETUP COMPLETED");

		return builder;
	}

	/**
	 * initializes the properties and the crawler
	 * 
	 * @return CrawljaxConfigurationBuilder
	 * @throws Exception
	 */
	private static CrawljaxConfigurationBuilder initSecondCrawl() throws Exception {

		System.out.println("[LOG]\tSECOND CRAWL SETUP");

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(Settings.URL);

		try {
			SecondCrawlConfigurationAndSpecification.myCrawlRules(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("[LOG]\tSECOND CRAWL SETUP COMPLETED");

		return builder;
	}

	/**
	 * Run Crawljax with the configurations give in input
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
	}

}
