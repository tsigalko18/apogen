package abstractdt;

import org.jsoup.nodes.Element;

public class RadioField extends InputField {

	protected boolean isChecked;
	
	public RadioField(Element singleElement) {
		super(singleElement);
		this.type = "radio";
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
