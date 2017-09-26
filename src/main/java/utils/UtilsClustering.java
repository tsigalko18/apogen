package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Difference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import treeEdit.LblTree;
import treeEdit.RTED_InfoTree_Opt;
import abstractdt.Cluster;
import abstractdt.Getter;

import com.crawljax.util.DomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UtilsClustering {

	/**
	 * read cluster of web pages from a file having the format:
	 * <li>0;page1;page2</li>
	 * <li>1;page3;page4</li>
	 * <li>2;page5;page6</li>
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, LinkedList<String>> readClustersFromFile(String filename) throws IOException {

		LinkedHashMap<Integer, LinkedList<String>> matrix = new LinkedHashMap<Integer, LinkedList<String>>();

		try {
			List<String> lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
			for (String line : lines) {
				String[] temp = line.split(";");

				LinkedList<String> list = new LinkedList<String>();

				for (int i = 1; i < temp.length; i++) {
					list.add(temp[i]);
				}

				matrix.put(new Integer(temp[0]), list);

			}
		} catch (IOException e) {
			System.out.println("[ERR]\tError while reading clusters file: " + filename);
		}

		return matrix;

	}

	/**
	 * This method implements the Levenshtein Distance algorithm between two strings
	 * 
	 * @param s0
	 *            first string to be compared
	 * @param s1
	 *            second string to be compared
	 * @return the cost to turn s0 into s1
	 */
	public static int levenshteinDistance(String s0, String s1) {
		int len0 = s0.length() + 1;
		int len1 = s1.length() + 1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];

		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i;

		// dynamically computing the array of distances

		// transformation cost for each letter in s1
		for (int j = 1; j < len1; j++) {
			// initial cost of skipping prefix in String s1
			newcost[0] = j;

			// transformation cost for each letter in s0
			for (int i = 1; i < len0; i++) {
				// matching current letters in both strings
				int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

				// computing cost for each transformation
				int cost_replace = cost[i - 1] + match;
				int cost_insert = cost[i] + 1;
				int cost_delete = newcost[i - 1] + 1;

				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}

			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newcost;
			newcost = swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}

	/**
	 * This method implements the equality check between two strings using the
	 * Levenshtein Distance algorithm
	 * 
	 * @param threshold
	 * @param dom1
	 * @param dom2
	 * @return true/false
	 */
	public static boolean levenshteinEquals(double threshold, String dom1, String dom2) {

		double l = levenshteinDistance(dom1, dom2);

		if (l >= ((dom1.length() + dom2.length()) * (1.0 - threshold))) {

			return false;
		}

		return true;
	}

	/**
	 * This method implements the equality check between two strings using the
	 * Levenshtein Distance algorithm
	 * 
	 * @param threshold
	 * @param dom1
	 * @param dom2
	 * @return true/false
	 * @throws IOException
	 */
	public static boolean similarAccordingToDomDiversity(double threshold, String dom1, String dom2)
			throws IOException {

		double l = getDomDiversity(dom1, dom2);

		if (l >= threshold) {

			return false;
		}

		return true;
	}

	/**
	 * Get a scalar value for the Dom Diversity using the Robust Tree Edit Distance
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 * @throws IOException
	 */
	public static double getDomDiversity(String dom1, String dom2) throws IOException {

		LblTree domTree1 = getDomTree(dom1);
		LblTree domTree2 = getDomTree(dom2);

		double DD = 0.0;
		RTED_InfoTree_Opt rted;
		double ted;

		rted = new RTED_InfoTree_Opt(1, 1, 1);

		// compute tree edit distance
		rted.init(domTree1, domTree2);

		int maxSize = Math.max(domTree1.getNodeCount(), domTree2.getNodeCount());

		rted.computeOptimalStrategy();
		ted = rted.nonNormalizedTreeDist();
		ted /= (double) maxSize;

		DD = ted;
		return DD;
	}

	/**
	 * Get a scalar value for the Dom Diversity using the Robust Tree Edit Distance
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 * @throws IOException
	 */
	public static double robustTreeEditDistance(String dom1, String dom2) throws IOException {

		LblTree domTree1 = getDomTree(dom1);
		LblTree domTree2 = getDomTree(dom2);

		double DD = 0.0;
		RTED_InfoTree_Opt rted;
		double ted;

		rted = new RTED_InfoTree_Opt(1, 1, 1);

		// compute tree edit distance
		rted.init(domTree1, domTree2);

		rted.computeOptimalStrategy();
		ted = rted.nonNormalizedTreeDist();

		DD = ted;
		return DD;
	}

	private static LblTree getDomTree(String dom1) throws IOException {

		org.w3c.dom.Document doc1 = DomUtils.asDocument(dom1);

		LblTree domTree = null;

		DocumentTraversal traversal = (DocumentTraversal) doc1;
		TreeWalker walker = traversal.createTreeWalker(doc1.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
		domTree = createTree(walker);

		return domTree;
	}

	/**
	 * Recursively construct a LblTree from DOM tree
	 *
	 * @param walker
	 *            tree walker for DOM tree traversal
	 * @return tree represented by DOM tree
	 */
	public static LblTree createTree(TreeWalker walker) {
		Node parent = walker.getCurrentNode();
		LblTree node = new LblTree(((Element) parent).getNodeName(), -1); // treeID = -1
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			node.add(createTree(walker));
		}
		walker.setCurrentNode(parent);
		return node;
	}

	/**
	 * creates a copy of the Crawljax report to be later modified with the merging
	 * information
	 * 
	 * @throws IOException
	 */
	public static void createCrawljaxResultCopy() throws IOException {
		File source = new File("output/result.json");
		File dest = new File("output/resultAfterMerging.json");
		FileUtils.copyFile(source, dest);
	}

	/**
	 * reads the clustering result and calculates the diffs The output is a
	 * cluster.json file inside output/ directory
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static void readClusteringResult(String res) throws IOException, ParserConfigurationException, SAXException {

		try {

			List<Cluster> clusters = new LinkedList<Cluster>();

			List<String> lines = Files.readAllLines(Paths.get(res), Charset.defaultCharset());

			for (String line : lines) {
				String[] temp = line.split(";");

				LinkedList<String> list = new LinkedList<String>();
				List<String> slavesList = new LinkedList<String>();
				List<abstractdt.Getter> diffsForGettersGeneration = new LinkedList<abstractdt.Getter>();

				for (int i = 1; i < temp.length; i++) {
					list.add(temp[i]);
				}

				// now the state with the lowest ID is the Master!
				// CONSIDER REVERSING THE LIST (take the last as Master)
				// CONSIDER ORDERING THE LIST BY DOM SIZE
				// if(!list.contains("state3.html")){
				// Collections.sort(list, new NaturalOrderComparator());
				// }
				// else{
				// Collections.sort(list, new NaturalOrderComparator());
				// Collections.reverse(list);
				// }

				Collections.sort(list, new NaturalOrderComparator());

				String domMaster = UtilsStaticAnalyzer.getDOMFromDirectory(list.get(0).replace(".html", ""));

				for (int i = 1; i < list.size(); i++) {

					slavesList.add(list.get(i).replace(".html", ""));

					String source = list.get(0).replace(".html", "");
					String target = list.get(i).replace(".html", "");

					System.out.println("[LOG]\tCalculating diff between: " + source + " and " + target);

					String domSlave = UtilsStaticAnalyzer.getDOMFromDirectory(list.get(i).replace(".html", ""));
					List<Difference> diffList = UtilsDiff.customisedDomDiff(domMaster, domSlave);

					List<abstractdt.Getter> customDiffList = UtilsDiff.printsDiff(source, target, diffList);

					for (abstractdt.Getter diff : customDiffList) {
						if (!diffsForGettersGeneration.contains(diff)) {
							diffsForGettersGeneration.add(diff);
						}
					}

				}

				if (!slavesList.isEmpty()) {
					Cluster c = new Cluster(list.get(0).replace(".html", ""), slavesList, diffsForGettersGeneration);
					clusters.add(c);
				} else {
					// creo un cluster con una sola pagina
					Cluster c = new Cluster(list.get(0).replace(".html", ""), new LinkedList<String>(),
							new LinkedList<abstractdt.Getter>());
					clusters.add(c);
				}

			}

			FileWriter file = new FileWriter("output/cluster.json");

			writeClusterFile(file, clusters);

			file.flush();
			file.close();

		} catch (IOException e) {
			throw new IOException("Error reading file output/clusteringResult.txt");
		}

	}

	/**
	 * Write clusters to as a JSON file
	 * 
	 * @param file
	 * @param clusters
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static void writeClusterFile(FileWriter file, List<Cluster> clusters) throws IOException {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("clusters", clusters);

		ObjectMapper mapper = new ObjectMapper();

		file.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject));

	}

	/**
	 * Modifies the Crawljax output accordingly to cluster information
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static void modifyCrawljaxResultAccordingToClusters()
			throws FileNotFoundException, IOException, ParseException {

		// open file
		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader("output/resultAfterMerging.json"));
		JSONObject jsonResults = (JSONObject) obj;

		obj = parser.parse(new FileReader("output/cluster.json"));
		JSONObject jsonClusters = (JSONObject) obj;

		/*
		 * TODO: foreach read slaves states from cluster.json remove it from
		 * resultAfterMerging.json and remove/modify the related edges
		 */
		JSONObject allStates = (JSONObject) jsonResults.get("states");
		JSONArray allEdges = (JSONArray) jsonResults.get("edges");
		JSONArray allClusters = (JSONArray) jsonClusters.get("clusters");

		System.out.println("[INFO] #edges before merging: " + allEdges.size());

		allEdges = removeIntraClusterEdges(allEdges, allClusters);

		allEdges = modifyInterClusterEdges(allEdges, allClusters);

		System.out.println("[INFO] #edges after merging: " + allEdges.size());

		// System.out.println("[INFO] #states before merging: " + allStates.size());

		// allStates = removeStatesFromResultFile(allStates, allClusters);

		// System.out.println("[INFO] #states after merging: " + allStates.size());

		JSONObject resultAfterMerging = new JSONObject();
		resultAfterMerging.put("states", allStates);
		resultAfterMerging.put("edges", allEdges);

		ObjectMapper mapper = new ObjectMapper();

		FileUtils.writeStringToFile(new File("output/resultAfterMerging.json"),
				mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultAfterMerging));

	}

	@SuppressWarnings("unchecked")
	private static JSONArray removeIntraClusterEdges(JSONArray allEdges, JSONArray allClusters) {

		List<JSONObject> listOfEdgesToDelete = new LinkedList<JSONObject>();

		for (int i = 0; i < allClusters.size(); i++) {

			JSONObject jsonGeneralData = new JSONObject();
			jsonGeneralData = (JSONObject) allClusters.get(i);

			List<String> intraClusterSlavesPages = (List<String>) jsonGeneralData.get("slaves");
			intraClusterSlavesPages.add((String) jsonGeneralData.get("master"));

			for (int j = 0; j < intraClusterSlavesPages.size(); j++) {

				for (int x = 0; x < allEdges.size(); x++) {

					JSONObject edge = (JSONObject) allEdges.get(x);

					String from = (String) edge.get("from");
					String to = (String) edge.get("to");

					String page = intraClusterSlavesPages.get(j);

					if (page.equals(from) && intraClusterSlavesPages.contains(to)) {
						listOfEdgesToDelete.add(edge);
					}

				}

			}

			allEdges.removeAll(listOfEdgesToDelete);

		}

		return allEdges;
	}

	@SuppressWarnings("unchecked")
	private static JSONArray modifyInterClusterEdges(JSONArray allEdges, JSONArray allClusters) {

		for (int x = 0; x < allEdges.size(); x++) {

			JSONObject edge = (JSONObject) allEdges.get(x);
			String from = (String) edge.get("from");
			String to = (String) edge.get("to");

			/*
			 * M -> M' M -> S' S -> M' S -> S'
			 */
			if (isMaster(from, allClusters) && isMaster(to, allClusters)) {
				// M -> M'
				System.err.println(from + "-> " + to);
				System.err.println("do nothing");
			} else if (isMaster(from, allClusters) && !isMaster(to, allClusters)) {
				// M -> S'
				System.err.println(from + "-> " + to);
				String theMaster = getMaster(to, allClusters);
				System.err.println("get " + to + " master: " + theMaster);

				edge.put("to", theMaster);
			} else if (!isMaster(from, allClusters) && isMaster(to, allClusters)) {
				// S -> M'
				System.err.println(from + "-> " + to);
				String theMaster = getMaster(from, allClusters);
				System.err.println("get " + from + " master: " + theMaster);

				edge.put("from", theMaster);
			} else if (!isMaster(from, allClusters) && !isMaster(to, allClusters)) {
				// S -> S'
				System.err.println(from + "-> " + to);

				String theMaster_of_from = getMaster(from, allClusters);
				System.err.println("get " + from + " master: " + theMaster_of_from);

				String theMaster_of_to = getMaster(to, allClusters);
				System.err.println("get " + from + " master: " + theMaster_of_to);

				edge.put("from", theMaster_of_from);
				edge.put("to", theMaster_of_to);
			}

		}

		return allEdges;
	}

	private static boolean isMaster(String m, JSONArray array) {

		for (int i = 0; i < array.size(); i++) {

			JSONObject cluster = (JSONObject) array.get(i);

			String master = (String) cluster.get("master");

			if (m.equals(master)) {
				return true;
			}
		}
		return false;
	}

	private static String getMaster(String s, JSONArray array) {

		for (int i = 0; i < array.size(); i++) {

			JSONObject cluster = (JSONObject) array.get(i);

			String master = (String) cluster.get("master");
			JSONArray slaves = (JSONArray) cluster.get("slaves");
			slaves.remove(master);

			if (slaves.contains(s)) {
				return master;
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private static Object[] removeEdgesFromResultFile(JSONArray allEdges, JSONObject allStates, JSONArray allClusters) {

		List<String> listOfEdgesToDelete = new LinkedList<String>();

		for (int j = 0; j < allClusters.size(); j++) {

			JSONObject jsonGeneralData = new JSONObject();
			jsonGeneralData = (JSONObject) allClusters.get(j);
			List<String> l = (List<String>) jsonGeneralData.get("slaves");

			for (int k = 0; k < l.size(); k++) {

				listOfEdgesToDelete.add(l.get(k));

			}
		}

		Object[] r = new Object[2];

		for (String e : listOfEdgesToDelete) {
			Object[] res = deleteEdgeFromJSONArray(e, allEdges, allStates);
			allEdges = (JSONArray) res[1];
			allStates = (JSONObject) res[0];
		}

		// test correct removal
		for (String e : listOfEdgesToDelete) {
			if (isInEdgesArray(e, allEdges)) {
				System.err.println("[ERROR] something went wrong: " + e + " is still present!");
			}
		}

		r[0] = allStates;
		r[1] = allEdges;

		return r;

	}

	// @SuppressWarnings("unchecked")
	// private static JSONArray modifyEdgesFromResultFile(JSONArray allEdges,
	// JSONArray allClusters) {
	//
	// List<String> listOfEdgesToModify = new LinkedList<String>();
	//
	// for(int j=0; j<allClusters.size(); j++){
	//
	// JSONObject jsonGeneralData = new JSONObject();
	// jsonGeneralData = (JSONObject) allClusters.get(j);
	//
	// String m = (String) jsonGeneralData.get("master");
	// List<String> l = (List<String>) jsonGeneralData.get("slaves");
	//
	// for (int k = 0; k < l.size(); k++) {
	// listOfEdgesToModify.add(l.get(k));
	// }
	//
	// for (String e : listOfEdgesToModify) {
	// JSONArray modifiedEdges = modifyEdgeFromJSONArray(e, m, allEdges);
	// allEdges = modifiedEdges;
	// }
	//
	// }
	//
	// return allEdges;
	//
	// }

	@SuppressWarnings("unchecked")
	private static Object[] deleteEdgeFromJSONArray(String e, JSONArray allEdges, JSONObject allStates) {

		List<JSONObject> toDelete = new LinkedList<JSONObject>();

		for (int i = 0; i < allEdges.size(); i++) {

			JSONObject edge = (JSONObject) allEdges.get(i);

			String to = (String) edge.get("to");
			String from = (String) edge.get("from");
			String id = (String) edge.get("id");
			id = id.replace("xpath ", "");

			if (e.equals(to) || e.equals(from)) {

				// allEdges.remove(edge);
				toDelete.add(edge);

				// remove candidate elements ce
				// where ce.xpath == id
				// allStates = removeCandidateElement(id, allStates);
			}

		}

		allEdges.removeAll(toDelete);

		Object[] result = new Object[2];
		result[0] = allStates;
		result[1] = allEdges;

		return result;
	}

	private static JSONObject removeCandidateElement(String id, JSONObject allStates) {

		for (Object o : allStates.keySet()) {
			JSONObject jo = (JSONObject) allStates.get(o);
			JSONArray ca = (JSONArray) jo.get("candidateElements");
			for (int i = 0; i < ca.size(); i++) {
				JSONObject c = (JSONObject) ca.get(i);
				if (c.get("xpath").equals(id)) {
					ca.remove(i);
				}
			}
		}

		return allStates;
	}

	private static boolean isInEdgesArray(String e, JSONArray array) {

		for (int i = 0; i < array.size(); i++) {

			JSONObject edge = (JSONObject) array.get(i);

			String to = (String) edge.get("to");
			String from = (String) edge.get("from");

			if (e.equals(to) || e.equals(from)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject removeStatesFromResultFile(JSONObject allStates, JSONArray allClusters) {

		for (int i = 0; i < allClusters.size(); i++) {

			JSONObject jsonGeneralData = new JSONObject();
			jsonGeneralData = (JSONObject) allClusters.get(i);
			List<String> l = (List<String>) jsonGeneralData.get("slaves");
			String m = (String) jsonGeneralData.get("master");
			l.remove(m);

			for (String s : l) {
				allStates.remove(s);
			}
		}
		return allStates;
	}

	public static LinkedHashMap<Integer, LinkedList<String>> runClustering(String a, String f,
			String number_of_clusters) throws Exception {

		File dir = new File("output/clusters/");

		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".arff");
			}
		});

		// Hierarchical
		if (a.equals("0")) {
			return clusterer.WekaClusterers.runHierarchical(files[0].toString(), number_of_clusters, "0");
			// K-means++
		} else if (a.equals("1")) {
			return clusterer.WekaClusterers.runKmeans(files[0].toString(), number_of_clusters, "1");
		} else if (a.equals("2")) {
			// k medoids
		}

		return null;
	}

	// /**
	// * This methods analyses the DOMs of the retrieved states
	// * clustering them by means of the Levenshtein distance
	// * metric. Only adjacent states are compared though
	// * @throws IOException
	// * @throws ParseException
	// * @throws SAXException
	// * @throws ParserConfigurationException
	// */
	// public static void createClustersByDOMLevenshteinDistance(double threshold)
	// throws IOException, ParseException, ParserConfigurationException,
	// SAXException {
	//
	// String domsDirectory = "output/doms/";
	// File dir = new File(domsDirectory);
	//
	// List<File> files = (List<File>) FileUtils.listFiles(dir,
	// TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	//
	// // Sorting the states file names in following the integer natural order
	// // Necessary for a correct comparison
	// Collections.sort(files, new NaturalOrderComparator());
	//
	// List<Cluster> clusters = new LinkedList<Cluster>();
	//
	// FileWriter file = null;
	// try {
	// file = new FileWriter("output/cluster.json");
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// try {
	//
	// for(int i=0; i < files.size(); i++){
	//
	// List<String> slavesList = new LinkedList<String>();
	// List<abstractdt.Diff> diffsForGettersGeneration = new
	// LinkedList<abstractdt.Diff>();
	//
	// for(int j=i+1; j < files.size(); j++){
	//
	// boolean adjacent =
	// areStatesAdjacent(files.get(i).getName().replace(".html", ""),
	// files.get(j).getName().replace(".html", ""));
	//
	// //adjacent = true;
	//
	// if(adjacent){
	// String dom1 = null, dom2 = null;
	// try {
	// dom1 = FileUtils.readFileToString(files.get(i));
	// dom2 = FileUtils.readFileToString(files.get(j));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// boolean levDist = _UtilsMerging.levenshteinEquals(threshold, dom1, dom2);
	//
	// if(levDist) {
	// System.out.println("[LOG] " + files.get(i).getName().replace(".html", "") +
	// " && " + files.get(j).getName().replace(".html", "") +
	// " are found similar.");
	// //System.out.println("[LOG] " + files.get(j).getName().replace(".html", "") +
	// // " will be initially merged inside " +
	// files.get(i).getName().replace(".html", ""));
	// //System.out.println(files.get(i).getName() + " && " + files.get(j).getName()
	// + " = " + levDist);
	//
	// slavesList.add(files.get(j).getName().replace(".html", ""));
	//
	// // TODO: calculate diff
	// /*
	// List<Difference> diffList = UtilsDiff.customisedDomDiff(dom1, dom2);
	// diffsForGettersGeneration.addAll(
	// UtilsDiff.printsDiff(files.get(i).getName().replace(".html", ""),
	// files.get(j).getName().replace(".html", ""), diffList));
	// */
	// }
	// }
	// }
	//
	// // if the list of slaves is not empty (i.e., there is no cluster)
	// if(!slavesList.isEmpty()){
	// Cluster c = new Cluster(files.get(i).getName().replace(".html", ""),
	// slavesList, diffsForGettersGeneration);
	// clusters.add(c);
	// }
	// }
	//
	// // refactor later to eliminate file
	// writeClusterFile(file, clusters);
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// file.flush();
	// file.close();
	// }
	//
	// runTransitiveClosure(clusters);
	//
	// }

	// /**
	// * This methods analyses the DOMs of the retrieved states
	// * clustering them by means of the RTED metric.
	// * @param l
	// * @throws IOException
	// * @throws ParseException
	// * @throws SAXException
	// * @throws ParserConfigurationException
	// */
	// public static void clusterStates(int k, int threshold, int l) throws
	// IOException, ParseException, ParserConfigurationException, SAXException {
	//
	// String domsDirectory = "output/doms/";
	// File dir = new File(domsDirectory);
	//
	// List<File> files = (List<File>) FileUtils.listFiles(dir,
	// TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	//
	// // Sorting the states file names in following the integer natural order
	// // Necessary for a correct comparison
	// Collections.sort(files, new NaturalOrderComparator());
	//
	// List<Cluster> clusters = new LinkedList<Cluster>();
	//
	// FileWriter file = null;
	// try {
	// file = new FileWriter("output/cluster.json");
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// try {
	//
	// for(int i=0; i < files.size(); i++){
	//
	// List<String> slavesList = new LinkedList<String>();
	// List<abstractdt.Diff> diffsForGettersGeneration = new
	// LinkedList<abstractdt.Diff>();
	//
	// System.err.println("\n\tfiles.get(i) = " + files.get(i));
	//
	// // this cycle calculates all the states that are similar to the ith state
	// for(int j=i+1; j < files.size(); j++){
	//
	// System.out.println("\tfiles.get(j) = " + files.get(j));
	//
	// boolean adjacent =
	// areStatesAdjacent(files.get(i).getName().replace(".html", ""),
	// files.get(j).getName().replace(".html", ""));
	//
	// //if(adjacent) {
	// if(true) {
	//
	// String dom1 = null, dom2 = null, strategy = null;
	// boolean metric = false;
	//
	// // calculate different similarities depending on the strategy
	// switch (k) {
	// case 0 : // URL
	// strategy = "URL";
	//
	// try {
	// dom1 = FileUtils.readFileToString(files.get(i)); // get URL for state
	// files.get(i)
	// dom2 = FileUtils.readFileToString(files.get(j)); // get URL for state
	// files.get(j)
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// metric = _UtilsMerging.levenshteinEquals(threshold, dom1, dom2);
	//
	// break;
	// case 1 : // DOM-levenshtein distance
	// strategy = "DOM-levenshtein distance";
	//
	// try {
	// dom1 = FileUtils.readFileToString(files.get(i));
	// dom2 = FileUtils.readFileToString(files.get(j));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// metric = _UtilsMerging.levenshteinEquals(threshold, dom1, dom2);
	//
	// break;
	// case 2 : // DOM-rted distance
	// strategy = "DOM-rted distance";
	//
	// try {
	// dom1 = FileUtils.readFileToString(files.get(i));
	// dom2 = FileUtils.readFileToString(files.get(j));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// metric = _UtilsMerging.similarAccordingToDomDiversity(threshold, dom1, dom2);
	//
	// break;
	// }
	//
	// if(metric) {
	// System.out.println("[LOG] " + files.get(i).getName().replace(".html", "") +
	// " && " + files.get(j).getName().replace(".html", "") +
	// " are found similar according to strategy: " + strategy);
	//
	// slavesList.add(files.get(j).getName().replace(".html", ""));
	//
	// }
	// }
	// }
	//
	// // if the list of slaves is not empty (i.e., there is no cluster)
	// if(!slavesList.isEmpty()){
	// // master is the ith state
	// Cluster c = new Cluster(files.get(i).getName().replace(".html", ""),
	// slavesList, diffsForGettersGeneration);
	// clusters.add(c);
	// }
	// }
	//
	// // refactor later to eliminate file
	// writeClusterFile(file, clusters);
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// file.flush();
	// file.close();
	// }
	//
	// runTransitiveClosure(clusters);
	//
	// }
	//
	// public static void runTransitiveClosure(List<Cluster> clusters) throws
	// IOException, ParseException {
	//
	// System.out.println("[LOG] Run the transitive closure on the graph\n");
	//
	// // Transitive Closure
	// Map<String, Set<String>> map = parseJSONClustersIntoHashMap();
	//
	// System.out.println("\t[INFO] Initial Graph");
	// System.out.println("\t"+map.toString()+"\n");
	//
	// map = transitiveClosure(map);
	//
	// System.out.print("\t[INFO] Transitive Closure");
	// System.out.println("\n\t"+map.toString()+"\n");
	//
	// map = removeDuplicates(map);
	//
	// System.out.print("\t[INFO] Duplicates Removal");
	// System.out.println("\n\t"+map.toString()+"\n");
	//
	// // Look-up of JSON file
	//
	// clusters = applyTransitiveTransformationToClusters(map, clusters);
	//
	// // refactor later
	// FileWriter file = new FileWriter("output/cluster.json");
	// writeClusterFile(file, clusters);
	// file.flush();
	// file.close();
	//
	// }

	// private static List<Cluster> applyTransitiveTransformationToClusters(
	// Map<String, Set<String>> map, List<Cluster> clusters) {
	//
	// List<Cluster> result = new LinkedList<Cluster>();
	//
	// for (Cluster cluster : clusters) {
	//
	// // if cluste.master is not in the map, i have to delete it
	// if(!map.containsKey(cluster.getMaster())){
	//
	// String belongsToWhatState = checkBelonging(map, cluster.getMaster());
	//
	// if(belongsToWhatState == null){
	// System.out.println("[ERR] Critical error in UtilsMerging.checkBelonging");
	// System.exit(1);
	// }
	//
	// int indexOfBelonging = 0;
	// // find the right cluster
	// for(int i=0; i < clusters.size(); i++){
	// if(clusters.get(i).getMaster().equals(belongsToWhatState)){
	// indexOfBelonging = i;
	// break;
	// }
	// }
	//
	// // add slaves
	// clusters.get(indexOfBelonging).getSlaves().addAll(cluster.getSlaves());
	//
	// // append diff
	// try {
	// clusters.get(indexOfBelonging).getDiffs().addAll(cluster.getDiffs());
	// }
	// catch(NullPointerException e) {
	// //System.out.println("[LOG] applyTransitiveTransformationToClusters Diff is
	// disabled");
	// }
	//
	// result.remove(indexOfBelonging);
	// // remove cluster
	// result.add(clusters.get(indexOfBelonging));
	// //clusters.remove(cluster);
	//
	// }
	// else {
	// result.add(cluster);
	// }
	// }
	//
	// return result;
	// }
	//
	// private static String checkBelonging(Map<String, Set<String>> map, String
	// master) {
	//
	// for (String key : map.keySet()) {
	// Set<String> values = map.get(key);
	// if(values.contains(master)){
	// return key;
	// }
	// }
	//
	// return null;
	// }

	// private static Map<String, Set<String>> transitiveClosure(
	// Map<String, Set<String>> map) {
	//
	// Map<String, Set<String>> aNewMap = new HashMap<String, Set<String>>();
	// Set<String> c = new HashSet<String>();
	//
	// for (String key : map.keySet()) {
	// c = getClosure(map, key, new HashSet<String>());
	// aNewMap.put(key, c);
	// }
	//
	// return aNewMap;
	//
	// }

	// private static Set<String> getClosure(Map<String, Set<String>> map, String
	// key, Set<String> v) {
	//
	// if(v.contains(key) || !map.containsKey(key)){
	// return new HashSet<String>();
	// }
	//
	// Set<String> result = new HashSet<String>();
	// v.add(key);
	//
	// for (String slave : map.get(key)) {
	// result.addAll(getClosure(map, slave, v));
	// result.add(slave);
	// }
	//
	// return result;
	//
	// }
	//
	// private static Map<String, Set<String>> removeDuplicates(
	// Map<String, Set<String>> map) {
	//
	// Map<String, Set<String>> aNewMap = new HashMap<String, Set<String>>();
	// Set<String> values = new HashSet<String>();
	// Set<String> toRemove = new HashSet<String>();
	//
	// for (String key : map.keySet()) {
	//
	// values = map.get(key);
	//
	// for (String string : values) {
	// if(map.containsKey(string)){
	// toRemove.add(string);
	// }
	// }
	// }
	//
	// aNewMap = map;
	//
	// for (String tr : toRemove) {
	// aNewMap.remove(tr);
	// }
	//
	// return aNewMap;
	//
	// }

	// private static Map<String, Set<String>> parseJSONClustersIntoHashMap() throws
	// JsonParseException, JsonMappingException, IOException, ParseException {
	//
	// Map<String, Set<String>> map = new HashMap<String, Set<String>>();
	//
	// JSONParser parser = new JSONParser();
	// Object obj = parser.parse(new FileReader("output/cluster.json"));
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// JSONArray clusters = (JSONArray) jsonObject.get("clusters");
	//
	// for(int i=0; i < clusters.size(); i++){
	// JSONObject cluster = (JSONObject) clusters.get(i);
	//
	// Set<String> set = new HashSet<String>();
	// JSONArray array = (JSONArray) cluster.get("slaves");
	//
	// for(int j=0; j < array.size(); j++){
	// set.add((String) array.get(j));
	// }
	//
	// map.put((String) cluster.get("master"), set);
	// }
	//
	// return map;
	// }

	// /**
	// * checks whether two states are adjacent, i.e.
	// * there exist an edges connecting state1 and state2
	// * @param state1
	// * @param state2
	// * @return
	// * @throws FileNotFoundException
	// * @throws IOException
	// * @throws ParseException
	// */
	// private static boolean areStatesAdjacent(String state1, String state2) throws
	// FileNotFoundException, IOException, ParseException {
	//
	// JSONParser parser = new JSONParser();
	//
	// Object obj = parser.parse(new FileReader("output/result.json"));
	// JSONObject jsonResults = (JSONObject) obj;
	//
	// JSONArray allEdges = (JSONArray) jsonResults.get("edges");
	//
	// for(int i=0; i<allEdges.size(); i++){
	//
	// JSONObject jsonGeneralData = new JSONObject();
	// jsonGeneralData = (JSONObject) allEdges.get(i);
	//
	// if(jsonGeneralData.get("from").equals(state1) &&
	// jsonGeneralData.get("to").equals(state2)){
	// return true;
	// }
	// }
	//
	// return false;
	// }

	// /**
	// * Modifies the Crawljax report accordingly to cluster information
	// * @throws ParseException
	// * @throws IOException
	// * @throws FileNotFoundException
	// */
	// @SuppressWarnings("unchecked")
	// public static void modifyCrawljaxResultAccordingToClusters() throws
	// FileNotFoundException, IOException, ParseException {
	//
	// // open file
	// JSONParser parser = new JSONParser();
	//
	// Object obj = parser.parse(new FileReader("output/resultAfterMerging.json"));
	// JSONObject jsonResults = (JSONObject) obj;
	//
	// obj = parser.parse(new FileReader("output/cluster.json"));
	// JSONObject jsonClusters = (JSONObject) obj;
	//
	// /* TODO: foreach read slaves states from cluster.json
	// * remove it from resultAfterMerging.json and
	// * remove/modify the related edges
	// */
	// JSONObject allStates = (JSONObject) jsonResults.get("states");
	// JSONArray allEdges = (JSONArray) jsonResults.get("edges");
	// JSONArray allClusters = (JSONArray) jsonClusters.get("clusters");
	//
	//// System.out.println("[INFO] #states before merging: " + allStates.size());
	////
	//// allStates = removeStatesFromResultFile(allStates, allClusters);
	////
	//// System.out.println("[INFO] #states after merging: " + allStates.size());
	//
	// System.out.println("[INFO] #edges before merging: " + allEdges.size());
	//
	// //Object[] r = removeEdgesFromResultFile(allEdges, allStates, allClusters);
	// //allEdges = modifyEdgesFromResultFile(allEdges, allClusters);
	//
	// //allStates = (JSONObject) r[0];
	// //allEdges = (JSONArray) r[1];
	//
	// System.out.println("[INFO] #edges after merging: " + allEdges.size());
	//
	// JSONObject resultAfterMerging = new JSONObject();
	// resultAfterMerging.put("states", allStates);
	// resultAfterMerging.put("edges", allEdges);
	//
	// ObjectMapper mapper = new ObjectMapper();
	//
	// //String result =
	// mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultAfterMerging);
	// //System.err.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allEdges));
	//
	// FileUtils.writeStringToFile(new File("output/resultAfterMerging.json"),
	// mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultAfterMerging));
	//
	// }

}
