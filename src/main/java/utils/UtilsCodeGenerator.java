package utils;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.stmt.BlockStmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import abstractdt.CandidateWebElement;
import abstractdt.Edge;
import abstractdt.Form;
import abstractdt.FormField;
import abstractdt.InputField;
import abstractdt.State;
import abstractdt.Getter;

/**
 * Utils class containing auxiliary and configuration methods for the Code
 * Generator
 * 
 * @author Andrea Stocco
 * 
 */
public class UtilsCodeGenerator {

	/**
	 * returns a basic plain CompilationUnit object
	 * 
	 * @return
	 */
	public static CompilationUnit createBasicCompilationUnit() {
		return new CompilationUnit();
	}

	/**
	 * returns a CompilationUnit object decorated with package and some basic
	 * import instructions
	 * 
	 * @return
	 */
	public static CompilationUnit createEnhancedCompilationUnit() {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("po")));
		cu.setImports(UtilsCodeGenerator.getAllImports());
		return cu;
	}

	/**
	 * set the TypeDeclaration of a CompilationUnit i.e., whether is a class or
	 * an interface
	 * 
	 * @param c
	 * @param className
	 */
	public static void setTypeDeclaration(CompilationUnit c, String className) {
		// create the type declaration
		ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(
											ModifierSet.PUBLIC, false, className);

		ASTHelper.addTypeDeclaration(c, type);
	}

	/**
	 * adds a WebDriver instance to the CompilationUnit c
	 * 
	 * @param c CompilationUnit
	 */
	public static void setWebDriverVariable(CompilationUnit c) {

		VariableDeclarator v = new VariableDeclarator();
		v.setId(new VariableDeclaratorId("driver"));

		FieldDeclaration f = ASTHelper.createFieldDeclaration(
				ModifierSet.PRIVATE,
				ASTHelper.createReferenceType("WebDriver", 0), v);

		ASTHelper.addMember(c.getTypes().get(0), f);
	}

	/**
	 * adds a constructor to the CompilationUnit c with a WebDriver instance and
	 * PageFactory initialization
	 * 
	 * @param c
	 * @param s
	 */
	public static void setDefaultConstructor(CompilationUnit c, State s) {
		
        // creates the class constructor
		ConstructorDeclaration constructor = new ConstructorDeclaration();
		constructor.setName(s.getName());
		constructor.setModifiers(ModifierSet.PUBLIC);

        // sets the WebDriver instance parameter
		List<Parameter> parameters = new LinkedList<>();
		parameters.add(ASTHelper.createParameter(
				ASTHelper.createReferenceType("WebDriver", 0), "driver"));

		constructor.setParameters(parameters);
		constructor.setJavaDoc(new JavadocComment("\n\t\tPage Object for "
				+ s.getName() + " (" + s.getStateId() + ") \n\t"));

		// add the body to the constructor
		BlockStmt constructor_block = new BlockStmt();
		constructor.setBlock(constructor_block);

		// add basic statements do the constructor method body
		ASTHelper.addStmt(constructor_block, new NameExpr(
				"this.driver = driver"));
		ASTHelper.addStmt(constructor_block, new NameExpr(
				"PageFactory.initElements(driver, this)"));

		ASTHelper.addMember(c.getTypes().get(0), constructor);
	}

	/**
	 * set the package of the CompilationUnit c
	 * 
	 * @param c
	 */
	public static void setPackage(CompilationUnit c) {
		c.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("po")));
	}

	/**
	 * adds Selenium imports to the compilation unit
	 */
	private static List<ImportDeclaration> getAllImports() {
		List<ImportDeclaration> imports = new LinkedList<>();

		imports.add(new ImportDeclaration(
                        new NameExpr("org.openqa.selenium"), false, true));
		imports.add(new ImportDeclaration(
                        new NameExpr("org.openqa.selenium.support.FindBy"), false, false));
		imports.add(new ImportDeclaration(
                        new NameExpr("org.openqa.selenium.support.PageFactory"), false, false));

		return imports;
	}

	/**
	 * creates the webElements and WedDriver variables together with the correct
	 * locators (for now XPath or CSS locators are used)
	 * 
	 * @param c
	 *            CompilationUnit
	 * @param webElements
	 *            List<CandidateWebElement>
	 */
	public static void setVariables(CompilationUnit c,
			Set<CandidateWebElement> webElements) {

		setWebElements(c, webElements);
		setWebDriverVariable(c);

	}

	/**
	 * creates the webElements instances
	 * 
	 * @param c
	 *            CompilationUnit
	 * @param webElements
	 *            List<CandidateWebElement>
	 */
	private static void setWebElements(CompilationUnit c,
			Set<CandidateWebElement> webElements) {

		for (CandidateWebElement cwe : webElements) {

			VariableDeclarator webElement = new VariableDeclarator();
			webElement.setId(new VariableDeclaratorId(cwe.getVariableName()));

			FieldDeclaration field = ASTHelper.createFieldDeclaration(
					ModifierSet.PRIVATE,
					ASTHelper.createReferenceType("WebElement", 0), webElement);

			List<AnnotationExpr> list_espr = new LinkedList<>();

			NormalAnnotationExpr locator_annotation = new NormalAnnotationExpr();
			locator_annotation.setName(new NameExpr("FindBy"));

			List<MemberValuePair> list_mvp = new LinkedList<>();
			MemberValuePair mvp = new MemberValuePair();

			if (cwe.getCssLocator() == null) {
				String xpathLocator = cwe.getXpathLocator();
				xpathLocator = "\"" + xpathLocator + "\"";
				mvp = new MemberValuePair("xpath", new NameExpr(xpathLocator));
			} else if (cwe.getCssLocator() != null) {
				String cssLocator = cwe.getCssLocator();
				cssLocator = "\"" + cssLocator + "\"";
				mvp = new MemberValuePair("css", new NameExpr(cssLocator));
			}

			list_mvp.add(mvp);
			locator_annotation.setPairs(list_mvp);
			list_espr.add(0, locator_annotation);

			field.setAnnotations(list_espr);
			ASTHelper.addMember(c.getTypes().get(0), field);
		}
	}

	/**
	 * For each CompilationUnit c associated to a State s creates the link
	 * methods to navigate towards other page objects
	 * 
	 * @param c
	 * @param s
	 */
	public static void setLinkMethods(CompilationUnit c, State s) {

		for (Edge edge : s.getLinks()) {

			String towards = UtilsStaticAnalyzer.getStateNameFromStateId(
					edge.getTo());

			// add the necessary import
			ImportDeclaration new_import = new ImportDeclaration(
                                new NameExpr("po." + towards), false, false);
			
			if(!towards.equals("") && !c.getImports().contains(new_import)){
				c.getImports().add(new_import);
			}
			
			String l = edge.getVia();
			l = l.replace("xpath ", "");
			String we = s.getWebElementNameFromLocator(l);

			if (we.equals("")) {
                            System.err.println(
                                "[ERROR] UtilsCodeGenerator.setLinkMethods getWebElementNameFromLocator failed");
				//System.exit(1);
			}

			String eventType = edge.getEvent() + "()";
			
			MethodDeclaration method = 
                            new MethodDeclaration(
                                ModifierSet.PUBLIC, ASTHelper.createReferenceType(
                                    towards, 0), "goTo" + towards);
                                    // + "_via_" + we);	
			
			// add a body to the method
			BlockStmt block = new BlockStmt();
			method.setBody(block);

			// add a statement do the method body
			ASTHelper.addStmt(block, new NameExpr(we + "." + eventType));
			ASTHelper.addStmt(block, new NameExpr("return new " 
                                + towards + "(driver)"));

			String name = method.getName();
			int occ = 0;
			
			for (BodyDeclaration bd : c.getTypes().get(0).getMembers()) {
                            if(bd instanceof MethodDeclaration){
                                if(((MethodDeclaration) bd).getName().contains(name)){
                                    occ++;
				}
                            }
			}
			
			if(occ > 0){
				method.setName(name + "_" + occ);
			}
			
			ASTHelper.addMember(c.getTypes().get(0), method);
		}
	}

	private static int countTowards(Set<Edge> links, Edge edge3) {
		
		int c = 0;
		
		for (Edge edge : links) {
			if(edge3.getTo().equals(edge.getTo())){
				c++;
			}
		}
		
		return c;
	}

	/**
	 * For each CompilationUnit c associated to a State s creates the form
	 * methods.
	 * <p>
	 * It follows a naive approach: parses the form objects and puts everything
	 * in the method
	 * </p>
	 * 
	 * @param c
	 *            CompilationUnit
	 * @param s
	 *            State
	 */
	public static void setFormMethods(CompilationUnit c, State s) {

		if (s.getForms() == null) {
			return;
		}

		for (Form f : s.getForms()) {

			MethodDeclaration method = new MethodDeclaration(
					ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, f.getFormName());
			BlockStmt block = new BlockStmt();
			method.setBody(block);

			for (int i = 0; i < f.getFormFieldList().size(); i++) {

				addIndexedParameterToFormMethod(f, i, method);

			}

			for (FormField field : f.getFormFieldList()) {

				addFormInstructionToBlockMethod(block, f, field);

			}

			ASTHelper.addMember(c.getTypes().get(0), method);
		}

	}

	/**
	 * /** For each CompilationUnit c associated to a State s creates the form
	 * methods.
	 * <p>
	 * It follows a more sophisticated approach: parses the form objects and
	 * creates a method for each submit/button only
	 * </p>
	 * 
	 * @param c
	 *            CompilationUnit
	 * @param s
	 *            State
	 * @throws Exception
	 */
	public static void setFormMethodsFromButtonAndSubmit(CompilationUnit c,
			State s) throws Exception {

		if (s.getForms() == null) {
			return;
		}

		for (Form f : s.getForms()) {

			for (InputField i : f.getSubmitList()) {
				System.out.println("[LOG] " + f.getSubmitList().size()
						+ " submit/button(s) found in form " + f.getFormName());

				MethodDeclaration method = new MethodDeclaration(
						ModifierSet.PUBLIC, ASTHelper.VOID_TYPE,
						f.getFormName() + "_" + i.getVariableName());

				BlockStmt block = new BlockStmt();
				method.setBody(block);

				if (f.getSubmitList().size() == 1) {

					for (int j = 0; j < f.getFormFieldList().size(); j++) {

						addIndexedParameterToFormMethod(f, j, method);

					}

					for (FormField field : f.getFormFieldList()) {

						addFormInstructionToBlockMethod(block, f, field);

					}

				} else if (f.getSubmitList().size() > 1) {
					addFormInstructionToBlockMethod(block, f, i);
				} else {
					throw new Exception("Form does not contains any submit!");
				}

				ASTHelper.addMember(c.getTypes().get(0), method);

			}
		}

	}

	private static void addParameterToFormMethod(Form f,
			MethodDeclaration method) {

		Parameter par = ASTHelper.createParameter(
				ASTHelper.createReferenceType("String", 0), "param");
		par.setVarArgs(false);
		ASTHelper.addParameter(method, par);

	}

	private static void addIndexedParameterToFormMethod(Form f, int i,
			MethodDeclaration method) {

		if (f.getFormFieldList().get(i).getDefaultAction().equals("sendKeys")) {
			Parameter par = ASTHelper.createParameter(
					ASTHelper.createReferenceType("String", 0), "args" + i);
			par.setVarArgs(false);
			ASTHelper.addParameter(method, par);
		}

	}

	private static void addFormInstructionToBlockMethod(BlockStmt block,
			Form f, FormField field) {

		switch (field.getDefaultAction()) {
			case "sendKeys" :
				ASTHelper.addStmt(block, new NameExpr(field.getVariableName()
						+ "." + field.getDefaultAction() + "(args"
						+ f.getFormFieldList().indexOf(field) + ")"));
				break;

			case "click" :
				ASTHelper.addStmt(block, new NameExpr(field.getVariableName()
						+ "." + field.getDefaultAction() + "()"));
				break;
			default :
				break;
		}

	}

	/**
	 * formats the string to get a valid variable name
	 * 
	 * @param s
	 * @return
	 */
	public static String formatToVariableName(String s) {
		
		String res = s;

		res = UtilsStaticAnalyzer.toSentenceCase(res);
		res = res.replaceAll(" ", "");
		res = StringUtils.uncapitalize(res);

		return res;
	}

	/**
	 * Creates getters methods to be used for assertions. Created on differences
	 * between adjacent pages
	 * 
	 * @param c
	 * @param s
	 */
	public static void setGettersMethods(CompilationUnit c, State s) {

		if (s.getDiffs() == null) {
			return;
		}

		for (abstractdt.Getter d : s.getDiffs()) {

			// /////////////////////////////////////////////////////////////
			// Add the WebElement
			// /////////////////////////////////////////////////////////////
			VariableDeclarator webElement = new VariableDeclarator();
			webElement.setId(new VariableDeclaratorId(d.getWebElementName()));

			FieldDeclaration field = ASTHelper.createFieldDeclaration(
					ModifierSet.PRIVATE,
					ASTHelper.createReferenceType("WebElement", 0), webElement);

			List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();

			NormalAnnotationExpr na = new NormalAnnotationExpr();
			na.setName(new NameExpr("FindBy"));

			List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
			MemberValuePair mvp = new MemberValuePair();

			String xpathLocator = d.getLocator();
			xpathLocator = "\"" + xpathLocator + "\"";
			mvp = new MemberValuePair("xpath", new NameExpr(xpathLocator));

			list_mvp.add(mvp);
			na.setPairs(list_mvp);
			list_espr.add(0, na);

			field.setAnnotations(list_espr);
			ASTHelper.addMember(c.getTypes().get(0), field);

			// /////////////////////////////////////////////////////////////
			// Add the Getter method
			// /////////////////////////////////////////////////////////////
			MethodDeclaration method = new MethodDeclaration(
					ModifierSet.PUBLIC, ASTHelper.createReferenceType("String",
							0), "get_" + d.getWebElementName());

			// add a body to the method
			BlockStmt block = new BlockStmt();
			method.setBody(block);

			/**
			 * public String getGroupsName() { return groupContainer.getText();
			 * }
			 */
			JavadocComment javaDoc = new JavadocComment("\n\t\tsource: "
					+ d.getSourceState() + "" + "\n\t\ttarget: "
					+ d.getTargetState() + "" + "\n\t\tcause: " + d.getCause()
					+ "" + "\n\t\tbefore: " + d.getBefore() + ""
					+ "\n\t\tafter: " + d.getAfter() + "" + " \n\t");
			method.setJavaDoc(javaDoc);

			// add a statement do the method body
			ASTHelper.addStmt(block,
					new NameExpr("return " + d.getWebElementName()
							+ ".getText()"));

			ASTHelper.addMember(c.getTypes().get(0), method);

		}

	}

	public static void setSourceCode(CompilationUnit c, State s) {
		s.setSourceCode(c.toString());
	}
}
