package utils;

import java.util.HashSet;
import java.util.Set;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

final class DomDifferenceListener implements DifferenceListener {

	public void skippedComparison(Node control, Node test) {
	}

	public int differenceFound(Difference difference) {

		Set<String> blackList = new HashSet<String>();
		blackList.add("br");
		blackList.add("style");
		blackList.add("script");

		if (difference.getControlNodeDetail() == null || difference.getControlNodeDetail().getNode() == null
				|| difference.getTestNodeDetail() == null || difference.getTestNodeDetail().getNode() == null) {
			return RETURN_ACCEPT_DIFFERENCE;
		}

		// if (ignoreAttributes.contains(difference.getTestNodeDetail().getNode()
		// .getNodeName())
		// || ignoreAttributes.contains(difference.getControlNodeDetail()
		// .getNode().getNodeName())) {
		// return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
		// }

		if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
			if (blackList.contains(difference.getControlNodeDetail().getNode().getParentNode().getNodeName())) {
				return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
		}

		return RETURN_ACCEPT_DIFFERENCE;
	}
}