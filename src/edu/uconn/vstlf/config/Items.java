package edu.uconn.vstlf.config;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public enum Items{
	
	MinLoad("minload","0"),
	MaxLoad("maxload","28000"),
	TimeZone("timezone","America/New_York"),
	Longitude("longitude","-72.25"),
	Latitude("latitude", "41.85");
	
	private static String _filename = "./anns/config";

	
	private String _key;
	private String _defaultValue;
	private Items(String key, String defaultValue){
		_key = key;
		_defaultValue = defaultValue;
	}
	
	public String key(){
		return _key;
	}
	
	
	public static String file(){
		return _filename;
	}
	
	private static Map<String,String> _map = new TreeMap<String,String>();      
	
	public static synchronized String get(Items item){
		if(_map.containsKey(item._key))
			return _map.get(item._key);
		else
			return item._defaultValue;
	}
	
	public static synchronized void put(Items item, String val){
		_map.put(item._key,val);
	}
	
	public static synchronized void load(String filename)throws Exception{
		File file = new File(filename);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(file);
		dom.getDocumentElement().normalize();
		NodeList nodes = dom.getElementsByTagName("vstlf:item");
		for(int i = 0; i < nodes.getLength(); i++){
			Node node = nodes.item(i);
			Element elm = (Element) node;
			String key = elm.getAttribute("key");
			String val = elm.getAttribute("value");
			_map.put(key,val);
		}
	}
	
	public static synchronized void save(String filename)throws Exception{
		PrintWriter out = new PrintWriter(filename);
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.println("<vstlf:config>");
		for(Items item : Items.values())
			out.format("\t<vstlf:item key=\"%s\" value=\"%s\" />\n",
						item._key,
						get(item));
		out.println("</vstlf:config>\n");
		out.close();
	}
}