package abstractdt;

import org.jsoup.nodes.Element;

public class HiddenField extends InputField {

	public HiddenField(Element singleElement) {
		super(singleElement);
		this.type = "hidden";
	}

}
