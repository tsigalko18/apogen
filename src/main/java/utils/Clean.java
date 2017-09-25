package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class Clean
{

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException
    {

	if (args.length < 1)
	{
	    usage();
	    System.exit(1);
	}
	else if (args[0].equals("-u"))
	{

	    if (args.length <= 1)
	    {
		print("please specify a complete URL to fetch!");
		return;
	    }

	    String url = args[1];
	    print("Fetching %s...", url);
	    // org.jsoup.nodes.Document doc = Jsoup.connect(url).get();

	    print("Saving %s as HTML...", url);
	    URL u = new URL(url);
	    File f = new File(url + ".html");
	    FileUtils.copyURLToFile(u, f);

	    print("Cleaning %s as XHTML...", url);

	    FileInputStream in = new FileInputStream(f);
	    String s = f.toString().replace("html", "xhtml");
	    File output = new File(s);
	    FileOutputStream out = new FileOutputStream(output);

	    clean(in, out);

	    out.close();
	    in.close();
	}
	else if (args[0].equals("-f"))
	{

	    if (args.length <= 1)
	    {
		print("Please specify a filename to fetch!");
		return;
	    }

	    // SAXParserFactory spf = SAXParserFactory.newInstance();
	    // SAXParser sp = spf.newSAXParser();
	    // XMLReader xr = sp.getXMLReader();

	    try
	    {
		FileInputStream i = new FileInputStream("http:/localhost:8888/" + args[1]);
		String s = args[1].toString().replace("html", "xhtml");
		File output = new File(s);
		FileOutputStream out = new FileOutputStream(output);

		clean(i, out);

		out.close();
		i.close();
	    }
	    catch (FileNotFoundException e)
	    {
		print("File not found!");
		return;
	    }
	}
	else
	{
	    usage();
	    System.exit(1);
	}
	// print("Extraction done!");
	return;
    }

    private static void usage()
    {
	System.out.println("Usage:  java -cp :./jsoup-1.7.2.jar:./commons-io-2.4.jar:./jtidy-r938.jar" + " Clean -u <url> | -f <file>");
    }

    private static void print(String msg, Object... args)
    {
	System.out.println(String.format(msg, args));
    }

    // private static String trim(String s, int width)
    // {
    // if (s.length() > width)
    // return s.substring(0, width - 1) + ".";
    // else
    // return s;
    // }

    public static void clean(FileInputStream input, FileOutputStream output)
    {

	// Declaring Tidy object to sanitize HTML
	Tidy tidy = new Tidy();

	/*
	 * 
	 * // set configuration values tidy.setDropEmptyParas(false); // drop empty P elements tidy.setTrimEmptyElements(false); tidy.setFixUri(false); tidy.setFixBackslash(false); tidy.setDocType("omit"); // omit the doctype
	 * tidy.setEncloseBlockText(false); // wrap blocks of text in P elements tidy.setEncloseText(false); // wrap text right under BODY element in P elements tidy.setHideEndTags(false); // force optional end tags tidy.setIndentContent(false); //
	 * indent content for easy reading tidy.setLiteralAttribs(false); // no new lines in attributes tidy.setLogicalEmphasis(false); // replace i and b by em and strong, respectively tidy.setMakeClean(false); // strip presentational cruft
	 * tidy.setNumEntities(true); // convert entities to their numeric form tidy.setWord2000(false); // strip Word 2000 cruft tidy.setXHTML(true); // output XHTML tidy.setQuoteNbsp(true); tidy.setXmlPi(false); // add <?xml?> processing
	 * instruction tidy.setPrintBodyOnly(false); tidy.setIndentAttributes(false); tidy.setIndentCdata(false); tidy.setIndentContent(false); tidy.setBreakBeforeBR(false); tidy.setSpaces(0); tidy.setHideComments(true); tidy.setFixComments(true);
	 * //tidy.setSmartIndent(true);
	 */

	// set configuration values
	tidy.setDropEmptyParas(false); // drop empty P elements
	tidy.setTrimEmptyElements(false);
	tidy.setFixUri(false);
	tidy.setFixBackslash(false);
	tidy.setDocType("omit"); // omit the doctype
	tidy.setEncloseBlockText(false); // wrap blocks of text in P elements
	tidy.setEncloseText(false); // wrap text right under BODY element in P elements
	tidy.setHideEndTags(false); // force optional end tags
	tidy.setIndentContent(true); // indent content for easy reading
	tidy.setLiteralAttribs(false); // no new lines in attributes
	tidy.setLogicalEmphasis(false); // replace i and b by em and strong, respectively
	tidy.setMakeClean(false); // strip presentational cruft
	tidy.setNumEntities(true); // convert entities to their numeric form
	tidy.setWord2000(false); // strip Word 2000 cruft
	tidy.setXHTML(true); // output XHTML
	tidy.setQuoteNbsp(true);
	tidy.setXmlPi(false); // add <?xml?> processing instruction
	tidy.setPrintBodyOnly(false);
	tidy.setIndentAttributes(false);
	tidy.setIndentCdata(true);
	tidy.setIndentContent(true);
	tidy.setBreakBeforeBR(false);
	tidy.setSpaces(0);
	tidy.setHideComments(true);
	tidy.setFixComments(true);
	tidy.setSmartIndent(true);
	tidy.setWraplen(30000);

	tidy.parseDOM(input, output);

	return;
    }
}
