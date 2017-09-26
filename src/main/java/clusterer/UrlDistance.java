package clusterer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import utils.NaturalOrderComparator;
import utils.UtilsClustering;

public class UrlDistance {

	static Map<String, LinkedHashMap<String, BigDecimal>> urlDistancesMap;
	static Dataset data;
	static String directory;

	public UrlDistance(String dir) {
		directory = dir;
		data = new DefaultDataset();
		urlDistancesMap = new LinkedHashMap<String, LinkedHashMap<String, BigDecimal>>();
	}

	/**
	 * calculate the URL distance matrix
	 */
	public void init() throws ParserConfigurationException, SAXException, IOException, ParseException {

		calculateUrlDistanceMatrix();

	}

	/**
	 * @return the urlDistancesMap
	 */
	public Map<String, LinkedHashMap<String, BigDecimal>> getUrlDistancesMap() {
		return urlDistancesMap;
	}

	@SuppressWarnings("unchecked")
	private void calculateUrlDistanceMatrix() throws IOException, ParseException {

		System.out.print("[LOG] URLs Leveshtein distance matrix: ");

		String domsDirectory = directory;
		File dir = new File(domsDirectory);

		List<File> files = (List<File>) FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter("html"),
				TrueFileFilter.INSTANCE);

		Collections.sort(files, new NaturalOrderComparator());

		for (int i = 0; i < files.size(); i++) {

			URL url1 = getUrlFromState(files.get(i).getName().replace(".html", ""));

			LinkedHashMap<String, BigDecimal> distanceVector = new LinkedHashMap<String, BigDecimal>();

			for (int j = 0; j < files.size(); j++) {

				String urlPath1 = null, urlPath2 = null;

				URL url2 = getUrlFromState(files.get(j).getName().replace(".html", ""));

				urlPath1 = url1.getPath();
				urlPath2 = url2.getPath();

				urlPath1 = removeUrlsParametersValues(urlPath1);
				urlPath2 = removeUrlsParametersValues(urlPath2);

				double l = 0;

				l = UtilsClustering.levenshteinDistance(urlPath1, urlPath2);

				BigDecimal bd = new BigDecimal(l);

				distanceVector.put(files.get(j).getName(), bd);
			}

			urlDistancesMap.put(files.get(i).getName(), distanceVector);

		}

		System.out.println(urlDistancesMap.size() + " features ... DONE");

	}

	/**
	 * retrieve the URL of a given state
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	private URL getUrlFromState(String filename) throws FileNotFoundException, IOException, ParseException {

		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader(directory.replace("doms/", "") + "result.json"));

		JSONObject jsonObject = (JSONObject) obj;

		JSONObject states = (JSONObject) jsonObject.get("states");

		for (Object state : states.keySet()) {

			JSONObject stateObject = (JSONObject) states.get(state);

			if (stateObject.get("name").equals(filename)) {
				URL u = new URL((String) stateObject.get("url"));
				return u;
			}

		}

		return null;
	}

	/**
	 * * Remove parameters from a URL.
	 * 
	 * @throws MalformedURLException
	 */
	public static String removeUrlsParametersValues(String uri) throws MalformedURLException {

		int i = uri.lastIndexOf('?');

		if (i == -1) {
			return uri;
		}

		String[] params = uri.substring(i + 1).split("&");

		for (int j = 0; j < params.length; j++) {

			String p = params[j];
			int k = p.indexOf('=');

			if (k == -1) {
				break;
			}

			String value = p.substring(k + 1);

			uri = uri.replace(value, "");

		}

		URL u = new URL(uri);
		return u.getFile();
		// return uri;
	}

	/**
	 * create the URL distances matrix
	 * 
	 * @return
	 */
	public Dataset createDataset() {

		for (String k : urlDistancesMap.keySet()) {

			Collection<BigDecimal> v = urlDistancesMap.get(k).values();
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