package edu.uconn.vstlf.config;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uconn.vstlf.prediction.DaubSpec;

public class DaubConfig {
	static private DaubConfig inst_;
	static public DaubConfig getInstance() throws Exception
	{
		if (inst_ == null) inst_ = new DaubConfig("config.xml");
		return inst_;
	}
	
	private DaubSpec dbSpec_;
	
	public DaubSpec getDaubSpec()
	{ return dbSpec_; }
	
	private DaubConfig(String configFile) throws Exception
	{
		 File fXmlFile = new File(configFile);
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		 Document doc = dBuilder.parse(fXmlFile);		  
		 Element root = (Element)doc.getDocumentElement();

		 NodeList dcElems = root.getElementsByTagName("DecomposeLevel");
		 Element dcElem = (Element)dcElems.item(0);
		 int nLvls = Integer.parseInt(dcElem.getAttribute("nlvls"));
		 
		 dbSpec_ = new DaubSpec(nLvls);
	}
}
