package abstractdt;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Attribute;

/**
 * Class representing an abstraction of the 
 * Form Element w.r.t. W3C specification
 * A Form can contain several types of controls
 * <p>
 * <!ENTITY % InputType
  "(TEXT | PASSWORD | CHECKBOX |
    RADIO | SUBMIT | RESET |
    FILE | HIDDEN | IMAGE | BUTTON)"
   >
   <p>
   LABEL, SELECT, OPTGROUP, OPTION,
   TEXTAREA, FIELDSET, LEGEND, BUTTON
 *
 * @author tsigalko18
 *
 */
public class Form {
	
	protected String id, _class, title, formName, returnValue; // NB: name is deprecated in XHTML
	private List<Attribute> attributes;
	protected List<FormField> formFieldList;
	private boolean isContainer = false, isSubmit = true;
	
	public Form() {
		
	}
	
	/**
	 * @return the attributes
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	

	/**
	 * @param attribute add the attribute to the list
	 */
	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
	}

	/**
	 * @return the formName
	 */
	public String getFormName() {
		return formName;
	}

	/**
	 * @param formName the formName to set
	 */
	public void setFormName(String formName) {
		this.formName = formName;
	}

	/**
	 * @return the returnValue
	 */
	public String getReturnValue() {
		return returnValue;
	}

	/**
	 * @param returnValue the returnValue to set
	 */
	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	/**
	 * @return the formFieldList
	 */
	public List<FormField> getFormFieldList() {
		return formFieldList;
	}

	/**
	 * @param formFieldList the formFieldList to set
	 */
	public void setFormFieldList(List<FormField> formFieldList) {
		this.formFieldList = formFieldList;
	}
	
	/**
	 * gets the list of the input field of type submit/button
	 * in order to create the methods
	 * @return
	 */
	public List<InputField> getSubmitList(){
		List<InputField> submits = new LinkedList<InputField>();
		
		for (FormField inputField : this.getFormFieldList()) {
			if(inputField instanceof InputField && 
					(((InputField) inputField).type.equals("submit") || 
							((InputField) inputField).type.equals("button"))){
				submits.add((InputField) inputField);	
			}
		}
		return submits;
	}
	
	/**
	 * sets isContainer property
	 * @param b
	 */
	public void setContainer(boolean b){
		this.isContainer = b;
	}
	
	/**
	 * returns true if the form is used as a
	 * container of elements
	 * @return
	 */
	public boolean isContainer(){
		return isContainer;
	
	}
	
	/**
	 * returns true if the form is designed
	 * to submit values
	 * @return
	 */
	public boolean isSubmit(){
		return isSubmit;
		
	}
	
	/**
	 * auxiliary method to populate the form fields 
	 * @param args
	 */
	public void populateForm(FormField... args){
		for (FormField ff : args) {
			this.getFormFieldList().add(ff);
		}
	}
	
	/**
	 * auxiliary method to submit the form information
	 */
	public void submitForm(){
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((formName == null) ? 0 : formName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Form other = (Form) obj;
		if (formName == null) {
			if (other.formName != null)
				return false;
		} else if (!formName.equals(other.formName))
			return false;
		return true;
	}
	
}
