package abstractdt;

public class Getter {

	private String sourceState;
	private String targetState;
	private String cause;
	private String before;
	private String after;
	private String locator;
	private String webElementName;
	
	public Getter(String source, String target, String cause, String before, String after, String locator, String we) {
		
		super();
		this.sourceState = source;
		this.targetState = target;
		this.cause = cause;
		this.before = before;
		this.after = after;
		this.locator = locator;
		this.webElementName = we;
	}
	
	/**
	 * @return the sourceState
	 */
	public String getSourceState() {
		return sourceState;
	}

	/**
	 * @param sourceState the sourceState to set
	 */
	public void setSourceState(String sourceState) {
		this.sourceState = sourceState;
	}

	/**
	 * @return the targetState
	 */
	public String getTargetState() {
		return targetState;
	}

	/**
	 * @param targetState the targetState to set
	 */
	public void setTargetState(String targetState) {
		this.targetState = targetState;
	}

	public Getter() {
		super();
	}
	
	/**
	 * @return the cause
	 */
	public String getCause() {
		return cause;
	}
	/**
	 * @param cause the cause to set
	 */
	public void setCause(String cause) {
		this.cause = cause;
	}
	/**
	 * @return the before
	 */
	public String getBefore() {
		return before;
	}
	/**
	 * @param before the before to set
	 */
	public void setBefore(String before) {
		this.before = before;
	}
	/**
	 * @return the after
	 */
	public String getAfter() {
		return after;
	}
	/**
	 * @param after the after to set
	 */
	public void setAfter(String after) {
		this.after = after;
	}
	/**
	 * @return the locator
	 */
	public String getLocator() {
		return locator;
	}
	/**
	 * @param locator the locator to set
	 */
	public void setLocator(String locator) {
		this.locator = locator;
	}

	public String getWebElementName() {
		return webElementName;
	}

	public void setWebElementName(String webElementName) {
		this.webElementName = webElementName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sourceState == null) ? 0 : sourceState.hashCode());
		result = prime * result
				+ ((webElementName == null) ? 0 : webElementName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Getter other = (Getter) obj;
		if (sourceState == null) {
			if (other.sourceState != null)
				return false;
		} else if (!sourceState.equals(other.sourceState))
			return false;
		if (webElementName == null) {
			if (other.webElementName != null)
				return false;
		} else if (!webElementName.equals(other.webElementName))
			return false;
		return true;
	}
}
