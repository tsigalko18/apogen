package abstractdt;

import org.jsoup.nodes.Element;

public class PasswordField extends InputField {

	public PasswordField(Element singleElement) {
		super(singleElement);
		this.type = "password";
	}

}
