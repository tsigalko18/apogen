package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

/**
 * Class representing an abstraction of the 
 * Anchor Element w.r.t. W3C specification
 * 
 * @author tsigalko18
 *
 */
public class Anchor extends FormField {

	protected String type, name, linkText;
	
	public Anchor(Element singleElement) {
		super();
		tag = "a";
		this.defaultAction = "click";
		this.variableName = UtilsStaticAnalyzer.getElementName(singleElement);
		this.locator = singleElement.cssSelector();
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
	 * @return the linkText
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * @param linkText the linkText to set
	 */
	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	@Override
	void revealMe() {
		System.out.println("Hello, I am an " + this.getTag());
	}

}
