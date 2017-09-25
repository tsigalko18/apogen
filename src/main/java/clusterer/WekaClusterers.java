package clusterer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class WekaClusterers {

	static String matrix;
	static LinkedHashMap<Integer, LinkedList<String>> goldStandardMap;
	static LinkedHashMap<Integer, LinkedList<String>> clustersMap;

	/**
	 * Run WEKA Hierarchical clustering on the parameter ARFF file
	 * searching for numClusters clusters
	 * @param filename
	 * @param numClusters
	 * @param linkage
	 * @return 
	 * @throws Exception 
	 */
	public static LinkedHashMap<Integer, LinkedList<String>> runHierarchical(String filename, String numClusters,
			String linkage) throws Exception {

		String[] options = new String[6];
		options[0] = "-t";
		options[1] = filename;
		options[2] = "-N";
		options[3] = numClusters;
		options[4] = "-L";
		options[5] = linkage;

		HierarchicalClusterer c = new HierarchicalClusterer();
		
		c.setNumClusters(Integer.parseInt(numClusters));
		c.setDebug(false);
		c.setPrintNewick(true);
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(0);
		
		c.buildClusterer(data);
		
		LinkedHashMap<Integer, LinkedList<String>> output = new LinkedHashMap<Integer, LinkedList<String>>();
		
		// initialize clusters map
		for(int i=0; i<Integer.parseInt(numClusters); i++){
			output.put(new Integer(i), new LinkedList<String>());
		}
		
		for (Instance instance : data) {
			//System.out.println(instance.stringValue(0) + "\t" + c.clusterInstance(instance));
			output.get(c.clusterInstance(instance)).add(instance.stringValue(0));
		}
			
		return output;
	}

	/**
	 * Run WEKA SimpleKMeans or KMeans++ on the parameter ARFF file
	 * searching for numClusters clusters
	 * @param filename
	 * @param numClusters
	 * @param init
	 * @throws Exception 
	 */
	public static LinkedHashMap<Integer, LinkedList<String>> runKmeans(String filename, String numClusters, String init) throws Exception {
		
		String[] options = new String[10];
		options[0] = "-t";
		options[1] = filename;
		options[2] = "-init";
		options[3] = init;
		options[4] = "-N";
		options[5] = numClusters;
		options[6] = "-I";
		options[7] = "100";
		options[8] = "-c";
		options[9] = "first";
	
		String s = ClusterEvaluation.evaluateClusterer(
					new SimpleKMeans(), options);
		
		return parseKMeansOutput(s, numClusters);

	}
	
	/**
	 * Run K-medoids on the parameter ARFF file
	 * searching for numClusters clusters
	 * @param filename
	 * @param numClusters
	 * @param init
	 * @throws Exception
	 */
	public static LinkedHashMap<Integer, LinkedList<String>> runKMedoids(
			String filename, String numClusters, boolean distance)
			throws Exception {

		String[] options = new String[6];
		options[0] = "-t";
		options[1] = filename;
		options[2] = "-c";
		options[3] = "first";
		options[4] = "-N";
		options[5] = numClusters;

		String s = ClusterEvaluation.evaluateClusterer(new KMedoids(distance), options);

		return parseKMeansOutput(s, numClusters);
	}

	/*
	private static Map<Integer, LinkedList<String>> printMap(Map<Integer, LinkedList<String>> map) {
		System.out.println("CLUSTERS");
		for (Integer cluster : map.keySet()) {
			System.out.println("\t"+ cluster + "\t" + map.get(cluster));
		}
		System.out.println();
		return map;
	}
	*/
	
	/**
	 * parse the K-means output to get the clustering results
	 * @param s
	 * @param numClusters
	 * @return
	 * @throws IOException
	 */
	private static LinkedHashMap<Integer, LinkedList<String>> parseKMeansOutput(String s, String numClusters) throws IOException {
		
		LinkedHashMap<Integer, LinkedList<String>> output = new LinkedHashMap<Integer, LinkedList<String>>();
		
		int start = s.indexOf("Classes to Clusters:");
		int end = s.indexOf("\nCluster", start+"Classes to Clusters:".length());
		
		s = s.substring(start, end);
		s = s.replaceAll("Classes to Clusters:\n\n", "");
		
		List<String> lines = IOUtils.readLines(new StringReader(s));
		
		// initialize clusters map
		for(int i=0; i<Integer.parseInt(numClusters); i++){
			output.put(new Integer(i), new LinkedList<String>());
		}
		
		for (String string : lines) {
			
			// skip the initial header
			if(string.contains("assigned to cluster")){
				continue;
			}
			else {
				//System.err.println(string);
				string = string.replace(" ", "");
				//System.err.println(string);
				
				int where = string.indexOf('1');
				String who = string.substring(string.indexOf("|")+1, string.length());
				
				//System.out.println(who + "\t" + where);
				output.get(where).add(who);
			}
		}
		
		return output;
	}
	
}
