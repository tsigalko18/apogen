package utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import apogen.Settings;
import apogen.Settings.Feature;
import clusterer.DomDistance;
import clusterer.TagFrequency;
import clusterer.UrlDistance;
import clusterer.WordFrequency;
import clusterer.DomDistance.Distance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/**
 * Classes representing the datasets for a given app
 * 
 * @author Andrea Stocco
 *
 */
public class UtilsDataset {

	String outputDirectory;
	static Map<String, LinkedHashMap<String, BigDecimal>> tagsFrequencyMatrix;
	static Map<String, LinkedHashMap<String, BigDecimal>> wordsBodyFrequencyMatrix;
	static Map<String, LinkedHashMap<String, BigDecimal>> wordsThalFrequencyMatrix;
	static Map<String, LinkedHashMap<String, BigDecimal>> urlsDistancesMatrix;
	static Map<String, LinkedHashMap<String, BigDecimal>> domsLevenshteinDistancesMatrix;
	static Map<String, LinkedHashMap<String, BigDecimal>> domsRobustTreeEditDistancesMatrix;
	static Dataset tagsFrequenciesDataset;
	static Dataset wordsBodyFrequenciesDataset;
	static Dataset wordsThalFrequenciesDataset;
	static Dataset urlsDistancesDataset;
	static Dataset domsLevenshteinDistancesDataset;
	static Dataset domsRobustTreeEditDistancesDataset;

	public UtilsDataset(String app) {
		outputDirectory = app;
		tagsFrequencyMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		wordsBodyFrequencyMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		wordsThalFrequencyMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		urlsDistancesMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		domsLevenshteinDistancesMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		domsRobustTreeEditDistancesMatrix = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		tagsFrequenciesDataset = new DefaultDataset();
		wordsBodyFrequenciesDataset = new DefaultDataset();
		wordsThalFrequenciesDataset = new DefaultDataset();
		urlsDistancesDataset = new DefaultDataset();
		domsLevenshteinDistancesDataset = new DefaultDataset();
		domsRobustTreeEditDistancesDataset = new DefaultDataset();
	}

	public void createDatasets() throws IOException {

		createClustersDir();

		if (Settings.FEATURE_VECTOR == Feature.DOM_RTED) {
			createDomsRTEDDistancesMatrix();
		} else if (Settings.FEATURE_VECTOR == Feature.DOM_LEVENSHTEIN) {
			createDomsLevenshteinDistancesMatrix();
		} else if (Settings.FEATURE_VECTOR == Feature.TAG_FREQUENCY) {
			createTagsFrequenciesMatrix();
		} else if (Settings.FEATURE_VECTOR == Feature.URL_LEVENSHTEIN) {
			createUrlsDistancesMatrix();
		} else {
			throw new InputMismatchException("[ERR]\tUtilsDataset@createDatasets: Unexpected dataset input");
		}
		// createWordsFrequenciesMatrix();
	}

	/**
	 * create cluster directory
	 * 
	 * @throws IOException
	 */
	private void createClustersDir() throws IOException {
		FileUtils.deleteDirectory(new File("output/clusters/"));
		try {
			FileUtils.forceMkdir(new File("output/clusters/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the tag frequencies matrix for app. Uses the tag master vector, i.e.,
	 * the vector of all tags that are present in at least one page
	 */
	public void createTagsFrequenciesMatrix() {

		if (!checkExistancy(0)) {

			TagFrequency tf = new TagFrequency(outputDirectory + "doms/");

			try {
				tf.run();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}

			tagsFrequencyMatrix = tf.getTagsFrequenciesMap();
			tagsFrequenciesDataset = tf.createDataset();
			exportDataset(0, tagsFrequenciesDataset);
		}

	}

	/**
	 * Creates the words frequencies matrix for app. Uses different classes for the
	 * textual content, i.e., title/heading/tables/lists/font/body/a
	 */
	public void createWordsFrequenciesMatrix() {

		if (!checkExistancy(1) || !checkExistancy(2)) {

			WordFrequency wf = new WordFrequency(outputDirectory + "doms/");

			try {
				wf.run();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}

			wordsBodyFrequencyMatrix = wf.getWordsBodyFrequenciesMap();
			wordsThalFrequencyMatrix = wf.getWordsThalFrequenciesMap();
			wordsBodyFrequenciesDataset = wf.createDatasetBody();
			wordsThalFrequenciesDataset = wf.createDatasetThal();
			exportDataset(1, wordsBodyFrequenciesDataset);
			exportDataset(2, wordsThalFrequenciesDataset);
		}
	}

	/**
	 * Creates the URLs distances matrix for app. Uses the Levenshtein distance
	 */
	public void createUrlsDistancesMatrix() {

		if (!checkExistancy(3)) {

			UrlDistance ud = new UrlDistance(outputDirectory);

			try {
				ud.init();
			} catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
				e.printStackTrace();
			}

			urlsDistancesMatrix = ud.getUrlDistancesMap();
			urlsDistancesDataset = ud.createDataset();
			exportDataset(3, urlsDistancesDataset);

		}
	}

	/**
	 * Creates the DOMs distances matrix for app. Uses the Levenshtein distance
	 */
	public void createDomsLevenshteinDistancesMatrix() {

		if (!checkExistancy(4)) {

			DomDistance dd_lev = new DomDistance(outputDirectory + "doms/", Distance.LEVENSHTEIN);

			try {
				dd_lev.init();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}

			domsLevenshteinDistancesMatrix = dd_lev.getDomDistancesMap();
			domsLevenshteinDistancesDataset = dd_lev.createDataset();
			exportDataset(4, domsLevenshteinDistancesDataset);
		}
	}

	/**
	 * Creates the DOMs distances matrix for app. Uses the Robust Tree Edit distance
	 */
	public void createDomsRTEDDistancesMatrix() {

		if (!checkExistancy(5)) {

			DomDistance dd_rted = new DomDistance(outputDirectory + "doms/", Distance.RTED);

			try {
				dd_rted.init();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}

			domsRobustTreeEditDistancesMatrix = dd_rted.getDomDistancesMap();
			domsRobustTreeEditDistancesDataset = dd_rted.createDataset();
			exportDataset(5, domsRobustTreeEditDistancesDataset);

		}
	}

	private boolean checkExistancy(int i) {

		String filename = "";

		switch (i) {
		case 0: // tag frequency
			filename = outputDirectory + "clusters/" + "app-tags-frequencies-matrix.csv";

		case 1: // word body frequency
			filename = outputDirectory + "clusters/" + "app-words-body-frequencies-matrix.csv";
			break;

		case 2: // word thal frequency
			filename = outputDirectory + "clusters/" + "app-words-thal-frequencies-matrix.csv";
			break;

		case 3: // url levenshtein distance
			filename = outputDirectory + "clusters/" + "app-url-levenshtein-distance-matrix.csv";
			break;

		case 4: // dom levenshtein distance
			filename = outputDirectory + "clusters/" + "app-page-levenshtein-distance-matrix.csv";
			break;

		case 5: // dom rted distance
			filename = outputDirectory + "clusters/" + "app-dom-rted-distance-matrix.csv";
			break;

		default:
			return false;
		}

		File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			return true;
		} else
			return false;

	}

	/**
	 * exports the given dataset in CSV and ARFF formats
	 * 
	 * @param i
	 * @param data
	 */
	public void exportDataset(int i, Dataset data) {

		String filename = "";

		switch (i) {
		case 0: // tag frequency
			filename = outputDirectory + "clusters/" + "app-tags-frequencies-matrix.csv";
			break;

		case 1: // word body frequency
			filename = outputDirectory + "clusters/" + "app-words-body-frequencies-matrix.csv";
			break;

		case 2: // word thal frequency
			filename = outputDirectory + "clusters/" + "app-words-thal-frequencies-matrix.csv";
			break;

		case 3: // url levenshtein distance
			filename = outputDirectory + "clusters/" + "app-url-levenshtein-distance-matrix.csv";
			break;

		case 4: // dom levenshtein distance
			filename = outputDirectory + "clusters/" + "app-page-levenshtein-distance-matrix.csv";
			break;

		case 5: // dom rted distance
			filename = outputDirectory + "clusters/" + "app-dom-rted-distance-matrix.csv";
			break;

		default:
			break;
		}

		// saves the CSV: for Java-ML
		try {
			File f = new File(filename);
			if (!f.exists() && !f.isDirectory()) {
				FileHandler.exportDataset(data, new File(filename), false, ",");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// converts to ARFF format: for WEKA
		try {
			convertCSVtoArff(filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void convertCSVtoArff(String filename) throws Exception {

		// load CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(filename));

		// CSV uses no header
		String[] options = new String[1];
		options[0] = "-H";
		loader.setOptions(options);

		Instances data = loader.getDataSet();

		// save ARFF
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);

		filename = filename.replace(".csv", ".arff");

		// saver.setDestination(new File(filename));
		saver.setFile(new File(filename));
		saver.writeBatch();

	}

	public static void cleanDoctype(String s) throws IOException {
		File fileToClean = new File(s);
		String pageCleaned = FileUtils.readFileToString(fileToClean);
		pageCleaned = pageCleaned.replaceAll("<!DOCTYPE((.|\n|\r)*?)\">", "");
		FileUtils.writeStringToFile(fileToClean, pageCleaned);
	}
}
