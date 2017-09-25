package clusterer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import utils.NaturalOrderComparator;
import utils.UtilsClustering;

@SuppressWarnings("unchecked")
public class DomDistance {
	
	public enum Distance {
		LEVENSHTEIN, RTED
	}
	
	public static List<String> tagsMasterVector;
	public static Map<String, LinkedHashMap<String, BigDecimal>> domDistancesMap;
	static List<String> stringsToRemove;
	public static Dataset data;
	public static String directory;
	static Distance distance;
	
	public DomDistance(String dir, Distance dist){
		directory = dir;
		distance = dist;
		tagsMasterVector = new LinkedList<String>();
		domDistancesMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
		data = new DefaultDataset();
		stringsToRemove = new LinkedList<String>();
	}
	
	public void init() throws ParserConfigurationException,
			SAXException, IOException {
		
		calculateDomDistanceMatrix();
	
	}
	
	/**
	 * @return the domDistancesMap
	 */
	public Map<String, LinkedHashMap<String, BigDecimal>> getDomDistancesMap() {
		return domDistancesMap;
	}

	private void calculateDomDistanceMatrix() throws IOException {
		
		System.out.println("[LOG] DOMs distance matrix using " + distance + ": ");
		
		String domsDirectory = directory;
		File dir = new File(domsDirectory);
		
		List<File> files = (List<File>) FileUtils.listFiles(dir,
				FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);
				
		Collections.sort(files, new NaturalOrderComparator());
		
		int totalMatches = files.size() * files.size();
		
		List<Integer> v = new LinkedList<Integer>();
		for(int conta = 1; conta < 101; conta++){
			v.add(totalMatches * conta / 100);
		}
		
		int matchesDone = 0;
		double percent = 0;
		ProgressBar.printProgBar((int) percent);
		
		for (int i = 0; i < files.size(); i++) {
			
			LinkedHashMap<String, BigDecimal> distanceVector = new LinkedHashMap<String, BigDecimal>();
			
			//System.err.println("\n"+files.get(i));
			String dom1 = null;
			String dom2 = null;
			for(int j=0; j < files.size(); j++){
				
				//System.err.print("\t"+files.get(j));
				try {
					dom1 = FileUtils.readFileToString(files.get(i));
					dom2 = FileUtils.readFileToString(files.get(j));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				double l = 0;
				
				if(distance == Distance.LEVENSHTEIN){
					//dom1 = cleanDomFromText(files.get(i));
					//dom2 = cleanDomFromText(files.get(j));
					l = UtilsClustering.levenshteinDistance(dom1, dom2);
					//System.err.println(l);
				}
				else if(distance == Distance.RTED){
					l = UtilsClustering.robustTreeEditDistance(dom1, dom2);
					//System.err.println(l);
				}
				
				matchesDone++;
				
				//System.err.println("\nmatchesDone: "+matchesDone);
				
				if(v.contains(matchesDone)){
					percent++;
					ProgressBar.printProgBar((int) percent);
					System.out.print("\t"+matchesDone+"/"+totalMatches);
				}
				
				//System.err.println("\t"+l);
				
				BigDecimal bd = new BigDecimal(l);
				
				distanceVector.put(files.get(j).getName(), bd);
			}
		
			domDistancesMap.put(files.get(i).getName(), distanceVector);
						
		}
		
		System.out.println("\n[LOG] " + domDistancesMap.size() + " features ... DONE");
	}
	
	public static String cleanDomFromText(File f) throws IOException{
		
		Document d = Jsoup.parse(f, null);
		
		stringsToRemove = new LinkedList<String>();
		
		//System.out.println(d);
		
		getStringsToRemove(d.getAllElements());
		
		//System.err.println(stringsToRemove);
		
		String domToString = d.outerHtml();
		
		for (String s : stringsToRemove) {
			domToString = domToString.replace(s, "");
		}
		
		//System.out.println(domToString);
		
		return domToString;
	}
	
	private static void getStringsToRemove(Elements allElements) {
		
		for (Element e : allElements) {
			if(e.hasText()){
				stringsToRemove.add(e.ownText());
			}
			getStringsToRemove(e.children());
		}
	}
	
	public Dataset createDataset() {

		for (String k : domDistancesMap.keySet()) {

			Collection<BigDecimal> v = domDistancesMap.get(k).values();
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