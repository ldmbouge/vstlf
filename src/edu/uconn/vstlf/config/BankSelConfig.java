package edu.uconn.vstlf.config;

import java.io.File;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BankSelConfig {
	static private BankSelConfig inst_;
	static public BankSelConfig getInstance() throws Exception
	{
		if (inst_ == null) inst_ = new BankSelConfig("config.xml");
		return inst_;
	}
	
	private TreeMap<Integer, Integer> bankSels_ = new TreeMap<Integer, Integer>();
	
	public BankSelConfig(String file) throws Exception
	{
		 File fXmlFile = new File(file);
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		 Document doc = dBuilder.parse(fXmlFile);		  
		 Element root = (Element)doc.getDocumentElement();

		 NodeList bsElems = root.getElementsByTagName("BankSelection");
		 Element bsElem = (Element)bsElems.item(0);
		 
		 NodeList items = bsElem.getElementsByTagName("Item");
		 for (int i = 0; i < items.getLength(); ++i) {
			 Element item = (Element)items.item(i);
			 int minsOff = Integer.parseInt(item.getAttribute("mins_off"));
			 int bank = Integer.parseInt(item.getAttribute("use_bank"));
			 bankSels_.put(minsOff, bank);
		 }
	}
	
	// select the bank by minutes out
	public int select(int minsOff)
	{
		return bankSels_.get(minsOff);
	}
}
