/************************************************************************
 MIT License

 Copyright (c) 2010 University of Connecticut

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/

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

import edu.uconn.vstlf.data.Calendar;

public enum Items{
	
	MinLoad("minload","5000"),
	MaxLoad("maxload","28000"),
	TimeZone("timezone","America/New_York"),
	Longitude("longitude","-72.6166667"),
	Latitude("latitude", "42.2041667"),
	TestMode("testmode", "false");
	
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
	
	public String value(){
		return get(this);
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
	public static Calendar makeCalendar() {
		Calendar cal = new Calendar(Items.get(TimeZone));
		return cal;
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
		File dir = new File("anns");
		if(!dir.exists())
			dir.mkdirs();
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