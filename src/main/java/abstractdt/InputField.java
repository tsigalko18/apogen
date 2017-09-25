package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

/**
 * Class representing an abstraction of the 
 * Input Element w.r.t. W3C specification.
 * @author tsigalko18
 *
 */
public class InputField extends FormField {
	
	protected String type, alt;
	protected boolean isMethod;
	
	public InputField(Element singleElement) {
		super();
		tag = "input";
		this.type = "text"; // default type
		this.variableName = UtilsStaticAnalyzer.getElementName(singleElement);
		this.locator = singleElement.cssSelector();
		this.isMethod = false;
	}
	
	public InputField(String name, String css_locator) {
		super();
		tag = "input";
		this.type = "text"; // default type
		this.variableName = name;
		this.locator = css_locator;
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
	
	/**
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}

	/**
	 * @param alt the alt to set
	 */
	public void setAlt(String alt) {
		this.alt = alt;
	}
	
	/**
	 * @return the isMethod
	 */
	public boolean isMethod() {
		return isMethod;
	}

	/**
	 * @param isMethod the isMethod to set
	 */
	public void setMethod(boolean isMethod) {
		this.isMethod = isMethod;
	}

	public void revealWhoIAm(){
		System.out.println("Hello, I am an " + this.getTag() + " of type " + this.getType());
	}
	
}
