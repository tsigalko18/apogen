package abstractdt;

import org.jsoup.nodes.Element;

public class ImageField extends InputField {

	public ImageField(Element singleElement) {
		super(singleElement);
		this.type = "image";
		this.defaultAction = "click";
	}

}
