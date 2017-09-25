package abstractdt;

/**
 * CandidateWebElement is a class representing an abstraction of the candidate
 * elements as they are retrieved by Crawljax
 * 
 * @author tsigalko18
 * 
 */
public class CandidateWebElement {

	private String variableName;
	private String xpathLocator;
	private String cssLocator;
	private Point location;
	private Dimension dimension;
	private boolean isAssert;

	public CandidateWebElement(String variableName, String locator,
			Point location, Dimension dimension) {
		super();
		this.variableName = variableName;
		this.xpathLocator = locator;
		this.location = location;
		this.dimension = dimension;
		this.isAssert = false;
	}

	/**
	 * @return the isAssert
	 */
	public boolean isAssert() {
		return isAssert;
	}

	/**
	 * @param isAssert
	 *            the isAssert to set
	 */
	public void setAssert(boolean isAssert) {
		this.isAssert = isAssert;
	}

	public CandidateWebElement(String variableName, String css_locator) {
		super();
		this.variableName = variableName;
		this.cssLocator = css_locator;
	}

	/**
	 * default empty constructor
	 */
	public CandidateWebElement() {
		super();
	}

	/**
	 * get variable name
	 * 
	 * @return
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * set variable name
	 * 
	 * @param variableName
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * @return the xpathLocator
	 */
	public String getXpathLocator() {
		return xpathLocator;
	}

	/**
	 * @param xpathLocator
	 *            the xpathLocator to set
	 */
	public void setXpathLocator(String xpathLocator) {
		this.xpathLocator = xpathLocator;
	}

	/**
	 * @return the cssLocator
	 */
	public String getCssLocator() {
		return cssLocator;
	}

	/**
	 * @param cssLocator
	 *            the cssLocator to set
	 */
	public void setCssLocator(String cssLocator) {
		this.cssLocator = cssLocator;
	}

	/**
	 * get location, i.e., top-left point coordinates where the element is
	 * displayed on the screen
	 * 
	 * @return
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * set location
	 * 
	 * @param location
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * get dimension, i.e., width and height of the rectangle containing the
	 * rendered web element on the screen
	 * 
	 * @return
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/**
	 * set dimension
	 * 
	 * @param dimension
	 */
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CandidateWebElement ["
				+ (variableName != null ? "variableName=" + variableName : "")
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((xpathLocator == null) ? 0 : xpathLocator.hashCode());
		result = prime * result
				+ ((variableName == null) ? 0 : variableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CandidateWebElement other = (CandidateWebElement) obj;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		else if (variableName.toLowerCase().equals(other.variableName.toLowerCase())
					&& (xpathLocator == null || cssLocator == null)
					&& (other.xpathLocator == null || other.cssLocator == null))
				return true;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

}
