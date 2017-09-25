package utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.plugins.crawloverview.CrawlOverview;

import abstractdt.StateVertexLevensteinEquals;
import apogen.Crawler;
import apogen.Settings;

/**
 * Utils class containing auxiliary and configuration methods for the Crawler
 * 
 * @author Andrea Stocco
 *
 */
public class UtilsCrawler {

	/**
	 * Personalized list of Crawljax crawling rules
	 * 
	 * @throws Exception
	 */
	public static void myCrawlRules(CrawljaxConfigurationBuilder builder) throws Exception {

		// read configurations
		long timeoutEvent = Settings.WAIT_TIME_AFTER_EVENT;
		long timeoutReload = Settings.WAIT_TIME_AFTER_RELOAD;
		String browser = Settings.BROWSER;

		// set where do/don't click

		builder.crawlRules().clickOnce(false);
		builder.crawlRules().insertRandomDataInInputForms(true);
		builder.crawlRules().clickElementsInRandomOrder(false); // it is default

		/**
		 * DEFAULT ELEMENTS click("a"); click("button");
		 * click("input").withAttribute("type", "submit");
		 * click("input").withAttribute("type", "button");
		 */
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().crawlFrames(true);
		builder.crawlRules().crawlHiddenAnchors(true);
		builder.crawlRules().click("input").withAttribute("type", "submit");
		builder.crawlRules().click("input").withAttribute("type", "button");
		builder.crawlRules().click("button").withAttribute("type", "submit");
		builder.crawlRules().click("th");

		// but don't click these
		builder.crawlRules().dontClick("a").withAttribute("href", "notes.htm");
		builder.crawlRules().dontClick("a").withAttribute("href", "/petclinic/vets.json");
		builder.crawlRules().dontClick("a").withAttribute("href", "/petclinic/vets.xml");

		// set depth / time limits
		int crawl_depth = Settings.CRAWL_DEPTH;
		long max_runtime = Settings.MAX_RUNTIME;
		int max_states = Settings.MAX_STATES;

		// if there is no pre-specified configuration, set no limits

		// set crawling depth
		if (crawl_depth != 0) {
			builder.setMaximumDepth(crawl_depth);
		} else {
			builder.setUnlimitedCrawlDepth();
		}

		// set crawling runtime
		if (max_runtime != 0) {
			builder.setMaximumRunTime(max_runtime, TimeUnit.MINUTES);
		} else {
			builder.setUnlimitedRuntime();
		}

		// set maximum states
		if (max_states != 0) {
			builder.setMaximumStates(max_states);
		} else {
			builder.setUnlimitedStates();
		}

		// set timeouts
		builder.crawlRules().waitAfterReloadUrl(timeoutReload, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(timeoutEvent, TimeUnit.MILLISECONDS);

		// set the type of browser (Firefox=FF, PhantomJS=PH)
		if (browser.equals("FF")) {
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		} else if (browser.equals("PH")) {
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.PHANTOMJS, 1));
		} else {
			throw new Exception("[ERROR]\tconfig.properties: browser not recognized (use FF, PH)");
		}

		// set custom state comparison
		setCustomStateVertexFactory(builder);

		// set input values
		if(Settings.USE_INPUT_SPECIFICATION)
			builder.crawlRules().setInputSpec(getInputSpecification());

		// add CrawlOverview plugin
		builder.addPlugin(new CrawlOverview());

		// CrawlOverview output
		File outFolder = new File(Settings.OUT_DIR);
		if (outFolder.exists()) {
			FileUtils.deleteDirectory(outFolder);
		}
		builder.setOutputDirectory(outFolder);

	}

	/**
	 * Input specification containing credentials The credentials are read from
	 * the app.properties file
	 * 
	 * @return InputSpecification
	 */
	public static InputSpecification getInputSpecification() {

		Properties configFile = new Properties();
		InputSpecification input = new InputSpecification();

		try {

			configFile.load(Crawler.class.getClassLoader().getResourceAsStream("app.properties"));

			String url = configFile.getProperty("URL");

			if (url.contains("addressbook")) {
				String user_field = configFile.getProperty("USERNAME_FIELD");
				String user_value = configFile.getProperty("USERNAME_VALUE");
				String password_field = configFile.getProperty("PASSWORD_FIELD");
				String password_value = configFile.getProperty("PASSWORD_VALUE");
				String submit_field = configFile.getProperty("SUBMIT_FIELD");
				String submit_text = configFile.getProperty("SUBMIT_TEXT");

				// fills the login form with the credentials
				Form loginForm = new Form();
				loginForm.field(user_field).setValue(user_value);
				loginForm.field(password_field).setValue(password_value);
				input.setValuesInForm(loginForm).beforeClickElement(submit_field).withText(submit_text);

				//// ADDRESS BOOK specific
				// fills the form to add a user in the addressbook
				Form addUser = new Form();
				addUser.field("firstname").setValue("Andrea");
				addUser.field("lastname").setValue("Stocco");
				addUser.field("bday").setValue("18");
				addUser.field("bmonth").setValue("August");
				addUser.field("byear").setValue("1985");
				input.setValuesInForm(addUser).beforeClickElement("submit").withAttribute("value", "Enter");
			}

			if (url.contains("petclinic")) {

				// FIND OWNER
				// String find_field =
				// configFile.getProperty("FINDOWNER_FIELD");
				// String find_value =
				// configFile.getProperty("FINDOWNER_VALUE");
				//
				// Form findOwner = new Form();
				// findOwner.field(find_field).setValue(find_value);
				// input.setValuesInForm(findOwner).beforeClickElement("button").withText("Find
				// Owner");

				// ADD OWNER
				String firstname_field = configFile.getProperty("NEW_OWNER_FIRSTNAME_FIELD");
				String firstname_value = configFile.getProperty("NEW_OWNER_FIRSTNAME_VALUE");
				String lastname_field = configFile.getProperty("NEW_OWNER_LASTNAME_FIELD");
				String lastname_value = configFile.getProperty("NEW_OWNER_LASTNAME_VALUE");
				String address_field = configFile.getProperty("NEW_OWNER_ADDRESS_FIELD");
				String address_value = configFile.getProperty("NEW_OWNER_ADDRESS_VALUE");
				String city_field = configFile.getProperty("NEW_OWNER_CITY_FIELD");
				String city_value = configFile.getProperty("NEW_OWNER_CITY_VALUE");
				String telephone_field = configFile.getProperty("NEW_OWNER_TELEPHONE_FIELD");
				String telephone_value = configFile.getProperty("NEW_OWNER_TELEPHONE_VALUE");

				Form newOwner = new Form();
				newOwner.field(firstname_field).setValue(firstname_value);
				newOwner.field(lastname_field).setValue(lastname_value);
				newOwner.field(address_field).setValue(address_value);
				newOwner.field(city_field).setValue(city_value);
				newOwner.field(telephone_field).setValue(telephone_value);
				input.setValuesInForm(newOwner).beforeClickElement("button").withText("Add Owner");

				/*
				 * String search_field = configFile.getProperty("SEARCH_FIELD");
				 * String search_value = configFile.getProperty("SEARCH_VALUE");
				 * String search_button_field =
				 * configFile.getProperty("SEARCH_BUTTON_FIELD"); String
				 * search_button_value =
				 * configFile.getProperty("SEARCH_BUTTON_VALUE");
				 * 
				 * Form search = new Form();
				 * search.field(search_field).setValue(search_value);
				 * input.setValuesInForm(search).beforeClickElement(
				 * search_button_field).withText(search_button_value);
				 * 
				 * String pet_name_field =
				 * configFile.getProperty("PET_NAME_FIELD"); String
				 * pet_name_value = configFile.getProperty("PET_NAME_VALUE");
				 * String pet_birthday_field =
				 * configFile.getProperty("PET_BIRTHDATE_FIELD"); String
				 * pet_birthday_value =
				 * configFile.getProperty("PET_BIRTHDATE_VALUE"); String
				 * pet_type_field = configFile.getProperty("PET_TYPE_FIELD");
				 * String pet_type_value =
				 * configFile.getProperty("PET_TYPE_VALUE"); String
				 * pet_button_field =
				 * configFile.getProperty("PET_ADD_BUTTON_FIELD"); String
				 * pet_button_value =
				 * configFile.getProperty("PET_ADD_BUTTON_VALUE");
				 * 
				 * Form addPet = new Form();
				 * addPet.field(pet_name_field).setValue(pet_name_value);
				 * addPet.field(pet_birthday_field).setValue(pet_birthday_value)
				 * ; addPet.field(pet_type_field).setValue(pet_type_value);
				 * input.setValuesInForm(addPet).beforeClickElement(
				 * pet_button_field).withText(pet_button_value);
				 */
			}

			if (url.contains("phonecat")) {
				///// PHONECAT specific : Crawljax crashes!
				// <input class="ng-valid ng-dirty ng-valid-parse ng-touched
				///// ng-empty" ng-model="$ctrl.query"/>
				// Form searchPhone = new Form();
				// searchPhone.field("ng-valid ng-dirty ng-valid-parse
				// ng-touched ng-empty").setValue("Dell");
				// input.setValuesInForm(searchPhone);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return input;
	}

	/**
	 * set a custom StateVertexFactory for the builder Crawljax configuration
	 * 
	 * @param builder
	 */
	public static void setCustomStateVertexFactory(CrawljaxConfigurationBuilder builder) {

		builder.setStateVertexFactory(new StateVertexFactory() {
			@Override
			public StateVertex newStateVertex(int id, String url, String name, String dom, String strippedDom) {
				return new StateVertexLevensteinEquals(id, url, name, dom, strippedDom);
			}
		});
	}

}
