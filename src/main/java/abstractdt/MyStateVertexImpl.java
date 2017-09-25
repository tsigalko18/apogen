package abstractdt;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.dom.Document;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.StateVertex;
import com.crawljax.util.DomUtils;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class MyStateVertexImpl implements StateVertex {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int id;
	private final String dom;
	private final String strippedDom;
	private final String url;
	private String name;
	
	private ImmutableList<CandidateElement> candidateElements;
	
	public MyStateVertexImpl(int id, String name, String dom) {
		this(id, null, name, dom, dom);
	}

	/**
	 * Defines a State.
	 * 
	 * @param url
	 *            the current url of the state
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 * @param strippedDom
	 *            the stripped dom by the OracleComparators
	 */
	public MyStateVertexImpl(int id, String url, String name, String dom, String strippedDom) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
	}

	
	public String getName() {
		return name;
	}

	
	public String getDom() {
		return dom;
	}

	
	public String getStrippedDom() {
		return strippedDom;
	}

	
	public String getUrl() {
		return url;
	}

	
	public int hashCode() {
		return Objects.hashCode(strippedDom);
	}

	
	public boolean equals(Object object) {
		if (object instanceof StateVertex) {
			StateVertex that = (StateVertex) object;
			return Objects.equal(this.strippedDom, that.getStrippedDom());
		}
		return false;
	}

	
	public int getId() {
		return id;
	}

	
	public Document getDocument() throws IOException {
		return DomUtils.asDocument(this.dom);
	}

	
	public void setElementsFound(LinkedList<CandidateElement> elements) {
		this.candidateElements = ImmutableList.copyOf(elements);
	}

	
	public ImmutableList<CandidateElement> getCandidateElements() {
		return candidateElements;
	}

}
