package clusterer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMedoids;
//import net.sf.javaml.clustering.KMedoidsDistance;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;

public class JavaMLClusterers {
    
    public static LinkedHashMap<Integer, LinkedList<String>> runKmedoid(String filename, String numClusters, boolean distance) throws IOException{
		
    	LinkedHashMap<Integer, LinkedList<String>> output = null;
    	Clusterer c = new KMedoids(Integer.parseInt(numClusters), 500, new EuclideanDistance());
    	
//		if (distance) {
////			c = new KMedoidsDistance(Integer.parseInt(numClusters), 500,
////					new EuclideanDistance());
//			 c = new KMedoids(Integer.parseInt(numClusters), 500, new EuclideanDistance());
//		} else {
//			c = new KMedoids(Integer.parseInt(numClusters), 500,
//					new EuclideanDistance());
//		}

		Dataset data = FileHandler.loadDataset(new File(filename), 0, ",");

		Dataset[] clusters = c.cluster(data);

		output = convert(clusters);
    	
    	return output;
    	
    }
    
    public static LinkedHashMap<Integer, LinkedList<String>> convert(Dataset[] clusters){
    	
    	LinkedHashMap<Integer, LinkedList<String>> output = new LinkedHashMap<Integer, LinkedList<String>>();
    	
    	for(int i=0; i<clusters.length; i++){
    		
    		LinkedList<String> list = new LinkedList<String>();
    		
    		for(int j=0; j<clusters[i].size(); j++){
    			//System.out.println("\t" + clusters[i].classValue(j));
    			list.add(""+clusters[i].classValue(j));
    		}
    		
    		output.put(new Integer(i), list);
    	}
    	
    	return output;
    }
    
    /*
    public static void printCluster(Dataset[] clusters) {
		
		System.out.println("\n*** CLUSTERS: "+ clusters.length +" ***");
		
    	for(int i=0; i<clusters.length; i++){
    		System.out.println("Cluster " + i);
    		for(int j=0; j<clusters[i].size(); j++){
    			System.out.println("\t" + clusters[i].classValue(j));
    		}
    			
    	}
	}
	*/
    
}
