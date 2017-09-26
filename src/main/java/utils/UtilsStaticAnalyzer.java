package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import abstractdt.Anchor;
import abstractdt.Button;
import abstractdt.ButtonField;
import abstractdt.CandidateWebElement;
import abstractdt.CheckBoxField;
import abstractdt.Getter;
import abstractdt.Dimension;
import abstractdt.Edge;
import abstractdt.Form;
import abstractdt.FormField;
import abstractdt.Image;
import abstractdt.ImageField;
import abstractdt.InputField;
import abstractdt.PasswordField;
import abstractdt.Point;
import abstractdt.RadioField;
import abstractdt.Select;
import abstractdt.State;
import abstractdt.SubmitField;
import abstractdt.TextArea;
import apogen.Settings;
import apogen.StaticAnalyzer;

/**
 * Utils class containing auxiliary and configuration methods for the Static
 * Analyzer
 * 
 * @author Andrea Stocco
 *
 */
public class UtilsStaticAnalyzer {

	/**
	 * fills the statesList with the information parsed from result.json
	 * 
	 * @param clustering
	 * @return
	 */
	public static List<State> createMergedStateObjects() {

		List<State> statesList = StaticAnalyzer.getStatesList();

		JSONParser parser = new JSONParser();

		try {

			// dangerous if gen_po_dir is a precious dir in the file system!!!
			// FileUtils.deleteDirectory(new File(gen_po_dir));
			int total_getters = 0;

			Object obj = null;
			if (Settings.CLUSTERING) {
				try {
					obj = parser.parse(new FileReader(Settings.OUT_DIR + "resultAfterMerging.json"));
				} catch (FileNotFoundException e) {
					System.out.println("[LOG]\tFile output/resultAfterMerging.json not found.");
					System.exit(1);
				}
			} else {
				try {
					obj = parser.parse(new FileReader(Settings.OUT_DIR + "result.json"));
				} catch (FileNotFoundException e) {
					System.out.println("[LOG]\tFile output/result.json not found.");
					System.exit(1);
				}

			}

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject states = (JSONObject) jsonObject.get("states");
			JSONArray edges = (JSONArray) jsonObject.get("edges");

			// create a PO for each state
			for (Object state : states.keySet()) {

				JSONObject stateObject = (JSONObject) states.get(state);

				// create a new State object
				State s = new State((String) stateObject.get("name"), (String) stateObject.get("url"));

				// name the state with the URL
				s.setName(UtilsStaticAnalyzer.getClassNameFromUrl(s, statesList));

				// save the DOM of the state
				s.setDom(UtilsStaticAnalyzer.getDOMFromDirectory(s.getStateId()));

				JSONArray candidates = (JSONArray) stateObject.get("candidateElements");
				JSONArray failedEvents = (JSONArray) stateObject.get("failedEvents");

				// save the web elements of the state
				Set<CandidateWebElement> totalListOfWebElements = new HashSet<CandidateWebElement>();
				totalListOfWebElements = getCandidateWebElementsList(candidates, failedEvents, s.getStateId(),
						s.getDom());

				// TODO: create web elements from form fields. DIFFICULT
				// getWebElementsContainedInForms(s);

				s.setWebElements(totalListOfWebElements);

				setConnections(state, edges, s);

				s.setForms(createFormObjects(s));

				statesList.add(s);
			}

			List<State> toRemove = new LinkedList<State>();

			for (State s : statesList) {

				List<String> slaves = UtilsStaticAnalyzer.getSlaves(s.getStateId(), Settings.OUT_DIR);

				for (String slave : slaves) {
					int index = UtilsStaticAnalyzer.getState(statesList, slave);
					State toMerge = statesList.get(index);

					// merge web elements
					s.getWebElements().addAll(toMerge.getWebElements());

					// merge links methods
					for (Edge e : toMerge.getLinks()) {
						if (!slaves.contains(e.getTo())) {
							s.getLinks().addAll(toMerge.getLinks());
						}
					}

					// merge forms methods
					// duplicates are removed depending on the
					// form name!
					Set<Form> stateForms = s.getForms();
					Set<Form> toAdd = new HashSet<Form>();

					for (Form formToAdd : toMerge.getForms()) {

						if (stateForms.size() == 0) {
							toAdd.addAll(toMerge.getForms());
						} else {
							if (!stateForms.contains(formToAdd)) {
								if (!toAdd.contains(formToAdd))
									toAdd.add(formToAdd);
							}
						}
					}
					s.getForms().addAll(toAdd);

					// add the state to the list to be removed
					toRemove.add(toMerge);
				}

				// add diffs
				// TODO: da controllare il funzionamento di s.getStateId()
				Set<Getter> list_diff = UtilsStaticAnalyzer.retrieveDiffsFromFile(s.getStateId());
				s.setDiffs(list_diff);

				System.out.println("[LOG]\t" + list_diff.size() + " getter(s) found in " + s.getName());
				total_getters += list_diff.size();
			}

			System.out.println("[LOG]\t#states before merging: " + statesList.size());

			statesList.removeAll(toRemove);

			System.out.println("[LOG]\t#states after merging: " + statesList.size());

			UtilsStaticAnalyzer.printStatesList(statesList);
			System.out.println("[LOG]\tTotal generated getter(s): " + total_getters);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return statesList;
	}

	private static int getState(List<State> statesList, String slave) {
		for (int i = 0; i < statesList.size(); i++) {
			if (statesList.get(i).getStateId().equals(slave)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * gets the Diffs objects from cluster.json file
	 * 
	 * @param state
	 * @param out_dir
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	private static Set<Getter> retrieveDiffsFromFile(String state) throws ParseException {

		Set<Getter> result = new HashSet<Getter>();

		JSONParser parser = new JSONParser();
		Object obj;
		try {
			obj = parser.parse(new FileReader(Settings.OUT_DIR + "cluster.json"));
		} catch (IOException e1) {
			System.out.println("[LOG]\tfile cluster.json missing for diffs calculation");
			return new HashSet<Getter>();
		}
		JSONObject jsonObject = (JSONObject) obj;

		JSONArray clusters = (JSONArray) jsonObject.get("clusters");

		for (int i = 0; i < clusters.size(); i++) {
			JSONObject aCluster = (JSONObject) clusters.get(i);

			if (aCluster.get("master").equals(state)) {

				JSONArray arrayOfDiffs = (JSONArray) aCluster.get("diffs");

				for (Object o : arrayOfDiffs) {
					JSONObject aSingleDiff = (JSONObject) o;

					Getter e = new Getter();
					e.setSourceState(aSingleDiff.get("sourceState").toString());
					e.setTargetState(aSingleDiff.get("targetState").toString());
					e.setCause(aSingleDiff.get("cause").toString());
					e.setWebElementName(aSingleDiff.get("webElementName").toString());

					switch (aSingleDiff.get("cause").toString()) {
					case "added element":
						e.setBefore("null");
						e.setAfter(aSingleDiff.get("after").toString());
						e.setLocator(aSingleDiff.get("locator").toString());
						break;
					case "removed element":
						e.setBefore("null");
						e.setAfter("null");
						e.setLocator("null");
						break;
					case "textual change":
						e.setBefore(aSingleDiff.get("before").toString());
						e.setAfter(aSingleDiff.get("after").toString());
						e.setLocator(aSingleDiff.get("locator").toString());
					default:
						break;
					}

					result.add(e);
				}
			}
		}

		return result;
	}

	/**
	 * get slaves objects from cluster.json file
	 * 
	 * @param state
	 * @param out_dir
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	private static List<String> getSlaves(String state, final String out_dir) throws ParseException {

		List<String> result = new LinkedList<String>();

		JSONParser parser = new JSONParser();
		Object obj;
		try {
			obj = parser.parse(new FileReader(out_dir + "cluster.json"));
		} catch (IOException e) {
			System.out.println("[LOG]\tcluster.json file missing for slaves calculation");
			return new LinkedList<String>();
		}
		JSONObject jsonObject = (JSONObject) obj;

		JSONArray clusters = (JSONArray) jsonObject.get("clusters");

		for (int i = 0; i < clusters.size(); i++) {
			JSONObject aCluster = (JSONObject) clusters.get(i);

			if (aCluster.get("master").equals(state)) {

				JSONArray arrayOfSlaves = (JSONArray) aCluster.get("slaves");

				for (Object o : arrayOfSlaves) {
					// JSONObject aSlave = (JSONObject) o;
					result.add(o.toString());
				}
			}
		}

		return result;
	}

	/**
	 * sets the Edge objects of a class with the information retrieved in the
	 * result.json file
	 * 
	 * @param state
	 * @param edges
	 * @param s
	 */
	private static void setConnections(Object state, JSONArray edges, State s) {

		for (int j = 0; j < edges.size(); j++) {
			JSONObject connection = (JSONObject) edges.get(j);

			if (connection.get("from").equals((String) state)) {
				Edge e = new Edge((String) connection.get("from"), (String) connection.get("to"));

				e.setVia((String) connection.get("id"));
				e.setEvent((String) (String) connection.get("eventType"));

				s.addConnection(e);
			}
		}

	}

	/**
	 * Statically analyze the DOM of the State s to create comprehensive Form
	 * objects
	 * 
	 * @param s
	 * @return
	 */
	public static Set<Form> createFormObjects(State s) {

		Set<Form> fl = new HashSet<Form>();
		String dom = s.getDom();

		Document doc = Jsoup.parse(dom, "UTF-8");
		Elements forms = doc.getElementsByTag("form");
		System.out.println("[LOG]\t" + forms.size() + " form(s) found in " + s.getName());

		for (org.jsoup.nodes.Element currentForm : forms) {
			Form formObject = new Form();

			formObject.setAttributes(currentForm.attributes().asList());
			formObject.setFormName(getFormName(formObject, currentForm));
			formObject.setReturnValue("void");
			formObject.setFormFieldList(analyzeFormBody(s, currentForm));

			fl.add(formObject);
		}

		return fl;
	}

	/**
	 * Analyzes the form f creating the right objects for the retrieved form fields.
	 * Returns a list of Form Fields
	 * 
	 * @param s
	 * @param e
	 * @return
	 */
	private static List<FormField> analyzeFormBody(State s, org.jsoup.nodes.Element e) {

		List<FormField> results = new LinkedList<FormField>();

		Elements itsBiggerOnTheInside = e.getAllElements();

		for (org.jsoup.nodes.Element singleElement : itsBiggerOnTheInside) {
			switch (singleElement.tag().getName()) {
			case "input":
				switch (singleElement.attr("type")) {
				case "text":
					InputField i = new InputField(singleElement);

					CandidateWebElement c = isWebElementPresent(s.getWebElements(), i);
					if (c == null) {
						results.add(i);
						s.addWebElement(new CandidateWebElement(i.getVariableName(), i.getLocator()));
					} else {
						i.setVariableName(c.getVariableName());
						results.add(i);
					}
					break;

				case "password":
					PasswordField p = new PasswordField(singleElement);

					c = isWebElementPresent(s.getWebElements(), p);
					if (c == null) {
						results.add(p);
						s.addWebElement(new CandidateWebElement(p.getVariableName(), p.getLocator()));
					} else {
						p.setVariableName(c.getVariableName());
						results.add(p);
					}
					break;

				case "checkbox":
					CheckBoxField ch = new CheckBoxField(singleElement);

					c = isWebElementPresent(s.getWebElements(), ch);
					if (c == null) {
						results.add(ch);
						s.addWebElement(new CandidateWebElement(ch.getVariableName(), ch.getLocator()));
					} else {
						ch.setVariableName(c.getVariableName());
						results.add(ch);
					}
					break;

				case "radio":
					RadioField r = new RadioField(singleElement);

					c = isWebElementPresent(s.getWebElements(), r);
					if (c == null) {
						results.add(r);
						s.addWebElement(new CandidateWebElement(r.getVariableName(), r.getLocator()));
					} else {
						r.setVariableName(c.getVariableName());
						results.add(r);
					}
					break;

				case "image":
					ImageField im = new ImageField(singleElement);

					c = isWebElementPresent(s.getWebElements(), im);
					if (c == null) {
						results.add(im);
						s.addWebElement(new CandidateWebElement(im.getVariableName(), im.getLocator()));
					} else {
						im.setVariableName(c.getVariableName());
						results.add(im);
					}
					break;

				case "submit":
					SubmitField sub = new SubmitField(singleElement);

					c = isWebElementPresent(s.getWebElements(), sub);
					if (c == null) {
						results.add(sub);
						s.addWebElement(new CandidateWebElement(sub.getVariableName(), sub.getLocator()));
					} else {
						sub.setVariableName(c.getVariableName());
						results.add(sub);
					}
					break;

				case "hidden":
					// Hidden Fields are now not considered
					/*
					 * HiddenField hid = new HiddenField(singleElement);
					 * 
					 * c = isWebElementPresent(s.getWebElements(), hid); if(c == null) {
					 * results.add(hid); s.addWebElement(new CandidateWebElement(
					 * hid.getVariableName(), hid.getLocator())); } else {
					 * hid.setVariableName(c.getVariableName()); results.add(hid); }
					 */
					break;

				case "button":
					ButtonField but = new ButtonField(singleElement);

					c = isWebElementPresent(s.getWebElements(), but);
					if (c == null) {
						results.add(but);
						s.addWebElement(new CandidateWebElement(but.getVariableName(), but.getLocator()));
					} else {
						but.setVariableName(c.getVariableName());
						results.add(but);
					}
					break;

				// default is text input field
				default:
					InputField def = new InputField(singleElement);

					c = isWebElementPresent(s.getWebElements(), def);
					if (c == null) {
						results.add(def);
						s.addWebElement(new CandidateWebElement(def.getVariableName(), def.getLocator()));
					} else {
						def.setVariableName(c.getVariableName());
						results.add(def);
					}
					break;
				}
				break;

			// maybe anchors in forms should be new methods!
			case "a":
				Anchor anch = new Anchor(singleElement);

				CandidateWebElement c = isWebElementPresent(s.getWebElements(), anch);
				if (c == null) {
					results.add(anch);
					s.addWebElement(new CandidateWebElement(anch.getVariableName(), anch.getLocator()));
				} else {
					anch.setVariableName(c.getVariableName());
					results.add(anch);
				}
				break;

			case "button":
				Button btn = new Button(singleElement);

				c = isWebElementPresent(s.getWebElements(), btn);
				if (c == null) {
					results.add(btn);
					s.addWebElement(new CandidateWebElement(btn.getVariableName(), btn.getLocator()));
				} else {
					btn.setVariableName(c.getVariableName());
					results.add(btn);
				}
				break;

			case "img":
				Image image = new Image(singleElement);

				c = isWebElementPresent(s.getWebElements(), image);
				if (c == null) {
					results.add(image);
					s.addWebElement(new CandidateWebElement(image.getVariableName(), image.getLocator()));
				} else {
					image.setVariableName(c.getVariableName());
					results.add(image);
				}
				break;

			case "select":
				Select sel = new Select(singleElement);

				c = isWebElementPresent(s.getWebElements(), sel);
				if (c == null) {
					results.add(sel);
					s.addWebElement(new CandidateWebElement(sel.getVariableName(), sel.getLocator()));
				} else {
					sel.setVariableName(c.getVariableName());
					results.add(sel);
				}
				break;

			case "textarea":
				TextArea texta = new TextArea(singleElement);

				c = isWebElementPresent(s.getWebElements(), texta);
				if (c == null) {
					results.add(texta);
					s.addWebElement(new CandidateWebElement(texta.getVariableName(), texta.getLocator()));
				} else {
					texta.setVariableName(c.getVariableName());
					results.add(texta);
				}
				break;

			default:
				break;
			}
		}

		return results;
	}

	/**
	 * checks whether the web element is present in the form list
	 * 
	 * @param webElements
	 * @param ff
	 * @return
	 */
	private static CandidateWebElement isWebElementPresent(Set<CandidateWebElement> webElements, FormField ff) {

		for (CandidateWebElement ce : webElements) {
			if (ce.getVariableName().toLowerCase().equals(ff.getVariableName().toLowerCase())) {
				return ce;
			}
		}

		return null;
	}

	/**
	 * gets a meaningful form name from the id and name attributes analysis to be
	 * used as a variable name
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	private static String getFormName(Form f, org.jsoup.nodes.Element e) {

		String formName = getNameFromAttributes(e);

		if (formName.length() == 0) {
			System.err.println("[WARNING]\tForm Name not found");
			System.err.println("[WARNING]\tCreating temporary variable name using form object hash code");
			formName = "form_" + String.valueOf(f.hashCode());
			formName = formatToVariableName(formName);
			f.setContainer(true);
		}

		return formName;
	}

	/**
	 * gets a meaningful element name from the id and name attributes analysis to be
	 * used as a variable name
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	public static String getElementName(org.jsoup.nodes.Element e) {

		String elementName = getNameFromAttributes(e);

		elementName = elementName.replace(".", "");
		elementName = elementName.replace(",", "");
		elementName = elementName.replace(":", "");
		elementName = elementName.replace(";", "");
		elementName = elementName.replace("/", "");
		elementName = elementName.replace("(", "");
		elementName = elementName.replace(")", "");
		elementName = elementName.replace("-", "");
		elementName = elementName.replace("!", "");
		elementName = elementName.replace("?", "");
		elementName = elementName.replace("[", "");
		elementName = elementName.replace("]", "");
		elementName = elementName.replace("{", "");
		elementName = elementName.replace("}", "");
		elementName = elementName.replace("'", "");
		elementName = elementName.replaceAll(" ", "");
		elementName = elementName.replaceAll("\n", "");
		elementName = elementName.replaceAll("\t", "");
		elementName = elementName.replace("™", "");
		elementName = elementName.replace("✓", "");
		elementName = elementName.replace("✘", "");
		elementName = elementName.replace("↓", "");
		elementName = elementName.replace("@", "_AT_");

		if (elementName.length() == 0) {
			System.err.println("[WARNING]\tCreating temporary variable name using element object hash code");
			elementName = e.tagName() + "_" + String.valueOf(e.hashCode());
			elementName = formatToVariableName(elementName);
		}

		return e.tagName() + "_" + elementName;
	}

	/**
	 * checks whether the attributes values to be used as variable name
	 * 
	 * @param id
	 * @param name
	 */
	public static String getNameFromAttributes(org.jsoup.nodes.Element e) {

		String id = e.attr("id");
		String name = e.attr("name");
		String value = e.attr("value");
		String _class = e.attr("class");
		String action = e.attr("action");

		String text = e.text();
		text = text.replaceAll("\t", "_");
		text = text.replaceAll("\n", "_");
		text = text.replaceAll(" ", "_");
		text = text.replaceAll("-", "_");

		if (id.length() != 0) {
			id = id.replace("-", "_");
			return id;
		} else if (name.length() != 0) {
			return name;
		} else if (value.length() != 0) {
			return value;
		} else if (_class.length() != 0) {
			return _class;
		} else if (action.length() != 0) {
			// retrieves the page name
			action = action.substring(action.lastIndexOf('/') + 1, action.length());
			// removes the php extension
			action = action.replace(".php", "");
			// removes the & and ?
			if (action.contains("?"))
				action = action.substring(0, action.indexOf('?'));
			// camel case the string
			action = toSentenceCase(action);
			return action;
		} else if (text.length() != 0) {
			if (text.length() > 35)
				text.substring(0, 10);
			return text;
		} else {
			System.err.println("[WARNING]\tName not found: UtilsStaticAnalyzer.getNameFromAttributes");
			return "";
		}

	}

	/**
	 * pretty prints the list of the states objects retrieved by Crawljax
	 * 
	 * @param List<State>
	 *            a list of State objects
	 */
	public static void printStatesList(List<State> l) {
		String s = "****** STATES LIST ******";
		System.out.println(s);
		for (State state : l) {
			System.out.println(state.toString());
		}
		for (int i = 0; i < s.length(); i++)
			System.out.println("*");
	}

	/**
	 * returns the state name associated at the Crawljax id given as parameter,
	 * empty string otherwise
	 * 
	 * @param stateid
	 * @return
	 */
	public static String getStateNameFromStateId(String stateid) {
		for (State s : StaticAnalyzer.getStatesList()) {
			if (s.getStateId().equals(stateid)) {
				return s.getName();
			}
		}
		return "";
	}

	/**
	 * BETA VERSION: trims the url of the web page, to get an meaningful name for
	 * the PO test the correct use of last index of and extension removal
	 * 
	 * @param statesList
	 * @throws MalformedURLException
	 */
	public static String getClassNameFromUrl(State state, List<State> statesList) throws MalformedURLException {

		// new add: check it does not lead to inconsistencies
		if (state.getStateId().equals("index")) {
			return toSentenceCase("index");
		}

		String s = state.getUrl();

		String toTrim = "";
		URL u = new URL(s);

		toTrim = u.toString();

		// retrieves the page name
		toTrim = toTrim.substring(toTrim.lastIndexOf('/') + 1, toTrim.length());
		// removes the php extension if any
		toTrim = toTrim.replace(".php", "");
		// removes the html extension if any
		toTrim = toTrim.replace(".html", "");
		// removes the & and ?
		if (toTrim.contains("?"))
			toTrim = toTrim.substring(0, toTrim.indexOf('?'));
		// camel case the string
		toTrim = toSentenceCase(toTrim);
		// check the uniqueness, solve the ambiguity otherwise
		toTrim = solveAmbiguity(toTrim, statesList);

		if (toTrim == "") {
			toTrim = u.getFile();

			// retrieves the page name
			toTrim = toTrim.substring(toTrim.lastIndexOf('/') + 1, toTrim.length());
			// removes the php extension if any
			toTrim = toTrim.replace(".php", "");
			// removes the html extension if any
			toTrim = toTrim.replace(".html", "");
			// removes the & and ?
			if (toTrim.contains("?"))
				toTrim = toTrim.substring(0, toTrim.indexOf('?'));
			// camel case the string
			toTrim = toSentenceCase(toTrim);
			// check the uniqueness, solve the ambiguity otherwise
			toTrim = solveAmbiguity(toTrim, statesList);

		}

		if (toTrim == "") {
			return toSentenceCase(state.getStateId());
		}

		if (NumberUtils.isNumber(toTrim) || NumberUtils.isDigits(toTrim)) {
			return toSentenceCase("PageObject_" + toTrim);
		}

		return toTrim;

	}

	/**
	 * TODO: to check the existence of states with same names the use of equals is
	 * not applicable. contains seems to work, but it has to be test with many
	 * different states
	 */
	private static String solveAmbiguity(String toTrim, List<State> statesList) {

		if (toTrim == "") {
			return "";
		}

		int occurrences = 0;
		for (State state : statesList) {
			if (state.getName().contains(toTrim)) {
				occurrences++;
			}
		}
		if (occurrences == 0) {
			return toTrim;
		} else
			return toTrim + "" + occurrences;
	}

	/**
	 * formats a string to camelCase
	 * 
	 * @param tempVarName
	 * @return
	 */
	private static String formatToVariableName(String tempVarName) {

		String res = tempVarName;

		res = toSentenceCase(res);
		res = res.replaceAll("\\s", "");
		res = StringUtils.uncapitalize(res);

		return res;
	}

	/**
	 * BETA VERSION: converts an URL to UpperCamelCase TODO: test the correct
	 * behavior
	 */
	public static String toSentenceCase(String inputString) {
		String result = "";
		if (inputString.length() == 0) {
			return result;
		}
		char firstChar = inputString.charAt(0);
		char firstCharToUpperCase = Character.toUpperCase(firstChar);
		result = result + firstCharToUpperCase;
		boolean terminalCharacterEncountered = false;
		char[] terminalCharacters = { '.', '?', '!', '_', ' ', '-' };
		for (int i = 1; i < inputString.length(); i++) {
			char currentChar = inputString.charAt(i);
			if (terminalCharacterEncountered) {
				if (currentChar == ' ') {
					result = result + currentChar;
				} else {
					char currentCharToUpperCase = Character.toUpperCase(currentChar);
					result = result + currentCharToUpperCase;
					terminalCharacterEncountered = false;
				}
			} else {
				char currentCharToLowerCase = Character.toLowerCase(currentChar);
				result = result + currentCharToLowerCase;
			}
			for (int j = 0; j < terminalCharacters.length; j++) {
				if (currentChar == terminalCharacters[j]) {
					terminalCharacterEncountered = true;
					break;
				}
			}
		}
		result = result.replaceAll("-", "");
		return result;
	}

	/**
	 * Retrieves the DOM associated to a state in the doms/ directory
	 * 
	 * @param state
	 * @return
	 * @throws IOException
	 */
	public static String getDOMFromDirectory(String state) throws IOException {

		String doms_dir = Settings.DOMS_DIR;

		String result = "";
		String fileToRetrieve = doms_dir + state + ".html";
		File f = new File(fileToRetrieve);
		try {
			result = FileUtils.readFileToString(f, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * parses the result.json creating a list of candidate web elements from the
	 * edges information
	 * 
	 * @param candidates
	 * @param failedEvents
	 * @return
	 * @throws Exception
	 */
	public static Set<CandidateWebElement> getCandidateWebElementsList(JSONArray candidates, JSONArray failedEvents,
			String state, String dom) throws Exception {

		Set<CandidateWebElement> candidateElements = new HashSet<CandidateWebElement>();
		int c = 0;

		for (int x = 0; x < candidates.size(); x++) {

			// System.err.println(candidates.size());

			boolean toSkip = false;
			JSONObject o = (JSONObject) candidates.get(x);
			String xp = (String) o.get("xpath");

			// the elements in failedEvents should not be considered
			if (failedEvents.size() != 0) {
				for (int ind = 0; ind < failedEvents.size(); ind++) {
					String failed = (String) failedEvents.get(ind);
					failed = failed.replace("xpath ", "");
					if (xp.equals(failed)) {
						toSkip = true;
					}
				}
			}

			if (toSkip == false) {
				// first tries to get a meaningful variable name
				String tempVarName = getSmartNameForWebElement(xp, dom, state, o, candidateElements);

				if (tempVarName.length() == 0) {
					tempVarName = state + "_var_" + o.hashCode();
					System.err.println("[WARNING] I couldn't find a meaningful field name for the element: " + xp
							+ "\nof state: " + state + " so I used an hashcode");
				}

				tempVarName = formatToVariableName(tempVarName);

				if (tempVarName.length() == 0) {
					System.err.println("[ERROR] Empty field name " + o);
					System.exit(1);
				} else if (isDuplicateVariableName(tempVarName, candidateElements)) {
					System.err.println("[WARNING] Duplicate field name " + tempVarName + " in state " + state);
					tempVarName = tempVarName.concat("_1");
				}

				Point p = new Point((long) o.get("top"), (long) o.get("left"));
				Dimension d = new Dimension((long) o.get("width"), (long) o.get("height"));

				CandidateWebElement cwe = new CandidateWebElement();
				cwe.setVariableName(tempVarName);
				cwe.setXpathLocator(xp);
				cwe.setCssLocator(null);
				cwe.setLocation(p);
				cwe.setDimension(d);

				c++;

				if (!candidateElements.contains(cwe))
					candidateElements.add(cwe);
			}
		}

		return candidateElements;
	}

	/**
	 * checks whether the parameter String name in input has already been used as
	 * variable name
	 * 
	 * @param tempVarName
	 * @param candidateElements
	 * @return true/false
	 */
	private static boolean isDuplicateVariableName(String tempVarName, Set<CandidateWebElement> candidateElements) {

		for (CandidateWebElement c : candidateElements) {
			if (c.getVariableName().contains(tempVarName))
				return true;
		}

		return false;
	}

	/**
	 * This function queries the DOM dom with the XPath xp and tries to associate a
	 * meaningful string to be used as a variable name for the retrieved tag element
	 * 
	 * @param xp
	 * @param dom
	 * @param o
	 * @param state
	 * @param candidateElements
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	static String getSmartNameForWebElement(String xp, String dom, String state, JSONObject o,
			Set<CandidateWebElement> candidateElements) throws UnsupportedEncodingException {

		String result = digForAMeaningfulName(xp, dom);

		if (result.length() == 0) {
			result = state + "_var" + o.hashCode();
			System.err.println("[WARNING] No meaningful variable name for the element: " + xp + "\nof state: " + state
					+ ": an hashcode has been used");
		}

		result = formatToVariableName(result);

		if (result.length() == 0) {
			System.err.println("[ERROR] Empty field name " + o);
			System.exit(1);
		} else if (isDuplicateVariableName(result, candidateElements)) {
			System.err.println("[WARNING] Duplicate field name " + result + " in state " + state);
			// tempVarName = tempVarName + o.hashCode();
		}

		return result;

	}

	private static String digForAMeaningfulName(String xp, String dom) throws UnsupportedEncodingException {

		xp = xp.toLowerCase();

		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);
		props.setOmitDoctypeDeclaration(true);

		TagNode node = cleaner.clean(dom);
		dom = "<html>\n" + cleaner.getInnerHtml(node) + "\n</html>";

		// workaround: htmlcleaner works with rel xpaths
		xp = xp.replace("html[1]/", "/");
		try {
			Object[] result = node.evaluateXPath(xp);

			if (result.length > 0) {
				TagNode r = (TagNode) result[0];
				return digTheTagTreeForAString(r);
			}

		} catch (XPatherException e) {
			e.printStackTrace();
		}

		// couldn't find a representative string :(

		return "";
	}

	/**
	 * aux function for getSmartNameForWebElement method
	 * 
	 * @param TagNode
	 *            t
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String digTheTagTreeForAString(TagNode t) throws UnsupportedEncodingException {

		// gli attributi da considerare sono stati presi da w3c.org
		switch (t.getName()) {
		case "div":
			/*
			 * // TODO: I DIV FORSE E' MEGLIO NON CONSIDERARLI E ANDARE GIU' NEI FIGLI
			 * if(t.getAttributeByName("id")!=null){ return t.getName() + "_" +
			 * t.getAttributeByName("id").toString(); } else if(t.getParent() != null){
			 * return t.getName() + "_" + digTheTagTreeForAString(t.getParent()); } else {
			 * for (TagNode child : t.getChildTagList()) { return child.getName() + "_" +
			 * digTheTagTreeForAString(child); } }
			 */
			break;
		case "a":
			// considero id, name
			if (t.getAttributeByName("id") != null) {
				return t.getName() + "_" + t.getAttributeByName("id").toString();
			} else if (t.getAttributeByName("name") != null) {
				return t.getName() + "_" + t.getAttributeByName("name").toString();
			}
			break;
		case "input":
			// considero id, name, value, type
			if (t.getAttributeByName("id") != null) {
				return t.getName() + "_" + t.getAttributeByName("id").toString();
			} else if (t.getAttributeByName("name") != null && !t.getAttributeByName("name").equals("new")) {
				return t.getName() + "_" + t.getAttributeByName("name").toString();
			} else if (t.getAttributeByName("value") != null) {
				return t.getName() + "_" + t.getAttributeByName("value").toString();
			} else if (t.getAttributeByName("type") != null) {
				return t.getName() + "_" + t.getAttributeByName("type").toString();
			}
			break;
		case "img":
			// considero alt
			if (t.getAttributeByName("alt") != null) {
				return t.getName() + "_" + t.getAttributeByName("alt").toString();
			}
			break;

		case "dd":
			if (t.getParent() != null) {

				TagNode parent = t.getParent();
				int childNumber = -1;
				for (TagNode tn : parent.getChildTagList()) {
					childNumber++;
					if (tn.equals(t)) {
						break;
					}
				}

				if (childNumber == 0) {
					return t.getName() + "_" + digTheTagTreeForAString(parent);
					// return digTheTagTreeForAString(parent);
				} else if (childNumber != -1) {
					return t.getName() + "_" + digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
					// return digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
				}
			}
			break;

		case "li":
			if (t.getParent() != null) {

				TagNode parent = t.getParent();
				int childNumber = -1;
				for (TagNode tn : parent.getChildTagList()) {
					childNumber++;
					if (tn.equals(t)) {
						break;
					}
				}

				if (childNumber == 0) {
					return t.getName() + "_" + digTheTagTreeForAString(parent);
					// return digTheTagTreeForAString(parent);
				} else if (childNumber != -1) {
					return t.getName() + "_" + digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
					// return digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
				}
			}
			break;

		case "td":
			if (t.getParent() != null && t.getParent().getName().equals("th")) {

				TagNode parent = t.getParent();

				int childNumber = -1;
				for (TagNode tn : parent.getChildTagList()) {
					childNumber++;
					if (tn.equals(t)) {
						break;
					}
				}

				if (childNumber == 0) {
					return t.getName() + "_" + digTheTagTreeForAString(parent);
				} else if (childNumber != -1) {
					return t.getName() + "_" + digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
				}

			}
			break;

		default:

			// if(t.getParent() != null){
			// TagNode parent = t.getParent();
			// return t.getName() + "_" + digTheTagTreeForAString(parent);
			// }
			// else {
			//
			// for (TagNode child : t.getChildTagList()) {
			// return child.getName() + "_" + digTheTagTreeForAString(child);
			// }
			//
			// }
			break;
		}

		String s = getTextFromNode(t);

		if (!s.isEmpty())
			return s;

		// if(t.getParent() != null){
		// TagNode parent = t.getParent();
		// return t.getName() + "_" + digTheTagTreeForAString(parent);
		// }
		if (t.getParent() != null) {

			TagNode parent = t.getParent();
			int childNumber = -1;
			for (TagNode tn : parent.getChildTagList()) {
				childNumber++;
				if (tn.equals(t)) {
					break;
				}
			}

			if (childNumber == 0) {
				return t.getName() + "_" + digTheTagTreeForAString(parent);
			} else if (childNumber != -1) {
				return t.getName() + "_" + digTheTagTreeForAString(parent.getChildTags()[--childNumber]);
			}
		} else {
			for (TagNode child : t.getChildTagList()) {
				return child.getName() + "_" + digTheTagTreeForAString(child);
			}
		}

		return "";
	}

	public static TagNode findThead(List<TagNode> list) {
		for (TagNode tagNode : list) {
			if (tagNode.getName().equals("thead")) {
				return tagNode;
			}
		}
		return null;
	}

	public static String getTextFromNode(TagNode t) throws UnsupportedEncodingException {

		// first try to see if there is some interesting text in the node itself
		CharSequence nodeText = t.getText();
		String s = nodeText.toString().trim();

		// returns either: 1) text or 2) attributes or 3) children
		// I'm not considering 1 char or long texts
		if (s.length() != 0 /* && nodeText.length() <= 35 && nodeText.length() > 1 */) {
			if (s.length() > 35) {
				s = s.substring(0, 25);
			}
			s = s.replace(".", "");
			s = s.replace(",", "");
			s = s.replace(":", "");
			s = s.replace(";", "");
			s = s.replace("/", "");
			s = s.replace("(", "");
			s = s.replace(")", "");
			s = s.replace("-", "");
			s = s.replace("!", "");
			s = s.replace("?", "");
			s = s.replace("[", "");
			s = s.replace("]", "");
			s = s.replace("{", "");
			s = s.replace("}", "");
			s = s.replace("'", "");
			s = s.replace(" ", "");
			s = s.replace("\n", "");
			s = s.replace("\t", "");
			s = s.replace("\b", "");
			s = s.replace("™", "");
			s = s.replace("✓", "");
			s = s.replace("✘", "");
			s = s.replace("↓", "");
			s = s.replace("@", "_AT_");
			s = s.replace("\"", "");
			s = s.replace(String.valueOf((char) 160), "_");

			if (!s.isEmpty()) {
				return t.getName() + "_" + s;
			}
		}

		return "";
	}

	public static String getSmartNameForWebElementGetter(String xp, String dom, String state)
			throws UnsupportedEncodingException {

		String result = digForAMeaningfulName(xp, dom);

		if (result.length() == 0) {
			result = state + "_var" + state.hashCode();
			System.err.println("[WARNING] I couldn't find a meaningful field name for the element: " + xp
					+ "\nof state: " + state + " so I used an hashcode");
		}

		result = formatToVariableName(result);

		if (result.length() == 0) {
			System.err.println("[ERROR] Empty field name " + state + " - " + xp);
			System.exit(1);
		}

		result = result.replace(".", "");
		result = result.replace(":", "");
		result = result.replace("/", "");
		result = result.replace("(", "");
		result = result.replace(")", "");
		result = result.replace("-", "");
		result = result.replace("!", "");
		result = result.replace("[", "");
		result = result.replace("]", "");
		result = result.replace("{", "");
		result = result.replace("}", "");
		result = result.replace(" ", "");
		result = result.replace("&", "");
		result = result.replace(String.valueOf((char) 160), "");

		return result;
	}

	// /**
	// * fills the statesList with the information parsed from result.json
	// * @param clustering
	// * @return
	// */
	// public static List<State> createStateObjects(final List<State> statesList,
	// final String gen_po_dir, final String out_dir, String clustering){
	//
	// JSONParser parser = new JSONParser();
	//
	// try {
	//
	// FileUtils.deleteDirectory(new File(gen_po_dir));
	// int total_getters = 0;
	//
	// Object obj = null;
	//
	// if(clustering.equals("y")){
	// obj = parser.parse(new FileReader(out_dir + "resultAfterMerging.json"));
	// }
	// else {
	// obj = parser.parse(new FileReader(out_dir + "result.json"));
	// }
	//
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// JSONObject states = (JSONObject) jsonObject.get("states");
	//
	// JSONArray edges = (JSONArray) jsonObject.get("edges");
	//
	// for (Object state : states.keySet()) {
	//
	// JSONObject stateObject = (JSONObject) states.get(state);
	//
	// State s = new State((String) stateObject.get("name"), (String)
	// stateObject.get("url"));
	// String tempClassName = UtilsStaticAnalyzer.getClassNameFromUrl(s,
	// statesList);
	//
	// s.setName(tempClassName);
	//
	// String dom = UtilsStaticAnalyzer.getDOMFromDirectory(s.getStateId());
	// s.setDom(dom);
	//
	// JSONArray candidates = (JSONArray) stateObject.get("candidateElements");
	// JSONArray failedEvents = (JSONArray) stateObject.get("failedEvents");
	//
	// Set<CandidateWebElement> totalListOfWebElements = new
	// HashSet<CandidateWebElement>();
	// totalListOfWebElements = getCandidateWebElementsList(candidates,
	// failedEvents, s.getStateId(), s.getDom());
	//
	// // TODO: create web elements from form fields. DIFFICULT
	// //getWebElementsContainedInForms(s);
	//
	// s.setWebElements(totalListOfWebElements);
	//
	// setConnections(state, edges, s);
	//
	// s.setForms(createFormObjects(s));
	//
	// if(clustering.equals("y")){
	// Set<Diff> list_diff = UtilsStaticAnalyzer.retrieveDiffsFromFile((String)
	// stateObject.get("name"), out_dir);
	// s.setDiffs(list_diff);
	//
	// System.out.println("[LOG] " + list_diff.size() + " getter(s) found in " +
	// s.getName());
	// total_getters += list_diff.size();
	// }
	// statesList.add(s);
	// }
	//
	// UtilsStaticAnalyzer.printStatesList(statesList);
	// System.out.println("[LOG] Total generated getter(s): " + total_getters);
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return statesList;
	// }
}
