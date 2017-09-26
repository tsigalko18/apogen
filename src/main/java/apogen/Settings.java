package apogen;

import java.io.File;

public class Settings {

	// Crawler Settings
	public static final int CRAWL_DEPTH = 0; /* 0 = unlimited. */
	public static final int MAX_STATES = 0; /* 0 = unlimited. */
	public static final int MAX_RUNTIME = 5; /* 5 minutes. */
	public static final int WAIT_TIME_AFTER_EVENT = 500;
	public static final int WAIT_TIME_AFTER_RELOAD = 500;
	public static final String URL = "http://www.disi.unige.it/person/StoccoA/testWebsite/";

	// Output Settings
	public static final String FILESEPARATOR = File.separator;
	public static final String GEN_PO_DIR = "po" + FILESEPARATOR;
	public static final String OUT_DIR = "output" + FILESEPARATOR;
	public static final String DOMS_DIR = OUT_DIR + "doms" + FILESEPARATOR;
	public static final String CLUST_DIR = OUT_DIR + "clusters" + FILESEPARATOR;
	public static final String BROWSER = "FF"; // PH-FF

	// Apogen Settings
	public static boolean CRAWLING = true;
	public static boolean CLUSTERING = true;
	public static boolean REPEAT_STATIC_ANALYSIS = true;
	public static boolean GENERATE_CODE = true;
	public static boolean USE_INPUT_SPECIFICATION = false;
	public static String NUMBER_OF_CLUSTERS = "2";

}
