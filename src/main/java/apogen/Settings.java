package apogen;

import java.io.File;

public class Settings {

	// Crawler Settings
	public static final int CRAWL_DEPTH = 0; /* 0 = unlimited. */
	public static final int MAX_STATES = 0; /* 0 = unlimited. */
	public static final int MAX_RUNTIME = 1; /* 5 minutes. */
	public static final int WAIT_TIME_AFTER_EVENT = 500;
	public static final int WAIT_TIME_AFTER_RELOAD = 500;
	public static final String URL = "http://www.disi.unige.it/person/StoccoA/testWebsite/";

	// Output Settings
	public static final String FILESEPARATOR = File.separator;
	public static final String GEN_PO_DIR = "po" + FILESEPARATOR;
	public static final String OUT_DIR1 = "output1" + FILESEPARATOR;
	public static final String OUT_DIR2 = "output2" + FILESEPARATOR;
	public static final String DOMS_DIR1 = OUT_DIR1 + "doms" + FILESEPARATOR;
	public static final String DOMS_DIR2 = OUT_DIR2 + "doms" + FILESEPARATOR;
	public static final String FINAL_OUTPUT_DIR = "finalOutput" + FILESEPARATOR;
	public static final String CLUST_DIR = FINAL_OUTPUT_DIR + "clusters" + FILESEPARATOR;
	public static final String BROWSER = "PH"; // CH-FF-PH

	// Apogen Settings
	public static CrawlingMode CRAWLING = CrawlingMode.SINGLE;

	public static boolean CLUSTERING = false;
	public static ClusteringAlgorithm CLUSTERINGALGO = ClusteringAlgorithm.HIERARCHICAL;
	public static Feature FEATURE_VECTOR = Feature.DOM_RTED;

	public static boolean REPEAT_STATIC_ANALYSIS = true;
	public static boolean GENERATE_CODE = true;
	public static boolean USE_INPUT_SPECIFICATION = false;
	public static String NUMBER_OF_CLUSTERS = "2";

	public enum CrawlingMode {
		SINGLE, DOUBLE
	};

	public enum ClusteringAlgorithm {
		HIERARCHICAL, KMEANS
	}

	public enum Feature {
		DOM_RTED, DOM_LEVENSHTEIN, TAG_FREQUENCY, URL_LEVENSHTEIN
	}
}
