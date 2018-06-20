package abstractdt;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.dom.Document;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.StateVertex;
import com.crawljax.util.DomUtils;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import utils.UtilsClustering;

public class StateVertexLevensteinEquals implements StateVertex {

	private static final long serialVersionUID = 1L;
	private int id;
	private String dom;
	private String strippedDom;
	private String url;
	private String name;
	private ImmutableList<CandidateElement> candidateElements;

	public StateVertexLevensteinEquals(int id, String url, String name, String dom, String strippedDom) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
	}

	public int hashCode() {
		return Objects.hashCode(strippedDom);
	}

	public boolean equals(Object object) {

		if (object instanceof StateVertex) {
			StateVertex that = (StateVertex) object;
			try {
				return UtilsClustering.similarAccordingToDomDiversity(0.01, this.getDom(), that.getDom());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;

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
