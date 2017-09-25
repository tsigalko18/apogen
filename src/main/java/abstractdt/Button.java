package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

public class Button extends FormField {

	protected String name, value;
	protected String type; // (button|submit|reset) submit 
	
	public Button(Element singleElement) {
		super();
		tag = "button";
		this.defaultAction = "click";
		this.variableName = UtilsStaticAnalyzer.getElementName(singleElement);
		this.locator = singleElement.cssSelector();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	
}
