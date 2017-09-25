package abstractdt;

import java.util.HashSet;
import java.util.Set;
import abstractdt.Getter;

/**
 * Class State represents a web application state 
 * as it is saved by Crawljax
 * 
 * @author Andrea Stocco
 *
 */
public class State {
	
	private String stateId;
	private String url;
	private String name;
	private String dom;
	private Set<CandidateWebElement> webElements;
	private Set<Edge> links;
	private Set<Form> forms;
	private Set<Getter> differences;
	private String sourceCode;

	/**
	 * full constructor for the State class
	 * @param stateId
	 * @param url
	 * @param name
	 * @param webElements
	 * @param dom
	 */
	public State(
			String stateId, String url, String name,
			Set<CandidateWebElement> webElements, String dom) {
		
		setStateId(stateId);
		setUrl(url);
		setLinks(new HashSet<Edge>());
		setName(name);
		setWebElements(webElements);
		setDom(dom);
	}
	
	/**
	 * default constructor for the State class
	 * @param stateId
	 * @param url
	 */
	public State(String stateId, String url){
		setStateId(stateId);
		setUrl(url);
		setLinks(new HashSet<Edge>());
	}

	/**
	 * get the state id
	 * @return
	 */
	public String getStateId() {
		return stateId;
	}

	/**
	 * set the state id
	 * @param stateId
	 */
	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	/**
	 * get the url
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * set the url
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * get the name
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * get the web elements list
	 * @return
	 */
	public Set<CandidateWebElement> getWebElements() {
		return webElements;
	}
	
	/**
	 * set the web elements list
	 * @param webElements
	 */
	public void setWebElements(Set<CandidateWebElement> webElements) {
		this.webElements = webElements;
	}
	
	/**
	 * adds a web element to the list if not already present
	 * @param e
	 */
	public void addWebElement(CandidateWebElement e){
		if(!this.webElements.contains(e)){
			this.webElements.add(e);
		}
	}

	/**
	 * get the DOM
	 * @return
	 */
	public String getDom() {
		return dom;
	}

	/**
	 * set the DOM
	 */
	public void setDom(String dom) {
		this.dom = dom;
	}

	/**
	 * get the links
	 * @return
	 */
	public Set<Edge> getLinks() {
		return links;
	}

	/**
	 * set the links
	 * @param links
	 */
	public void setLinks(Set<Edge> links) {
		this.links = links;
	}
	
	/**
	 * @return the forms
	 */
	public Set<Form> getForms() {
		return forms;
	}

	/**
	 * @param forms the forms to set
	 */
	public void setForms(Set<Form> forms) {
		this.forms = forms;
	}

	/**
	 * add a connection to the set of links
	 * @param c
	 */
	public void addConnection(Edge c) {
		if(!this.links.contains(c)){
			this.links.add(c);	
		}
	}
	
	/**
	 * @return the sourceCode
	 */
	public String getSourceCode() {
		return sourceCode;
	}

	/**
	 * @param sourceCode the sourceCode to set
	 */
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public Set<Getter> getDiffs() {
		return differences;
	}

	public void setDiffs(Set<Getter> differences) {
		this.differences = differences;
	}
	
	/**
	 * get the variable name associated to the XPath locator
	 * given as parameter. It returns an empty string
	 * otherwise
	 * @param xpathLocator
	 * @return
	 */
	public String getWebElementNameFromLocator(String xpathLocator){
		if(xpathLocator==null) {
			System.err.println(xpathLocator);
		}
		for (CandidateWebElement c : this.webElements) {
			if(c.getXpathLocator() != null && c.getXpathLocator().equals(xpathLocator)){
				return c.getVariableName();
			}
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "State [" + (stateId != null ? "stateId=" + stateId + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (links != null ? "links=" + links : "") + "]";
	}

}
