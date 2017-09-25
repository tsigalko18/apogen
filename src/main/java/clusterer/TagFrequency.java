package clusterer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import utils.Clean;

public class TagFrequency {

	static List<String> tagsMasterVector;
	static Map<String, LinkedHashMap<String, BigDecimal>> tagsFrequenciesMap;
	static Dataset data;
	static String directory;

	/**
	 * creates an instance of TagFrequency class
	 * @param dir
	 */
	public TagFrequency(String dir){
		directory = dir;
		tagsMasterVector = new LinkedList<String>();
		tagsFrequenciesMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		data = new DefaultDataset();
	}
	
	/**
	 * runs the TagFrequency elaboration
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void run() throws ParserConfigurationException,
			SAXException, IOException {

		/*
		 * PREPROCESSING STEP: creates tags vector
		 */
		createTagsMasterVector();

		/*
		 * ELABORATION STEP: calculates tag frequency 
		 */
		
		Map<String, LinkedHashMap<String, BigDecimal>> tagsMap = calculateTagsFrequency();
		tagsFrequenciesMap = tagsMap;
		
		//printMap(tagsMap);
		
	}
	
	/**
	 * gets the Tags Frequencies map
	 * @return the tagsFrequenciesMap
	 */
	public Map<String, LinkedHashMap<String, BigDecimal>> getTagsFrequenciesMap() {
		return tagsFrequenciesMap;
	}
	
	/**
	 * prints the Tags Frequencies map
	 * @param allFeatures
	 */
	private static void printMap(
			Map<String, LinkedHashMap<String, BigDecimal>> allFeatures) {
		
		System.out.print("[LOG] keys: " + allFeatures.size() + ", values: ");
		
		for (String s : allFeatures.keySet()) {
			System.out.println(allFeatures.get(s).size());
			break;
		}
		
		for (String s : allFeatures.keySet()) {
			System.out.println(s);
			System.out.println("\t"+allFeatures.get(s));
		}
		
		
	}

	/**
	 * checks whether parameter string is numeric
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		try {
			//double d = Double.parseDouble(str);
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	/**
	 * calculates the Tags Frequencies
	 */
	private static Map<String, LinkedHashMap<String, BigDecimal>> calculateTagsFrequency()
			throws ParserConfigurationException, SAXException, IOException {

		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		LinkedHashMap<String, BigDecimal> tagsFrequencyMap = new LinkedHashMap<String, BigDecimal>();
		Map<String, LinkedHashMap<String, BigDecimal>> tagsMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();

		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		for (int i = 0; i < files.size(); i++) {

			String page = files.get(i).getName();
			
			//if(page.contains("tidy")){
				tagsFrequencyMap = new LinkedHashMap<String, BigDecimal>();
				
				//BigDecimal sum = new BigDecimal(0.0);

				for (String t : tagsMasterVector) {
					BigDecimal f = tagFrequency(page, t);
					tagsFrequencyMap.put(t, f);
					
					//sum = sum.add(f);
					
				}

//				System.out.format("sum is: " + sum);
//				System.out.println(String.format("%03.8f", sum));
				
				tagsMap.put(page, tagsFrequencyMap);
			//}
		}
		
		return tagsMap;

	}

	/**
	 * elaborates the tag frequency of tag t in the page page
	 * @param p
	 * @param t
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static BigDecimal tagFrequency(String p, String t)
			throws ParserConfigurationException, SAXException, IOException {

		Document d = createDocument(directory + p);

		NodeList nl = d.getElementsByTagName(t);

		double tagCardinality = nl.getLength();
		double total = countTags(d.getElementsByTagName("html").item(0));
		BigDecimal frequency = new BigDecimal(tagCardinality * 100 / total);

		return frequency;

	}

	/**
	 * creates the tag master vector, i.e.,
	 * the vector of all tags used in the web app
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static void createTagsMasterVector()
			throws ParserConfigurationException, SAXException, IOException {

		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);
		
		for (int i = 0; i < files.size(); i++) {
			String tempName = domsDirectory + files.get(i).getName();
			
			if(!tempName.contains("tidy")){
				FileInputStream inputstream = new FileInputStream(tempName);
				
				tempName = domsDirectory + files.get(i).getName().replace(".html", "-tidy.html");
			
				Path path = Paths.get(tempName);

				if (Files.notExists(path)) {
					FileOutputStream outputStream = new FileOutputStream(tempName);
					Clean.clean(inputstream, outputStream);
				}
			
			}

		}
		
		files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		for (int i = 0; i < files.size(); i++) {
			
			if(files.get(i).getName().contains("tidy")){
				
				//System.out.println("File: " + files.get(i).getName());
				
				//UtilsDataset.cleanDoctype(domsDirectory + files.get(i).getName());
				
				Document d = createDocument(domsDirectory + files.get(i).getName());
				
				tagScraper(d, d.getFirstChild(), tagsMasterVector);
			}

		}
		
		files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);
		
		for (int i = 0; i < files.size(); i++) {
			if(files.get(i).getName().contains("tidy")){
				Path source = Paths.get(domsDirectory + files.get(i).getName());
				Path target = Paths.get(domsDirectory + files.get(i).getName().replace("-tidy", ""));
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}

		System.out.print("[LOG] Tags Master Vector (" + tagsMasterVector.size() + " tags): ");
		System.out.println(tagsMasterVector);

	}

//	/**
//	 * tag scraper method
//	 * @param d
//	 * @param node
//	 * @param visited2
//	 */
//	private static void tagScraper(Document d, Node node, List<String> visited2) {
//			
//		if (node == null) {
//			return;
//		} else if(node.getNodeType() == Node.ELEMENT_NODE) {
//					
//			if (!visited2.contains(node.getNodeName())) {	
//				visited2.add(node.getNodeName());
//			}	
//			
//			NodeList nl = node.getChildNodes();
////			System.out.print("tag: " + node.getNodeName().toUpperCase());
////			System.out.print("\tchild nodes: " + node.getChildNodes());
////			System.out.println("\tnext sibling: " + node.getNextSibling());
//			
//			for (int i = 0; i < nl.getLength(); i++) {
//				tagScraper(d, nl.item(i), visited2);
//			}
//					
//		}
//
//	}
	
//	/**
//	 * scrape the tag
//	 * @param node
//	 * @param visited
//	 */
//	private static void tagScraper(Node node, List<String> visited) {
//		if (node == null) {
//			return;
//		}
//		else {//if (node.getNodeType() == 1) {		
//			if (!visited.contains(node.getNodeName())) {
//				visited.add(node.getNodeName());
//			}
//			
//			System.err.println(node.getNodeName() + "\t" + node.getNodeType());
//			
//			Node child = node.getFirstChild();
////			for (int i = 0; i < nl.getLength(); i++) {
////				tagScraper(d, nl.item(i), visited);
////			}
//			while (child != null) {
//				tagScraper(child, visited);
//	            child = child.getNextSibling();
//	        }
//		}
//	}
	
	/**
	 * scrape the textual content
	 * @param d
	 * @param node
	 * @param visited
	 */
	private static void tagScraper(Document d, Node node, List<String> visited) {

		if (node == null) {
			return;
		}
		else if (node.getNodeType() == Node.ELEMENT_NODE){
			
			//System.out.println("\tProcessing... " + node.getNodeName());
			
			if(!visited.contains(node.getNodeName())) {
				visited.add(node.getNodeName());
			}
			
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				tagScraper(d, nl.item(i), visited);
			}
		}

	}

	/**
	 * parses the HTML page in a Document object
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
		docFactory.setNamespaceAware(false);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(name);
		
		return doc;
		
		/* 
		 * Xerces-J 2.0.1 introduced a bug in the HTML DOM implementation 
		 * based on it. The bug causes the element nodes in the resultant 
		 * HTML document object to be of type org.apache.xerces.dom.ElementNSImpl
		 * instead of the appropriate HTML DOM element objects. The problem 
		 * affects NekoHTML as well
		 */ 
		//return utils.UtilsDiff.asDocument(name, false);
		
	}

	/**
	 * counts the tags cardinality of a node
	 * @param node
	 * @return
	 */
	private static int countTags(org.w3c.dom.Node node) {

		int numTags = 0;
		
		if (node == null) {
			return 0;
		} else {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				
				numTags++;
			
				for (int i = 0; i < node.getChildNodes().getLength(); i++) {
					numTags += countTags(node.getChildNodes().item(i));
				}
			}
			
		}
		return numTags;
		
	}

	/**
	 * exports the tags frequencies map in a 
	 * Java-ML Dataset 
	 * @return
	 */
	public Dataset createDataset() {

		for (String k : tagsFrequenciesMap.keySet()) {

			Collection<BigDecimal> v = tagsFrequenciesMap.get(k).values();
			double[] features = new double[v.size()];
			int count = 0;

			for (BigDecimal bd : v) {
				features[count] = bd.doubleValue();
				count++;
			}

			Instance instance = new DenseInstance(features, k);
			data.add(instance);

		}

		return data;

	}
	

}