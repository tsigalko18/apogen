package abstractdt;

import org.jsoup.nodes.Element;

public class CheckBoxField extends InputField {

	protected boolean isChecked;
	
	public CheckBoxField(Element singleElement) {
		super(singleElement);
		this.type = "checkbox";
		this.isChecked = false;
		this.defaultAction = "click";
	}

	/**
	 * @return the isChecked
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/**
	 * @param isChecked the isChecked to set
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
