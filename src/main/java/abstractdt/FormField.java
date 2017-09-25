package abstractdt;

public abstract class FormField {
	
	protected String tag, id, _class, title;
	protected String variableName, defaultAction, locator;
	
	public FormField() {
		super();
		this.defaultAction = "sendKeys";
	}
	
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the _class
	 */
	public String get_class() {
		return _class;
	}

	/**
	 * @param _class the _class to set
	 */
	public void set_class(String _class) {
		this._class = _class;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * @return the defaultAction
	 */
	public String getDefaultAction() {
		return defaultAction;
	}

	/**
	 * @param defaultAction the defaultAction to set
	 */
	public void setDefaultAction(String defaultAction) {
		this.defaultAction = defaultAction;
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

	void revealMe() {
	}
}

