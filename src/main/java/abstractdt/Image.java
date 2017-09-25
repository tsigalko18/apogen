package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

/**
 * Class representing an abstraction of the 
 * Image Element w.r.t. W3C specification
 * @author tsigalko18
 *
 */
public class Image extends FormField {

	protected String src, alt, name, height, width; 
	
	public Image(Element singleElement) {
		super();
		tag = "img";
		this.defaultAction = "click";
		this.variableName = UtilsStaticAnalyzer.getElementName(singleElement);
		this.locator = singleElement.cssSelector();
	}
	
	/**
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
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
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	@Override
	void revealMe() {
		System.out.println("Hello, I am an " + this.getTag());
	}
	
}
