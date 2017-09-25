package abstractdt;

import org.jsoup.nodes.Element;

import utils.UtilsStaticAnalyzer;

public class TextArea extends FormField {

	protected String name, rows, cols;
	
	public TextArea(Element singleElement) {
		super();
		tag = "textarea";
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
	 * @return the rows
	 */
	public String getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(String rows) {
		this.rows = rows;
	}

	/**
	 * @return the cols
	 */
	public String getCols() {
		return cols;
	}

	/**
	 * @param cols the cols to set
	 */
	public void setCols(String cols) {
		this.cols = cols;
	}

	
	
}
