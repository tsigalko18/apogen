package utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.XMLUnit;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import abstractdt.Getter;

public class UtilsDiff {

	/**
	 * transforms a string into a Document object.
	 * 
	 * @param html
	 *            the HTML string.
	 * @return The DOM Document version of the HTML string.
	 * @throws IOException
	 *             if an IO failure occurs.
	 * @throws SAXException
	 *             if an exception occurs while parsing the HTML string.
	 */
	public static Document asDocument(String html, boolean useNamespace) throws IOException {
		DOMParser domParser = new DOMParser();
		try {
			domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
			domParser.setFeature("http://xml.org/sax/features/namespaces", useNamespace);
			domParser.parse(new InputSource(new StringReader(html)));
		} catch (SAXException e) {
			throw new IOException("Error while reading HTML: " + html, e);
		}
		return domParser.getDocument();
	}

	/**
	 * BETA version of APOGEN-DOM-differencing mechanism
	 * 
	 * @param doc1
	 * @param doc2
	 * @return list of Differences
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	@SuppressWarnings("unchecked")
	public static List<Difference> customisedDomDiff(String string, String string2)
			throws ParserConfigurationException, SAXException, IOException {

		org.w3c.dom.Document doc1 = asDocument(string, true);
		org.w3c.dom.Document doc2 = asDocument(string2, true);

		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");

		XMLUnit.setNormalizeWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setNormalize(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);

		Diff d = new Diff(doc1, doc2);
		DetailedDiff dd = new DetailedDiff(d);

		dd.overrideDifferenceListener(new DomDifferenceListener());
		dd.overrideElementQualifier(null);

		return dd.getAllDifferences();
	}

	/**
	 * prints diff
	 * 
	 * @param target
	 * @param source
	 * @param diff
	 * @throws IOException
	 */
	public static List<abstractdt.Getter> printsDiff(String source, String target, List<Difference> diff)
			throws IOException {

		List<abstractdt.Getter> personalisedDiffList = new LinkedList<abstractdt.Getter>();

		// PRETTY PRINTS RESULTS BASED ON THE KIND OF DIFFERENCE
		for (Difference difference : diff) {

			abstractdt.Getter proposedDiff = null;

			switch (difference.getId()) {

			// DIFFERENT TEXT VALUES: using getNodeValue() to retrieve the text
			case DifferenceConstants.TEXT_VALUE_ID:

				if (!difference.getTestNodeDetail().getValue().equals("TITLE")
						&& !difference.getTestNodeDetail().getValue().equals("SCRIPT")) {

					String newLocator = difference.getTestNodeDetail().getXpathLocation();

					if (newLocator.contains("text()")) {
						newLocator = newLocator.substring(0, newLocator.indexOf("text()") - 1);
					}

					String name = UtilsStaticAnalyzer.getSmartNameForWebElementGetter(newLocator,
							UtilsStaticAnalyzer.getDOMFromDirectory(target), target);

					proposedDiff = new abstractdt.Getter(source, target, "textual change",
							difference.getControlNodeDetail().getNode().getTextContent().trim(),
							difference.getTestNodeDetail().getNode().getTextContent().trim(), newLocator, name);

					if (!personalisedDiffList.contains(proposedDiff)) {
						personalisedDiffList.add(proposedDiff);
					}

					break;
				}

				break;

			// DIFFERENT CHILDREN NUMBER: using simple getNode() to get what is going on
			case DifferenceConstants.CHILD_NODE_NOT_FOUND_ID:

				// System.out.println("[LOG] *** DIFFERENT NUMBER OF CHILDREN DETECTED " +
				// difference.getId() + "***");

				// adding
				if (StringUtils.isEmpty(difference.getControlNodeDetail().getXpathLocation())
						&& !difference.getTestNodeDetail().getValue().equals("BR")
						&& !difference.getTestNodeDetail().getValue().equals("SCRIPT")
						&& !difference.getTestNodeDetail().getValue().equals("TITLE")) {

					// System.out.println("\tADDED ELEMENT");
					// System.out.println("\tadded element:\t" +
					// difference.getTestNodeDetail().getNode());
					// System.out.println("\tlocator\t: " +
					// difference.getTestNodeDetail().getXpathLocation());

					String newLocator = difference.getTestNodeDetail().getXpathLocation();

					if (newLocator.contains("text()")) {
						newLocator = newLocator.substring(0, newLocator.indexOf("text()") - 1);
					}

					String name = UtilsStaticAnalyzer.getSmartNameForWebElementGetter(newLocator,
							UtilsStaticAnalyzer.getDOMFromDirectory(target), target);

					proposedDiff = new abstractdt.Getter(source, target, "added element", null,
							difference.getTestNodeDetail().getNode().getNodeName(), newLocator, name);

					// NB: the equals has been redefined on the locator field!
					if (!personalisedDiffList.contains(proposedDiff)) {
						personalisedDiffList.add(proposedDiff);
					}

				}
				// deleting
				else if (StringUtils.isEmpty(difference.getTestNodeDetail().getXpathLocation())) {
					/*
					 * System.out.println("\tREMOVED ELEMENT"); // TODO: trattare questo caso?
					 * System.out.println("\tbefore:\t" +
					 * difference.getControlNodeDetail().getNode()); System.out.println("\tafter:\t"
					 * + difference.getTestNodeDetail().getNode());
					 * System.out.println("\tlocator\t: " +
					 * difference.getTestNodeDetail().getXpathLocation());
					 * 
					 * newName = difference.getTestNodeDetail().getXpathLocation();
					 * if(newName.contains("text()")){ newName = newName.substring(0,
					 * newName.indexOf("text()")-1); }
					 * 
					 * name = UtilsStaticAnalyzer.getSmartNameForWebElement(newName,
					 * UtilsStaticAnalyzer.getDOMFromDirectory(target));
					 * 
					 * 
					 * personalisedDiffList.add( new abstractdt.Diff(source, target,
					 * "removed element",
					 * difference.getControlNodeDetail().getNode().getNodeValue(), null, null,
					 * name));
					 */
				} else {
					/*
					 * System.out.println("\tbefore:\t" +
					 * difference.getControlNodeDetail().getNode()); System.out.println("\tafter:\t"
					 * + difference.getTestNodeDetail().getNode());
					 * System.out.println("\tlocator\t: " +
					 * difference.getTestNodeDetail().getXpathLocation());
					 * 
					 * newName = difference.getTestNodeDetail().getXpathLocation();
					 * if(newName.contains("text()")){ newName = newName.substring(0,
					 * newName.indexOf("text()")-1); }
					 * 
					 * name = UtilsStaticAnalyzer.getSmartNameForWebElementGetter(newName,
					 * UtilsStaticAnalyzer.getDOMFromDirectory(target), target);
					 * 
					 * proposedDiff = new abstractdt.Diff(source, target, "modified element",
					 * difference.getControlNodeDetail().getNode().getNodeValue(),
					 * difference.getTestNodeDetail().getNode().getNodeValue(),
					 * difference.getTestNodeDetail().getXpathLocation(), name);
					 * 
					 * // NB: the equals has been redefined on the locator field!
					 * if(!personalisedDiffList.contains(proposedDiff)){
					 * personalisedDiffList.add(proposedDiff); }
					 */
				}

				break;

			}

		}
		return personalisedDiffList;
	}
}
