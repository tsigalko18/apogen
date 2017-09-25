package apogen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xml.sax.SAXException;

import abstractdt.State;
import utils.UtilsClustering;
import utils.UtilsDataset;
import utils.UtilsStaticAnalyzer;

/**
 * This class is intended to analyze the output info provided by Crawljax and
 * create opportune State classes
 * 
 * @author Andrea Stocco
 *
 */
public class StaticAnalyzer {

	private static List<State> statesList;
	private static LinkedHashMap<Integer, LinkedList<String>> clustersMap;

	/**
	 * initializes the properties and runs the static analyzer
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 */
	public static void start() throws 
			IOException, ParserConfigurationException, SAXException, ParseException {
		init();
		run();
	}

	/**
	 * initialize the static analyzer, i.e. reads the opportune properties
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	private static void init() throws IOException,
			ParserConfigurationException, SAXException, ParseException {

		statesList = new LinkedList<State>();

		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null,
				"Would you like to run clustering over the model?\n",
				"Clustering", JOptionPane.YES_NO_OPTION);

		if (n == JOptionPane.YES_OPTION) {
			Settings.CLUSTERING = true;
			calculateClusters();
		} else {
			Settings.CLUSTERING = false;
		}
	}

	/**
	 * Merges the similar states accordingly to a the clustering result This
	 * method modifies Crawljax outputs in order to provide cluster and diff
	 * information
	 * 
	 * @param configFile
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 */
	public static void calculateClusters() throws IOException,
			ParserConfigurationException, SAXException, ParseException {

		System.out.println("[LOG] STARTED STATES MERGE");

		JFrame parent = new JFrame();

		int n = 1;
		while (n == 1) {

			String a = null, f = null;
			
			// ask for algorithm
			
			Object[] algo_possibilities = {"Hierarchical", "K-means"};//, "K-medoids"};
			
			String s = (String) JOptionPane.showInputDialog(
			                    null,
			                    "Choose the clustering algorithm",
			                    "Enter algorithm",
			                    JOptionPane.INFORMATION_MESSAGE,
			                    null,
			                    algo_possibilities,
			                    "Hierarchical");
			
			if ((s != null) && (s.length() > 0)) {
			    if(s.equals("Hierarchical")){
			    	a = "0";
			    } else if(s.equals("K-means")){
			    	a = "1";
			    }
			}
			
			// ask for feature
			
			Object[] feat_possibilities = {"DOM-RTED", "DOM-Lev", "Tag Frequency", "URL-Lev"};
			
			s = (String) JOptionPane.showInputDialog(
                    null,
                    "Choose the feature",
                    "Enter feature",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    feat_possibilities,
                    "DOM-RTED");

			if ((s != null) && (s.length() > 0)) {
				if(s.equals("DOM-RTED")){
					f = "0";
				} else if(s.equals("DOM-Lev")){
					f = "1";
				} else if(s.equals("Tag Frequency")){
					f = "2";
				} else if(s.equals("URL-Lev")){
					f = "3";
				}
			}

			String d = Settings.OUT_DIR;

			UtilsDataset ud = new UtilsDataset(d);
			ud.createDatasets(f);

			try {

				SpinnerNumberModel sModel = new SpinnerNumberModel(2, 2, 30, 1);
				JSpinner spinner = new JSpinner(sModel);

				int option = JOptionPane.showOptionDialog(parent, spinner,
						"Enter a number of clusters",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);
				
				if (option == JOptionPane.CANCEL_OPTION) { // user hits cancel
					
				} else if (option == JOptionPane.OK_OPTION) { // user entered a number
					Settings.NUMBER_OF_CLUSTERS = spinner.getValue().toString();
				}

				clustersMap = UtilsClustering.runClustering(a, f, Settings.NUMBER_OF_CLUSTERS);
			} catch (Exception e) {
				e.printStackTrace();
			}

			JSONArray list = new JSONArray();

			for (int i = 0; i < clustersMap.size(); i++) {
				for (int j = 0; j < clustersMap.get(i).size(); j++) {
					JSONObject obj = new JSONObject();
					obj.put("stateId", clustersMap.get(i).get(j));
					obj.put("clusterId", i);
					obj.put("clusterSize", clustersMap.get(i).size());
					list.add(obj);
				}
			}

			try {
				FileWriter file = new FileWriter("cve/clusters.json");
				file.write(list.toJSONString());
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// launch Clusters Visual Editor
			WebDriver cve = new FirefoxDriver();
			String cve_path = "file://" + System.getProperty("user.dir")
							  			+ "/cve/index.html";
			cve.manage().window().setPosition(new Point(0, 0));
			cve.manage().window().setSize(new Dimension(850, 1024));
			cve.get(cve_path);

			n = JOptionPane.showConfirmDialog(null,
					"Would you like to proceed?", "Clustering correct",
					JOptionPane.YES_NO_OPTION);
			if (n == 0) {
				break;
			}
		}

		System.gc();

		//n = 1;
		String res = "";
		//while (n == 1) {
			Runnable r = new Runnable() {
		
				public void run() {
					JFileChooser jfc = new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					// if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					// String file = jfc.getSelectedFile().getAbsolutePath();
					// }
				}
			};

			SwingUtilities.invokeLater(r);
		
			int returnVal;
			
			while(res.equals("") && !res.contains("clustersModified.txt")){
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				returnVal = fc.showOpenDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					res = fc.getSelectedFile().getAbsolutePath();
				}
				if(res.equals("") || !res.contains("clustersModified.txt")){
					JOptionPane.showMessageDialog(null, "Clusters File incorrect");
					res = "";
				}
				else {
					n = 0;
				}
			}
			
		//}

		UtilsClustering.readClusteringResult(res);
		UtilsClustering.createCrawljaxResultCopy();
		UtilsClustering.modifyCrawljaxResultAccordingToClusters();

		System.out.println("[LOG]\tENDED STATES MERGE");
	}

	/**
	 * runs the static analyzer: it parses the information contained in the
	 * Document Object Model retrieved by Crawljax and gets what is necessary
	 * for the page object's code generation
	 */
	private static void run() {
		System.out.println("[LOG]\tSTARTED STATIC ANALYSIS");

		statesList = UtilsStaticAnalyzer.createMergedStateObjects();

		System.out.println("[LOG]\tSTATIC ANALYSIS ENDED");
	}

	/**
	 * returns the list of the states
	 * 
	 * @return the statesList
	 */
	public static List<State> getStatesList() {
		return statesList;
	}

}

