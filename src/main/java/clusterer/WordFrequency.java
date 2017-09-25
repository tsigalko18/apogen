package clusterer;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("unused")
public class WordFrequency {

	public enum WordsCategoryClasses {
		BODY, THAL
	}
	
	static List<String> stopWords;
	static List<String> stopKeywords;
	static List<String> textualContentFromTitle;
	static List<String> textualContentFromHeadings;
	static List<String> textualContentFromTables;
	static List<String> textualContentFromLists;
	static List<String> textualContentFromFont;
	static List<String> textualContentFromBody;
	static List<String> textualContentFromAnchors;
	static List<String> textualContentFromThal;
	static Map<String, LinkedHashMap<String, BigDecimal>> wordsBodyFrequenciesMap;
	static Map<String, LinkedHashMap<String, BigDecimal>> wordsThalFrequenciesMap;
	static Dataset dataBody;
	static Dataset dataThal;
	static String directory;

	public WordFrequency(String dir){
		directory = dir;
		stopWords = new LinkedList<String>();
		textualContentFromTitle = new LinkedList<String>();
		textualContentFromHeadings = new LinkedList<String>();
		textualContentFromTables = null;
		textualContentFromLists = new LinkedList<String>();
		textualContentFromFont = null;
		textualContentFromBody = new LinkedList<String>();
		textualContentFromAnchors = new LinkedList<String>();
		textualContentFromThal = new LinkedList<String>();
		wordsBodyFrequenciesMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		wordsThalFrequenciesMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		dataBody = new DefaultDataset();
		dataThal = new DefaultDataset();
	}

	/**
	 * run the words frequencies calculation
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void run() throws ParserConfigurationException,
			SAXException, IOException {
		
		// PREPROCESSING STEP: gets the stopwords from txt files
		getStopWords();

		// PREPROCESSING STEP: creates tags vector and texts vectors by class 
		extractTextualContentFromBody();
		extractTextualContentFromThal();
		
		//System.out.println(textualContentFromBody);

		// ELABORATION STEP: calculates word frequency maps
		wordsBodyFrequenciesMap = calculateWordsBodyFrequency();
		wordsThalFrequenciesMap = calculateWordsThalFrequency();
		
		System.out.println("[LOG] words body vector: " + textualContentFromBody.size() + " words");
		System.out.println("[LOG] words thal vector: " + textualContentFromThal.size() + " words");
		
	}

	/**
	 * get the WordsThal Frequencies map
	 * @return the wordsThalFrequenciesMap
	 */
	public Map<String, LinkedHashMap<String, BigDecimal>> getWordsThalFrequenciesMap() {
		return wordsThalFrequenciesMap;
	}
	
	/**
	 * get the WordsBody Frequencies map
	 * @return the wordsBodyFrequenciesMap
	 */
	public Map<String, LinkedHashMap<String, BigDecimal>> getWordsBodyFrequenciesMap() {
		return wordsBodyFrequenciesMap;
	}
	
	/**
	 * print out the Words Frequencies map
	 * @param map
	 */
	private static void printMap(
			Map<String, LinkedHashMap<String, BigDecimal>> map) {
		
		System.out.println("Words Vector");
		System.out.print("keys: " + map.size() + ", values: ");
		
		for (String s : map.keySet()) {
			System.out.println(map.get(s).size());
			break;
		}
		
		for (String s : map.keySet()) {
			System.out.println(s);
			System.out.println("\t"+map.get(s));
		}
		
		System.out.println();
		
	}

	/**
	 * extract textual content from <body>
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromBody() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("body", textualContentFromBody);
		
		System.out.println("Textual Content from Body\n" + textualContentFromBody);
		System.out.println("Size: " + textualContentFromBody.size()+"\n");
		
	}
	
	/**
	 * extract textual content from titles, headings, anchors, and lists
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromThal() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("title", textualContentFromThal);
		
		extractTextualContentFromTag("h1", textualContentFromThal);

		extractTextualContentFromTag("h2", textualContentFromThal);

		extractTextualContentFromTag("h3", textualContentFromThal);
		
		extractTextualContentFromTag("h4", textualContentFromThal);
		
		extractTextualContentFromTag("h5", textualContentFromThal);

		extractTextualContentFromTag("h6", textualContentFromThal);
		
		extractTextualContentFromTag("a", textualContentFromThal);
		
		extractTextualContentFromTag("li", textualContentFromThal);

		extractTextualContentFromTag("ol", textualContentFromThal);

		extractTextualContentFromTag("ul", textualContentFromThal);
		
		System.out.println("Textual Content from THAL\n" + textualContentFromThal);
		System.out.println("Size: " + textualContentFromThal.size()+"\n");
		
	}

	/**
	 * extract textual content from <a>
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromAnchors() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("a", textualContentFromAnchors);
	
		System.out.println("Textual Content from Anchors\n" + textualContentFromAnchors);
		System.out.println("Size: " + textualContentFromAnchors.size()+"\n");
		
	}
	
	/**
	 * extract textual content from <strong>, <b>, <i>, <u>
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromFont() throws ParserConfigurationException, SAXException, IOException {

		extractTextualContentFromTag("strong", textualContentFromFont);

		extractTextualContentFromTag("b", textualContentFromFont);

		extractTextualContentFromTag("i", textualContentFromFont);
		
		extractTextualContentFromTag("u", textualContentFromFont);
		
		//System.out.println("Textual Content from Tables STRONG-B-I-U\n" + textualContentFromFont);
		//System.out.println("Size: " + textualContentFromFont.size()+"\n");
	}

	/**
	 * extract textual content from lists
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromLists() throws ParserConfigurationException, SAXException, IOException {

		extractTextualContentFromTag("li", textualContentFromLists);

		extractTextualContentFromTag("ol", textualContentFromLists);

		extractTextualContentFromTag("ul", textualContentFromLists);
		
		//System.out.println("Textual Content from Tables LI-OL-UL\n" + textualContentFromLists);
		//System.out.println("Size: " + textualContentFromLists.size()+"\n");
	}

	/**
	 * extract textual content from tables
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromTables() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("table", textualContentFromTables);

		extractTextualContentFromTag("tr", textualContentFromTables);

		extractTextualContentFromTag("td", textualContentFromTables);
		
		extractTextualContentFromTag("th", textualContentFromTables);
		
//		System.out.println("Textual Content from Tables TABLE-TR-TD-TH\n"
//				+ textualContentFromTables);
//		System.out.println("Size: " + textualContentFromTables.size()+"\n");
		
	}

	/**
	 * extract textual content from titles
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromTitle() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("title", textualContentFromTitle);
		
//		System.out.println("Textual Content from Title\n"
//				+ textualContentFromTitle);
//		System.out.println("Size: " + textualContentFromTitle.size()+"\n");
		
	}

	/**
	 * extract textual content from headings
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromHeadings() throws ParserConfigurationException, SAXException, IOException {
		
		extractTextualContentFromTag("h1", textualContentFromHeadings);

		extractTextualContentFromTag("h2", textualContentFromHeadings);

		extractTextualContentFromTag("h3", textualContentFromHeadings);
		
		extractTextualContentFromTag("h4", textualContentFromHeadings);
		
		extractTextualContentFromTag("h5", textualContentFromHeadings);

		extractTextualContentFromTag("h6", textualContentFromHeadings);
		
//		System.out.println("Textual Content from Headings H1-H6\n"
//				+ textualContentFromHeadings);
//		System.out.println("Size: " + textualContentFromHeadings.size()+"\n");
		
	}

	/**
	 * reads the stop words
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void getStopWords() throws IOException {

		stopWords 		= FileUtils.readLines(new File("stopwords_en_lextex.txt"), "utf-8");
		stopKeywords  	= FileUtils.readLines(new File("stopwords_keywords.txt"), "utf-8");
	}

	/**
	 * extract textual content from a tag
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void extractTextualContentFromTag(String tag,
			List<String> output) throws ParserConfigurationException,
			SAXException, IOException {

		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		for (int i = 0; i < files.size(); i++) {

			Document d = createDocument(domsDirectory + files.get(i).getName());
			textScraper(d, d.getElementsByTagName(tag).item(0), output);
		
		}

	}

	/**
	 * scrape the textual content
	 * @param d
	 * @param node
	 * @param visited
	 */
	private static void textScraper(Document d, Node node, List<String> visited) {

		if (node == null) {
			return;
		} else if (node.getTextContent() == null || node.getTextContent().isEmpty()) {
			return;
		}
		else {
			
			//System.out.println("Processing... " + node.getNodeName());
			
			String a = processWord(node.getTextContent());
			
			String[] splittedText = a.split(" ");
						
			for (String s : splittedText) {
		
				//System.out.println("s: " + s);
				//s = processWord(s);
				//System.out.println("s processed: " + s);
				
				if(s.length() == 0/* || s.equals("\n") || s.equals("\t")*/){
					continue;
				}
				
				if (	!stopWords.contains(s) // s is NOT a stop word
						&& !containedInAnyStopKeywords(s) // s is NOT a keyword of any kind
						&& !visited.contains(s) // s is NOT already present
						&& !isNumeric(s) // s is NOT a numeric value
						//&& !(s.contains("\n") || s.contains("\t") || s.contains("\\s") || s.contains("\\w"))
					) {
					visited.add(s);
				}
			}
			
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				textScraper(d, nl.item(i), visited);
			}
		}

	}

	/**
	 * true if s contains a stop word
	 * @param s
	 * @return
	 */
	private static boolean containedInAnyStopKeywords(String s) {
		
		for (String k : stopKeywords) {
			if(s.contains(k)){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * clean the word x
	 * @param x
	 * @return
	 */
	private static String processWord(String x) {
		x = x.replace("[", " ");
		x = x.replace("]", " ");
		x = x.replace("-", " ");
		x = x.replace("\n", " ");
		x = x.replace("\t", " ");
		x = x.replaceAll("\\s+", " ");
		x = x.replaceAll("\\W", " ");
		return x.replaceAll("[(){}|*,#'$\".:;!?<>%]", " ").toLowerCase();
	}
	
	/**
	 * clean the body content
	 * @param x
	 * @return
	 */
	private static String processPageContent(String x) {		
		x = x.replace("[", " ");
		x = x.replace("]", " ");
		x = x.replace("-", " ");
		x = x.replace("\n", " ");
		x = x.replace("\t", " ");
		x = x.replaceAll("\\s+", " ");
		return x.replaceAll("[(){}|,#'$\".:;!?<>%]", " ").toLowerCase();
	}

	/**
	 * check whether s is a number
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String s) {
		try {
			double d = Double.parseDouble(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * calculate <body> words frequencies
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Map<String, LinkedHashMap<String, BigDecimal>> calculateWordsBodyFrequency() throws ParserConfigurationException, SAXException, IOException {
		
		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		LinkedHashMap<String, BigDecimal> wordsFrequencyMap = new LinkedHashMap<String, BigDecimal>();
		Map<String, LinkedHashMap<String, BigDecimal>> wordsMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();

		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		for (int i = 0; i < files.size(); i++) {

			String page = files.get(i).getName();
			wordsFrequencyMap = new LinkedHashMap<String, BigDecimal>();
			
			//BigDecimal sum = new BigDecimal(0.0);

			for (String t : textualContentFromBody) {
				BigDecimal f = wordFrequency(page, t);
				wordsFrequencyMap.put(t, f);
				//sum = sum.add(f);
			}
			//System.out.println(String.format("%03.8f", sum));
			wordsMap.put(page, wordsFrequencyMap);

		}
		
		return wordsMap;
		
	}
	
	/**
	 * calculate <title> <headings> <a> <lists> words frequencies
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Map<String, LinkedHashMap<String, BigDecimal>> calculateWordsThalFrequency() throws ParserConfigurationException, SAXException, IOException {
		
		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		LinkedHashMap<String, BigDecimal> wordsFrequencyMap = new LinkedHashMap<String, BigDecimal>();
		Map<String, LinkedHashMap<String, BigDecimal>> wordsMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();

		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		for (int i = 0; i < files.size(); i++) {

			String page = files.get(i).getName();
			wordsFrequencyMap = new LinkedHashMap<String, BigDecimal>();

			for (String t : textualContentFromThal) {
				BigDecimal f = wordFrequency(page, t);
				wordsFrequencyMap.put(t, f);
			}

			wordsMap.put(page, wordsFrequencyMap);

		}
		
		return wordsMap;
		
	}
	
	/**
	 * calculate the frequency of word in page
	 */
	private static BigDecimal wordFrequency(String page, String word)
			throws ParserConfigurationException, SAXException, IOException {

		Document d = createDocument(directory + page);
		
		Node body = d.getElementsByTagName("html").item(0);
		String fullText = body.getTextContent();
		fullText = processPageContent(fullText);
		
		double wordCardinality = StringUtils.countMatches(fullText, word);
		double total = fullText.split(" ").length;
		BigDecimal frequency = new BigDecimal(wordCardinality * 100 / total);

		return frequency;
	}

	/**
	 * auxiliary function
	 * @param name
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document createDocument(String name)
			throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(name);

		return doc;
	}

	/**
	 * create the dataset for body frequencies
	 * @return
	 */
	public Dataset createDatasetBody() {

		for (String k : wordsBodyFrequenciesMap.keySet()) {

			Collection<BigDecimal> v = wordsBodyFrequenciesMap.get(k).values();
			double[] features = new double[v.size()];
			int count = 0;

			for (BigDecimal bd : v) {
				features[count] = bd.doubleValue();
				count++;
			}

			Instance instance = new DenseInstance(features, k);
			dataBody.add(instance);

		}

		return dataBody;

	}
	
	/**
	 * create the dataset for thal frequencies
	 * @return
	 */
	public Dataset createDatasetThal() {

		for (String k : wordsThalFrequenciesMap.keySet()) {

			Collection<BigDecimal> v = wordsThalFrequenciesMap.get(k).values();
			double[] features = new double[v.size()];
			int count = 0;

			for (BigDecimal bd : v) {
				features[count] = bd.doubleValue();
				count++;
			}

			Instance instance = new DenseInstance(features, k);
			dataThal.add(instance);

		}

		return dataThal;

	}
	

}