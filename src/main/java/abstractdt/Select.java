package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

public class Select extends FormField {

	public Select(Element singleElement) {
		super();
		tag = "select";
		this.variableName = UtilsStaticAnalyzer.getElementName(singleElement);
		this.locator = singleElement.cssSelector();
	}

}
